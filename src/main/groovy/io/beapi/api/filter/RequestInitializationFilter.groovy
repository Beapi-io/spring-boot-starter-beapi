/*
 * Copyright 2013-2022 Owen Rubel
 * API Chaining(R) 2022 Owen Rubel
 *
 * Licensed under the AGPL v2 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Owen Rubel (orubel@gmail.com)
 *
 */
package io.beapi.api.filter

import groovy.json.JsonSlurper
import org.json.JSONObject
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import io.beapi.api.utils.ErrorCodes
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component


import java.nio.charset.StandardCharsets
import java.util.regex.Matcher

import io.beapi.api.utils.ApiDescriptor
import org.apache.commons.io.IOUtils
import com.google.common.hash.Hashing

import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.web.servlet.HandlerMapping
import org.springframework.http.HttpStatus

/**
 * This class parses the URI attributes on initial request  &
 *
 * @author Owen Rubel
 */

//@Order(21)
@Component
@EnableConfigurationProperties([ApiProperties.class])
class RequestInitializationFilter extends OncePerRequestFilter{

    //private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RequestInitializationFilter.class);
    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml','multipart/form-data']
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
    private static final ArrayList CALL_TYPES = ['v','b','c','t']

    @Autowired
    ApplicationContext ctx


    PrincipleService principle
    ApiCacheService apiCacheService
    ApiProperties apiProperties
    String version


    // [CACHE]
    protected ApiDescriptor apiObject
    String cacheHash

    // todo : parse headers
    LinkedHashMap<String, List<String>> headers = [:]


    ArrayList uriList
    String uri
    LinkedHashMap receives = [:]
    ArrayList receivesList = []
    LinkedHashMap rturns = [:]
    String method
    String controller
    String requestFileType
    String responseFileType
    boolean reservedUri

    LinkedHashMap params = [:]
    String authority
    ArrayList deprecated
    String action
    boolean apidocFwd


    public RequestInitializationFilter(PrincipleService principle, ApiProperties apiProperties, ApiCacheService apiCacheService, String version, ApplicationContext ctx) {
        this.apiProperties = apiProperties
        this.version = version
        this.ctx = ctx
        this.principle = principle
        this.apiCacheService = apiCacheService
    }

    /**
     * (overridden method)
     * @param HttpServletRequest
     * @param HttpServletResponse
     * @param FilterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //logger.debug("doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) : {}");
        apidocFwd = false
        this.uri = request.getRequestURI()

        // todo : will imporove in future version
        if(apiProperties.reservedUris.contains(request.getRequestURI())) {
            // ### PUBLIC APIS ###
            ArrayList uriVars = uri.split('/')
            this.controller = uriVars[0]
        }else{
            // println("### APIS HANDLED BY THE STARTER ###")

            this.authority = this.principle.authorities()
            this.uriList = setUri(this.uri, this.version)

            // no action; show apidocs for controller
            if(!this.uriList[5]){
                this.uriList[7] = this.uriList[4]
                this.uriList[4] = 'apidoc'
                this.uriList[5] = 'show'
                apidocFwd = true
            }

            if(!request.getAttribute('principle')) {
                request.setAttribute('principle', this.authority)
            }

            try {
                this.method = request.getMethod()
                this.reservedUri = (apiProperties.reservedUris.contains(this.uri)) ? true : false

                // get apiObject

                def cache = apiCacheService?.getApiCache(uriList[4])
                def temp = cache[uriList[3]]
                this.deprecated = temp['deprecated'] as List

                // todo : if no action, default to apidoc/show/id


                if (cache) {
                    this.apiObject = temp[uriList[5]]
                    this.rturns = this.apiObject['returns'] as LinkedHashMap

                    ArrayList networkRoles = cache.networkGrpRoles
                    if (checkNetworkGrp(networkRoles, this.authority)) {
                        LinkedHashMap tmp1 = this.apiObject?.getReceivesList()
                        this.receivesList = (tmp1[this.authority]) ? tmp1[this.authority] : tmp1['permitAll']



                        request.setAttribute('receivesList', this.receivesList)

                        LinkedHashMap rturn = this.apiObject?.getReturnsList()
                        ArrayList returnsList = (rturn[this.authority]) ? rturn[this.authority] : rturn['permitAll']
                        request.setAttribute('returnsList', returnsList)
                    } else {
                        throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Request params do not match expect params for '${uri}'")
                    }

                    if(uriList[7]) {
                        if (!this.receivesList?.contains('id') && apidocFwd==false) {
                            writeErrorResponse(response, '400', request.getRequestURI());
                        }
                    }


                    parseParams(request, IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), request.getQueryString(), uriList[7])
                    if (!checkRequestParams(request.getAttribute('params'))) {
                        writeErrorResponse(response, '400', request.getRequestURI());
                        throw new Exception("[RequestInitializationFilter :: checkRequestParams] : Requestparams do not match expected params for this endpoint")
                    }


                    String temp2 = (request.getHeader('Accept') != null && request.getHeader('Accept') != "*/*") ? request.getHeader('Accept') : request.getContentType()
                    ArrayList reqMime = temp2.split(';')
                    ArrayList respMime = request.getContentType().split(';')

                    String requestMimeType = reqMime[0]
                    String requestEncoding = reqMime[1]


                    this.requestFileType = (SUPPORTED_MIME_TYPES.contains(requestMimeType)) ? getFormat(requestMimeType) : 'JSON'
                    if (!this.requestFileType) {
                        String msg = "Request MimeType must be one of the supported mimetypes (JSON/XML)"
                        writeErrorResponse(response, '400', request.getRequestURI(), msg);
                        throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Accept/Request mimetype unsupported")
                    }

                    this.responseFileType = (this.requestFileType) ? this.requestFileType : getFormat(respMime[0])
                    if (!this.responseFileType) {
                        String msg = "Response MimeType must be one of the supported mimetypes (JSON/XML)"
                        writeErrorResponse(response, '400', request.getRequestURI(), msg);
                        throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Content-type unsupported")
                    }

                    String responseMimeType = respMime[0]

                    request.setAttribute('responseMimeType', responseMimeType)
                    request.setAttribute('responseFileType', responseFileType)


                }
                // todo : test requestEncoding against apiProperties.encoding

            } catch (Exception e) {
                throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
            }

            request.setAttribute('uriList', this.uriList)

            if (this.apiObject) {
                // todo : create public api list
                if (this.method == 'GET') {

                    setCacheHash(request.getAttribute('params'), this.receivesList)

                    // RETRIEVE CACHED RESULT (only if using 'GET' method)
                    if ((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${cacheHash}" || apiObject?.cachedResult?."permitAll"?."${responseFileType}"?."${cacheHash}")) {
                        String cachedResult
                        try {
                            cachedResult = (apiObject['cachedResult'][authority]) ? apiObject['cachedResult'][authority][responseFileType][cacheHash] : apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]
                        } catch (Exception e) {
                            throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
                        }

                        if (cachedResult && cachedResult.size() > 0) {
                            // PLACEHOLDER FOR APITHROTTLING
                            response.setStatus(200);
                            PrintWriter writer = response.getWriter();
                            writer.write(cachedResult);
                            writer.close()
                            //response.writer.flush()
                            //return false
                        }
                    }
                }
            }
        }

        if(apidocFwd){
            def servletCtx = this.ctx.getServletContext()
            String newPath = "/v${uriList[2]}/${uriList[4]}/${uriList[5]}/${uriList[4]}"
            def rd = servletCtx?.getRequestDispatcher(newPath)
            rd.forward(request, response)
        }else{
            chain.doFilter(request, response)
        }

    }


    protected String getFormat(String mimeType){
        String format
        switch(mimeType){
            case 'text/json':
            case 'application/json':
                format = 'JSON'
                break;
            case 'text/xml':
            case 'application/xml':
                format = 'XML'
                break;
            default:
                break;
        }
        return format
    }


    public ArrayList setUri(String uri, String version){
        // [callType, version, appVersion, apiVersion, controller, action, trace, id]
        Integer callType
        boolean trace = false
        ArrayList uriList = []

        ArrayList uriVars = uri.split('/')
        String tempVersion = uriVars[1].toLowerCase()

        switch(tempVersion){
            case ~/([v|b|c|t])(${version})-([0-9]+)/:
            case ~/([v|b|c|t])(${version})/:
                def m = Matcher.lastMatcher
                callType = (CALL_TYPES.indexOf(m[0][1])+1)
                uriList.add(callType)
                uriList.add(m[0][2])
                uriList.add(this.version)
                uriList.add(((m[0][3])?m[0][3]:'1'))
                uriList.add(uriVars[2])
                uriList.add(uriVars[3])

                if(callType==5){ trace=true }
                uriList.add(trace)


                if(uriVars[4]){ uriList.add(URLDecoder.decode(uriVars[4], StandardCharsets.UTF_8.toString())) }
                break
        }

        return uriList
    }

    /**
     * Returns concatenated IDS as a HASH used as ID for the API cache
     * @see io.beapi.api.interceptor.ApiInterceptor#before()
     * @see BatchInterceptor#before()
     * @see ChainInterceptor#before()
     * @param LinkedHashMap List of ids required when making request to endpoint
     * @return a hash from all id's needed when making request to endpoint
     */
    protected void setCacheHash(LinkedHashMap params,ArrayList receivesList){
        StringBuilder hashString = new StringBuilder('')
        receivesList.each(){ it ->
            hashString.append(params[it])
            hashString.append("/")
        }
        this.cacheHash = Hashing.murmur3_32().hashString(hashString.toString(), StandardCharsets.UTF_8).toString()
    }

    protected boolean checkNetworkGrp(ArrayList networkRoles, String authority){
        return networkRoles.contains(authority)
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
    boolean checkRequestParams(LinkedHashMap methodParams){
        ArrayList checkList = this.receivesList
        ArrayList paramsList
        ArrayList reservedNames = ['batchLength','batchInc','chainInc','apiChain','_','batch','max','offset','chaintype']

        try {
            if(checkList && params) {
                if (checkList?.contains('*')) {
                    return true
                } else {
                    paramsList = methodParams.keySet() as ArrayList

                    // remove reservedNames from List
                    reservedNames.each() { paramsList.remove(it) }

                    //println("paramlist : "+paramsList)
                    //println("checklist : "+checkList)

                    if (paramsList.size() == checkList?.intersect(paramsList).size()) {
                        return true
                    }
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

    protected void parseParams(HttpServletRequest request, String formData, String uriData, String id){
        LinkedHashMap<String,String> get = parseGetParams(uriData, id)
        request.setAttribute('GET',get)
        LinkedHashMap<String,String> post = parsePutParams(formData)

        // set batchVars if they are present
        if(post['batchVars']){
            if(!request.getAttribute('batchVars')) { request.setAttribute('batchVars', post['batchVars']) }
            post.remove('batchVars')
        }

        if(post['chainOrder']){
            if(!request.getAttribute('chainOrder')) { request.setAttribute('chainOrder', post['chainOrder']) }
            post.remove('chainOrder')
        }

        if(post['chainType']){
            if(!request.getAttribute('chainType')) { request.setAttribute('chainType', post['chainType']) }
            post.remove('chainType')
        }

        if(post['chainKey']){
            if(!request.getAttribute('chainKey')) { request.setAttribute('chainKey', post['chainKey']) }
            post.remove('chainKey')
        }

        if(post['chainSize']){
            if(!request.getAttribute('chainSize')) { request.setAttribute('chainSize', post['chainSize']) }
            post.remove('chainSize')
        }

        if(post['chainParams']){
            if(!request.getAttribute('chainParams')) { request.setAttribute('chainParams', post['chainParams']) }
            post.remove('chainParams')
        }

        request.setAttribute('POST',post)
        LinkedHashMap<String,String> output = get + post
        request.setAttribute('params',output)
    }

    private LinkedHashMap parseGetParams(String uriData, String id){
        LinkedHashMap<String, String> output = [:]
        ArrayList pairs = uriData?.split("&");
        if(pairs) {
            pairs.each() { it ->
                int idx = it.indexOf("=");
                String key = URLDecoder.decode(it.substring(0, idx).toString(), "UTF-8");
                String val = URLDecoder.decode(it.substring(idx + 1).toString(), "UTF-8");
                output[key] = val;
            }
        }

        if (Objects.nonNull(id)) {
            output['id'] = id.toString()
        }

        return output
    }

    private LinkedHashMap parsePutParams(String formData){
        //String formData = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LinkedHashMap<String, String> output = [:]
        if (formData) {
            LinkedHashMap object
            try {
                switch (this.requestFileType) {
                    case 'JSON':
                        //object = new JSONObject(formData)
                        Object temp = new JsonSlurper().parseText(formData)
                        object = toToLinkedHashMap(temp)
                        break
                    case 'XML':
                        //object = XML.toJSONObject(formData)
                        break
                }
            } catch (Exception e) {
                throw new Exception("[RequestInitializationFilter :: parsePutParams] : Badly formatted '${this.requestFileType}'. Please check the syntax and try again")
            }


            if(object) {
                Set<String> keyset = object.keySet()
                Iterator keys = keyset.iterator();

                switch(keyset){
                    case {it.contains('chain')}:
                        LinkedHashMap temp = [:]
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.toString() == 'chain') {
                                LinkedHashMap chainOrder = [:]
                                def temp2 = object.get('chain').remove('order').entrySet()
                                temp2.each() { it ->
                                    chainOrder[it.getKey()] = it.getValue()
                                }

                                output['chainOrder'] = chainOrder
                                output['chainType'] = object.get('chain')['chaintype']
                                output['chainKey'] = object.get('chain')['initdata']
                                output['chainSize'] = chainOrder.size()
                            }else{
                                temp[key] = object.get(key)
                            }
                        }
                        if(!temp.isEmpty()){
                            output['chainParams'] = temp
                        }

                        break;
                    case {it.contains('batch')}:
                        output['batchVars'] = []
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.toString() == 'batch') {
                                output['batchVars'] = object.get('batch') as LinkedList
                            }else{
                                output[key] = object.get(key)
                            }
                        }
                        break;
                    default:
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (!RESERVED_PARAM_NAMES.contains(key)) {
                                output[key] = object.get(key)
                            } else {
                                throw new Exception("[RequestInitializationFilter :: parsePutParams] : Batch/Chain call attempted on regular API endpoint without sending required params [ie batch/chain]")
                            }
                        }
                        break;
                }
            }
        }
        return output
    }

    def toToLinkedHashMap(def obj) {
        if (obj instanceof org.apache.groovy.json.internal.LazyMap) {
            Map copy = [:];
            for (pair in (obj as Map)) {
                copy.put(pair.key.toString(), toToLinkedHashMap(pair.value));
            }
            return copy;
        }
        if (obj instanceof org.apache.groovy.json.internal.ValueList) {
            List copy = [];
            for (item in (obj as List)) {
                copy.add(toToLinkedHashMap(item.toString()));
            }
            return copy;
        }
        return obj;
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
        response.writer.flush()
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
        if(msg.isEmpty()){ msg = ErrorCodes.codes[statusCode]['long'] }
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        response.writer.flush()
    }
}
