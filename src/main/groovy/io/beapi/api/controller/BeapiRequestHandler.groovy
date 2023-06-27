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


import io.beapi.api.utils.ErrorCodes
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired
import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.Entity
import org.springframework.web.HttpRequestHandler
import org.slf4j.LoggerFactory;
import io.beapi.api.service.TraceService
import java.lang.reflect.Method;
import io.beapi.api.service.ApiCacheService
import org.springframework.context.ApplicationContext
import io.beapi.api.properties.ApiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.context.support.WebApplicationContextUtils

@EnableConfigurationProperties([ApiProperties.class])
class BeapiRequestHandler implements HttpRequestHandler {
    //private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BeapiController.class);

    @Autowired
    TraceService traceService

    //@Autowired
    //ApiProperties apiProperties

    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml','multipart/form-data']
    private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']

    /*
    * v : 'regular api call'
    * b : batch
    * c : api chain
    * t : trace
    * h : webhook CRUD
     */
    private static final ArrayList CALL_TYPES = ['v','b','c','t','h']

    public ArrayList uList
    protected boolean trace
    public String controller
    public String action
    public String apiversion
    public String authority
    protected Set keyList = []
    public LinkedHashMap<String,String> params = [:]



    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //logger.info("handleRequest(HttpServletRequest, HttpServletResponse) : {}");
        //println("### BeapiRequestHandler...")
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        this.authority = request.getAttribute('principle')
        this.uList = request.getAttribute('uriList')
        this.apiversion = uList[3]

        // NOTE : CONTROLLER and ACTION can be reset in batchexchange/chainexchange so DONT USE URILIST!!!!
        this.controller = request.getAttribute('controller')
        this.action = request.getAttribute('action')

        trace = uList[6]
        this.params = request.getAttribute('params') as LinkedHashMap
        this.keyList = request.getAttribute('keyList')

        Object output


        // TRACESERVICE CHECK
        if (trace == true) { traceService.startTrace(controller, action, request.getSession().getId()) }

        // create method call
        Class<?> classObj = this.getClass();
        try {
            Method method = classObj.getMethod(action, HttpServletRequest.class, HttpServletResponse.class);

            // invoke method
            if (Objects.nonNull(method)) {
                try {
                    output = method.invoke(this, request, response)
                } catch (IllegalArgumentException e) {
                    //writeErrorResponse(response, '422', request.getRequestURI());
                    throw Exception("[BeapiController > handleRequest] : IllegalArgumentException - full stack trace follows :", e);
                } catch (IllegalAccessException e) {
                    //writeErrorResponse(response, '422', request.getRequestURI());
                    throw Exception("[BeapiController > handleRequest] : IllegalAccessException - full stack trace follows :", e);
                }
            }

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
                            println("### object not null : "+tmp)
                            result = tmp
                        }else{
                            println("### object VERY VERY null")
                            writeErrorResponse(response, '422', request.getRequestURI(),"Expected Output does not match IOState RESPONSE params. Please conact the administrator.")
                        }

                    }else{
                        result = tempResult
                    }
                }
                request.setAttribute('responseBody', result)
            } else {

                writeErrorResponse(response, '404', request.getRequestURI())
            }
        } catch (SecurityException e) {
            // bad privileges for endpoint; shouldn't hit this
            //writeErrorResponse(response,'422',request.getRequestURI());
            throw Exception("[BeapiController > handleRequest] : SecurityException - full stack trace follows :", e);
        } catch (NoSuchMethodException e) {
            // cannot find endpoint
            //writeErrorResponse(response,'422',request.getRequestURI());
            throw Exception("[BeapiController > handleRequest] : NoSuchMethodException - full stack trace follows :", e);
        }

    }

    ArrayList convertModel(Object obj){
        try{
            ArrayList output = []
            if(obj){
                switch(obj){
                    case {it.getClass().getAnnotation(Entity.class) != null}:
                        try {
                            output.add(formatEntity(obj))
                        }catch(Exception e){
                            throw new Exception("[ControllerUtil > convertModel] : Exception formatting Response Entity - full stack trace follows :",e)
                        }
                        return output
                        break;
                    case {it instanceof Map}:
                    case {it instanceof LinkedHashMap}:
                    case {it instanceof HashMap}:
                        try{
                            output.add(formatMap(obj))
                        }catch(Exception e){
                            throw new Exception("[ControllerUtil > convertModel] : Exception formatting Response Map - full stack trace follows :",e)
                        }
                        return output
                        break;
                    case {it instanceof LinkedList}:
                    case {it instanceof ArrayList}:
                    case {it instanceof Set}:
                        obj.each() { list ->
                            switch(list){
                                case {it.getClass().getAnnotation(Entity.class) != null}:
                                    try {
                                        output.add(formatEntity(list))
                                    }catch(Exception e){
                                        throw new Exception("[ControllerUtil > convertModel] : Exception formatting Response Entity - full stack trace follows :",e)
                                    }
                                    break;
                                case {it instanceof Map}:
                                case {it instanceof LinkedHashMap}:
                                case {it instanceof HashMap}:
                                    try{
                                        def tmp = formatMap(list)
                                        output.add(tmp)
                                    }catch(Exception e){
                                        //throw new Exception("[BeapiRequestHandler > convertModel] : Exception formatting Response Map - full stack trace follows :",e)
                                        println("[BeapiRequestHandler > convertModel] : Exception formatting Response Map - full stack trace follows :"+e)
                                    }
                                    break;
                                default:
                                    // todo : throw error; response values MUST have at least ONE KEY to be checked against IO State / constructors
                                    throw new Exception("[ControllerUtil > convertModel] : List/Set for '${controller}/${action}'must contain MAP or DOMAIN OBJECT")
                                    break;
                            }
                        }
                        return output
                        break;
                    default:
                        // todo : throw error ; unsupported return type
                        throw new Exception("[ControllerUtil > convertModel] : Unsupported return type; Please file a support ticket to have this return type added.")
                }
            }
            return output
        }catch(Exception e){
            throw new Exception("[BeapiController > convertModel] : Exception - full stack trace follows :",e)
        }
    }

    /**
     * Given an Object detected as a Entity, processes in a standardized format and returns a LinkedHashMap;
     * Used by convertModel, formatList, formatMap
     * @see #convertModel(Object)
     * @param Object data
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    LinkedHashMap formatEntity(Object obj){
        ObjectMapper omapper = new ObjectMapper()
        LinkedHashMap<String,Object> map = [:]
        try{
            map = omapper.convertValue(obj,Map.class)
        }catch(Exception e){
            throw new Exception("[BeapiController > formatEntity] : Exception formatting Response Entity - full stack trace follows :",e)
        }

        return map
    }

    /**
     * Given a LinkedHashMap detected as a Map, processes in a standardized format and returns a LinkedHashMap;
     * Used by convertModel and called by the PostHandler
     * @see #convertModel(Map)
     * @param LinkedHashMap map
     * @return LinkedHashMap commonly formatted linkedhashmap
     */
    LinkedHashMap formatMap(Map map){
        LinkedHashMap newMap = [:]
        if(map) {
            map.each() { key, val ->
                if (val) {
                    if (java.lang.Class.isInstance(val.class)) {
                        newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || val in java.util.Map) ? val : val.toString()
                    } else if (val.getClass().getAnnotation(Entity.class) != null) {
                        newMap[key] = formatEntity(val)
                    } else {
                        newMap[key] = ((val in java.util.ArrayList || val in java.util.List) || (val in java.util.Map || val in java.util.Map || val in java.util.LinkedHashMap)) ? val : val.toString()
                    }
                }
            }
        }

        return newMap
    }

    /*
    * checks multiple return sets(bodyList) against expected returns keyset(responseList)
    */
    ArrayList parseResponseParams(ArrayList bodyList, Set responseList){
        ArrayList output = []

        println("responseList : "+responseList)

        try {
            bodyList.each() { body ->
                ArrayList paramsList = (body.size() == 0) ? [:] : body.keySet() as ArrayList
                    paramsList.each() { it2 ->
                        if (!responseList.contains(it2)) {
                            body.remove(it2.toString())
                        }
                    }

                    println("responseList : "+responseList)
                    println("responseKeys : "+body.keySet())

                    if (responseList.size()==body.keySet().size()) {
                        output.add(body)
                    }
            }

        }catch(Exception e){
            throw new Exception("[ApiExchange :: parseResponseParams] : Exception - full stack trace follows:",e)
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
        if(msg.isEmpty()){
            msg = ErrorCodes.codes[statusCode]['long']
        }
        String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
        response.getWriter().write(message)
        response.writer.flush()
    }

}

