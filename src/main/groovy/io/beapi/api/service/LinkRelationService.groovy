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

import io.beapi.api.utils.ErrorCodes
import io.beapi.api.utils.UriObject
import io.beapi.api.utils.ApiDescriptor
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.json.JSONObject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class LinkRelationService {

	@Autowired
	ApiCacheService apiCacheService;

	UriObject uObj
	protected ApiDescriptor apiObject
	protected String authority

	public LinkRelationService(ApiCacheService apiCacheService, PrincipleService principle){
		this.apiCacheService = apiCacheService
		this.authority=principle.authorities()
	}


	// NOTE: This has to occur AFTER RETURNSET has been generated
	// check for header and if 'true', replace header value with json/xml link relations
	// we can then use this HERE (for cache) or for return response in ApiInterceptor
	//
	// keep in mind, 'linkRelations 'dataset' are based on RETURNSET
	// so companyId FOREIGNKEY will be in RETURNSET which influences the links for 'LinkRelations'
	private String processLinkRelations(HttpServletRequest request, HttpServletResponse response, ApiDescriptor apiObject){
		// can only be used on callType 'v' (not with BATCH / CHAIN / TRACE / etc)
		this.uObj = request.getAttribute('uriObj')

		String output = ""
		if(this.uObj.callType==1) {
			if (request.getHeader('X-LINK-RELATIONS') == "true" || request.getHeader('X-LINK-RELATIONS') == "TRUE") {
				apiObject.fkeys.each() { LinkedHashMap it ->
					it.each() { k, v ->
						output += generateLinks(k, v, request.getContentType(), apiObject)
					}
				}
			}
		}
		return output
	}

	// for each 'fkey'(ie controller), check all 'foreign reference' endpoints against
	// this.authority (ie ROLE) and if user has access, build out REQUEST/RESPONSE json for each
	// and return
	private String generateLinks(String id, String controller, String contentType, ApiDescriptor apiObject){
		contentType = (contentType)?:"application/json"
		LinkedHashMap output = [:]
		String path
		LinkedHashMap cache = apiCacheService?.getApiCache(controller)
		def temp = cache[this.uObj.getApiVersion()]
		temp.each(){ k, v ->
			if(!['deprecated', 'defaultAction', 'testOrder'].contains(k)){
				// [ApiDescriptor]
				apiObject = temp[k]
				ArrayList receives = []
				if((apiObject?.getReceivesList()[this.authority])) {
					apiObject?.getReceivesList()[this.authority].each(){ receives.add(it) }
				}else{
					apiObject?.getReceivesList()['permitAll'].each(){ receives.add(it) }
				}
				//if(apiObject?.getReceivesList()['permitAll']) {
				//	apiObject?.getReceivesList()['permitAll'].each(){ receives.add(it) }
				//}

				Set returns = new HashSet<>()
				if(apiObject?.getReturnsList()[this.authority]) {
					apiObject?.getReturnsList()[this.authority].each(){ returns.add(it) }
				}else{
					apiObject?.getReturnsList()['permitAll'].each(){ returns.add(it) }
				}
				//if(apiObject?.getReturnsList()['permitAll']) {
				//	apiObject?.getReturnsList()['permitAll'].each(){ returns.add(it) }
				//}

				if(receives || returns){
					// set PATH first
					if(apiObject.method=='GET' && receives.contains('id')){
						LinkedHashMap requestParams = [:]
						apiObject.getReceives().each(){ k1, v1->
							if((controller+v1.name[0].capitalize()) == id){
								requestParams.put(v1.name[0], v1.paramType[0].toString())
							}else{
								requestParams.put(v1.name[0], v1.paramType[0].toString())
							}
						}

						String encodedURL
						requestParams.each(){k2, v2 ->
							encodedURL = k2 + "=[${v2}]&";
						}
						path = "v${this.uObj.getAppVersion()}/${controller}/${k}?${encodedURL}"
						output = ['path': path,'receives':[],'returns':returns]

					}else{
						path = "v${this.uObj.getAppVersion()}/${controller}/${k}"
						output = ['path': path,'receives':receives,'returns':returns]
					}
				}

			}
		}

		String out = formatOutput(contentType, output)
		return out
	}

	protected String formatOutput(String contentType, LinkedHashMap output){
		String out
		switch(contentType){
			case 'text/xml':
			case 'application/xml':
				//out = XML.toJSONObject(output).toString()
				break;
			case 'text/json':
			case 'application/json':
			default:
				out = new JSONObject(output).toString()
				break;
		}
		return out
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


