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
package io.beapi.api.controller

import io.beapi.api.domain.User
import io.beapi.api.service.PrincipleService
import io.beapi.api.utils.ErrorCodes
import io.beapi.api.utils.UriObject

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException
import org.springframework.beans.factory.annotation.Autowired
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.Entity
import org.springframework.web.HttpRequestHandler

import io.beapi.api.service.TraceService
import io.beapi.api.service.PrincipleService
import java.lang.reflect.Method
import io.beapi.api.service.ApiCacheService
import org.springframework.context.ApplicationContext
import io.beapi.api.properties.ApiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.context.support.WebApplicationContextUtils
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory

import java.util.concurrent.CompletableFuture


@EnableConfigurationProperties([ApiProperties.class])
class BeapiRequestHandler implements HttpRequestHandler {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BeapiRequestHandler.class)
    protected String markerText = "DEVNOTES"
    protected Marker devnotes = MarkerFactory.getMarker(markerText)

    @Autowired
    protected TraceService traceService

    @Autowired
    protected ApiProperties apiProperties

    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml','multipart/form-data']
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']

    /*
    * v : 'regular api call'
    * b : batch
    * c : api chain
    * t : trace
     */
    private static final ArrayList CALL_TYPES = ['v','b','c','t']

    private UriObject uObj
    protected boolean trace
    public String controller
    public String action
    public String apiversion
    protected String authority
    protected Set keyList = []
    protected LinkedHashMap<String,String> params = [:]



    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
        //logger.info("handleRequest(HttpServletRequest, HttpServletResponse) : {}")
        //println("### BeapiRequestHandler...")

        //ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext())

        this.authority = request.getAttribute('principle')
        this.uObj = request.getAttribute('uriObj')
        this.apiversion = this.uObj.getApiVersion()

        // NOTE : CONTROLLER and ACTION can be reset in batchexchange/chainexchange so DONT USE URIObject!!!!
        this.controller = request.getAttribute('controller')
        this.action = request.getAttribute('action')

        trace = this.uObj.isTrace()
        this.params = request.getAttribute('params') as LinkedHashMap
        this.keyList = request.getAttribute('keyList')

        Object output

        // TRACESERVICE CHECK
        if (trace == true) { traceService.startTrace(controller, action, request.getSession().getId()); }

        // create method call
        Class<?> classObj = this.getClass()
        try {
            Method method = classObj.getMethod(action, HttpServletRequest.class, HttpServletResponse.class)

            // invoke method
            if (Objects.nonNull(method)) {
                try {
                    output = method.invoke(this, request, response)
                } catch (IllegalArgumentException e) {
                    logger.warn(devnotes,"[ BAD URI ] : YOU ARE ATTEMPTING TO CALL AN ENDPOINT THAT DOES NOT EXIST. IF THIS IS AN ISSUE, CHECK THAT THE CONTROLLER/METHOD EXISTS AND THAT IT IS PROPERLY REPRESENTED IN THE IOSTATE FILE.")
                    writeErrorResponse(response, '422', request.getRequestURI())
                    throw new Exception("[BeapiController > handleRequest] : IllegalArgumentException - full stack trace follows :", e)
                } catch (IllegalAccessException e) {
                    logger.warn(devnotes, "[ BAD URI ] : YOU ARE ATTEMPTING TO CALL AN ENDPOINT THAT DOES NOT EXIST. IF THIS IS AN ISSUE, CHECK THAT THE CONTROLLER/METHOD EXISTS AND THAT IT IS PROPERLY REPRESENTED IN THE IOSTATE FILE.")
                    writeErrorResponse(response, '422', request.getRequestURI())
                    throw new Exception("[BeapiController > handleRequest] : IllegalAccessException - full stack trace follows :", e)
                }catch (java.lang.reflect.InvocationTargetException e){
                    // ignore
                };
            };

            if (output != null) {
                ArrayList result = []
                if (trace == true) {
                    Object trace = traceService.endAndReturnTrace(controller, action, request.getSession().getId())
                    result = convertModel(trace)
                } else {
                    ArrayList tempResult = convertModel(output)

                    // todo : tempResult is good; problem lies with parseResponseParams
                    Set responseList = request.getAttribute('responseList')

                    // todo : fix bug HERE!!!!!!
                    if(!responseList.contains("*")){
                        def tmp = (tempResult.isEmpty())?tempResult : parseResponseParams(tempResult, responseList)
                        if(Objects.nonNull(tmp)){
                            result = tmp
                        }else{
                            writeErrorResponse(response, '422', request.getRequestURI(),"Expected Output does not match IOState RESPONSE params. Please conact the administrator.")
                        };
                    }else{
                        result = tempResult
                    };
                };
                request.setAttribute('responseBody', result)
            } else {
                logger.warn(devnotes,"[ NO OUTPUT ] : OUTPUT EXPECTED AND NONE RETURNED. IF THIS IS AN ISSUE, CHECK THAT THE CONTROLLER/METHOD IS RETURNING THE PARAMS AS REPRESENTED IN THE APPROPRIATE IOSTATE FILE UNDER FOR THIS/CONTROLLER/METHOD (UNDER 'RESPONSE') AS A LINKEDHASHMAP.")
                writeErrorResponse(response, '404', request.getRequestURI())
            };
        } catch (SecurityException e) {
            // bad privileges for endpoint; shouldn't hit this
            //writeErrorResponse(response,'422',request.getRequestURI())
            throw new Exception("[BeapiController > handleRequest] : SecurityException - full stack trace follows :", e)
        } catch (NoSuchMethodException e) {
            // cannot find endpoint
            //writeErrorResponse(response,'422',request.getRequestURI())
            logger.warn(devnotes,"[ BAD URI ] : YOU ARE ATTEMPTING TO CALL AN ENDPOINT THAT DOES NOT EXIST. IF THIS IS AN ISSUE, CHECK THAT THE CONTROLLER/METHOD EXISTS AND THAT IT IS PROPERLY REPRESENTED IN THE IOSTATE FILE.")
            throw new Exception("[BeapiController > handleRequest] : NoSuchMethodException - full stack trace follows :", e)
        };
    };

    protected ArrayList convertModel(Object obj) throws Exception{
        try{
            ArrayList output = []
            if(obj){
                switch(obj){
                    case {it.getClass().getAnnotation(Entity.class) != null}:
                        output.add(formatEntity(obj))
                        return output
                        break;
                    case {it instanceof LinkedHashMap}:
                    case {it instanceof HashMap}:
                    case {it instanceof Map}:
                        output.add(formatMap(obj))
                        return output
                        break;
                    case {it instanceof ArrayList}:
                    case {it instanceof Set}:
                    case {it instanceof LinkedList}:
                        obj.each() { list ->
                            switch(list){
                                case {it.getClass().getAnnotation(Entity.class) != null}:
                                    output.add(formatEntity(list))
                                    break;
                                case {it instanceof LinkedHashMap}:
                                case {it instanceof HashMap}:
                                case {it instanceof Map}:
                                    def tmp = formatMap(list)
                                    output.add(tmp)
                                    break;
                                default:
                                    logger.warn(devnotes,"[ BAD DATASET ] : YOU ARE ATTEMPTING TO CONVERT A BAD DATATYPE; ONLY [ENTITY,MAP,LINKEDHASHMAP,HASHMAP,LINKEDLIST,ARRAYLIST,SET] ARE SUPPORTED.")
                                    // todo : throw error; response values MUST have at least ONE KEY to be checked against IO State / constructors
                                    throw new Exception("[ControllerUtil > convertModel] : List/Set for '${controller}/${action}'must contain MAP or DOMAIN OBJECT")
                                    break;
                            };
                        };
                        return output
                        break;
                    default:
                        logger.warn(devnotes,"[ BAD DATASET ] : YOU ARE ATTEMPTING TO CONVERT A BAD DATATYPE; ONLY [ENTITY,MAP,LINKEDHASHMAP,HASHMAP,LINKEDLIST,ARRAYLIST,SET] ARE SUPPORTED.")
                        // todo : throw error ; unsupported return type
                        throw new Exception("[ControllerUtil > convertModel] : Unsupported return type; Please file a support ticket to have this return type added.")
                };
            };
            return output
        }catch(Exception e){
            logger.warn(devnotes,"[ BAD DATASET ] : YOU ARE ATTEMPTING TO CONVERT A BAD DATATYPE; ONLY [ENTITY,MAP,LINKEDHASHMAP,HASHMAP,LINKEDLIST,ARRAYLIST,SET] ARE SUPPORTED.")
            throw new Exception("[BeapiController > convertModel] : Exception - full stack trace follows :",e)
        };
    };

    /**
     * Given an Object detected as a Entity, processes in a standardized format and returns a LinkedHashMap;
     * Used by convertModel, formatList, formatMap
     * @see #convertModel(Object)
     * @param Object data
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    protected LinkedHashMap formatEntity(Object obj) throws Exception{
        ObjectMapper omapper = new ObjectMapper()
        LinkedHashMap<String,Object> map = [:]
        try{
            map = omapper.convertValue(obj,Map.class)
        }catch(Exception e){
            logger.warn(devnotes,"[ BAD ENTITY ] : YOU ARE ATTEMPTING TO CONVERT A BAD ENTITY. TO FIX THIS, MAKE SURE YOUR ENTITY CAN BE SEEN ON THE CLASSPATH. ")
            throw new Exception("[BeapiController > formatEntity] : Exception formatting Response Entity - full stack trace follows :",e)
        };
        return map
    };

    /**
     * Given a LinkedHashMap detected as a Map, processes in a standardized format and returns a LinkedHashMap;
     * Used by convertModel and called by the PostHandler
     * @see #convertModel(Map)
     * @param LinkedHashMap map
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    protected LinkedHashMap formatMap(Map map){
        LinkedHashMap newMap = [:];
        try{
            if(map) {
                map.each() { key, val ->
                    if(val) {
                        switch (val) {
                            case (java.lang.Class.isInstance(val.class)):
                                newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || val in java.util.Map) ? val : val.toString()
                                break;
                            case (val.getClass().getAnnotation(Entity.class) != null):
                                newMap[key] = formatEntity(val)
                                break;
                            default:
                                newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || (val in java.util.Map || val in java.util.Map || val in java.util.LinkedHashMap)) ? val : val.toString()
                                break;
                        };
                    }
                };
            };
        }catch(Exception e){
            logger.warn(devnotes,"[ BAD DATASET ] : YOU ARE ATTEMPTING TO CONVERT A BAD DATATYPE; ONLY [ENTITY,MAP,LINKEDHASHMAP,HASHMAP,LINKEDLIST,ARRAYLIST,SET] ARE SUPPORTED.")
            throw new Exception("[BeapiRequestHandlerUtil > convertModel] : Exception formatting Response Map - full stack trace follows :",e)
        };
        return newMap;
    }

    /*
    * checks multiple return sets(bodyList) against expected returns keyset(responseList)
    */
    private ArrayList parseResponseParams(ArrayList bodyList, Set responseList) throws Exception{
        ArrayList output = []
        try {
            bodyList.each() { body ->
                ArrayList paramsList = (body.size() == 0) ? [] : body.keySet() as ArrayList
                    paramsList.each() { it2 ->
                        if (!responseList.contains(it2)) {
                            body.remove(it2.toString())
                        };
                    };

                    // println("responseList : "+responseList)
                    // println("responseKeys : "+body.keySet())

                    if (responseList.size()==body.keySet().size()) {
                        output.add(body)
                    };
            };
        }catch(Exception e){
            logger.warn(devnotes,"[ BAD PARAMS ] : UNKNOWN EXCEPTION. PLEASE FILE A TICKET. ")
            throw new Exception("[ApiExchange :: parseResponseParams] : Exception - full stack trace follows:",e)
        };

        return output
    };

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
        response.writer.flush()
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
    };

}

