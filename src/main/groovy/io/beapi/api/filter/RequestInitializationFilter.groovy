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

import org.json.JSONObject
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import io.beapi.api.utils.ErrorCodes
import io.beapi.api.utils.UriObject
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
    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml']
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
    private static final ArrayList CALL_TYPES = ['v','b','c','r','t']

    @Autowired
    ApplicationContext ctx


    PrincipleService principle
    ApiCacheService apiCacheService
    ApiProperties apiProperties
    String version
    LinkedHashMap networkGrpRoles

    // [CACHE]
    protected ApiDescriptor apiObject
    String cacheHash

    // todo : parse headers
    LinkedHashMap<String, List<String>> headers = [:]

    ArrayList networkRoles
    ArrayList uriList
    String uri
    UriObject uriObject
    LinkedHashMap receives = [:]
    ArrayList receivesList = []
    LinkedHashMap rturns = [:]
    String method
    String controller
    String requestFileType
    String responseFileType
    boolean reservedUri
    int cores
    LinkedHashMap params = [:]
    String authority
    ArrayList deprecated
    String action
    String handlerType
    ArrayList networkGrps = []

    public RequestInitializationFilter(PrincipleService principle, ApiProperties apiProperties, ApiCacheService apiCacheService, String version, ApplicationContext ctx) {
        this.apiProperties = apiProperties
        this.version = version
        this.cores = apiProperties.getProcCores()
        this.ctx = ctx
        this.networkGrpRoles = apiProperties.security.networkRoles
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
        //println("### RequestInitializationFilter")


        this.uri = request.getRequestURI()

        // todo : will imporove in future version
        if(apiProperties.reservedUris.contains(request.getRequestURI())) {
            // ### PUBLIC APIS ###
            ArrayList uriVars = uri.split('/')
            this.controller = uriVars[0]

        }else{
            // ### APIS HANDLED BY THE STARTER ###
            request.getSession().removeAttribute('handler')
            request.getSession().removeAttribute('handlerName')

            this.authority = this.principle.authorities()
            this.uriList = setUri(this.uri, this.version)

            HandlerMapping handlerMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.ctx, HandlerMapping.class, true, false).get("simpleUrlHandlerMapping");
            def test = handlerMappings.getHandler(request)?.getHandler()
            String handler = test?.class?.getName()
            String handlerName = test?.class?.getSimpleName()
            String shortName = this.uriList[4].capitalize() + "Controller"

            request.setAttribute('uriList', uriList)
            request.setAttribute('cores', this.cores)

            try {
                // make sure they exist as handler and in IO state
                if (test && shortName == handlerName) {
                    request.getSession().setAttribute('handler', handler)
                    request.getSession().setAttribute('handlerName', handlerName)
                }

                this.method = request.getMethod()
                this.reservedUri = (apiProperties.reservedUris.contains(this.uri)) ? true : false

                // get apiObject
                def cache = apiCacheService.getApiCache(uriList[4])
                def temp = cache[uriList[3]]

                //String defaultAction = (temp['defaultAction'])?temp['defaultAction']:'error'
                //this.action = (uriList[5])?uriList[5]:defaultAction

                this.deprecated = temp['deprecated'] as List

                if (cache) {
                    this.apiObject = temp[uriList[5]]

                    String handlerType = this.apiObject.getType()
                    if (request.getAttribute('handlerType')) { request.removeAttribute('handlerType') }
                    request.setAttribute('handlerType', handlerType)

                    this.receives = this.apiObject.getReceives()
                    this.rturns = this.apiObject['returns'] as LinkedHashMap

                    String networkGrp = this.apiObject['networkGrp']
                    ArrayList networkRoles = this.networkGrpRoles[networkGrp].collect() { k, v -> v }
                    LinkedHashMap corsNetworkGroups = apiProperties.security.corsNetworkGroups

                    corsNetworkGroups[networkGrp].each { k, v -> this.networkGrps.add(v) }

                    String temp2 = (request.getHeader('Accept') != null) ? request.getHeader('Accept') : request.getContentType()
                    ArrayList tempMimeType = temp2.split(';')
                    String requestMimeType = tempMimeType[0]
                    String requestEncoding = tempMimeType[1]

                    this.requestFileType = (SUPPORTED_MIME_TYPES.contains(requestMimeType)) ? getFormat(requestMimeType) : 'JSON'
                    this.responseFileType = (this.requestFileType) ? this.requestFileType : getFormat(responseMimeType)
                    String responseMimeType = requestMimeType

                    request.setAttribute('responseMimeType', responseFileType)
                    request.setAttribute('responseFileType', responseFileType)

                    parseParams(request, IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), request.getQueryString(), uriList[7])

                    //validateMethod()
                    if (checkNetworkGrp(networkRoles, this.authority)) {
                        setReceivesList(request)
                        setReturnsList(request)
                    } else {
                        throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Request params do not match expect params for '${uri}'")
                    }

                    if (!checkRequestParams(request.getSession().getAttribute('params'))) {
                        writeErrorResponse(response, '400', request.getRequestURI());
                    }

                    // todo : test requestEncoding against apiProperties.encoding
                }
            } catch (Exception e) {
                throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
            }

            if (this.apiObject) {
                // todo : create public api list
                if (this.method == 'GET') {

                    setCacheHash(request.getSession().getAttribute('params'), this.receivesList)

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

        //processFilterChain(request, response, chain)
        chain.doFilter(request, response)
    }

/*
    // Check CORS
    private void processFilterChain(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        //ArrayList headers = Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(Function.identity()), h -> Collections.list(request.getHeaders(h))))

        boolean options = ('OPTIONS'==request.method.toUpperCase())

        if(!apiProperties.reservedUris.contains(request.getRequestURI())) {
            if(!networkGrps){
                String msg = "NETWORKGRP for IO State file :" + controller + " cannot be found. Please double check it against available NetworkGroups in the beapi_api.yml config file."
                writeErrorResponse(response, '401', request.getRequestURI(), msg);
            }
        }

        String origin = request.getHeader('Origin')

        if (options) {
            response.addHeader('Allow', 'GET, HEAD, POST, PUT, DELETE, TRACE, PATCH')
            if (origin != 'null') {
                //response.setHeader("Access-Control-Allow-Headers", "Cache-Control,  Pragma, WWW-Authenticate, Origin, X-Requested-With, authorization, Content-Type,Access-Control-Request-Headers,Access-Control-Request-Method,Access-Control-Allow-Credentials")
                response.addHeader('Access-Control-Allow-Headers', 'Accept, Accept-Charset, Accept-Datetime, Accept-Encoding, Accept-Ext, Accept-Features, Accept-Language, Accept-Params, Accept-Ranges, Access-Control-Allow-Headers, Access-Control-Allow-Methods, Access-Control-Allow-Origin, Access-Control-Expose-Headers, Access-Control-Max-Age, Access-Control-Request-Headers, Access-Control-Request-Method, Age, Allow, Alternates, Authentication-Info, Authorization, C-Ext, C-Man, C-Opt, C-PEP, C-PEP-Info, CONNECT, Cache-Control, Compliance, Connection, Content-Base, Content-Disposition, Content-Encoding, Content-ID, Content-Language, Content-Length, Content-Location, Content-MD5, Content-Range, Content-Script-Type, Content-Security-Policy, Content-Style-Type, Content-Transfer-Encoding, Content-Type, Content-Version, Cookie, Cost, DAV, DELETE, DNT, DPR, Date, Default-Style, Delta-Base, Depth, Derived-From, Destination, Differential-ID, Digest, ETag, Expect, Expires, Ext, From, GET, GetProfile, HEAD, HTTP-date, Host, IM, If, If-Match, If-Modified-Since, If-None-Match, If-Range, If-Unmodified-Since, Keep-Alive, Label, Last-Event-ID, Last-Modified, Link, Location, Lock-Token, MIME-Version, Man, Max-Forwards, Media-Range, Message-ID, Meter, Negotiate, Non-Compliance, OPTION, OPTIONS, OWS, Opt, Optional, Ordering-Type, Origin, Overwrite, P3P, PEP, PICS-Label, POST, PUT, Pep-Info, Permanent, Position, Pragma, ProfileObject, Protocol, Protocol-Query, Protocol-Request, Proxy-Authenticate, Proxy-Authentication-Info, Proxy-Authorization, Proxy-Features, Proxy-Instruction, Public, RWS, Range, Referer, Refresh, Resolution-Hint, Resolver-Location, Retry-After, Safe, Sec-Websocket-Extensions, Sec-Websocket-Key, Sec-Websocket-Origin, Sec-Websocket-Protocol, Sec-Websocket-Version, Security-Scheme, Server, Set-Cookie, Set-Cookie2, SetProfile, SoapAction, Status, Status-URI, Strict-Transport-Security, SubOK, Subst, Surrogate-Capability, Surrogate-Control, TCN, TE, TRACE, Timeout, Title, Trailer, Transfer-Encoding, UA-Color, UA-Media, UA-Pixels, UA-Resolution, UA-Windowpixels, URI, Upgrade, User-Agent, Variant-Vary, Vary, Version, Via, Viewport-Width, WWW-Authenticate, Want-Digest, Warning, Width, xsrf-token, X-Content-Duration, X-Content-Security-Policy, X-Content-Type-Options, X-CustomHeader, X-DNSPrefetch-Control, X-Forwarded-For, X-Forwarded-Port, X-Forwarded-Proto, X-Frame-Options, X-Modified, X-OTHER, X-PING, X-PINGOTHER, X-Powered-By, X-Requested-With')
                response.addHeader('Access-Control-Allow-Methods', 'POST, PUT, DELETE, TRACE, PATCH, OPTIONS')
                response.addHeader('Access-Control-Expose-Headers', 'xsrf-token, Location,X-Auth-Token')
                response.addHeader('Access-Control-Max-Age', '3600')
            }
        }

        if (networkGrps && networkGrps.contains(origin)) {
            request.setAttribute('CORS', true)
            response.setHeader('Access-Control-Allow-Origin', origin)
            response.addHeader('Access-Control-Allow-Credentials', 'true')
        } else if (!networkGrps) { // no networkGrps??? white list all
            // add CORS access control headers for all origins
            if (origin) {
                response.setHeader('Access-Control-Allow-Origin', origin)
                response.addHeader('Access-Control-Allow-Credentials', 'true')
            } else {
                response.addHeader('Access-Control-Allow-Origin', '*')
            }
        }
        response.status = HttpStatus.OK.value()

        if(!options ) {
            chain.doFilter(request, response)
        }
        //chain.doFilter(request, response)
    }
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
                // todo : check if callType is 'resource'
                break;
        }
        return format
    }

    protected void setReceivesList(HttpServletRequest request){
        ArrayList result = []
        this.receives.each() { k, v ->
            if([this.authority,'permitAll'].contains(k)) {
                v.each { it2 ->
                    if (!result.contains(it2['name'])) {
                        result.add(it2['name'])
                    }
                }
            }
        }
        this.receivesList = result
        request.getSession().setAttribute('receivesList', result)
    }

    protected void setReturnsList(HttpServletRequest request){
        ArrayList result = []
        this.rturns.each() { k, v ->
            if([this.authority,'permitAll'].contains(k)) {
                v.each() { it2 ->
                    if (!result.contains(it2['name'])) {
                        result.add(it2['name']) }
                }
            }
        }
        request.getSession().setAttribute('returnsList', result)
    }

    public ArrayList setUri(String uri, String version){
        // [callType, version, appVersion, apiVersion, controller, action, trace, id]
        Integer callType
        boolean trace = false
        ArrayList uriList = []

        ArrayList uriVars = uri.split('/')
        String tempVersion = uriVars[1].toLowerCase()

        switch(tempVersion){
            case ~/([v|b|c|r|t])(${version})-([0-9]+)/:
            case ~/([v|b|c|r|t])(${version})/:
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
            throw new Exception("[RequestInitializationFilter :: checkRequestParams] : Exception - full stack trace follows:",e)
        }
        return false
    }

    protected void parseParams(HttpServletRequest request, String formData, String uriData, String id){
        LinkedHashMap<String,String> get = parseGetParams(uriData, id)
        request.getSession().setAttribute('GET',get)
        LinkedHashMap<String,String> post = parsePutParams(formData)

        // set batchVars if they are present
        if(post['batchVars']){
            if(!request.getSession().getAttribute('batchVars')) { request.getSession().setAttribute('batchVars', post['batchVars']) }
            post.remove('batchVars')
        }

        if(post['chainOrder']){
            if(!request.getSession().getAttribute('chainOrder')) { request.getSession().setAttribute('chainOrder', post['chainOrder']) }
            post.remove('chainOrder')
        }

        if(post['chainType']){
            if(!request.getSession().getAttribute('chainType')) { request.getSession().setAttribute('chainType', post['chainType']) }
            post.remove('chainType')
        }

        if(post['chainKey']){
            if(!request.getSession().getAttribute('chainKey')) { request.getSession().setAttribute('chainKey', post['chainKey']) }
            post.remove('chainKey')
        }

        if(post['chainSize']){
            if(!request.getSession().getAttribute('chainSize')) { request.getSession().setAttribute('chainSize', post['chainSize']) }
            post.remove('chainSize')
        }

        if(post['chainParams']){
            if(!request.getSession().getAttribute('chainParams')) { request.getSession().setAttribute('chainParams', post['chainParams']) }
            post.remove('chainParams')
        }

        request.getSession().setAttribute('POST',post)

        LinkedHashMap<String,String> output = get + post
        request.getSession().setAttribute('params',output)
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
        if(Objects.nonNull(id)){ output['id'] = id.toString() }
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
                        object = XML.toJSONObject(formData)
                        break
                }
            } catch (Exception e) {
                throw new Exception("[RequestInitializationFilter :: parsePutParams] : Badly formatted '${this.requestFileType}'. Please check the syntax and try again")
            }

            if(object) {
                Iterator<String> keys = object.keys();
                Set<String> keyset = object.keySet()

                switch(keyset){
                    case {it.contains('chain')}:
                        LinkedHashMap temp = [:]
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.toString() == 'chain') {
                                LinkedHashMap chainOrder = [:]
                                def temp2 = object.get('chain').remove('order').entrySet()
                                temp2.each() { it ->
                                    chainOrder[it.getKey()] = it.getValue().toString()
                                }

                                output['chainOrder'] = chainOrder
                                output['chainType'] = object.get('chain')['chaintype']
                                output['chainKey'] = object.get('chain')['initdata']
                                output['chainSize'] = chainOrder.size()
                            }else{
                                temp[key] = object.get(key).toString()
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
                                output[key] = object.get(key).toString()
                            }
                        }
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
        //response.writer.flush()
    }
}
