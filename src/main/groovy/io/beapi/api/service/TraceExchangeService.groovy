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
package io.beapi.api.service


import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.json.*
import org.springframework.security.web.header.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class TraceExchangeService extends ApiExchange{

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TraceExchangeService.class);
	private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
	String cacheHash
	ApiCacheService apiCacheService
	TraceService traceService
	PrincipleService principle

	boolean overrideAutoMimeTypes = false
	String sessionId

	public TraceExchangeService(ApiCacheService apiCacheService, TraceService traceService) {
		try {
			this.apiCacheService = apiCacheService
			this.traceService = traceService
		} catch (Exception e) {
			println("# [Beapi] TraceExchangeService - initialization Exception - ${e}")
			System.exit(0)
		}
	}


    // [REQUEST]
    boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority){
		String sessionId = request.getSession().getId()

		def post = request.getAttribute('POST')
		def get = request.getAttribute('GET')

		LinkedHashMap<String,String> output = get + post
		request.setAttribute('params',output)

		traceService.startTrace('ApiInterceptor','initVars',sessionId)
        initVars(request,response,authority)
		traceService.endTrace('ApiInterceptor','initVars',sessionId)

		if(!validateMethod()){
			writeErrorResponse(response,'405',request.getRequestURI());
			return false
		}else{
			//parseParams(request, IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), request.getQueryString(),uList[7])
			// routing call to controller
			return true
		}

    }

    void apiResponse(HttpServletResponse response,ArrayList body){
        String output = parseOutput(body, responseFileType)

        //if(method=='GET') {
        //    apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, output)
        //}

        PrintWriter writer = response.getWriter();
        writer.write(output);
        writer.close()
        response.writer.flush()
    }


	private void initVars(HttpServletRequest request, HttpServletResponse response, String authority) {
		String accept = request.getHeader('Accept')
		String contentType = request.getContentType()

		this.responseFileType = request.getAttribute('responseFileType')
		this.uList = request.getAttribute('uriList')
		this.callType = uList[0]
		this.version = uList[1]
		this.appversion = uList[2]
		this.apiversion = uList[3]
		this.controller = uList[4]
		request.setAttribute('controller',this.controller)
		this.action = uList[5]
		request.setAttribute('action',this.action)
		this.trace = uList[6]
		this.id = uList[7]
		this.method = request.getMethod()

		this.authority = authority
		this.cache = apiCacheService.getApiCache(this.controller)
		this.method = request.getMethod()
		this.uri = request.getRequestURI()
		this.receivesList = request.getAttribute('receivesList')
		this.returnsList = request.getAttribute('returnsList')

		// TODO : set 'max'
		// TODO : set 'offset'

		try {
			//this.appVersion = request.getAttribute('version')
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List
			this.apiObject = temp[this.action]

			this.keyList = this.apiObject?.getKeyList()

			this.receives = this.apiObject.getReceives()
			//this.receivesAuths = this.receives.keySet()
			this.rturns = this.apiObject['returns'] as LinkedHashMap
			this.returnsAuths = this.rturns.keySet()
			//this.networkGrp = this.apiObject['networkGrp']
			this.method = request.getMethod()
			//LinkedHashMap tempNetworkRoles = networkGrpRoles[this.networkGrp].each(){ it-> it.getValue() }
			//this.networkRoles = tempNetworkRoles.collect{entry -> entry.value}
		} catch (Exception e) {
			throw new Exception("[ExchangeObject :: init] : Exception. full stack trace follows:", e)
		}
	}

}
