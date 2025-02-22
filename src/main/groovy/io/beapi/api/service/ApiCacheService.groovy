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


import org.springframework.stereotype.Controller
import org.slf4j.LoggerFactory
import io.beapi.api.properties.ApiProperties
import io.beapi.api.utils.ApiDescriptor
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.CacheManager
import net.sf.ehcache.Cache
import net.sf.ehcache.Ehcache;

import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service;

import groovyx.gpars.*

import groovy.json.JsonOutput
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.ListableBeanFactory
import javax.annotation.PostConstruct
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * A class for caching processed api calls and returning them
 * @author Owen Rubel
 */
@Service
//@EnableConfigurationProperties([ApiProperties.class])
class ApiCacheService{

	//@Autowired
	private CacheManager cacheManager;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApiCacheService.class);

	/**
	 * ApiCache class constructor.
	 * @param  cacheManager cache manager to use for this cache
	 * @return instance of ApiCacheService
	 * @see RequestInitializationFilter#processRequest
	 */
	public ApiCacheService(CacheManager cacheManager) {
		this.cacheManager = cacheManager
		//this.version = version
	}



	/**
	 * Only flush on RESTART.
	 * DO NOT flush while LIVE!!!
	 * Need to lock this down to avoid process calling this.
	 *
	 * Flushes all data from the API Cache; generally only called on startup to create a 'clean' cache
	 * @see BeapiApiFrameworkGrailsPlugin
	 * @return
	 */
	@CacheEvict(value='ApiCache', allEntries=true)
	void flushAllApiCache(){}


	/**
	 * Method to set the apicache associated with the controller name
	 * @param controllername String representing controllername for designated endpoint
	 * @param apidesc LinkedHashMap a map of all apidoc information for all roles which can be easily traversed
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap setApiCache(String controllername,LinkedHashMap apidesc) throws Exception{
		//logger.debug("setApiCache(String ,LinkedHashMap) : {}",controllername)
		def cache = cacheManager.getCache('ApiCache');
		cache.put(controllername,apidesc);
		return apidesc;
	}

	/**
	 * Method to set the apicache associated with the controller name using pregenerated ApiDescriptor
	 * @param controllername String representing controller name for designated endpoint
	 * @param methodname String representing methodname for designated endpoint
	 * @param apidoc Object representing ApiDescriptor for current application
	 * @param apiversion String representing apiversion of current request
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap setApiCache(String controllername, String methodname, ApiDescriptor apidoc, String apiversion){
		//logger.debug("setApiCache(String ,String ,ApiDescriptor ,String) : {}","${controllername}/${methodname}")
		try{
			LinkedHashMap cache = getApiCache(controllername)

			if(!cache["${apiversion}"]){
				cache["${apiversion}"] = new LinkedHashMap()
			}

			if(!cache["${apiversion}"][methodname]){
				cache["${apiversion}"][methodname] = new LinkedHashMap()
			}

			cache["${apiversion}"][methodname]['name'] = apidoc.name
			//cache["${apiversion}"][methodname]['description'] = apidoc.description
			cache["${apiversion}"][methodname]['receives'] = apidoc.receives
			cache["${apiversion}"][methodname]['returns'] = apidoc.returns

			//cache["${apiversion}"][methodname]['stats'] = [] // [[code:200,cnt:56,time:123456789]]

			// can only be generated upon first being called
			//cache[apiversion][methodname]['doc'] = generateApiDoc(controllername, methodname,apiversion,cache)
			//cache[apiversion][methodname]['doc']['hookRoles'] = cache[apiversion][methodname]['hookRoles']
			//cache[apiversion][methodname]['doc']['batchRoles'] = cache[apiversion][methodname]['batchRoles']

			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setApiCache] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * Method to set the 'ApiCache' cache object
	 * @param controllername String representing controller name for designated endpoint
	 * @param apidesc LinkedHashMap representing api object
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	@CachePut(value='ApiCache',key="#controllername")
	boolean setCache(String controllername,LinkedHashMap apidesc) throws Exception{
		//logger.debug("setCache(String, LinkedHashMap) : {}",controllername)

		//if(controllername=='hook'){
		//	println("setCache : ${apidesc}")
		//}

		try{
			net.sf.ehcache.Cache temp = cacheManager.getCache('ApiCache').getNativeCache()
			if(temp.put(controllername,apidesc)){
				return true
			}else{
				return false
			}
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setCache] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * Method to set the cached result associated with endpoint; only works with GET method
	 * and uses HASH of all id's as ID for the cache itself. Also checks authority and format
	 * @param cacheHash String hash of all ids sent for given endpoint
	 * @param controllerName String representing controllername for designated endpoint
	 * @param apiversion String representing  current request version
	 * @param methodname String representing request method
	 * @param authority String representing authority of user making current request for cache which we are storing
	 * @param format String representing format of cache being stored (ie xml/json)
	 * @param content String representing content of 'response' to be added to endpoint cache
	 * @return LinkedHashMap of Cached data associated with controllername
	 */
	// TODO: parse for XML as well
	// todo : turn into object
	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap setApiCachedResult(String cacheHash, String controllername, String apiversion, String methodname, String authority, String format, String content) throws Exception{
		//logger.debug("setApiCachedResults(String, String, String, String, String, String, LinkedHashMap) : {}")
		try {
			LinkedHashMap cachedResult = [:]

			cachedResult[authority] = [:]
			cachedResult[authority][format] =[:]
			cachedResult[authority][format][cacheHash] = content

			LinkedHashMap cache = getApiCache(controllername)
			if (cache?."${apiversion}"?."${methodname}"?.'cachedResult') {
				cachedResult = cache["${apiversion}"][methodname]['cachedResult']
				if(cachedResult[authority]){
					if(cachedResult[authority][format]){
						cache["${apiversion}"][methodname]['cachedResult'][authority][format][cacheHash] = content
					}else{
						cache["${apiversion}"][methodname]['cachedResult'][authority][format] =[:]
						cache["${apiversion}"][methodname]['cachedResult'][authority][format][cacheHash] = content
					}
				}else{
					cache["${apiversion}"][methodname]['cachedResult'][authority] = [:]
					cache["${apiversion}"][methodname]['cachedResult'][authority][format] =[:]
					cache["${apiversion}"][methodname]['cachedResult'][authority][format][cacheHash] = content
				}
			}else{
				cache["${apiversion}"][methodname]['cachedResult'] = [:]
				cache["${apiversion}"][methodname]['cachedResult'][authority] = [:]
				cache["${apiversion}"][methodname]['cachedResult'][authority][format] =[:]
				cache["${apiversion}"][methodname]['cachedResult'][authority][format][cacheHash] = content
			}
			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setApiCachedResults] : Exception - full stack trace follows:",e)
		}
	}

	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap unsetApiCachedResult(String controllername, String actionname, String apiversion) throws Exception{
		//logger.debug("unsetApiCachedResults(String, String, String) : {}")
		try {
			LinkedHashMap cache = getApiCache(controllername)
			if (cache["${apiversion}"]) {
				cache["${apiversion}"][actionname]['cachedResult'] = [:]
			}
			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: unsetApiCachedResults] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param controllername String representing controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	LinkedHashMap getApiCache(String controllername) throws Exception{
		//logger.debug("getApiCache(String) : {}",controllername)
		if(controllername!=null) {
			try {
				//cacheManager.setTransactionAware(false);
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('ApiCache')?.getNativeCache()
				// do check; check with put
				LinkedHashMap cache2 = temp.get(controllername)?.getObjectValue()
				return cache2
			} catch (Exception e) {
				throw new Exception("[ApiCacheService :: getApiCache] : no cache found for handler '${controllername}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[ApiCacheService :: getApiCache] : no cache found for 'NULL_HANDLER' '${controllername}'.")
		}
		return [:]
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param controllername String representing controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	ApiDescriptor getApiDescriptor(String controllername, String version, String action) throws Exception{
		logger.warn("getApiCache(String) : {}",controllername)
		if(controllername!=null) {
			try {
				//cacheManager.setTransactionAware(false);
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('ApiCache')?.getNativeCache()
				// do check; check with put
				LinkedHashMap cache2 = temp.get(controllername)?.getObjectValue()
				def temp2 = cache2[version]
				if(action=='null'){
					String tmpAction = temp2['defaultAction']
					logger.warn("action is null; using 'defaultAction' : "+tmpAction)
					ApiDescriptor apiObject = temp2[tmpAction]
					return apiObject
				}else{
					logger.warn("action is NOT null : "+action)
					ApiDescriptor apiObject = temp2[action]
					return apiObject
				}
			} catch (Exception e) {
				throw new Exception("[ApiCacheService :: getApiCache] : no cache found for handler '${controllername}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[ApiCacheService :: getApiCache] : no cache found for 'NULL_HANDLER' '${controllername}'.")
		}
		return [:]
	}

	/**
	 * Method to load the list of all object contained in the 'ApiCache' cache
	 * @return A List of keys of all object names contained with the 'ApiCache'
	 */
	ArrayList<String> getCacheKeys(){
		//logger.debug("getCacheKeys() : {}")
		//cacheManager.setTransactionAware(false);
		net.sf.ehcache.Ehcache temp = cacheManager.getCache('ApiCache').getNativeCache()
		return temp.getKeys()
	}

}
