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

import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext
import javax.json.*
import org.springframework.security.web.header.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// NOTE : CALLTYPE = 2
@Service
public class BatchExchangeService extends ApiExchange{

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BatchExchangeService.class);
	LinkedHashMap returns = [:]
	LinkedHashMap networkRoles
	LinkedList batch = []
	ApiCacheService apiCacheService
	ApplicationContext ctx

	public BatchExchangeService(ApiCacheService apiCacheService, ApplicationContext applicationContext) {
		try {
			this.apiCacheService = apiCacheService
			this.ctx = applicationContext
		} catch (Exception e) {
			println("# [Beapi] BatchExchangeService - initialization Exception - ${e}")
			System.exit(0)
		}
	}

	boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority) {

		initVars(request,response,authority)

		setBatchParams(request)

		if(this.apiObject) {
			// todo : create public api list
			if(this.apiObject.updateCache && this.method == 'GET') {

				setCacheHash(request.getAttribute('cacheHash'))

				// RETRIEVE CACHED RESULT (only if using 'GET' method)
				if((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${cacheHash}")) {

					String cachedResult = (this.apiObject['cachedResult'][authority][responseFileType][cacheHash])?:this.apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]

					if (cachedResult && cachedResult.size() > 0) {
						// PLACEHOLDER FOR APITHROTTLING
						String linkRelations = linkRelationService.processLinkRelations(request, response, this.apiObject)
						String newResult = (linkRelations)?"[${cachedResult},${linkRelations}]":cachedResult

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
			writeErrorResponse(response,'405',request.getRequestURI());
			return false;
		}

		if (!checkRequestParams(request.getAttribute('params'))) {
			writeErrorResponse(response, '400', request.getRequestURI());
			return false;
		}

		// routing call to controller
		return true
	}

	void batchResponse(HttpServletRequest request, HttpServletResponse response, ArrayList body){
		if (body) {
			if(request.getAttribute('batchVars').isEmpty()) {
				// concat and return
				parseBatchOutput(body, request, response, this.responseFileType)

				if(this.apiObject.updateCache && method == 'GET') {
					apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, responseBody[0])
				}else{
					if(response.getStatus()==200){
						apiCacheService.unsetApiCachedResult(this.controller,  this.action, this.apiversion)
					}
				}
			}else{
				// concat and forward
				parseBatchOutput(body, request, response, this.responseFileType)

				if(this.apiObject.updateCache && method == 'GET') {
					apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, responseBody[0])
				}else{
					if(response.getStatus()==200){
						apiCacheService.unsetApiCachedResult(this.controller,  this.action, this.apiversion)
					}
				}

				/*
				if (apiThrottle) {
					if(checkLimit(contentLength.length,this.authority)) {
						statsService.setStatsCache(userId, response.status, request.requestURI)
						render(text: getContent(content, contentType), contentType: contentType)
						return false
					}else{
						statsService.setStatsCache(userId, 404, request.requestURI)
						return false
					}
				} else {
					if(controller=='apidoc') {
						render(text: newModel as JSON, contentType: contentType)
					}else {
						render(text: content, contentType: contentType)
					}
					if(cachedEndpoint['hookRoles']) {
						List hookRoles = cachedEndpoint['hookRoles'] as List
						String service = "${controller}/${action}"
						hookService.postData(service, content, hookRoles, this.mthdKey)
					}
				}
				 */

				String path = "/b${version}/${controller}/${action}/**";
				try {
					this.ctx.getServletContext().getRequestDispatcher(path).forward(request, response);
				}catch(Exception e){
					PrintWriter writer = response.getWriter();
					writer.write("Unable to forward to ${path} : ");
					writer.close();
					response.writer.flush();
				}
			}
		}else{
			writeErrorResponse(response,'422',this.uri,'No data returned for this call');
		}
	}


	private void initVars(HttpServletRequest request, HttpServletResponse response, String authority) throws Exception{
		String accept = request.getHeader('Accept')
		String contentType = request.getContentType()

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
		this.cache = apiCacheService.getApiCache(this.controller)

		this.uri = request.getRequestURI()
		//this.receivesList = request.getAttribute('receivesList')
		//this.returnsList = request.getAttribute('returnsList')

		// TODO : set 'max'
		// TODO : set 'offset'

		try {
			//this.appVersion = request.getAttribute('version')
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List
			//this.apiObject = temp[this.action]

			this.apiObject = apiCacheService.getApiDescriptor(this.controller, this.apiversion, this.action)

			this.keyList = this.apiObject?.getKeyList()

			this.receivesList = (this.apiObject.receivesList[this.authority]) ? this.apiObject.receivesList[this.authority] : this.apiObject.receivesList['permitAll']
			this.returnsList = (this.apiObject.returnsList[this.authority]) ? this.apiObject.returnsList[this.authority] : this.apiObject.returnsList['permitAll']
			if(!request.getAttribute('responseList')){ request.setAttribute('responseList',this.returnsList) }

			//this.handler = this.apiObject['handler']
			//request.setAttribute('handler',this.handler)
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

	void parseBatchOutput(ArrayList responseBody, HttpServletRequest request, HttpServletResponse response, String responseFileType){
		this.batch.add(responseBody[0])

		if(request.getAttribute('batchVars').isEmpty()) {
			String output = "["
			int inc = 0
			this.batch.each(){ it ->
				if(inc>0){ output += ',' }
				output += parseBodyByFiletype(it, responseFileType)
				inc+=1
			}
			output += "]"
			PrintWriter writer = response.getWriter();
			writer.write(output);
		}
	}

	String parseBodyByFiletype(LinkedHashMap responseBody, String responseFileType){
		switch(responseFileType){
			case 'JSON':
				return new JSONObject(responseBody).toString()
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
	}

	void setBatchParams(HttpServletRequest request) throws Exception{
		try {
			if(request.getAttribute('batchVars')) {
				LinkedHashMap output = [:]
				def temp = request.getAttribute('batchVars').remove(0).entrySet()
				temp.each(){ output[it.key] = it.value }

				// todo : check keys against against 'receivesList' in IO State for role
				request.setAttribute('params',output)
			}
		} catch (Exception e) {
			throw new Exception("[BatchExchangeService :: setBatchParams] : Exception - full stack trace follows:", e)
		}
	}

}
