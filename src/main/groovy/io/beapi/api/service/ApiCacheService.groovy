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
import org.springframework.beans.factory.ListableBeanFactory
import groovy.json.JsonOutput
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct


/**
 * A class for caching processed api calls and returning them
 * @author Owen Rubel
 */
@Service
//@EnableConfigurationProperties([ApiProperties.class])
class ApiCacheService{


	@Autowired
	private CacheManager cacheManager;


	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApiCacheService.class);

	//private static ApiCacheService instance;
	//public static ApiCacheService getInstance() { return instance; }

	//@PostConstruct
	//void init() {
	//	instance = this;
	//}

	public ApiCacheService(CacheManager cacheManager) {
		this.cacheManager = cacheManager
		//this.version = version
	}



	/*
	 * Only flush on RESTART.
	 * DO NOT flush while LIVE!!!
	 * Need to lock this down to avoid process calling this.
	 *
	 * Flushes all data from the API Cache; generally only called on startup to create a 'clean' cache
	 * @see BeapiApiFrameworkGrailsPlugin
	 * @return
	 */
	@CacheEvict(value='ApiCache', allEntries = true)
	void flushAllApiCache(){
		try {
/*
            Map<String, Object> controllers = listableBeanFactory.getBeansWithAnnotation(Controller.class);
            controllers.each(){ k,v ->
                Pattern pattern = Pattern.compile(/(.+)Controller/)
                Matcher match = pattern.matcher(k)
                if (match.find()) {
                    String controllername = match[0][1]
                    flushApiCache(controllername)
                }
            }
*/
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: flushApiCache] : Error :",e)
		}


	}

/*
	boolean flushApiCache(String controllername){
		def cache = setApiCache(controllername,[:])
	}
*/


	//@org.springframework.cache.annotation.CachePut(value="ApiCache",key="#controllername")
	/**
	 * Method to set the apicache associated with the controller name
	 * @param String controllername for designated endpoint
	 * @param LinkedHashMap a map of all apidoc information for all roles which can be easily traversed
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap setApiCache(String controllername,LinkedHashMap apidesc){
		//logger.debug("setApiCache(String ,LinkedHashMap) : {}",controllername)
		def cache = cacheManager.getCache('ApiCache');
		cache.put(controllername,apidesc);
		return apidesc;
	}

	/**
	 * Method to set the apicache associated with the controller name using pregenerated ApiDescriptor
	 * @param String controllername for designated endpoint
	 * @param String methodname for designated endpoint
	 * @param ApiDescriptor apidoc for current application
	 * @param String apiversion of current application
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
			cache["${apiversion}"][methodname]['description'] = apidoc.description
			cache["${apiversion}"][methodname]['receives'] = apidoc.receives
			cache["${apiversion}"][methodname]['returns'] = apidoc.returns

			//cache["${apiversion}"][methodname]['stats'] = [] // [[code:200,cnt:56,time:123456789]]

			// can only be generated upon first being called
			cache[apiversion][methodname]['doc'] = generateApiDoc(controllername, methodname,apiversion,cache)
			cache[apiversion][methodname]['doc']['hookRoles'] = cache[apiversion][methodname]['hookRoles']
			cache[apiversion][methodname]['doc']['batchRoles'] = cache[apiversion][methodname]['batchRoles']

			return cache
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: setApiCache] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * Method to set the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	@CachePut(value='ApiCache',key="#controllername")
	boolean setCache(String controllername,LinkedHashMap apidesc){
		//logger.debug("setCache(String, LinkedHashMap) : {}",controllername)
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
	 * @param String hash of all ids sent for given endpoint
	 * @param String controllername for designated endpoint
	 * @param String apiversion of current application
	 * @param String methodname for designated endpoint
	 * @param String authority of user making current request for cache which we are storing
	 * @param String format of cache being stored (ie xml/json)
	 * @param String content of 'response' to be added to endpoint cache
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	// TODO: parse for XML as well
	// todo : turn into object
	@CachePut(value='ApiCache',key="#controllername")
	LinkedHashMap setApiCachedResult(String cacheHash, String controllername, String apiversion, String methodname, String authority, String format, String content){
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
	LinkedHashMap unsetApiCachedResult(String controllername, String actionname, String apiversion){
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
	 * Method to autogenerate the apidoc data set from loaded IO state files
	 * @param String controllername for designated endpoint
	 * @param String actionname for designated endpoint
	 * @param String apiversion of current application
	 * @return A LinkedHashMap of all apidoc information for all roles which can be easily traversed
	 */
	LinkedHashMap generateApiDoc(String controllername, String actionname, String apiversion){
		//logger.debug("generateApiDoc(String, String, String) : {}","${controllername}/${actionname}/v${apiversion}")

		try{
			LinkedHashMap doc = [:]
			LinkedHashMap cache = getApiCache(controllername)

            // TODO : GET CURRENT APP VERSION
			//String apiPrefix = "v${this.version}"

			if(cache){
				String path = "/${apiversion}/${controllername}/${actionname}"
				doc = ['path':path,'method':cache[apiversion][actionname]['method'],'description':cache[apiversion][actionname]['description']]
				if(cache[apiversion][actionname]['receives']){
					doc['receives'] = [:]
					for(receiveVal in cache[apiversion][actionname]['receives']){
						if(receiveVal?.key) {
							doc['receives']["$receiveVal.key"] = receiveVal.value
						}
					}
				}

				if(cache[apiversion][actionname]['pkey']) {
					doc['pkey'] = []
					cache[apiversion][actionname]['pkey'].each(){
							doc['pkey'].add(it)
					}
				}

				if(cache[apiversion][actionname]['fkeys']) {
					doc['fkeys'] = [:]
					for(fkeyVal in cache[apiversion][actionname]['fkeys']){
						if(fkeyVal?.key) {
							doc['fkeys']["$fkeyVal.key"] = JsonOutput.toJson(fkeyVal.value)
						}
					}
				}

				doc['receives'] = [:]
				if(cache[apiversion][actionname]['receives']){
					for(returnVal in cache[apiversion][actionname]['receives']){
						if(returnVal?.key) {
							doc['receives']["$returnVal.key"] = returnVal.value
						}
					}
				}

				doc['returns'] = [:]
				if(cache[apiversion][actionname]['returns']){
					for(returnVal in cache[apiversion][actionname]['returns']){
						if(returnVal?.key) {
							doc['returns']["$returnVal.key"] = returnVal.value
						}
					}
				}

			}

			return doc
		}catch(Exception e){
			throw new Exception("[ApiCacheService :: generateApiDoc] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	LinkedHashMap getApiCache(String controllername){
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
