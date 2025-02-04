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
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import io.beapi.api.service.StatsService
import io.beapi.api.service.ErrorService
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.ServletRequestAttributes
// NOTE : CALLTYPE = 1
/**
 *
 * This class provides basic methods for calltype=1 (ie endpoints requested like '/v0.1/')
 * @author Owen Rubel
 *
 * @see ApiExchange
 *
 */
@Service
public class ExchangeService extends ApiExchange{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExchangeService.class);
	String markerText = "DEVNOTES";
	Marker devnotes = MarkerFactory.getMarker(markerText);

	private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']

	StatsService statsService
	ApiCacheService apiCacheService
	LinkRelationService linkRelationService
	ErrorService errorService

	boolean overrideAutoMimeTypes = false




	/**
	 *
	 * Constructor for ExchangeService
	 * @author Owen Rubel
	 *
	 * @param statsService Injected Bean for StatsService
	 * @param linkRelationService Injected Bean for linkRelationService
	 * @param apiCacheService Injected Bean for apiCacheService
	 * @see ApiExchange
	 *
	 */
	public ExchangeService(ErrorService errorService, StatsService statsService, LinkRelationService linkRelationService, ApiCacheService apiCacheService) {
		try {
			this.linkRelationService = linkRelationService
			this.apiCacheService = apiCacheService
			this.statsService = statsService
			this.errorService = errorService
		} catch (Exception e) {
			println("# [Beapi] ExchangeService - initialization Exception - ${e}")
			System.exit(0)
		}
	}

	/**
	 *
	 * method for handling endpoint requests
	 * @author Owen Rubel
	 *
	 * @param request injected HttpServletRequest for handling request
	 * @param response injected HttpServletResponse for handling response
	 * @param authority injected principal authority
	 * @see ApiExchange
	 *
	 */
    boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority){
		//println("### ExchangeService :: apiRequest")
		initVars(request,response,authority)

		if(this.apiObject) {
			// todo : create public api list

			if(this.apiObject.updateCache && this.method == 'GET') {
				setCacheHash(request.getAttribute('cacheHash'))

				// RETRIEVE CACHED RESULT (only if using 'GET' method)
				if ((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${cacheHash}")) {

					String cachedResult = (this.apiObject['cachedResult'][authority][responseFileType][cacheHash]) ?: this.apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]

					if (cachedResult && cachedResult.size() > 0) {
						// PLACEHOLDER FOR APITHROTTLING
						String linkRelations = linkRelationService.processLinkRelations(request, response, this.apiObject)
						String newResult = (linkRelations) ? "[${cachedResult},${linkRelations}]" : cachedResult

						response.setStatus(200);
						PrintWriter writer = response.getWriter();
						writer.write(newResult);
						writer.close()
						//response.writer.flush()
						return false
					}
				}
			}

		}

		if(!validateMethod()){
			logger.warn(devnotes,"[ INVALID REQUEST METHOD ] : SENT REQUEST METHOD FOR '${this.uObj.getController()}/${this.uObj.getAction()}' DOES NOT MATCH EXPECTED 'REQUEST' METHOD OF '${apiObject['method'].toUpperCase()}'. IF THIS IS AN ISSUE, CHECK THE REQUESTMETHOD IN THE IOSTATE FILE FOR THIS CONTROLLER/ACTION.")
println("not valid method")
			try {
				writeErrorResponse(request, response, '405');
			}catch(Exception e){
				println("exception :"+e)
			}
			return false
		}else{
			return true
		}
    }

	/**
	 *
	 * method for handling endpoint response
	 * @author Owen Rubel
	 *
	 * @param request injected HttpServletRequest for handling request
	 * @param response injected HttpServletResponse for handling response
	 * @param body injected response body for parsing
	 * @see ApiExchange
	 *
	 */
    void apiResponse(HttpServletRequest request,HttpServletResponse response, ArrayList body){
		//println("### ExchangeService :: apiResponse")
        String output = parseOutput(body, responseFileType)
		// return/update cache if 'updateCache' is true
		if(this.apiObject.updateCache && method == 'GET') {
			apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, output)
		} else {
			if (response.getStatus() == 200) {
				apiCacheService.unsetApiCachedResult(this.controller, this.action, this.apiversion)
			}
		}



		try{
			statsService.setStat((String)response.getStatus(),(String)request.getRequestURI())
		}catch(Exception e){
			println("### [statsService :: postHandle] exception (1) : "+e)
		}



		/*
		return LinkRelations only upon request with this header
		 */
        PrintWriter writer = response.getWriter();
		if(request.getHeader('X-LINK-RELATIONS') == "true"){
			String linkRelations = linkRelationService.processLinkRelations(request, response, this.apiObject)
			String newResult = (linkRelations)?"[${output},${linkRelations}]":"[${output}]"
			writer.write(newResult);
		}else{
			writer.write(output);
		}
        writer.close()
        //response.writer.flush()
    }

	/**
	 *
	 * method for initializing variables used for handling request/response
	 * @author Owen Rubel
	 *
	 * @param request injected HttpServletRequest for handling request
	 * @param response injected HttpServletResponse for handling response
	 * @param authority injected principal authority
	 * @see ApiExchange
	 *
	 */
	private void initVars(HttpServletRequest request, HttpServletResponse response, String authority) throws Exception{
		//String accept = request.getHeader('Accept')
		//String contentType = request.getContentType()

		this.responseFileType = request.getAttribute('responseFileType')

		this.uObj = request.getAttribute('uriObj')
		this.callType = this.uObj.getCallType()
		this.version = this.uObj.getAppVersion()
		this.appversion = this.uObj.getDefaultAppVersion()
		this.apiversion = this.uObj.getApiVersion()
		this.controller = this.uObj.getController()

		request.setAttribute('controller',this.controller)
		this.action = this.uObj.getAction()
		request.setAttribute('action',this.action)
		this.trace = this.uObj.isTrace()
		this.id = this.uObj.getId()
		this.method = request.getMethod()
		this.authority = authority
		this.uri = request.getRequestURI()


		// TODO : set 'max'
		// TODO : set 'offset'

		try {
			this.apiObject = apiCacheService.getApiDescriptor(this.controller, this.apiversion, this.action)

			LinkedHashMap receives = this.apiObject?.getReceivesList()

			this.receivesList = (receives[this.authority]) ? receives[this.authority] : receives['permitAll']

			LinkedHashMap returns = this.apiObject?.getReturnsList()
			this.returnsList = (returns[this.authority]) ? returns[this.authority] : returns['permitAll']
			if(!request.getAttribute('responseList')){ request.setAttribute('responseList',this.returnsList) }

			this.method = request.getMethod()
		} catch (Exception e) {
			logger.warn(devnotes,"[ NO IOSTATE ] : URI IS PARSEABLKE BUT NO IOSTATE FILE WAS PARSED THAT MATCHES THIS URI FOR '${this.controller}/${this.action}'. MAKE SURE YOU ARE USING 'camelCase' IN THE URI OR THAT THE IOSTATE FILE EXISTS AND IS PROPERLY DECLARED. ")
			throw new Exception("[ExchangeService :: init] : Exception. full stack trace follows:", e)
		}

	}
}
