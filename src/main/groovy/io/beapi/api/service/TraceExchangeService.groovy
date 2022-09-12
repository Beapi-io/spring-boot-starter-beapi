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


import io.beapi.api.service.ApiExchange
import io.beapi.api.utils.ErrorCodes
import io.beapi.api.service.TraceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.json.*
import org.springframework.security.web.header.*
import groovyx.gpars.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class TraceExchangeService extends ApiExchange{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TraceExchangeService.class);
	private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']
	String cacheHash
	ApiCacheService apiCacheService
	TraceService traceService
	PrincipleService principle
	int cores
	boolean overrideAutoMimeTypes = false
	String sessionId

	public TraceExchangeService(ApiCacheService apiCacheService, TraceService traceService) {
		try {
			this.apiCacheService = apiCacheService
			this.traceService = traceService
		} catch (Exception e) {
			println("# [Beapi] IoStateService - initialization Exception - ${e}")
			System.exit(0)
		}
	}


    // [REQUEST]
    boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority){
		String sessionId = request.getSession().getId()

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

        if(method=='GET') {
            apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, output)
        }

        PrintWriter writer = response.getWriter();
        writer.write(output);
        writer.close()
        response.writer.flush()
    }


	void initVars(HttpServletRequest request, HttpServletResponse response, String authority) {
		String accept = request.getHeader('Accept')
		String contentType = request.getContentType()
		this.cores = request.getAttribute('cores')
		this.responseFileType = request.getAttribute('responseFileType')
		this.uList = request.getAttribute('uriList')
		this.callType = uList[0]
		this.version = uList[1]
		this.appversion = uList[2]
		this.apiversion = uList[3]
		this.controller = uList[4]
		request.getSession().setAttribute('controller',this.controller)
		this.action = uList[5]
		request.getSession().setAttribute('action',this.action)
		this.trace = uList[6]
		this.id = uList[7]
		this.method = request.getMethod()

		this.authority = authority
		this.cache = apiCacheService.getApiCache(this.controller)
		this.method = request.getMethod()
		this.uri = request.getRequestURI()
		this.receivesList = request.getSession().getAttribute('receivesList')
		this.returnsList = request.getSession().getAttribute('returnsList')

		// TODO : set 'max'
		// TODO : set 'offset'

		try {
			//this.appVersion = request.getSession().getAttribute('version')
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List
			this.apiObject = temp[this.action]
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
