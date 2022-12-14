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

/*
*PARAMS located in :
*request.getSession().getAttribute('chainOrder')
*request.getSession().getAttribute('chainType')
*request.getSession().getAttribute('chainKey')
*request.getSession().getAttribute('chainSize')
*
 */
// NOTE : CALLTYPE = 3
@Service
public class ChainExchangeService extends ApiExchange{

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ChainExchangeService.class);

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

		initChainVars(request, response,authority)

		return true
	}

	void chainResponse(HttpServletRequest request, HttpServletResponse response, ArrayList body){
		// first compare to cache for ROLE and parse out appropriate data to return
		if (body) {
			setNewChainPath(request, body)

			String role
			if(request.getAttribute('chainOrder').isEmpty() && this.newPath==null) {
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

				concatChainOutput(body, request, response, this.responseFileType)

				def servletCtx = this.ctx.getServletContext()
				def rd = servletCtx?.getRequestDispatcher(this.newPath)
				println("newpath : "+this.newPath)
				rd.forward(request, response)
			}
		}else{
			writeErrorResponse(response,'422',this.uri,'No data returned for this call')
		}
	}

	void initChainVars(HttpServletRequest request, HttpServletResponse response, String authority){
		this.chainType = request.getAttribute('chainType')
		this.chainSize = request.getAttribute('chainSize')
		this.chainOrder = request.getAttribute('chainOrder')
		this.chainParams = request.getAttribute('chainParams')

		if (this.chainOrder.size()!=this.chainSize) {
			['returnsList','uriList'].each {
				request.removeAttribute(it)
			}
			// reinitializationg for forward
			this.controller =  request.getAttribute('controller')
			this.action = request.getAttribute('action')

			this.cache = apiCacheService.getApiCache(this.controller)
			def temp = cache[this.apiversion]
			this.defaultAction = temp['defaultAction']
			this.deprecated = temp['deprecated'] as List

			this.apiObject = temp[this.action]

			this.keyList = this.apiObject?.getKeyList()

			//this.handler = this.apiObject['handler']
			//request.setAttribute('handler',this.handler)
			this.receives = this.apiObject.getReceives()
			this.rturns = this.apiObject['returns'] as LinkedHashMap
			this.returnsAuths = this.rturns.keySet()

			this.uri = request.getRequestURI()

			this.receivesList = getReceivesList(this.receives)
			this.returnsList = getReturnsList(this.rturns)
			request.setAttribute('returnsList',this.returnsList)
		}else{
			// initial call
			String accept = request.getHeader('Accept')
			String contentType = request.getContentType()

			this.responseFileType = request.getAttribute('responseFileType')

			this.uList = request.getAttribute('uriList')
			this.controller = uList[4]
			request.setAttribute('controller',this.controller)
			this.action = uList[5]
			request.setAttribute('action',this.action)

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
			//request.setAttribute('handler',this.handler)
			this.receives = this.apiObject.getReceives()
			this.rturns = this.apiObject['returns'] as LinkedHashMap
			this.returnsAuths = this.rturns.keySet()

			this.uri = request.getRequestURI()
			this.receivesList = request.getAttribute('receivesList')
			this.returnsList = request.getAttribute('returnsList')
		}

		if (request.getMethod() != 'GET') {
			switch (request.getAttribute('chainType')) {
				case 'postchain':
					if (request.getAttribute('chainOrder').size() == 0) {
						//check method
						if(this.apiObject['method'].toUpperCase() != this.method){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
						request.setAttribute('params', request.getAttribute('chainParams'))
					}else{
						if(this.apiObject['method'].toUpperCase() != 'GET'){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
					}
					break
				case 'prechain':
					if (request.getAttribute('chainOrder').size() == request.getAttribute('chainSize')) {
						if(this.apiObject['method'].toUpperCase() != this.method){
							writeErrorResponse(response,'405',request.getRequestURI());
						}
						request.setAttribute('params', request.getAttribute('chainParams'))
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
			request.removeAttribute(it)
		}
	}

	void setNewChainPath(HttpServletRequest request, ArrayList body){
		String method = request.getMethod()
		LinkedHashMap chainParams = [:]
		String newPath

		try {
			if(request.getAttribute('chainOrder')) {
				def temp = request.getAttribute('chainOrder').entrySet().iterator().next()

				// todo : check keys against against 'receivesList' in IO State for role
				ArrayList pathuri = []
				temp.each() { it ->
					String path = it.getKey()
					request.getAttribute('chainOrder').remove(path)

					pathuri = path.split('/')
					String newCont = pathuri[0].uncapitalize()
					String newAct = pathuri[1]

					request.removeAttribute('controller')
					request.removeAttribute('action')
					request.setAttribute('controller',pathuri[0].uncapitalize())
					request.setAttribute('action',pathuri[1])

					request.removeAttribute('params')
					request.removeAttribute('GET')
					request.removeAttribute('POST')

					String id = new String(body[0]["${request.getAttribute('chainKey')}"].toString())

					if(id=='null'){
						this.params['id'] = null
						newPath = "/c${this.version}/${newCont}/${newAct}"
					}else{
						if(request.getAttribute('chainOrder').size()==0 && request.getAttribute('chainType')=='postchain' && request.getMethod()!='GET'){
							LinkedHashMap<String,String> post = request.getAttribute('chainParams')
							request.setAttribute('POST', post)
							request.setAttribute('params',post)
							newPath = "/c${this.version}/${newCont}/${newAct}/"
						}else if(request.getAttribute('chainOrder').size()==request.getAttribute('chainSize') && request.getAttribute('chainType')=='prechain' && request.getMethod()!='GET'){
							LinkedHashMap<String,String> post = request.getAttribute('chainParams')
							request.setAttribute('POST', post)
							request.setAttribute('params',post)
							newPath = "/c${this.version}/${newCont}/${newAct}/"
						}else{
							LinkedHashMap<String,String> get = [:]
							get['id'] = id
							request.setAttribute('GET',get)
							request.setAttribute('params',get)
							newPath = "/c${this.version}/${newCont}/${newAct}/${id}"
						}
					}

					request.setAttribute('chainKey', it.getValue())
					this.controller = request.getAttribute('controller')
					this.action = request.getAttribute('action')

				}

			}
		} catch (Exception e) {
			throw new Exception("[ChainExchangeService :: setNewChainPath] : Exception - full stack trace follows:", e)
		}

		this.newPath = newPath
	}

	void concatChainOutput(ArrayList responseBody, HttpServletRequest request, HttpServletResponse response, String responseFileType){
		this.chain.add(responseBody[0])
		if(request.getAttribute('chainOrder').isEmpty()) {
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
		if (request.getAttribute('params')){
			this.params = request.getAttribute('params')
		}

		try {
			if(request.getAttribute('chainVars')) {
				HashSet temp = new HashSet(request.getAttribute('chainVars').entrySet())

				// todo : check keys against against 'receivesList' in IO State for role

				temp.each() { it ->
					String key = it.getKey()
					request.getAttribute('chainVars').remove(key)
					String value = it.getValue()
					this.params.put(key,value)
				}
			}
		} catch (Exception e) {
			throw new Exception("[ChainExchangeService :: setChainParams] : Exception - full stack trace follows:", e)
		}

		request.setAttribute('params',this.params)
	}


}
