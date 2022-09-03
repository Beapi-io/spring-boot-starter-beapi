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

import org.json.JSONObject
import io.beapi.api.utils.ApiDescriptor
import javax.servlet.forward.*
import groovyx.gpars.*


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

    private static final ArrayList formats = ['XML','JSON']
    private static final ArrayList SUPPORTED_MIME_TYPES = ['text/json','application/json','text/xml','application/xml']

    int callType
    protected String method
    protected String defaultAction
    protected String appversion
    protected String apiversion
    public int cores
    String requestFormat
    protected String requestMimeType
    protected String requestFileType
    protected String responseMimeType
    protected String responseFileType
    protected LinkedHashMap receives = [:]
    protected ArrayList receivesAuths = []
    protected ArrayList receivesList = []
    protected LinkedHashMap rturns = [:]
    protected ArrayList returnsAuths = []
    protected ArrayList returnsList = []
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
    ArrayList uList
    String uri
    String version
    String controller
    String action
    String handler
    String auth
    String cacheHash
    String responseFileType
    String method
    boolean trace
    public String id
    int cores


    boolean validateMethod(){
        boolean result = false
        if(this.apiObject['method'].toUpperCase() == this.method){
            result = true
        }
        return result
    }

    String parseOutput(ArrayList responseBody, String responseFileType){
        String output = ''
        if(responseBody.size()<2) {
            output = parseBodyByFiletype(responseBody[0], responseFileType);
        }else{
            int inc = 0
            output = "["
            responseBody.each() { it2 ->
                if (inc > 0) { output += ',' }
                output += parseBodyByFiletype(it2, responseFileType)
                inc += 1
            }
            output += "]"
        }
        return output
    }

    protected String parseBodyByFiletype(LinkedHashMap responseBody, String responseFileType){
        String test
        switch(responseFileType){
            case 'JSON':
                test = new JSONObject(responseBody).toString()
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
    boolean checkRequestParams(LinkedHashMap methodParams){
        ArrayList checkList = this.receivesList
        ArrayList paramsList
        ArrayList reservedNames = ['batchLength','batchInc','chainInc','apiChain','batch','_','max','offset','chaintype']

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


    protected ArrayList getReturnsList(LinkedHashMap rturns){
        ArrayList result = []
        rturns.each() { k, v ->
            if(k==this.authority || k=='permitAll') {
                v.each() { it2 ->
                    if (!result.contains(it2['name'])) {
                        result.add(it2['name']) }
                }
            }
        }
        return result
    }

    protected ArrayList getReceivesList(LinkedHashMap receives){
        ArrayList result = []
        receives.each() { k, v ->
            if(k==this.authority || k=='permitAll') {
                v.each { it2 ->
                    if (!result.contains(it2['name'])) {
                        result.add(it2['name'])
                    }
                }
            }
        }
        return result
    }


}