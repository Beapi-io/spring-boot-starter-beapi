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


/**
 *
 * This abstract provides basic methods for all exchange services
 * @author Owen Rubel
 *
 * @see ApiInterceptor
 *
 */

abstract class ApiExchange{

    // todo : get supported mimetypes from properties
    //private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IoStateService.class);
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
    boolean overrideAutoMimeTypes = false

    //private static final ArrayList formats = ['XML','JSON']
    //private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml']

    int callType
    protected String method
    protected String defaultAction
    protected String appversion
    protected String apiversion

    String requestFormat
    protected String requestMimeType
    protected String requestFileType
    protected String responseMimeType
    protected String responseFileType
    protected LinkedHashMap receives = [:]
    protected ArrayList receivesAuths = []
    protected Set receivesList = []
    protected Set keyList = []
    protected LinkedHashMap rturns = [:]
    protected ArrayList returnsAuths = []
    protected Set returnsList = []
    // [CACHE]
    LinkedHashMap cache
    protected ApiDescriptor apiObject
    // [SECURITY] : reliant on apiProperties
    protected String networkGrp
    protected ArrayList networkRoles
    protected LinkedHashMap networkGrpRoles
    protected ArrayList deprecated
    protected String authority
    protected LinkedHashMap<String,Integer> rateLimit
    protected LinkedHashMap<String,Integer> dataLimit

    protected String cacheHash
    protected boolean cachedResponse = false

    // [BATCH]
    LinkedList batch = []

    UriObject uObj
    String uri
    String version
    String controller
    String action
    String auth
    String cacheHash
    String responseFileType
    String method
    boolean trace
    public String id


    /*
    * Validates request method in interceptors for each type of functionality;
    * validating here to better handle routing (filter is 'once per request')
     */
    boolean validateMethod(){
        boolean result = false
        if(this.apiObject['method'].toUpperCase() == this.method){
            result = true
        }
        return result
    }

    protected String parseOutput(ArrayList responseBody, String responseFileType){
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


    protected String parseBodyByFiletype(LinkedHashMap responseBody, String responseFileType){
        String test
        switch(responseFileType){
            case 'JSON':
                test = (responseBody!=null)?new JSONObject(responseBody).toString():'{}'
                //test = new JSONObject(responseBody).toString()
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
    boolean checkRequestParams(LinkedHashMap methodParams) throws Exception{
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


    /**
     * Returns concatenated IDS as a HASH used as ID for the API cache
     * @see io.beapi.api.interceptor.ApiInterceptor#before()
     * @see BatchInterceptor#before()
     * @see ChainInterceptor#before()
     * @param LinkedHashMap List of ids required when making request to endpoint
     * @return a hash from all id's needed when making request to endpoint
     */
    protected void setCacheHash(LinkedHashMap params,Set receivesList){
        StringBuilder hashString = new StringBuilder('')
        receivesList.each(){ it ->
            hashString.append(params[it])
            hashString.append("/")
        }
        this.cacheHash = Hashing.murmur3_32().hashString(hashString.toString(), StandardCharsets.UTF_8).toString()
    }


    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    void writeErrorResponse(HttpServletResponse response, String statusCode, String uri){
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${ErrorCodes.codes[statusCode]['long']}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        //response.writer.flush()
    }

    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    void writeErrorResponse(HttpServletResponse response, String statusCode, String uri, String msg){
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        if(msg.isEmpty()){
            msg = ErrorCodes.codes[statusCode]['long']
        }
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        //response.writer.flush()
    }

}