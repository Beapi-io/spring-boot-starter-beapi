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


import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.springframework.security.web.header.*
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import io.beapi.api.controller.BeapiRequestHandler;
import org.springframework.stereotype.Controller;
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("apidoc")
public class ApidocController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApidocController.class);

	@Autowired
	protected ApiCacheService apiCacheService

	List show(HttpServletRequest request, HttpServletResponse response){

		LinkedHashMap controllerResults = [:]
		ArrayList controllers = apiCacheService.getCacheKeys()
		List returnData = []
		if(params?.id){
			returnData.add(createApidocs(params.id))
		}else{
			controllers.each() {
				def temp = createApidocs(it)
				controllerResults[it] = temp[it]
			}
			returnData = [controllerResults]
		}
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

	/* deprecated
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
	 */

	private LinkedHashMap createApidocs(String controller){
		LinkedHashMap controllerResults = [:]

		def cache = apiCacheService.getApiCache(controller)
		def temp = cache[apiversion]

		ArrayList methodList = temp.keySet()

		if(!controllerResults[controller]) {
			controllerResults[controller] = [:]
		}
		if(!controllerResults[controller][apiversion]){
			controllerResults[controller][apiversion] = [:]
		}
		methodList.each() { it2 ->
			if(!['deprecated', 'defaultAction', 'testOrder'].contains(it2)) {
				if (!controllerResults?.it?.apiversion?.it2) {
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

					ArrayList networkRoles = cache.networkGrpRoles

					if (checkNetworkGrp(networkRoles)) {
						ArrayList batchRoles = apiObject.getBatchRoles()
						ArrayList hookRoles = apiObject.getHookRoles()


						LinkedHashMap receives = apiObject.getReceives()
						LinkedHashMap rturns = apiObject['returns'] as LinkedHashMap

						LinkedHashMap result = apiObject.toLinkedHashMap()

						result['batch'] = false
						result['hook'] = false

						if(batchRoles.contains(this.authority)){
							result['batch'] = true
						}
						result.remove('batchRoles')

						if(hookRoles.contains(this.authority)){
							result['hook'] = true
						}
						result.remove('hookRoles')

						result.remove('receives')
						ArrayList receivesList = (receives[this.authority])?receives[this.authority]:receives['permitAll']
						ArrayList rec = []

						receivesList.each(){ it5 ->
							LinkedHashMap receivesMap = [:]
							receivesMap['name'] = it5.name
							receivesMap['type'] = cache.values[it5.name].type
							receivesMap['desc'] = cache.values[it5.name].description
							receivesMap['mockData'] = cache.values[it5.name].mockData
							rec.add(receivesMap)
						}
						result['receives'] = rec

						result.remove('returns')
						ArrayList returnsList = (rturns[this.authority])?rturns[this.authority]:rturns['permitAll']
						ArrayList ret = []

						returnsList.each(){ it5 ->
							LinkedHashMap returnsMap = [:]
							returnsMap['name'] = it5.name
							returnsMap['type'] = cache.values[it5.name].type
							returnsMap['desc'] = cache.values[it5.name].description
							returnsMap['mockData'] = cache.values[it5.name].mockData
							ret.add(returnsMap)
						}
						result['returns'] = ret

						result.remove('pkeys')
						result.remove('fkeys')
						result.remove('roles')
						result.remove('networkGrp')

						controllerResults[controller][apiversion][it2] = result
					}else{
						controllerResults.remove(controller)
					}
				}
			}
		}
		return controllerResults
	}
}
