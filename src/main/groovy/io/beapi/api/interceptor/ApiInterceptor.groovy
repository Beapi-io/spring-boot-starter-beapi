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
* t : resource call
*
* This allows us the ability to move different call to different servers (should we want/need)
* so they do not affect 'regular calls' (ie 'v' callType)
 */
package io.beapi.api.interceptor

import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ErrorService
import io.beapi.api.service.ExchangeService

import java.lang.reflect.Field
import io.beapi.api.service.TraceExchangeService
//import io.beapi.api.service.HookExchangeService
import io.beapi.api.service.PrincipleService
import io.beapi.api.service.TraceService
import io.beapi.api.utils.ErrorCodes
import io.beapi.api.utils.UriObject
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.RequestContextUtils;

import io.beapi.api.properties.ApiProperties

import javax.crypto.KeyGenerator
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import jakarta.servlet.DispatcherType
import javax.json.*
import org.springframework.security.web.header.*
//import groovyx.gpars.*
import jakarta.servlet.RequestDispatcher
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import javax.crypto.KeyGenerator;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.scheduling.annotation.Async;

import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.ServletRequestAttributes

import io.beapi.api.utils.ErrorCodes
import org.springframework.web.servlet.support.RequestContextUtils;
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
//@ConditionalOnBean(name = ["principle"])
class ApiInterceptor implements HandlerInterceptor{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);
	String markerText = "DEVNOTES";
	Marker devnotes = MarkerFactory.getMarker(markerText);

	// TODO : inject stats service into interceptor and then into here

	private HttpServletRequest req;
	private HttpServletResponse resp;


	//ThrottleCacheService throttle
	PrincipleService principle
	private ApiProperties apiProperties
	ExchangeService exchangeService
	BatchExchangeService batchService
	ChainExchangeService chainService
	TraceExchangeService traceExchangeService
	//HookExchangeService hookExchangeService
	//int cores
	//LinkedHashMap networkGrpRoles
	LinkedHashMap cache
	UriObject uObj
	String authority
	ArrayList privateRoles = []
	int callType
	KeyGenerator keyGenerator

	ErrorService errorService

	public ApiInterceptor(ErrorService errorService, ExchangeService exchangeService, BatchExchangeService batchService, ChainExchangeService chainService, TraceExchangeService traceService, ApiProperties apiProperties) {
		//this.throttle = throttle
		this.exchangeService = exchangeService
		this.batchService = batchService
		this.chainService = chainService
		this.traceExchangeService = traceService
		this.apiProperties = apiProperties
		this.errorService = errorService
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//println("### ApiInterceptor / prehandle")

		RequestAttributes requestAttributes = RCH.getRequestAttributes();
		HttpServletRequest req = ((ServletRequestAttributes) requestAttributes).getRequest();
		HttpServletResponse resp = ((ServletRequestAttributes) requestAttributes).getResponse();

		if (handler instanceof ResourceHttpRequestHandler) {
			errorService.writeErrorResponse(request, response,'422',"No data returned for this call. This is an 'API Server'; Please limit your calls to API's only")
		}else {
			privateRoles = apiProperties.security.networkRoles['private'].collect() { k, v -> v }
			this.uObj = request.getAttribute('uriObj')

			if(this.uObj) {
				this.callType = this.uObj?.getCallType()
				this.authority = request.getAttribute('principle')

				switch (this.callType) {
					case 1:
						return exchangeService.apiRequest(request, response, this.authority)
						break
					case 2:
						if (apiProperties.batchingEnabled) {
							return batchService.apiRequest(request, response, this.authority)
						} else {
							try {
								int statusCode = 401
								Locale tmp = RequestContextUtils.getLocale(request);
								String lang = (tmp)?tmp.getLanguage():"en"
								response.setStatus(statusCode)
								String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.request.getRequestURI()}\"}"
								response.sendError(statusCode, message)
								//response.flushBuffer()
							}catch(Exception e){
								println(e)
							}
						}
						break
					case 3:
						if (apiProperties.chainingEnabled) {
							return chainService.apiRequest(request, response, this.authority)
						} else {
							try {
								int statusCode = 401
								Locale tmp = RequestContextUtils.getLocale(request);
								String lang = (tmp)?tmp.getLanguage():"en"
								response.setStatus(statusCode)
								String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.getRequestURI()}\"}"
								response.sendError(statusCode, message)
								//response.flushBuffer()
							}catch(Exception e){
								println(e)
							}
						}
						break
					case 4:
						if (privateRoles.contains(authority)) {
							return traceExchangeService.apiRequest(request, response, this.authority)
						}else{
							try {
								int statusCode = 401
								Locale tmp = RequestContextUtils.getLocale(request);
								String lang = (tmp)?tmp.getLanguage():"en"
								response.setStatus(statusCode)
								String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.getRequestURI()}\"}"
								response.sendError(statusCode, message)
								//response.flushBuffer()
							}catch(Exception e){
								println(e)
							}
						}
						break
				//case 5:
				//	return hookExchangeService.apiRequest(request, response, this.authority)
				//	break
					default:
						errorService.writeErrorResponse(request, response, '400')
						return false
				}
			}else{
				errorService.writeErrorResponse(request, response, '400')
				return false
			}
		}
	}

	@Override
	//@Async
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
		//logger.info("postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView) : {}")
		//println("### ApiInterceptor / posthandle")

		ArrayList body = []
		if(request.getAttribute('responseBody')){
			body = request.getAttribute('responseBody')
		}

		//String stat = (String)response.getStatus()
		//String uri = (String)request.getRequestURI()

		if(body == null){
			errorService.writeErrorResponse(request, response,'204','No data returned for this call.')
		}else {
			switch (callType){
				case 1:
					exchangeService.apiResponse(request,response,body)
					break
				case 2:
					if(apiProperties.batchingEnabled) {
						batchService.batchResponse(request, response, body)
					}else{
						int statusCode = 401
						Locale tmp = RequestContextUtils.getLocale(request);
						String lang = (tmp)?tmp.getLanguage():"en"
						response.setStatus(statusCode)
						String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.getRequestURI()}\"}"
						response.sendError(statusCode, message)
					}
					break
				case 3:
					if(apiProperties.chainingEnabled) {
						chainService.chainResponse(request, response, body)
					}else{
						int statusCode = 401
						Locale tmp = RequestContextUtils.getLocale(request);
						String lang = (tmp)?tmp.getLanguage():"en"
						response.setStatus(statusCode)
						String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.getRequestURI()}\"}"
						response.sendError(statusCode, message)
					}
					break
				case 4:
					traceExchangeService.apiResponse(response,body)
					break
				//case 5:
				//	hookExchangeService.apiResponse(response,body)
				//	break
				default:
					int statusCode = 400
					Locale tmp = RequestContextUtils.getLocale(request);
					String lang = (tmp)?tmp.getLanguage():"en"
					response.setStatus(statusCode)
					String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${request.getRequestURI()}\"}"
					response.sendError(statusCode, message)

			}
		}

		response.writer.flush()
	}

}