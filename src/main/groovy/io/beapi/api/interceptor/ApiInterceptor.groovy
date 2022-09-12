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

/*
* mapping needs to include 4 'callTypes' for load balancing:
* v : regular api call
* b : batching call
* c : chain call
* r : resource call
*
* This allows us the ability to move different call to different servers (should we want/need)
* so they do not affect 'regular calls' (ie 'v' callType)
 */
package io.beapi.api.interceptor

import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.TraceExchangeService
import io.beapi.api.service.PrincipleService
import io.beapi.api.service.TraceService
import io.beapi.api.utils.ErrorCodes

import io.beapi.api.utils.UriObject
import org.slf4j.LoggerFactory;
import groovyx.gpars.*

import io.beapi.api.properties.ApiProperties

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import javax.servlet.DispatcherType
import javax.json.*
import org.springframework.security.web.header.*
import groovyx.gpars.*
import javax.servlet.RequestDispatcher
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils

/**
 *
 * HandlerInterceptor for all API Calls. Routes call to appropriate ExchangeService methods for handling based on calltype.
 *
 * NOTE: calltype is checked in RequestInitializationFilter for compliance prior to parsing into UriObject
 *
 * @author Owen Rubel
 *
 */


@EnableConfigurationProperties([ApiProperties.class])
//@ConditionalOnBean(name = ["principle","apiCacheService"])
class ApiInterceptor implements HandlerInterceptor{
	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);

	// TODO : inject stats service into interceptor and then into here


	ApiCacheService apiCacheService
	PrincipleService principle
	private ApiProperties apiProperties
	ExchangeService exchangeService
	BatchExchangeService batchService
	ChainExchangeService chainService
	TraceExchangeService traceExchangeService
	//int cores
	//LinkedHashMap networkGrpRoles
	LinkedHashMap cache
	UriObject uObj
	ArrayList uList
	String authority
	ArrayList privateRoles = []
	int callType


	public ApiInterceptor(ExchangeService exchangeService, BatchExchangeService batchService, ChainExchangeService chainService, TraceExchangeService traceService,PrincipleService principle, ApiProperties apiProperties) {
		this.principle = principle
		this.exchangeService = exchangeService
		this.batchService = batchService
		this.chainService = chainService
		this.traceExchangeService = traceExchangeService
		this.apiProperties = apiProperties
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//if (request.getDispatcherType() != DispatcherType.REQUEST) {
		//	return true;
		//}
		//logger.info("preHandle(HttpServletRequest, HttpServletResponse, Object) : {}");

		privateRoles = apiProperties.security.networkRoles['private'].collect() { k, v -> v }
		this.uList = request.getAttribute('uriList')

		this.callType = uList[0]
		this.authority = principle.authorities()

		switch(callType){
			case 1:
				return exchangeService.apiRequest(request, response, this.authority)
				break
			case 2:
				if(apiProperties.batchingEnabled) {
					return batchService.batchRequest(request, response, this.authority)
				}else{
					writeErrorResponse(response,'401',request.getRequestURI())
					response.writer.flush()
					return false
				}
				break
			case 3:
				if(apiProperties.chainingEnabled) {
					return chainService.chainRequest(request, response, this.authority)
				}else{
					writeErrorResponse(response,'401',request.getRequestURI())
					response.writer.flush()
					return false
				}
				break
			//case 4:
			//	if(apiProperties.resourcesEnabled) {
			//		return exchangeService.resourceRequest(request, response)
			//	}else{
			//		writeErrorResponse(response,'401',request.getRequestURI())
			//	}
			//	break
			case 5:
				if(privateRoles.contains(authority)) {
					return traceExchangeService.apiRequest(request, response, this.authority)
				}
				break
			default:
				writeErrorResponse(response,'400',request.getRequestURI())
				response.writer.flush()
				return false
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
		//logger.info("postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView) : {}")
		ArrayList body = request.getSession().getAttribute('responseBody')


		if(!body){
			writeErrorResponse(response,'422',request.getRequestURI(),'No data returned for this call.')
		}else {
			switch (callType){
				case 1:
					exchangeService.apiResponse(response,body)
					response.writer.flush()
					break
			case 2:
				if(apiProperties.batchingEnabled) {
					batchService.batchResponse(request, response, body)
				}else{
					writeErrorResponse(response,'401',request.getRequestURI())
					response.writer.flush()
				}
				break
			case 3:
				if(apiProperties.chainingEnabled) {
					chainService.chainResponse(request, response, body)
				}else{
					writeErrorResponse(response,'401',request.getRequestURI())
					response.writer.flush()
				}
				break
			//case 4:
			//	if(apiProperties.resourcesEnabled) {
			//		exchangeService.resourceResponse(body, response)
			//	}else{
			//		writeErrorResponse(response,'401',request.getRequestURI())
			//	}
			//	break
				case 5:
					traceExchangeService.apiResponse(response,body)
					break
				default:
					writeErrorResponse(response, '400', request.getRequestURI())
					response.writer.flush()
			}
		}
		response.writer.flush()
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
		//response.writer.flush()
	}
}