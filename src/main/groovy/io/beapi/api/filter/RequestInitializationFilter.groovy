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
import io.beapi.api.service.LinkRelationService
import io.beapi.api.service.SessionService
import io.beapi.api.service.ThrottleService
import io.beapi.api.utils.UriObject
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
import org.apache.catalina.util.ParameterMap

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component


import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.regex.Matcher

import io.beapi.api.utils.ApiDescriptor
import org.apache.commons.io.IOUtils
import com.google.common.hash.Hashing

import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.method.HandlerMethod;
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.web.cors.CorsUtils
import javax.servlet.RequestDispatcher
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.slf4j.LoggerFactory
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import javax.servlet.http.HttpSession
import org.springframework.web.util.WebUtils
import javax.servlet.http.Cookie
import java.util.regex.Pattern;

/**
 * This class parses the URI attributes on initial request &
 * runs some simple access checks
 *
 * @author Owen Rubel
 */

//@Order(21)
@Component
@EnableConfigurationProperties([ApiProperties.class])
class RequestInitializationFilter extends OncePerRequestFilter{

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RequestInitializationFilter.class);
    private String markerText = "DEVNOTES";
    private Marker devnotes = MarkerFactory.getMarker(markerText);

    /*
    * WE DO NOT DO SUPPORT 'multipart/form-data'; THIS IS NOT A FILE SERVE!!!
     */
    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml']
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
    private static final ArrayList CALL_TYPES = ['v','b','c','t']

    @Autowired
    private ApplicationContext ctx

    protected ThrottleService throttleService
    protected LinkRelationService linkRelationService
    protected PrincipleService principle
    protected ApiCacheService apiCacheService
    protected ApiProperties apiProperties
    protected SessionService sessionService;

    // [CACHE]
    protected ApiDescriptor apiObject
    protected String cacheHash

    // todo : parse headers
    protected LinkedHashMap<String, List<String>> headers = [:]
    protected UriObject uObj
    protected String uri
    protected LinkedHashMap receives = [:]
    protected ArrayList receivesList
    protected LinkedHashMap rturns = [:]
    protected String method, version, controller, action
    protected String requestFileType, responseFileType
    protected boolean reservedUri
    protected LinkedHashMap params = [:]
    protected String authority
    protected ArrayList deprecated



    /**
     * @param PrincipleService principle
     * @param ApiProperties apiProperties
     * @param ApiCacheService apiCacheService
     * @param String version
     * @param ApplicationContext ctx
     */
    public RequestInitializationFilter(ThrottleService throttleService, LinkRelationService linkRelationService, PrincipleService principle, ApiProperties apiProperties, ApiCacheService apiCacheService, SessionService sessionService, String version, ApplicationContext ctx) {
        this.throttleService = throttleService
        this.linkRelationService = linkRelationService
        this.apiProperties = apiProperties
        this.version = version
        this.ctx = ctx
        this.principle = principle
        this.apiCacheService = apiCacheService
        this.sessionService = sessionService
    }

    /**
     * (overridden method)
     * @param HttpServletRequest request
     * @param HttpServletResponse response
     * @param FilterChain chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        println("### RequestInitializationFilter > "+request.getRequestURI())

        // [ SHOW SESSION VARIABLES ]
        //if(sessionService.sessionExists()) {
            println(sessionService.sessionExists()==true)
            println(request.getSession().getId()+"=="+WebUtils.getCookie(request, 'JSESSIONID')?.getValue())

            request.setCharacterEncoding("UTF-8")
            this.authority = (this.principle.authorities()) ? this.principle.authorities() : "ROLE_ANONYMOUS"


            Pattern p = ~/[v|b|c|r]${version}/
            Matcher match = p.matcher(request.getRequestURI()[1..4])

            // route for simpleHandlerMapping
            if (match.find()) {
                if (processRequest(request, response)) {
                    try {
                        Map<String, HandlerMapping> handlerMappingMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.ctx, HandlerMapping.class, true, false);
                        handlerMappingMap.each { k, v ->
                            v.getHandler(request).getClass()
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    // ratelimiting
                    if (apiProperties.throttle.active) {
                        try {
                            throttleService.incrementThrottle(this.authority)
                            //chain.doFilter(request, response);
                        } catch (Exception e) {
                            // return response as ratelimit failure and do not continue with chain
                            String msg = "Too many requests. Please wait " + (apiProperties.throttle.staleSession / 6000) + " minutes before making another request.: " + e
                            writeErrorResponse(response, '400', request.getRequestURI(), msg);
                        }
                    }
                } catch (org.springframework.security.access.AccessDeniedException ade) {
                    if (this.authority == 'ROLE_ANONYMOUS') {
                        logger.info("BAD AUTHORITY ACCESS ATTEMPT for {" + request.getRequestURI() + "} with  {\"ROLE_ANONYMOUS\"}")
                    } else {
                        logger.info("BAD AUTHORITY ACCESS ATTEMPT for {" + request.getRequestURI() + "} with auth {\"+this.authority+\"} by {" + principle.name + "}")
                    }
                } catch (Exception e) {
                    throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Exception - full stack trace follows:", e)
                }


            }

            try {
                chain.doFilter(request, response);
            } catch (Exception e) {
                throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Exception - full stack trace follows:", e)
            }
        //}else{
        //    String msg = "Invalid Session"
        //    writeErrorResponse(response, '400', request.getRequestURI(), msg);
        //}

    }


    /**
     * @param HttpServletRequest request
     * @param HttpServletResponse response
     * @returns boolean
     */
    private boolean processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //println("### processRequest ...")

        if(request){

            if(this.authority!='ROLE_ANONYMOUS') {
                //logger.debug("doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) : {}");
                String cachedResult
                this.uri = request.getRequestURI()

                /*
                * processing for 'static uris' in starter
                */
                if (apiProperties.reservedUris.contains(request.getRequestURI())) {
                    ArrayList uriVars = uri.split('/')
                    this.controller = uriVars[0]
                } else {
                    createUriObj()

                    request.setAttribute('uriObj', this.uObj)
                    setPrinciple(request)

                    try {
                        this.method = request.getMethod()
                        this.reservedUri = (apiProperties.reservedUris.contains(this.uri)) ? true : false
                        def cache = apiCacheService?.getApiCache(this.uObj.getController())

                        if (cache) {
                            def temp = cache[this.uObj.getApiVersion()]
                            this.deprecated = temp['deprecated'] as List
                            this.apiObject = temp[this.uObj.getAction()]

                            if (this.apiObject?.'returns') {
                                this.rturns = this.apiObject['returns'] as LinkedHashMap
                            } else {
                                logger.warn(devnotes,"[ BAD IOSTATE DEFINITION ] : IOSTATE DEFINITION FOR '${this.uObj.getController()}/${this.uObj.getAction()}' does not have 'RESPONSE' dataset for the authority '${this.authority}'. IF THIS IS AN ISSUE, FIX THIS BY ADDING THE 'ROLE' TO THE 'RESPONSE' DATASETS.")
                                writeErrorResponse(response, '400', request.getRequestURI());
                                return false
                            }

                            // CHECKING REQUEST/RESPONSE DATASETS (RBAC/ABAC)
                            ArrayList networkRoles = cache.networkGrpRoles

                            if (checkNetworkGrp(networkRoles, this.authority)) {
                                this.receivesList = (this.apiObject?.getReceivesList()[this.authority])?:this.apiObject?.getReceivesList()['permitAll']
                                request.setAttribute('receivesList', this.receivesList)

                                ArrayList returnsList = (this.apiObject?.getReturnsList()[this.authority])?:this.apiObject?.getReturnsList()['permitAll']
                                if (returnsList != null) {
                                    request.setAttribute('returnsList', returnsList)
                                }else{
                                    logger.warn(devnotes,"[ BAD IOSTATE DEFINITION ] : THE AUTHORITY '${this.authority}' DOES NOT EXIST IN THE 'RESPONSE' DATASET FOR '${this.uObj.getController()}/${this.uObj.getAction()}' IN YOUR IOSTATE FILE. IF THIS IS AN ISSUE, FIX THIS BY ADDING THIS 'ROLE' TO THE 'RESPONSE' DATASETS.")
                                    throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Authority '${this.authority}' for '${uri}' does not exist in IOSTATE 'REQUEST'")
                                }
                            } else {
                                logger.warn(devnotes,"[ BAD AUTHORITY ] : THE AUTHORITY '${this.authority}' DOES NOT EXIST IN THE 'NETWORKGRP' IN YOUR IOSTATE FILE FOR '${this.uObj.getController()}/${this.uObj.getAction()}'. IF THIS IS AN ISSUE, FIX BY ADDING 'ROLE' TO THE NETWORKGRP IN YOUR BEAPI_API.YML FILE")

                                String msg = "Authority '${this.authority}' for '${uri}' does not exist in IOState NetworkGrp"
                                writeErrorResponse(response, '401', request.getRequestURI(), msg);

                                return false
                                //throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Authority '${this.authority}' for '${uri}' does not exist in IOState NetworkGrp")
                            }

                            if (this.uObj.getId()) {
                                if (!this.receivesList?.contains('id')) {
                                    logger.warn(devnotes, "[ ATTRIBUTE BASED ACCESS CONTROL(ABAC) MISMATCH (1) ] : PARAMS SENT FOR '${this.uObj.getController()}/${this.uObj.getAction()}' DO NOT MATCH EXPECTED 'REQUEST' PARAMS. IF THIS IS AN ISSUE, FIX BY ADDING THE PARAM TO THE IOSTATE FILE.")
                                    writeErrorResponse(response, '400', request.getRequestURI());
                                    return false
                                }
                            }

                            String temp2 = (request.getHeader('Accept') != null && request.getHeader('Accept') != "*/*") ? request.getHeader('Accept') : (request.getContentType() == null) ? 'application/json' : request.getContentType()
                            ArrayList reqMime = temp2.split(';')
                            ArrayList respMime = reqMime
                            String requestMimeType = reqMime[0]
                            String responseMimeType = respMime[0]


                            this.requestFileType = (SUPPORTED_MIME_TYPES.contains(requestMimeType)) ? getFormat(requestMimeType) : 'JSON'
                            this.responseFileType = (this.requestFileType) ? this.requestFileType : getFormat(respMime[0])
                            if (!this.requestFileType || !this.responseFileType) {
                                logger.warn(devnotes,"[ UNSUPPORTED MIMETYPE (1) ] : ACCEPT/CONTENT-TYPE HEADERS MUST BE A SUPPORTED MIMETYPE OF '${SUPPORTED_MIME_TYPES}'. IF THIS IS AN ISSUE, FILE A TICKET TO ADD A NEW SUPPORTED MIMETYPE (https://github.com/Beapi-io/spring-boot-starter-beapi/issues).")

                                String msg = "Request MimeType must be one of the supported mimetypes (JSON/XML)"
                                writeErrorResponse(response, '400', request.getRequestURI(), msg);
                                return false
                                //throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Accept/Request mimetype unsupported")
                            }

                            //
                            request.setAttribute('responseMimeType', responseMimeType)
                            request.setAttribute('responseFileType', responseFileType)

                        }
                        // todo : test requestEncoding against apiProperties.encoding

                    } catch (Exception e) {
                        throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
                    }
                    parseParams(request, this.uObj.getId())

                    if (this.apiObject && this.uObj.callType==1) {
                        if(!checkRequestParams(request.getAttribute('params'))) {
                            logger.warn(devnotes,"[ ATTRIBUTE BASED ACCESS CONTROL(ABAC) MISMATCH (2) ] : PARAMS SENT FOR '${this.uObj.getController()}/${this.uObj.getAction()}' DO NOT MATCH EXPECTED 'REQUEST' PARAMS. IF THIS IS AN ISSUE, FIX BY ADDING THE PARAM TO THE IOSTATE FILE.")
                            writeErrorResponse(response, '400', request.getRequestURI(), "PARAMS SENT FOR '${this.uObj.getController()}/${this.uObj.getAction()}' DO NOT MATCH EXPECTED 'REQUEST' PARAMS. IF THIS IS AN ISSUE, FIX BY ADDING THE PARAM TO THE IOSTATE FILE.");
                            return false
                            //throw new Exception("[RequestInitializationFilter :: checkRequestParams] : Requestparams do not match expected params for this endpoint")
                        }

                        if (validCacheRequestMethod(this.method)) {

                            setCacheHash(request.getAttribute('params'), this.receivesList)
                            request.setAttribute('cacheHash', this.cacheHash)

                            if ((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${this.cacheHash}" || apiObject?.cachedResult?."permitAll"?."${responseFileType}"?."${this.cacheHash}")) {
                                try {
                                    cachedResult = (apiObject['cachedResult'][authority]) ? apiObject['cachedResult'][authority][responseFileType][cacheHash] : apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]
                                } catch (Exception e) {
                                    logger.warn(devnotes,"[ BAD CACHED RESULT ] : THIS SHOULD NOT HAPPEN. PLEASE FILE A TICKET WITH A FULL STACKTRACE AND EXPLANATION OF WHAT YOU WERE TRYING TO DO (https://github.com/Beapi-io/spring-boot-starter-beapi/issues).")
                                    throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
                                }

                                // todo : check throttle cache size
                                if(cachedResult && cachedResult.size() > 0) {
                                    // println("### RequestInitializationFilter (cachedResult)")
                                    // PLACEHOLDER FOR APITHROTTLING
                                    String linkRelations = linkRelationService.processLinkRelations(request, response, this.apiObject)
                                    String newResult = (linkRelations)?"[${cachedResult},${linkRelations}]":cachedResult

                                    response.setStatus(200);
                                    PrintWriter writer = response.getWriter();

                                    writer.write(newResult);
                                    writer.close()
                                    //response.writer.flush()
                                    return false
                                }
                            }
                        }

                    }
                }
                return true
            }
        }else{
            // println("No REQUEST")
            //return true
        }
        return false
    }

    /**
     * retrieve a finalized input/ouput set from receives/returns dataset for the role from the cache
     * @param LinkedHashMap input
     * @param String role
     * @returns Set
     */
    private Set getIOSet(LinkedHashMap input, String role){
        Set params = []
        if(input.keySet().contains[role]) {
            input[role].each() { it ->
                if (it) {
                    params.add(it)
                }
            }
        }else{
            input['permitAll'].each() { it ->
                if (it) {
                    params.add(it)
                }
            }
        }
        return params
    }

    private boolean validCacheRequestMethod(String method){
        if(method == 'GET' && this.apiObject['method'].toUpperCase()==method){
            return true
        }
        return false
    }

    /**
     * get simple string version of mimetype formats the application supports
     * @param String mimeType
     * @returns String
     */
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

    /**
     * hashes concatenated keys of response set and uses as ID for the API cache
     * @param LinkedHashMap params
     * @param ArrayList receivesList
     */
    protected void setCacheHash(LinkedHashMap params,ArrayList receivesList){
        //println("###setCacheHash")
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

    protected void setPrinciple(HttpServletRequest request){
        if (!request.getAttribute('principle')) {
            request.setAttribute('principle', this.authority)
        }
    }

    /**
     * Given the request params, check against expected parms in IOstate for users role; returns boolean
     * @param LinkedHashMap map of variables defining endpoint request variables
     * @return Boolean returns false if request variable keys do not match expected endpoint keys
     */
    boolean checkRequestParams(LinkedHashMap methodParams){
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

                        //println("received :"+checkList)
                        //println("expected :"+paramsList)

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

        }catch(Exception e) {
            throw new Exception("[RequestInitializationFilter :: checkRequestParams] : Exception - full stack trace follows:",e)
        }
        return false
    }

    protected void parseParams(HttpServletRequest request, String id){

        LinkedHashMap<String, String> get = [:]

        if(request.getQueryString()) {
            String queryString = URLDecoder.decode(request.getQueryString(), "UTF-8");
            get = parseGetParams(queryString, id)
            request.setAttribute('GET', get)
        }

        LinkedHashMap<String, String> post = parsePutParams(IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8))


        // set batchVars if they are present
        switch (this.uObj.callType) {
            case '1':
                break;
            case '2':
                if (post['batchVars']) {
                    if (!request.getAttribute('batchVars')) {
                        request.setAttribute('batchVars', post['batchVars'])
                    }
                    post.remove('batchVars')
                }
                break;
            case '3':
                if (post['chainOrder']) {
                    if (!request.getAttribute('chainOrder')) {
                        request.setAttribute('chainOrder', post['chainOrder'])
                    }
                    post.remove('chainOrder')
                }

                if (post['chainType']) {
                    if (!request.getAttribute('chainType')) {
                        request.setAttribute('chainType', post['chainType'])
                    }
                    post.remove('chainType')
                }

                if (post['chainKey']) {
                    if (!request.getAttribute('chainKey')) {
                        request.setAttribute('chainKey', post['chainKey'])
                    }
                    post.remove('chainKey')
                }

                if (post['chainSize']) {
                    if (!request.getAttribute('chainSize')) {
                        request.setAttribute('chainSize', post['chainSize'])
                    }
                    post.remove('chainSize')
                }

                if (post['chainParams']) {
                    if (!request.getAttribute('chainParams')) {
                        LinkedHashMap newMap = post['chainParams'].collectEntries { key, value -> [key, value.toString()] }
                        request.setAttribute('chainParams', newMap)
                    }
                    post.remove('chainParams')
                }
                break;
        }

        request.setAttribute('POST', post)
        LinkedHashMap<String, String> output = get + post
        request.setAttribute('params', output)
    }

    private LinkedHashMap parseGetParams(String uriData, String id){
        LinkedHashMap<String, String> output = [:]
        ArrayList pairs = uriData?.split("&");
        if(pairs) {
            pairs.each() { it ->
                int eq = it.indexOf("=");
                String key = it.substring(0, eq).toString();
                String val = it.substring(eq + 1).toString();
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
            JSONObject object
            try {
                switch (this.requestFileType) {
                    case 'JSON':
                        object = new JSONObject(formData)
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
                    case {it.contains('IOSTATE')}:
                        def slurp = new JsonSlurper().parseText(formData)
                        LinkedHashMap json = toToLinkedHashMap(slurp)
                        output = json
                        break;
                    default:
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (!RESERVED_PARAM_NAMES.contains(key)) {
                                output[key] = object.get(key).toString()
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
                copy.add(toToLinkedHashMap(item));
            }
            return copy;
        }
        return obj;
    }



    protected String getClientIpAddress(HttpServletRequest request) {
        String[] IP_HEADER_CANDIDATES = ["X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"];
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }


    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    private void writeErrorResponse(HttpServletResponse response, String statusCode, String uri){
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${ErrorCodes.codes[statusCode]['long']}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        //response.writer.flush()
        SecurityContextHolder.getContext().getRequestDispatcher("/error").forward(request, response);
    };

    // Todo : Move to exchangeService??
    /**
     * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
     * @param HttpServletResponse response
     * @param String statusCode
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    private void writeErrorResponse(HttpServletResponse response, String statusCode, String uri, String msg){
        response.setContentType("application/json")
        response.setStatus(Integer.valueOf(statusCode))
        if(msg.isEmpty()){
            msg = ErrorCodes.codes[statusCode]['long']
        }
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        //response.writer.flush()
        SecurityContextHolder.getContext().getRequestDispatcher("/error").forward(request, response);
    };

    private void createUriObj() {
        this.uObj = new UriObject(this.uri, this.version)
    }
}
