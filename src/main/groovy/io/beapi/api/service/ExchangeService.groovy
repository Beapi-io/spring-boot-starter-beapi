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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.json.*
import org.springframework.security.web.header.*
import groovyx.gpars.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// NOTE : CALLTYPE = 1
@Service
public class ExchangeService extends ApiExchange{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExchangeService.class);
	private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']

	ApiCacheService apiCacheService
	//PrincipleService principle

	boolean overrideAutoMimeTypes = false

	public ExchangeService(ApiCacheService apiCacheService) {
		try {
			this.apiCacheService = apiCacheService
		} catch (Exception e) {
			println("# [Beapi] IoStateService - initialization Exception - ${e}")
			System.exit(0)
		}
	}

    // [REQUEST]
    boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority){
		def post = request.getAttribute('POST')
		def get = request.getAttribute('GET')

		LinkedHashMap<String,String> output = get + post
		request.setAttribute('params',output)

        initVars(request,response,authority)

        //parseParams(request, IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8), request.getQueryString(),uList[7])
        // routing call to controller



		if(this.apiObject) {
			// todo : create public api list
			if(this.method == 'GET') {

				setCacheHash(request.getAttribute('params'), this.receivesList)

				// RETRIEVE CACHED RESULT (only if using 'GET' method)
				if((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${cacheHash}")) {
					String cachedResult
					try {
						cachedResult = (apiObject['cachedResult'][authority][responseFileType][cacheHash])?apiObject['cachedResult'][authority][responseFileType][cacheHash]:apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]
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
						return false
					}
				}
			}
		}


		if(!validateMethod()){
			writeErrorResponse(response,'405',request.getRequestURI());
			return false
		}else{
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
        //response.writer.flush()
    }

	void initVars(HttpServletRequest request, HttpServletResponse response, String authority) {
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
		//this.cache = apiCacheService.getApiCache(this.controller)

		this.method = request.getMethod()
		this.uri = request.getRequestURI()
		//this.receivesList = request.getAttribute('receivesList')
		//this.returnsList = request.getAttribute('returnsList')

		// TODO : set 'max'
		// TODO : set 'offset'


		try {
			//def temp = cache[this.apiversion]
			//this.defaultAction = request.getAttribute('defaultAction')
			//this.deprecated = request.getAttribute('deprecated')
			//this.apiObject = request.getAttribute('apiObject')
			this.apiObject = apiCacheService.getApiDescriptor(this.controller, this.apiversion, this.action)
			this.receivesList = (this.apiObject.receivesList[this.authority]) ? this.apiObject.receivesList[this.authority] : this.apiObject.receivesList['permitAll']
			this.returnsList = (this.apiObject.returnsList[this.authority]) ? this.apiObject.returnsList[this.authority] : this.apiObject.returnsList['permitAll']
			if(!request.getAttribute('responseList')){ request.setAttribute('responseList',this.returnsList) }

			//this.rturns = this.apiObject['returns'] as LinkedHashMap
			//this.returnsAuths = this.rturns.keySet()
			//this.networkGrp = this.apiObject['networkGrp']
			this.method = request.getMethod()
		} catch (Exception e) {
			throw new Exception("[ExchangeService :: init] : Exception. full stack trace follows:", e)
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
