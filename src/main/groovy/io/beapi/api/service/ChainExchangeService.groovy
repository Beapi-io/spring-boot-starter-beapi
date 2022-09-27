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
import org.json.JSONObject
import io.beapi.api.utils.ErrorCodes
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext
import javax.json.*
import org.springframework.security.web.header.*
import groovyx.gpars.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/*
PARAMS located in :
request.getSession().getAttribute('chainOrder')
request.getSession().getAttribute('chainType')
request.getSession().getAttribute('chainKey')
request.getSession().getAttribute('chainSize')

 */
// NOTE : CALLTYPE = 3
@Service
public class ChainExchangeService extends ApiExchange{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ChainExchangeService.class);

	LinkedHashMap networkRoles
	LinkedList chain = []
	String newPath
	String chainType
	int chainSize
	LinkedHashMap chainOrder =[:]
	LinkedHashMap chainParams =[:]
	ApiCacheService apiCacheService
	PrincipleService principle
	ApplicationContext ctx
	boolean overrideAutoMimeTypes = false


	public ChainExchangeService(ApiCacheService apiCacheService, ApplicationContext applicationContext) {
		try {
			this.apiCacheService = apiCacheService
			this.ctx = applicationContext
		} catch (Exception e) {
			println("# [Beapi] IoStateService - initialization Exception - ${e}")
			System.exit(0)
		}
	}



	boolean chainRequest(HttpServletRequest request, HttpServletResponse response, String authority) {
		println("### chainRequest...")
		//this.networkRoles = networkRoles
		//initVars(request,response,authority)
		//clearChainVars(request)
		initChainVars(request, response,authority)

		return true
	}

	void chainResponse(HttpServletRequest request, HttpServletResponse response, ArrayList body){
		// first compare to cache for ROLE and parse out appropriate data to return
		println("### chainResponse...")
		if (body) {
			// STORE CACHED RESULT
			//String authority = getUserRole(this.roles) as String

			//this.returnsList = getReturnsList(this.rturns, this.authority)
			//body = parseResponseParams(body, this.returnsList)

			setNewChainPath(request, body)

			String role
			if(request.getSession().getAttribute('chainOrder').isEmpty() && this.newPath==null) {
				// todo : cache attribute 'batchOutput'; concat of all batch output

				concatChainOutput(body, request, response, this.responseFileType)
				this.chain=[]

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

			}else{
				// concat output
				//this.apiObject?.returns?.permitAll?.each(){ it -> this.returnsList.add(it.name) }
				//request.getSession().removeAttribute('returnsList')
				//request.getSession().setAttribute('returnsList',this.returnsList)

				concatChainOutput(body, request, response, this.responseFileType)

				def servletCtx = this.ctx.getServletContext()
				def rd = servletCtx?.getRequestDispatcher(this.newPath)
				rd.forward(request, response)
			}
		}else{
			writeErrorResponse(response,'422',this.uri,'No data returned for this call')
		}
	}

	void initChainVars(HttpServletRequest request, HttpServletResponse response, String authority){
		this.chainType = request.getSession().getAttribute('chainType')
		this.chainSize = request.getSession().getAttribute('chainSize')
		this.chainOrder = request.getSession().getAttribute('chainOrder')
		this.chainParams = request.getSession().getAttribute('chainParams')

		if (this.chainOrder.size()!=this.chainSize) {
			['returnsList','uriList'].each {
				request.getSession().removeAttribute(it)
			}
			// reinitializationg for forward
			this.controller =  request.getSession().getAttribute('controller')
			this.action = request.getSession().getAttribute('action')

			this.cache = apiCacheService.getApiCache(this.controller)
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List

			this.apiObject = temp[this.action]
			//this.handler = this.apiObject['handler']
			//request.getSession().setAttribute('handler',this.handler)
			this.receives = this.apiObject.getReceives()
			this.rturns = this.apiObject['returns'] as LinkedHashMap
			this.returnsAuths = this.rturns.keySet()

			this.uri = request.getRequestURI()

			this.receivesList = getReceivesList(this.receives)
			this.returnsList = getReturnsList(this.rturns)
			request.getSession().setAttribute('returnsList',this.returnsList)
		}else{
			// initial call
			String accept = request.getHeader('Accept')
			String contentType = request.getContentType()
			this.cores = request.getAttribute('cores')
			this.responseFileType = request.getAttribute('responseFileType')

			this.uList = request.getAttribute('uriList')
			this.controller = uList[4]
			request.getSession().setAttribute('controller',this.controller)
			this.action = uList[5]
			request.getSession().setAttribute('action',this.action)

			this.id = uList[7]
			this.callType = uList[0]
			this.version = uList[1]
			this.appversion = uList[2]
			this.apiversion = uList[3]

			this.method = request.getMethod()
			this.authority = authority

			this.cache = apiCacheService.getApiCache(this.controller)
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List
			this.apiObject = temp[this.action]
			//this.handler = this.apiObject['handler']
			//request.getSession().setAttribute('handler',this.handler)
			this.receives = this.apiObject.getReceives()
			this.rturns = this.apiObject['returns'] as LinkedHashMap
			this.returnsAuths = this.rturns.keySet()

			this.uri = request.getRequestURI()
			this.receivesList = request.getSession().getAttribute('receivesList')
			this.returnsList = request.getSession().getAttribute('returnsList')
		}

		if (request.getMethod() != 'GET') {
			switch (request.getSession().getAttribute('chainType')) {
				case 'postchain':
					if (request.getSession().getAttribute('chainOrder').size() == 0) {
						//check method
						if(this.apiObject['method'].toUpperCase() != this.method){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
						request.getSession().setAttribute('params', request.getSession().getAttribute('chainParams'))
					}else{
						if(this.apiObject['method'].toUpperCase() != 'GET'){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
					}
					break
				case 'prechain':
					if (request.getSession().getAttribute('chainOrder').size() == request.getSession().getAttribute('chainSize')) {
						if(this.apiObject['method'].toUpperCase() != this.method){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
						request.getSession().setAttribute('params', request.getSession().getAttribute('chainParams'))
					}else{
						if(this.apiObject['method'].toUpperCase() != 'GET'){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
					}
					break;
				default:
					if(this.apiObject['method'].toUpperCase() != 'GET'){
						writeErrorResponse(response,'405',request.getRequestURI());
					}
					break;

			}
		}
	}


	void clearChainVars(HttpServletRequest request){
		['controller','action','receivesList','returnsList','uriList'].each {
			request.getSession().removeAttribute(it)
		}
	}

	void setNewChainPath(HttpServletRequest request, ArrayList body){
		String method = request.getMethod()
		LinkedHashMap chainParams = [:]
		String newPath

		try {
			if(request.getSession().getAttribute('chainOrder')) {
				def temp = request.getSession().getAttribute('chainOrder').entrySet().iterator().next()

				// todo : check keys against against 'receivesList' in IO State for role
				ArrayList pathuri = []
				temp.each() { it ->
					String path = it.getKey()
					request.getSession().getAttribute('chainOrder').remove(path)

					pathuri = path.split('/')
					String newCont = pathuri[0].uncapitalize()
					String newAct = pathuri[1]

					request.getSession().removeAttribute('controller')
					request.getSession().removeAttribute('action')
					request.getSession().setAttribute('controller',pathuri[0].uncapitalize())
					request.getSession().setAttribute('action',pathuri[1])

					request.getSession().removeAttribute('params')
					request.getSession().removeAttribute('GET')
					request.getSession().removeAttribute('POST')

					String id = new String(body[0]["${request.getSession().getAttribute('chainKey')}"].toString())

					if(id=='null'){
						this.params['id'] = null
						newPath = "/c${this.version}/${newCont}/${newAct}"
					}else{
						if(request.getSession().getAttribute('chainOrder').size()==0 && request.getSession().getAttribute('chainType')=='postchain' && request.getMethod()!='GET'){
							LinkedHashMap<String,String> post = request.getSession().getAttribute('chainParams')
							request.getSession().setAttribute('POST', post)
							request.getSession().setAttribute('params',post)
							newPath = "/c${this.version}/${newCont}/${newAct}/"
						}else if(request.getSession().getAttribute('chainOrder').size()==request.getSession().getAttribute('chainSize') && request.getSession().getAttribute('chainType')=='prechain' && request.getMethod()!='GET'){
							LinkedHashMap<String,String> post = request.getSession().getAttribute('chainParams')
							request.getSession().setAttribute('POST', post)
							request.getSession().setAttribute('params',post)
							newPath = "/c${this.version}/${newCont}/${newAct}/"
						}else{
							LinkedHashMap<String,String> get = [:]
							get['id'] = id
							request.getSession().setAttribute('GET',get)
							request.getSession().setAttribute('params',get)
							newPath = "/c${this.version}/${newCont}/${newAct}/${id}"
						}
					}

					request.getSession().setAttribute('chainKey', it.getValue())
					this.controller = request.getSession().getAttribute('controller')
					this.action = request.getSession().getAttribute('action')

				}

			}
		} catch (Exception e) {
			throw new Exception("[ChainExchangeService :: setNewChainPath] : Exception - full stack trace follows:", e)
		}

		this.newPath = newPath
	}

	void concatChainOutput(ArrayList responseBody, HttpServletRequest request, HttpServletResponse response, String responseFileType){
		this.chain.add(responseBody[0])
		if(request.getSession().getAttribute('chainOrder').isEmpty()) {
			String temp = ""
			int inc = 0
			this.chain.each(){ it ->
				if(inc>0){ temp += ',' }
				temp += parseBodyByFiletype(it, responseFileType)
				inc+=1
			}
			String output = "[${temp}]"
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
				//'XML' not supported for chaining
				return '{}'
				break;
			default:
				// unsupported mimetype
				return ''
				break;
		}
	}

	void setChainParams(HttpServletRequest request) {
		if (request.getSession().getAttribute('params')){
			this.params = request.getSession().getAttribute('params')
		}

		try {
			if(request.getSession().getAttribute('chainVars')) {
				HashSet temp = new HashSet(request.getSession().getAttribute('chainVars').entrySet())

				// todo : check keys against against 'receivesList' in IO State for role

				temp.each() { it ->
					String key = it.getKey()
					request.getSession().getAttribute('chainVars').remove(key)
					String value = it.getValue()
					this.params.put(key,value)
				}
			}
		} catch (Exception e) {
			throw new Exception("[ChainExchangeService :: setChainParams] : Exception - full stack trace follows:", e)
		}

		request.getSession().setAttribute('params',this.params)
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
