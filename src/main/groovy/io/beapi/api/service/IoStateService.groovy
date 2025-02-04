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



import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import groovy.json.JsonSlurper
import org.json.JSONObject

import io.beapi.api.properties.ApiProperties
import io.beapi.api.utils.ParamsDescriptor
import io.beapi.api.utils.ApiDescriptor
import io.beapi.api.utils.ApiParams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.method.HandlerMethod
import org.springframework.web.bind.annotation.RequestMethod
import groovy.ant.AntBuilder;
import java.lang.reflect.UndeclaredThrowableException
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties

/**
 *
 * Class for parsing connectors (IO State) in a usable form for starter
 * @author Owen Rubel
 *
 * @see ApiDescriptor
 *
 */
@Service
@EnableConfigurationProperties([ApiProperties.class])
public class IoStateService{

	@Autowired
	ApiProperties apiProperties

	@Autowired
	ApiCacheService apiCacheService;

	String version

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IoStateService.class);

	/**
	 *
	 * Constructor for ExchangeService
	 * @author Owen Rubel
	 *
	 * @param apiProperties properties object for comparison and validating
	 * @param applicationContext Application context used for recursive lookups
	 * @param apiCacheService Injected Bean for apiCacheService
	 * @param version application version; used in path comparison
	 *
	 */
	public IoStateService(ApiProperties apiProperties, ApplicationContext applicationContext, ApiCacheService apiCacheService, String version)  throws Exception {
		ApplicationContext ctx
		this.version = version
		//try {
			ctx = applicationContext
			this.apiProperties = apiProperties
			this.apiCacheService = apiCacheService
			initIoStateDir()
			ego()
			validateRpcNamingConventions(ctx,version)
		//}catch(Exception e){
		//	println("# [Beapi] IoStateService - initialization Exception - ${e}")
		//	System.exit(0)
		//}

	}


	private String getVersion(){
		ClassLoader classLoader = getClass().getClassLoader();
		URL incoming = classLoader.getResource("META-INF/build-info.properties")

		String version
		if (incoming != null) {
			Properties properties = new Properties();
			properties.load(incoming.openStream());
			version = properties.getProperty('build.version')
		}
		return version
	}

	/**
	 *
	 * method for initializinf IO State directory
	 * @author Owen Rubel
	 *
	 *
	 */
	void initIoStateDir(){
		try{
			this.apiCacheService.flushAllApiCache();
		}catch(Exception e){
			println("# IoStateService - flushCache Exception - ${e}")
			System.exit(0)
		}

		try{

			String baseDir = System.getProperty("user.dir");
			String userDir = System.getProperty("user.home")
			String apiObjectSrc = "${userDir}/${apiProperties.iostateDir}"

			println("### IOStateDir : "+apiObjectSrc)

			String projDir = ""

			def ant = new AntBuilder()

			ArrayList packName = this.getClass().getCanonicalName().split('\\.')
			packName.remove(packName.size()-1)
			packName.remove(packName.size()-1)

			packName.each(){
				projDir += it+'/'
			}

			parseFiles(apiObjectSrc.toString())


		}catch(Exception e){
			println("# IoStateService - initIoStateDir Exception - ${e}")
			System.exit(0)
		}

		ArrayList staticEndpoints = apiProperties.getStaticEndpoint()
		staticEndpoints.each(){
			String path = "${it}.json" as String
			parseResource(path)
		}
		//parseResource("apidoc.json")
		//parseResource("connector.json")

	}

	/**
	 *
	 * method for retrieving each file for parsing
	 * @author Owen Rubel
	 *
	 * @param path path for each IO State file being parsed
	 *
	 */
	private void parseResource(String path) throws IOException, UndeclaredThrowableException, IllegalArgumentException{
		//logger.debug("parseResource : {}")

		LinkedHashMap methods = [:]
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
		String text = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)) .lines().collect(Collectors.joining("\n"));

		if (text == null) {
			throw new IllegalArgumentException("file not found! " + path);
		} else {
			def slurp = new JsonSlurper().parseText(text)
			LinkedHashMap json = toToLinkedHashMap(slurp)


			try{
				parseJson(json.NAME.toString(), json)
				// Store these in cache
			}catch(java.lang.reflect.UndeclaredThrowableException e){
				println("#### [IoStateService - ${json.NAME.toString()}] UndeclaredThrowableException :"+e)
			}
		}

	}

	/**
	 *
	 * method for parsing each IO State file
	 * @author Owen Rubel
	 *
	 * @param path path for each IO State file being parsed
	 *
	 */
	private void parseFiles(String path) throws Exception{
		//logger.debug("parseFiles : {}")
		LinkedHashMap methods = [:]

		try {
			new File(path).eachFile() { file ->
				if(!file.isDirectory()) {
					String fileName = file.name.toString()

					ArrayList tmp = fileName.split('\\.')
					String fileChar1 = fileName.charAt(fileName.length() - 1)


						if (tmp[1] == 'json' && fileChar1 == "n") {

							def slurp = new JsonSlurper().parseText(file.text)

							LinkedHashMap json = toToLinkedHashMap(slurp)

							//JSONObject json = new JSONObject(resultJson)
							//logger.debug("parseFiles : Loading file - {}","${path}/${fileName}")

							try{

								parseJson(json.IOSTATE.NAME.toString(), json.IOSTATE)
							}catch(Exception e){
								throw new Exception("#### [IoStateService - ${json.NAME.toString()}] Exception :",e)
							}
						} else {
							//logger.debug("parseFiles : {}","[Bad File Type ( ${tmp[1]} )] - Ignoring file : ${fileName}")
						}
				}
			}

		}catch(Exception e){
			println('[IoStateService] : No IO State Files found for initialization :'+e)
			throw new Exception('[IoStateService] : No IO State Files found for initialization :',e)
		}
	}

	/**
	 *
	 * method for parsing IO State JSON into a commonly used object
	 * @author Owen Rubel
	 *
	 * @param apiName baseline name for json file
	 * @param json JSON data retrieved from file
	 *
	 */
	void parseJson(String apiName,LinkedHashMap json) throws Exception{
		//logger.debug("parseJson : {}")

		LinkedHashMap methods = [:]

		// DEPRECATED
		// String type = (json['TYPE'])?json['TYPE']:'controller'

		String networkGrp = (json['NETWORKGRP'])?json['NETWORKGRP']:'public'

		json['VERSION'].each(){ k, v ->
			String versKey = k
			LinkedHashMap apiVersion = v
			String defaultAction = (v['DEFAULTACTION'])?v['DEFAULTACTION']:'index'

			Set deprecated = []
			if(v['DEPRECATED']) {
				deprecated = v.DEPRECATED
			}

			String actionname

			try {
				apiVersion['URI'].each() { k2, v2 ->

					LinkedHashMap vals = toToLinkedHashMap(json['VALUES'])
					//def cache = apiCacheService.getApiCache(apiName.toString())
					//def cache = (temp?.get(apiName))?temp?.get(apiName):[:]


					//it.keySet().each(){ key ->
					actionname = k2

					ApiDescriptor apiDescriptor
					//Map apiParams

					LinkedHashMap uriObject = toToLinkedHashMap(v2)


					//String apiMethod = RequestMethod.valueOf(uriObject.METHOD).toString().toUpperCase()
					String apiMethod = RequestMethod.valueOf(uriObject.METHOD).toString()

					ArrayList apiRoles = []
					try{
						if (uriObject.ROLES.containsKey('DEFAULT')) {
							apiRoles = uriObject.ROLES.DEFAULT as List
						}
					}catch(Exception e){ println("### parseJson > apiroles error :"+e)}

					ArrayList networkRoles = []
					try{
						apiProperties.security.networkRoles["${networkGrp}"].each() { k3, v3 ->
							networkRoles.add(v3)
						}
					}catch(Exception e){ println("### parseJson > networkroles error :"+e)}

					if (apiRoles) {
						if (!(apiRoles - networkRoles.intersect(apiRoles).isEmpty())) {
							throw new Exception("[Runtime :: parseJson] : ${uriObject.ROLES.DEFAULT} does not match any networkRoles for ${apiName} NETWORKGRP :", e)
						}
					} else {
						networkRoles.each() { v4 ->
							apiRoles.add(v4)
						}
					}

					Set batchRoles = []
					try {
						if (uriObject.ROLES.containsKey('BATCH')) {
							batchRoles = uriObject.ROLES.BATCH as Set
						}
					}catch(Exception e){ println("### parseJson > batchroles error :"+e)}

					Set hookRoles = []
					try{
						if (uriObject.ROLES.containsKey('HOOK')) {
							hookRoles = uriObject.ROLES.HOOK as Set
						}
					}catch(Exception e){ println("### parseJson > hookroles error :"+e)}


					boolean updateCache = false
					try{
						if (uriObject.UPDATECACHE=="true") {
							updateCache = true
						}
					}catch(Exception e){ println("### parseJson > updateCache error :"+e)}

					LinkedHashMap rateLimit = []
					try{
						if (uriObject.RATELIMIT) {
							uriObject.RATELIMIT.each() { k5, v5 ->
								rateLimit.add(k5,v5)
							}
						}
					}catch(Exception e){ println("### parseJson > rateLimit error :"+e)}

					// TODO
					// add updateCache
					// add rateLimit
					try {
						apiDescriptor = createApiDescriptor(networkGrp, apiName, apiMethod, apiRoles, batchRoles, hookRoles, actionname, vals, apiVersion, updateCache, rateLimit)
					} catch (Exception e) {
						println("unable to create ApiDescriptor. Check your IO State Formatting  : " + e +". Line Number "+e.getStackTrace()[0].getLineNumber())
					}

					if (!methods["${versKey}"]) {
						methods["${versKey}"] = new LinkedHashMap()
					}


					if (!methods['networkGrp']) {
						methods['networkGrp'] = networkGrp
					}

					LinkedHashMap networkGrpRoles = apiProperties.security.networkRoles
					
					if (!methods['networkGrpRoles']) {
						methods['networkGrpRoles'] = []
						ArrayList temp = networkGrpRoles[networkGrp].values()
						methods['networkGrpRoles'] = temp
					}

					if (!methods['values']) {
						methods['values'] = new LinkedHashMap()
						// [cacheversion:1, 1:[deprecated:[], defaultAction:vehiclesByManufacturer, testOrder:[], testUser:null, vehiclesByManufacturer:io.beapi.api.utils.ApiDescriptor@5524b72f], values:{"name":{"type":"String","description":"vehicle name","mockData":"mockTest"},"manufacturer":{"type":"String","description":"manufacturer name","mockData":"mockTest"}}, currentStable:[value:1]]
						methods['values'] = vals
					}

					if (!methods['currentstable']) {
						methods['currentstable'] = new LinkedHashMap()
						methods['currentstable'] = json.CURRENTSTABLE as String
					}

					if (!methods["${versKey}"]['deprecated']) {
						methods["${versKey}"]['deprecated'] = new ArrayList()
						methods["${versKey}"]['deprecated'] = deprecated
					}

					if (!methods["${versKey}"]['defaultAction']) {
						methods["${versKey}"]['defaultAction'] = defaultAction
					}

					if (!methods["${versKey}"]['testOrder']) {
						methods["${versKey}"]['testOrder'] = new ArrayList()
						//methods[vers.key]['testOrder'] = testOrder
					}


					methods["${versKey}"][actionname] = apiDescriptor
					//}

				}
			}catch(Exception e){
				println("#### ParseJson Exception1 : "+e)
			}

			if(methods){
				// TODO : SETUP CACHE
				def cache
				try {
					println("#### Initializing connector cache for '${apiName}'")
					cache = apiCacheService.setApiCache(apiName, methods)
				}catch(Exception e){
					println("#### IoStateService Exception1 : "+e)
					throw new Exception("#### IoStateService Exception1 : ",e)
				}

				if(apiName=='hook'){}
				cache["${versKey}"].each(){ key1,val1 ->
					if(!['deprecated','defaultAction','testOrder'].contains(key1)){
						try {
							def test = apiCacheService.setApiCache(apiName, key1, val1, versKey)
						}catch(Exception e){
							println("#### IoStateService Exception2 : "+e)
						}

						//apiCacheService.setApiCache(apiName,key1, val1, versKey)

					}
				}
			}
		}


		//println("### METHODS : "+methods)



		//return methods
	}

	/**
	 *
	 * constructor for commonly used object from parsed IO State file
	 * @author Owen Rubel
	 *
	 * @param networkGrp
	 * @param apiName
	 * @param apiMethod
	 * @param apiRoles
	 * @param batchRoles
	 * @param hookRoles
	 * @param uri
	 * @param vals
	 * @param json
	 * @param updateCache
	 * @param rateLimit
	 *
	 */
	protected ApiDescriptor createApiDescriptor(String networkGrp, String apiname, String apiMethod, ArrayList apiRoles, LinkedHashSet batchRoles, LinkedHashSet hookRoles, String uri, LinkedHashMap vals, LinkedHashMap json, boolean updateCache, LinkedHashMap rateLimit) throws Exception{
		//logger.debug("createApiDescriptor : {}")
		LinkedHashMap<String, ParamsDescriptor> apiObject = new LinkedHashMap()
		ApiParams param = new ApiParams()

		Set fkeys = []
		Set pkeys= []
		List keys = []

		try{
			vals.each(){k,v ->


				keys.add(k)

				v.reference = (v.reference) ? v.reference : apiname
				param.setParam(v.type, k)
				String hasKey = (v?.key) ? v.key : null

				if (hasKey != null) {

					param.setKey(hasKey)

					String hasReference = (v?.reference) ? v.reference : apiname
					param.setReference(hasReference)


					if (['FKEY', 'INDEX', 'PKEY'].contains(v.key?.toUpperCase())) {
						switch (v.key) {
							case 'INDEX':
								if (v.reference != apiname) {
									LinkedHashMap fkey = ["${k}": "${v.reference}"]
									fkeys.add(fkey)
								}
								break;
							case 'FKEY':
								LinkedHashMap fkey = ["${k}": "${v.reference}"]
								fkeys.add(fkey)
								break;
							case 'PKEY':
								pkeys.add(k)
								break;
						}
					}
				}


				if (v.mockData!=null) {
					if(v.mockData.isEmpty()){
						param.setMockData('')
					}else {
						param.setMockData(v.mockData.toString())
					}
				} else {
					throw new Exception("[Runtime :: createApiDescriptor] : MockData Required for type '" + k + "' in IO State[" + apiname + "]")
				}

				// collect api vars into list to use in apiDescriptor
				apiObject[param.param.name] = param.toObject()
			}

		}catch(Exception e){
			println("[Runtime :: createApiDescriptor] : Issue with creating IO State. Check your Formatting :"+e)
		}

		/*
		[BeAPIFramework] : No IO State Files found for initialization :groovy.lang.MissingMethodException: No signature of method: demo.service.IoStateService$_getIOSet_closure6.doCall()
		is applicable for argument types: (org.json.JSONObject) values: [{"permitAll":[],"ROLE_ADMIN":["id"]}]
		 */

		LinkedHashMap requestObj = json.URI[uri].REQUEST
		LinkedHashMap responseObj =json.URI[uri].RESPONSE

		LinkedHashMap receives = [:]
		LinkedHashMap receivesList = [:]
		LinkedHashMap returns = [:]
		LinkedHashMap returnsList = [:]
		try {
			receives = getIOSet(requestObj, apiObject, keys, apiname)
			receives.each() { k, v ->
				receivesList[k] = v.collect() { it -> it.name }
			}

			returns = getIOSet(responseObj, apiObject, keys, apiname)
			returns.each() { k, v ->
				returnsList[k] = v.collect() { it -> it.name }
			}
		}catch(Exception e){
			throw new Error("#### [IoStateService : getIoSet] : Exception: ",e)
		}

		// [KEYLIST]
		Set keyList = []
		fkeys.each(){ it ->
			it.each(){ k,v ->
				if(k) {
					keyList.add(k)
				}
			}
		}
		if(pkeys) {
			keyList.add(pkeys)
		}

		ApiDescriptor service = new ApiDescriptor(networkGrp, apiMethod, pkeys, fkeys, apiRoles, apiname, receives, receivesList, returns, returnsList, keyList, updateCache, rateLimit)


		// override networkRoles with 'DEFAULT' in IO State

		batchRoles.each {
			if (!apiRoles.contains(it)) {
				throw new Exception("[Runtime :: createApiDescriptor] : BatchRoles in IO State[" + apiname + "] do not match default/networkRoles")
			}
		}
		service['batchRoles'] = batchRoles


		hookRoles.each {
			if (!apiRoles.contains(it)) {
				throw new Exception("[Runtime :: createApiDescriptor] : HookRoles in IO State[" + apiname + "] do not match default/networkRoles")
			}
		}
		service['hookRoles'] = hookRoles

		return service
	}



	/**
	 *
	 * method for getting ABAC values for an endpoint
	 * @author Owen Rubel
	 *
	 * @param io
	 * @param apiObject
	 * @param valuseKeys
	 * @param apiName
	 *
	 */
	private LinkedHashMap getIOSet(LinkedHashMap io, LinkedHashMap apiObject, List valueKeys, String apiName) throws Exception{
		//logger.debug("getIOSet : {}")

		// TODO : APIOBJECT IS ALWAYS EMPTY; NEED TO FIX APIDESCRIPTOR ABOVE

		LinkedHashMap<String,ParamsDescriptor> ioSet = [:]

		io.each(){ k,v ->
			if (!ioSet[k]) {
				ioSet[k] = []
			}

			def roleVars = v.toList()
			roleVars.each(){ val ->
				if (v.contains(val)) {
					if (!ioSet[k].contains(apiObject[val])) {
						if (apiObject[val]) {
							ioSet[k].add(apiObject[val])
						} else {
							throw new Exception("VALUE '" + val + "' is not a valid key for IO State [${apiName}]. Please check that this 'VALUE' exists")
						}
					}
				}
			}
		}

		def permitAll = ioSet['permitAll']

		ioSet.each(){ key, val ->
			if(key!='permitAll'){
				permitAll.each(){ it ->
					if(!ioSet[key].contains("-${it}")) {
						ioSet[key].add(it)
					}
				}
			}
		}


		//List ioKeys = []
		ioSet.each(){ k, v ->
			List ioKeys = v.collect(){ it -> it.name }

			if (!ioKeys.minus(valueKeys).isEmpty()) {
				throw new Exception("[Runtime :: getIOSet] : VALUES for IO State [" + apiName + "] do not match REQUEST/RESPONSE values for endpoints")
			}
		}



		return ioSet
	}

	/**
	 *
	 * convert object to a LinkedHashMap
	 * @author Owen Rubel
	 *
	 * @param obj
	 *
	 */
	def toToLinkedHashMap(def obj) {
		if (obj instanceof org.apache.groovy.json.internal.LazyMap) {
			Map copy = [:];
			for (pair in (obj as Map)) {
				copy.put(pair.key, toToLinkedHashMap(pair.value));
			}
			return copy;
		}
		if (obj instanceof org.apache.groovy.json.internal.ValueList) {
			List copy = [];
			for (item in (obj as List)) {
				copy.add(toToLinkedHashMap(item));
			}
			return copy;
		}
		return obj;
	}


	void ego() {
		println '####################################################################################################'
		println ' '
		println("""                                                                       
     //                                                                                    .@@@     
     //                                                                                     &@&     
     //                                                                                             
     // .//////////          .//////////.         .(@@@@@@@@@, @@/    (@@ ,@@@@@@@@@(       @@&     
     ////         .//.     ,//,         //*     ,@@@         @@@@/    (@@@@         @@@     @@&     
     //             //*   ,//            ///   ,@@             @@/    (@@             @@    @@&     
     //             ,//   //////////////////   %@@             %@/    (@%             @@#   @@&     
     //             ///   ///                   @@             @@/    (@@            ,@@    @@&     
     ////         .///     *//         .///      @@#         ,@@@/    (@@@,        .#@@     @@&     
     //  //////////.         ,///////////          @@@@@@@@@@& @@/    (@@ %@@@@@@@@@@       @@&     
                                                                      (@@                           
                                                                      (@@                           
                                                                      /@@                                                         
""")
		println ' '
		println '####################################################################################################'
		println ' '
	}


	/**
	 *
	 * This function is meant to enforce ALL 'RequestMappingHandlerMapping' endpoints comply with RPCNaming
	 * @author Owen Rubel
	 *
	 * @param applicationContect
	 * @param version
	 *
	 */
	boolean validateRpcNamingConventions(ApplicationContext applicationContext,String version) throws Exception {
		try {
			boolean result = true
			ArrayList reservedControllerNames = ['authenticate', 'register', 'error', 'jwtAuthentication', 'validate','resetPassword']

			RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
			applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods().each() { k, v ->
				LinkedHashMap map1 = [:]
				LinkedHashMap map2 = [:]
				HandlerMethod method = v;
				String temp = method.getMethod().getDeclaringClass().getSimpleName()
				String info = k.toString()
				// logger.info("validateRpcNamingConventions(ApplicationContext, String) : {}",info)

				/*
				* enforce proper requestmapping
				*/
				Pattern pattern = Pattern.compile(/(HEAD|GET|PUT|POST|DELETE|PATCH|TRACE|OPTIONS)\s\[?((\/[a-zA-Z0-9\\/\*]+(,\s)?)+)\]?/)
				Matcher match = pattern.matcher(info)


				if (!reservedControllerNames.contains(info)) {
					try {
						if (match.find()) {
							ArrayList tmpUri = match[0][2].split(", ")
							String lastUri
							tmpUri.each() {
								ArrayList newList = it.split('/')
								//if (!reservedControllerNames.contains(newList[1])) {
								if (map2.size() <= 0) {
									map2[newList[1]] = [:]
								}
								if (map2[newList[1]].size() <= 0) {
									map2[newList[1]][newList[2]] = [:]
								}
								if (map2[newList[1]][newList[2]].size() <= 0) {
									map2[newList[1]][newList[2]][newList[3]] = [:]
									map2[newList[1]][newList[2]][newList[3]]['alias'] = []
									lastUri = newList[3]
								} else {
									map2[newList[1]][newList[2]][lastUri]['alias'].add(newList[3])
								}
								//}
							}
						} else {
							if (info != "{ [/error]}") {
								String msg = "${info} does not match expected requestmapping naming convention of '/*/controller/method**'. Also check your that you have a request method declared for your endpoint and try again."
								throw new Exception("INVALID_RPC_NAMING(1) : " + msg);
								return false
							}
						}
					} catch (Exception e) {
						throw new Exception("INVALID_RPC_NAMING(2) 'reservedControllerNames' does not contain '${info}' : " + e);
					}

					if (!reservedControllerNames.contains(temp)) {

						Pattern pattern2 = Pattern.compile(/(.+)Controller/)
						Matcher match2 = pattern2.matcher(temp)
						if (match2.find()) {
							String controller = match2[0][1].uncapitalize()
							String action = method.getMethod().getName().uncapitalize()
							String uri = "/${controller}/${action}/"
							map1.put("controller", controller); // class name
							map1.put("method", action + "**"); // method name
						}


						if (map2) {
							try {
								if (!map2['*']) {
									result = false
								} else {
									// create mapping in cache for each '/[a|b|c]${version)}-[1-9]/'
									ArrayList entryPoints = ["v${version}", "b${version}", "c${version}"]
									entryPoints.each() { appVersion ->
										String methd = map1['method'].replace('*', '');
										String thisUri = "${appVersion}/${map1['controller']}/${methd}"
										(1..9).each() { apiVersion ->
											thisUri = "${appVersion}-${apiVersion}/${map1['controller']}/${methd}"
										}
									}
								}
							}catch(Exception e){
								throw new Exception("INVALID_RPC_NAMING(3) : " + e);
							}
						}
					}
				}
			}
		}catch(Exception e){
			println("Big ole error : "+e)
		}
	}
}
