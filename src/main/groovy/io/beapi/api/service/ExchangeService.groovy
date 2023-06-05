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


// NOTE : CALLTYPE = 1
@Service
public class ExchangeService extends ApiExchange{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ExchangeService.class);
	private static final ArrayList RESERVED_PARAM_NAMES = ['batch','chain']

	ApiCacheService apiCacheService

	boolean overrideAutoMimeTypes = false

	public ExchangeService(ApiCacheService apiCacheService) {
		try {
			this.apiCacheService = apiCacheService
		} catch (Exception e) {
			println("# [Beapi] ExchangeService - initialization Exception - ${e}")
			System.exit(0)
		}
	}

    // [REQUEST]
    boolean apiRequest(HttpServletRequest request, HttpServletResponse response, String authority){

		initVars(request,response,authority)


		if(this.apiObject) {
			// todo : create public api list
			if(this.method == 'GET') {

				setCacheHash(request.getAttribute('params'), this.receivesList)

				// RETRIEVE CACHED RESULT (only if using 'GET' method)
				if((this.apiObject?.cachedResult) && (this.apiObject?.cachedResult?."${this.authority}"?."${this.responseFileType}"?."${cacheHash}")) {

					String cachedResult
					//try {
						if(apiObject['cachedResult'][authority][responseFileType][cacheHash]){
							cachedResult = apiObject['cachedResult'][authority][responseFileType][cacheHash]
						}else{
							cachedResult = apiObject['cachedResult']['permitAll'][responseFileType][cacheHash]
						}
					//} catch (Exception e) {
					//	throw new Exception("[RequestInitializationFilter :: processFilterChain] : Exception - full stack trace follows:", e)
					//}

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
		//println("### apiResponse ###")
        String output = parseOutput(body, responseFileType)

        if(method=='GET') {
            apiCacheService.setApiCachedResult(cacheHash, this.controller, this.apiversion, this.action, this.authority, responseFileType, output)
        }

        PrintWriter writer = response.getWriter();
        writer.write(output);
        writer.close()
        response.writer.flush()
    }

	private void initVars(HttpServletRequest request, HttpServletResponse response, String authority) {
		//String accept = request.getHeader('Accept')
		//String contentType = request.getContentType()

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


		this.method = request.getMethod()
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
			throw new Exception("[ExchangeService :: init] : Exception. full stack trace follows:", e)
		}

	}


}
