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
import io.beapi.api.service.ErrorService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.StatsService
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
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import javax.servlet.DispatcherType
import javax.json.*
import org.springframework.security.web.header.*
//import groovyx.gpars.*
import javax.servlet.RequestDispatcher
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import javax.crypto.KeyGenerator;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Autowired

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
	StatsService statsService
	ErrorService errorService

	public ApiInterceptor(ErrorService errorService,StatsService statsService, ExchangeService exchangeService, BatchExchangeService batchService, ChainExchangeService chainService, TraceExchangeService traceService, ApiProperties apiProperties) {
		//this.throttle = throttle
		this.exchangeService = exchangeService
		this.batchService = batchService
		this.chainService = chainService
		this.traceExchangeService = traceService
		this.apiProperties = apiProperties
		this.statsService = statsService
		this.errorService = errorService
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		//println("### ApiInterceptor / prehandle")


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
							errorService.writeErrorResponse(request, response, '401')
							return false
						}
						break
					case 3:
						if (apiProperties.chainingEnabled) {
							return chainService.apiRequest(request, response, this.authority)
						} else {
							errorService.writeErrorResponse(request, response, '401')
							return false
						}
						break
					case 4:
						if (privateRoles.contains(authority)) {
							return traceExchangeService.apiRequest(request, response, this.authority)
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
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
		//logger.info("postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView) : {}")
		//println("### ApiInterceptor / posthandle")

		ArrayList body = []
		if(request.getAttribute('responseBody')){
			body = request.getAttribute('responseBody')
		}

		String stat = (String)response.getStatus()
		String uri = (String)request.getRequestURI()

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
						errorService.writeErrorResponse(request, response,'401')
					}
					break
				case 3:
					if(apiProperties.chainingEnabled) {
						chainService.chainResponse(request, response, body)
					}else{
						errorService.writeErrorResponse(request, response,'401')
					}
					break
				case 4:
					traceExchangeService.apiResponse(response,body)
					break
				//case 5:
				//	hookExchangeService.apiResponse(response,body)
				//	break
				default:
					errorService.writeErrorResponse(request, response, '400')

			}
		}
		response.writer.flush()
	}

}