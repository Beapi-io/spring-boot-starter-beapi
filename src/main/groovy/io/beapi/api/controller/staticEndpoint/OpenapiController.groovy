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
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.header.*
import org.springframework.stereotype.Controller

import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("openapi")
public class OpenapiController extends BeapiRequestHandler{

	@Autowired ApiProperties apiProperties;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OpenapiController.class);
	private static final ArrayList reservedUris = ['/authenticate','/register','/error','/login','/logout','/validate','/resetPassword']

	@Autowired protected ApiCacheService apiCacheService

	LinkedHashMap show(HttpServletRequest request, HttpServletResponse response){
		println("### [ApidocController :: showOpenApi]")
		// params.id == controller
		// so you can get just the docs back for a specific controller

		LinkedHashMap controllerResults = [
				"openapi": "3.0.0",
				"info":[:],
				"servers":[],
				"paths":[:],
				"components":[:]
		]
		controllerResults["info"]["version"] = apiversion
		controllerResults["info"]["title"] = "Api Docs"
		controllerResults["servers"] = [["url" : apiProperties.apiServer]]
		controllerResults["components"] = [
				"securitySchemes":[
					"bearerAuth": [
						"type": "http",
						"scheme": "bearer",
						"bearerFormat": "JWT"
					],
					"cookieAuth": [
						"type": "apiKey",
						"in": "cookie",
						"name": "JSESSIONID"
					]
				]
		]

		try {
			ArrayList controllers = apiCacheService.getCacheKeys()
			List returnData = []
			if (params?.id) {
				returnData.add(createApidocs(params.id))
			} else {
				controllers.each() {
					controllerResults["paths"].putAll(createOpenApidocs(it))
				}

				controllerResults["paths"].putAll(getPublicOpenApis())
			}
		}catch(Exception e){
			println(("[ApidocController :: show] : exception :"+e))
			throw new Exception("[ApidocController :: show] : exception :"+e)
		}

		return controllerResults
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


	private LinkedHashMap getPublicOpenApis(){
		LinkedHashMap controllerResults = [
				"/authenticate": [
						"post": [
								"requestBody":[
										"required": true,
										"content":[
												"application/json":[
														"schema":[
																"type": "object",
																"properties":[
																		"username":[ "type": "string" ],
																		"password":[ "type": "string" ]
																]
														]
												]
										]
								],
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object",
																		"properties":[
																				"token":[ "type": "string" ]
																		]
																]
														]
												]
										],
								]
						]
				],
				"/logout": [
						"get": [
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object"
																]
														]
												]
										],
								]
						]
				],
				"/validate[queryString]": [
						"get": [
								"parameters": [
										[
										"name"    : "id",
										"in"      : "query",
										"schema"  : ["type": "string"],
										"required": true
										]
								],
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object"
																]
														]
												]
										],
								]
						]
				],
				"/forgotPassword": [
						"post": [
								"requestBody":[
										"required": true,
										"content":[
												"application/json":[
														"schema":[
																"type": "object",
																"properties":[
																		"email":[ "type": "string" ],
																]
														]
												]
										]
								],
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object"
																]
														]
												]
										],
								]
						]
				],
				"/resetPassword": [
						"post": [
								"requestBody":[
										"required": true,
										"content":[
												"application/json":[
														"schema":[
																"type": "object",
																"properties":[
																		"password1":[ "type": "string" ],
																		"password2":[ "type": "string" ],
																		"id":[ "type": "string" ]
																]
														]
												]
										]
								],
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object"
																]
														]
												]
										],
								]
						]
				],
				"/register": [
						"post": [
								"requestBody":[
										"required": true,
										"content":[
												"application/json":[
														"schema":[
																"type": "object",
																"properties":[
																		"username":[ "type": "string" ],
																		"password":[ "type": "string" ],
																		"email":[ "type": "string" ]
																]
														]
												]
										]
								],
								"responses": [
										"200": [
												"description": "OK",
												"content": [
														"application/json":[
																"schema":[
																		"type": "object",
																		"properties":[
																				"token":[ "type": "string" ],
																				"firstName":["type":"string"],
																				"passwordExpired":["type":"boolean"],
																				"accountExpired":["type":"boolean"],
																				"oauthProvider":["type":"string"],
																				"username":["type":"string"],
																				"accountLocked":["type": "boolean"],
																				"password":["type": "string"],
																				"lastName":["type": "string"],
																				"oauthId":["type": "string"],
																				"enabled":["type": "boolean"],
																				"avatarUrl":["type": "string"],
																				"email":["type": "string"],
																				"id":["type": "integer"],
																				"version":["type":"integer"]
																		]
																]
														]
												]
										],
								]
						]
				],
		]
		return controllerResults
	}

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

	private LinkedHashMap createOpenApidocs(String controller){

		LinkedHashMap controllerResults = [:]


		def cache = apiCacheService.getApiCache(controller)
		def temp = cache[apiversion]

		ArrayList methodList = temp.keySet()

		methodList.each() { it2 ->
			if(!['deprecated', 'defaultAction', 'testOrder'].contains(it2)) {
				if (!controllerResults?.it?.apiversion?.it2) {
					def apiObject = temp[it2]



					ArrayList networkRoles = cache.networkGrpRoles

					if (checkNetworkGrp(networkRoles)) {
						String method = apiObject.getMethod()
						method = method.toLowerCase()

						LinkedHashMap receives = apiObject.getReceives()
						LinkedHashMap rturns = apiObject['returns'] as LinkedHashMap

						ArrayList receivesList = (receives[this.authority])?receives[this.authority]:receives['permitAll']
						LinkedHashMap receivesMap = [:]
						String queryString = "["
						int inc = 0
						receivesList.each(){ it5 ->

							if(['get','delete'].contains(method)){
								queryString += "${it5.name}=${cache.values[it5.name].type.toLowerCase()}"
								if(receivesList.size()<inc){
									queryString += "&"
								}
								inc++
							}

							receivesMap["${it5.name}"]=[:]
							receivesMap["${it5.name}"]["type"] = (cache.values[it5.name].type.toLowerCase()=='long')?'integer':cache.values[it5.name].type.toLowerCase()
							//rec.add(receivesMap)
						}
						queryString += "]"

						ArrayList returnsList = (rturns[this.authority])?rturns[this.authority]:rturns['permitAll']
						LinkedHashMap returnsMap = [:]
						returnsList.each(){ it5 ->
							returnsMap["${it5.name}"]=[:]

							switch(cache.values[it5.name].type.toLowerCase()){
								case ['array', 'boolean', 'integer', 'number', 'object', 'string']:
									returnsMap["${it5.name}"]["type"] = cache.values[it5.name].type.toLowerCase()
									break
								case 'long':
									returnsMap["${it5.name}"]["type"] = 'integer'
									break
								default:
									returnsMap["${it5.name}"]["type"] = 'object'
							}

							//returnsMap["${it5.name}"]["type"] = (cache.values[it5.name].type.toLowerCase()=='long')?'integer':cache.values[it5.name].type.toLowerCase()
						}


/* OpenApi conversion */




						String path = "/${apiversion}/${controller}/${it2}"
						//if(queryString!="?"){
						//	path = path+queryString
						//}



						controllerResults["${path}"] = [:]
						controllerResults["${path}"]["${method}"]=[:]


						if(['delete','get'].contains(method)){
							receivesMap.each() { String k, Map v ->
								try {
									controllerResults["${path}"]["${method}"] = [
											"security":[
													["bearerAuth": []],
													["cookieAuth": []]
											],
											"parameters":[
													[
													"name": k,
													"in": "query",
													"schema": ["type": v["type"]],
													"required": true
													]
											]


									]
								}catch(Exception e){
									println(("[ApidocController :: createApidocs] : exception :"+e))
									throw new Exception("[ApidocController :: createApidocs] : exception :"+e)
								}
							}
						}else {
							controllerResults["${path}"]["${method}"] = ["requestBody": [:]]
							controllerResults["${path}"]["${method}"]["requestBody"] = ["required": true, "content": [:]]
							controllerResults["${path}"]["${method}"]["requestBody"]["content"] = ["application/json": [:]]
							controllerResults["${path}"]["${method}"]["requestBody"]["content"]["application/json"] = ["schema": [:]]
							controllerResults["${path}"]["${method}"]["requestBody"]["content"]["application/json"]["schema"] = ["type": "object", "properties": [:]]
							controllerResults["${path}"]["${method}"]["requestBody"]["content"]["application/json"]["schema"]["properties"] = receivesMap
						}


						controllerResults["${path}"]["${method}"]["responses"] = ['200':[:]]
						controllerResults["${path}"]["${method}"]["responses"]['200'] = ["description": "OK","content":[:]]
						controllerResults["${path}"]["${method}"]["responses"]['200']["content"] = ["application/json":[:]]
						controllerResults["${path}"]["${method}"]["responses"]['200']["content"]["application/json"] = ["schema":[:]]
						controllerResults["${path}"]["${method}"]["responses"]['200']["content"]["application/json"]["schema"] = ["type": "object", "properties":[:]]
						controllerResults["${path}"]["${method}"]["responses"]['200']["content"]["application/json"]["schema"]["properties"] = returnsMap

					}
				}
			}
		}
		return controllerResults
	}
}
