/*
 * Copyright 2013-2019 Beapi.io
 * API Chaining(R) 2014 USPTO
 *
 * Licensed under the MPL-2.0 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.beapi.api.service

import io.beapi.api.utils.ErrorCodes
import org.json.JSONObject
import io.beapi.api.utils.ApiDescriptor
import org.springframework.web.servlet.support.RequestContextUtils;
import java.lang.reflect.Field
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.forward.*
import groovyx.gpars.*
import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets
import io.beapi.api.utils.UriObject

// AES/CTR encryption
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.beapi.api.service.StatsCacheService
import org.springframework.beans.factory.annotation.Autowired
import io.beapi.api.service.StatsService

/**
 *
 * This abstract provides basic methods for all exchange services
 * @author Owen Rubel
 *
 * @see ApiInterceptor
 *
 */

abstract sealed class ApiExchange permits ExchangeService, BatchExchangeService, ChainExchangeService, HookExchangeService, TraceExchangeService {

    // todo : get supported mimetypes from properties
    //private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IoStateService.class);
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
    //boolean overrideAutoMimeTypes = false

    //private static final ArrayList formats = ['XML','JSON']
    //private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml']

    @Autowired
    StatsService statsService

    protected int callType
    protected String defaultAction
    protected String appversion
    protected String apiversion

    //String requestFormat
    protected String requestMimeType
    protected String requestFileType
    protected String responseMimeType
    protected LinkedHashMap receives = [:]
    protected ArrayList receivesAuths = []
    protected Set receivesList = []
    protected Set keyList = []
    protected LinkedHashMap rturns = [:]
    protected ArrayList returnsAuths = []
    protected Set returnsList = []
    // [CACHE]
    protected LinkedHashMap cache
    protected ApiDescriptor apiObject
    // [SECURITY] : reliant on apiProperties
    protected String networkGrp
    protected ArrayList networkRoles
    protected LinkedHashMap networkGrpRoles
    protected ArrayList deprecated
    protected String authority
    protected LinkedHashMap<String,Integer> rateLimit
    protected LinkedHashMap<String,Integer> dataLimit

    protected boolean cachedResponse = false

    // [BATCH]
    //LinkedList batch = []

    protected UriObject uObj
    protected String uri
    protected String version
    protected String controller
    protected String action
    protected String auth
    protected String cacheHash
    protected String responseFileType
    protected String method
    protected boolean trace
    protected String id


    /**
    * Validates request method in interceptors for each type of functionality;
    * validating here to better handle routing (filter is 'onceperrequest')
     * @return boolean based on whether request method matches expected method for endpoint
     */
    protected boolean validateMethod(){
        boolean result = false
        if(this.apiObject['method'].toUpperCase() == this.method){
            result = true
        }
        return result
    }

    /**
     * Method to set the apicache associated with the controller name
     * @param responseBody ArrayList representing the response body
     * @param responseFileType String representing response fileType
     * @return A JSON/XML String representing the response body
     */
    protected String parseOutput(ArrayList responseBody, String responseFileType){
        //println("### parseOutput")
        if(responseBody.size()<2) {
            String output = parseBodyByFiletype(responseBody[0], responseFileType);
            return output
        }else{
            int inc = 0
            Set output = responseBody.collect() { it2 -> parseBodyByFiletype(it2, responseFileType) }
            return output.toString()
        }
        return ''
    }

    /**
     * Secondary Method to set the apicache associated with the controller name
     * @param responseBody ArrayList representing the response body
     * @param responseFileType String representing response fileType
     * @return A JSON/XML String representing the response body
     */
    protected String parseBodyByFiletype(LinkedHashMap responseBody, String responseFileType){
        //println("### parseBodyByFiletype : "+responseFileType)
        String test
        switch(responseFileType){
            case 'JSON':
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    test = (responseBody != null) ? objectMapper.writeValueAsString(responseBody) : '{}'
                }catch(Exception e){
                    println("[ApiExchange :: parseBodyByFiletype] : "+e)
                    throw new Exception("[ApiExchange :: parseBodyByFiletype] : "+e)
                }
                break;
            case 'XML':
                // TODO : move to an XMLService(??)
                //'XML'
                return '[]'
                break;
            default:
                // unsupported mimetype
                return ''
                break;
        }
        return test
    }



    /**
     * Given the request params and endpoint request definitions, test to check whether the request params match the expected endpoint params; returns boolean
     * @see io.beapi.api.interceptor.ApiInterceptor#before()
     * @see BatchInterceptor#before()
     * @see ChainInterceptor#before()
     * @param GrailsParameterMap Map of params created from the request data
     * @param LinkedHashMap map of variables defining endpoint request variables
     * @return Boolean returns false if request variable keys do not match expected endpoint keys
     */
    /*
    protected boolean checkRequestParams(LinkedHashMap methodParams) throws Exception{
        ArrayList checkList = this.receivesList
        ArrayList paramsList
        Set reservedNames = ['batchLength','batchInc','chainInc','apiChain','batch','_','max','offset','chaintype']

        try {
            if(checkList.contains('*')) {
                return true
            } else {
                paramsList = methodParams.keySet() as ArrayList
                // remove reservedNames from List
                reservedNames.each() { paramsList.remove(it) }

                if (paramsList.size() == checkList.intersect(paramsList).size()) {
                    return true
                }
            }

            // todo : set stats cache
            //statsService.setStatsCache(userId, response.status, request.requestURI)
            return false
        }catch(Exception e) {
            throw new Exception("[ApiExchange :: checkRequestParams] : Exception - full stack trace follows:",e)
        }
        return false
    }
     */

    /**
     * Given the request params, check against expected parms in IOstate for users role; returns boolean
     * @param LinkedHashMap map of variables defining endpoint request variables
     * @return Boolean returns false if request variable keys do not match expected endpoint keys
     */
    boolean checkRequestParams(LinkedHashMap methodParams){
        //println("###checkRequestParams")
        ArrayList checkList = this.receivesList
        ArrayList paramsList
        ArrayList reservedNames = ['batchLength','batchInc','chainInc','apiChain','_','batch','max','offset','chaintype']

        try {
            if(checkList){
                if(methodParams) {
                    if (checkList?.contains('*')) {
                        return true
                    } else {
                        paramsList = methodParams.keySet() as ArrayList

                        // remove reservedNames from List
                        reservedNames.each() { paramsList.remove(it) }
                        if (paramsList.size() == checkList?.intersect(paramsList).size()) {
                            return true
                        }
                    }
                }else{
                    return false
                }
            }else{
                return true
            }

            // todo : set stats cache
            //statsService.setStatsCache(userId, response.status, request.requestURI)
            return false
        }catch(Exception e) {
            throw new Exception("[RequestInitializationFilter :: checkRequestParams] : Exception - full stack trace follows:",e)
        }
        return false
    }


    /**
     * Returns concatenated IDS as a HASH used as ID for the API cache
     * @see io.beapi.api.interceptor.ApiInterceptor#before()
     * @see BatchInterceptor#before()
     * @see ChainInterceptor#before()
     * @param LinkedHashMap List of ids required when making request to endpoint
     * @return a hash from all id's needed when making request to endpoint
     */
    protected void setCacheHash(String cacheHash){
        this.cacheHash = cacheHash
    }


    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    protected void writeErrorResponse(HttpServletRequest request,HttpServletResponse response, String statusCode){
        //println("apiexchange :: writeerrorresponse")
        String uri = request.getRequestURI()
        Locale tmp = RequestContextUtils.getLocale(request);
        String lang = (tmp)?tmp.getLanguage():"en"

        ArrayList keys = []
        Field[] fields = ErrorCodes.getDeclaredFields();
        for (Field field : fields) {
            if(!['$staticClassInfo', '__$stMC', 'metaClass'].contains(field.getName())){
                keys.add(field.getName());
            }
        }
        lang = (keys.contains(lang))?lang:"en"

        try{
            statsService.setStat((String)statusCode,uri)
        }catch(Exception e){
            println("### [ApiExchange :: writeErrorResponse1] exception2 : "+e)
        }

        //statsCacheService.putStatsCache(statusCode, uri)
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode]['long']}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        response.writer.flush()

    }

    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    protected void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, String statusCode, String msg){
        //println("apiexchange :: writeerrorresponse2")
        String uri = request.getRequestURI()
        Locale tmp = RequestContextUtils.getLocale(request);
        String lang = (tmp)?tmp.getLanguage():"en"

        ArrayList keys = []
        Field[] fields = ErrorCodes.getDeclaredFields();
        for (Field field : fields) {
            if(!['$staticClassInfo', '__$stMC', 'metaClass'].contains(field.getName())){
                keys.add(field.getName());
            }
        }
        lang = (keys.contains(lang))?lang:"en"

        try{
            statsService.setStat((String)statusCode,uri)
        }catch(Exception e){
            println("### [ApiExchange :: writeErrorResponse2] exception : "+e)
        }

        //statsCacheService.putStatsCache(statusCode, uri)
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        if(msg.isEmpty()){
            msg = ErrorCodes."$lang"[statusCode]['long']
        }
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        response.writer.flush()
    }

}