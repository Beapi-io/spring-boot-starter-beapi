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
package io.beapi.api.controller.staticEndpoint

import io.beapi.api.controller.BeapiRequestHandler
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovyx.gpars.*
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiExchange
import io.beapi.api.utils.ErrorCodes
import org.slf4j.LoggerFactory
import org.springframework.security.web.header.*
import org.springframework.stereotype.Service
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import io.beapi.api.controller.BeapiRequestHandler;
import org.springframework.stereotype.Controller;
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("apidoc")
public class ApidocController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApidocController.class);

	@Autowired
	ApiCacheService apiCacheService

	@Autowired
	PrincipleService principle;


	String authority

	/*
	public ApidocService(ApiCacheService apiCacheService, PrincipleService principleService, ApiProperties apiProperties) {
		try {
			this.apiCacheService = apiCacheService
			this.principle = principleService
			this.apiProperties = apiProperties
		} catch (Exception e) {
			println("# [Beapi] IoStateService - initialization Exception - ${e}")
			System.exit(0)
		}
	}
	 */

	List show(HttpServletRequest request, HttpServletResponse response){
		this.authority = principle.authorities()

		LinkedHashMap controllerResults = [:]
		LinkedHashMap networkGrpRoles = apiProperties.security.networkRoles
		ArrayList controllers = apiCacheService.getCacheKeys()

		controllers.each(){
			def cache = apiCacheService.getApiCache(it)
			def temp = cache[apiversion]

			ArrayList methodList = temp.keySet()

			if(!controllerResults[it]) {
				controllerResults[it] = [:]
			}
			if(!controllerResults[it][apiversion]){
				controllerResults[it][apiversion] = [:]
			}
			methodList.each() { it2 ->
				if(!['deprecated', 'defaultAction', 'testOrder'].contains(it2)) {
					if (!controllerResults[it][apiversion][it2]) {
						def apiObject = temp[it2]


						// check if role  is in networkGrp
						// if pass, get receivesList/returnsList
						// get all uniques in receivesList/returnsList for compare against Values to see what needs to be removed

						/*
						* remove :
						* - ROLES
						* - networkGrp
						* - pkeys
						* - fkeys
						*
						* RESET
						* - receives
						* - returns
						 */

						String networkGrp = apiObject['networkGrp']
						ArrayList networkRoles = networkGrpRoles[networkGrp].collect() { k, v -> v }

						if (checkNetworkGrp(networkRoles)) {
							LinkedHashMap rceives = apiObject.getReceives()
							LinkedHashMap rturns = apiObject['returns'] as LinkedHashMap

							LinkedHashMap result = apiObject.toLinkedHashMap()

							result.remove('receives')
							result['receives'] = setReceivesList(rceives)

							result.remove('returns')
							result['returns'] = setReturnsList(rturns)

							result.remove('pkeys')
							result.remove('fkeys')
							result.remove('roles')
							result.remove('networkGrp')

							controllerResults[it][apiversion][it2] = result
						} else {
							throw new Exception("[RequestInitializationFilter :: doFilterInternal] : Request params do not match expect params for '${this.controller}/${this.action}'")
						}


					}
				}
			}
		}
		List returnData = [controllerResults]
		println(returnData)

		return returnData

    }

	protected boolean checkNetworkGrp(ArrayList networkRoles){
		return networkRoles.contains(this.authority)
	}

	protected ArrayList setReceivesList(LinkedHashMap rceives){
		ArrayList result = []
		rceives.each() { k, v ->
			if(k==this.authority || k=='permitAll') {
				v.each { it2 ->
					if (!result.contains(it2['name'])) {
						result.add(it2['name'])
					}
				}
			}
		}
		return result
	}

	protected ArrayList setReturnsList(LinkedHashMap rturns){
		ArrayList result = []
		rturns.each() { k, v ->
			if(k==this.authority || k=='permitAll') {
				v.each() { it2 ->
					if (!result.contains(it2['name'])) {
						result.add(it2['name']) }
				}
			}
		}
		return result
	}

}