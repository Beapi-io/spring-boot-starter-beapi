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

import groovyx.gpars.*
import io.beapi.api.utils.ApiDescriptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import io.beapi.api.utils.ApiHook
import io.beapi.api.service.PrincipleService
import com.google.common.hash.Hashing

import static groovyx.gpars.GParsPool.withPool
import javax.servlet.http.HttpServletRequest
import java.nio.charset.StandardCharsets
import java.util.concurrent.BlockingDeque

/**
 * A class for caching processed api calls and returning them
 * @author Owen Rubel
 */
@Service
//@EnableConfigurationProperties([ApiProperties.class])
class HookCacheService {


	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private PrincipleService principle;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(HookCacheService.class);

	//private static ApiCacheService instance;
	//public static ApiCacheService getInstance() { return instance; }

	//@PostConstruct
	//void init() {
	//	instance = this;
	//}

	public HookCacheService(CacheManager cacheManager) {
		this.cacheManager = cacheManager
		//this.version = version
	}

	// admin function
	LinkedHashMap listHook(){
		LinkedHashMap hooks = [:]
		ArrayList hookKeys = getCacheKeys()
		hookKeys.each(){ it ->
			LinkedHashMap cache = getHookCache(it)
			Set users = cache.keySet()
			users.each(){ it2 ->
				if(!hooks[it][it2]){
					hooks[it][it2] = [:]
				}
				hooks[it][it2] = cache["${it2}"]
			}
		}
		return hooks
	}

	// user function
	LinkedHashMap listHookByUser(){
		String ownerEmail = principle.email()
		LinkedHashMap hooks = [:]
		ArrayList hookKeys = getCacheKeys()
		hookKeys.each(){ it ->
			LinkedHashMap cache = getHookCache(it)
			Set users = cache.keySet()
			users.each(){ it2 ->
				if(cache["${ownerEmail}"]){
					if(!hooks[it][it2]){
						hooks[it][it2] = [:]
					}
					hooks[it][it2] = cache["${it2}"]
				}
			}
		}
		return hooks
	}

	// todo : fix. key is 'ownerEmail' now
	// admin function : used when sending
	LinkedHashMap getHookByEndpoint(String internalEndpoint){
		LinkedHashMap hooks = [:]
		LinkedHashMap cache = getHookCache(internalEndpoint)
		Set users = cache.keySet()
		users.each(){ it2 ->
				if(!hooks["${internalEndpoint}"][it2]){
					hooks["${internalEndpoint}"][it2] = [:]
				}
				hooks["${internalEndpoint}"][it2] = cache["${it2}"]
		}
		return hooks
	}



	// todo
	@CachePut(value='HookCache',key="#ownerEmail")
	LinkedHashMap setHook(String ownerEmail, ApiHook hook) throws Exception{
		try{
			ArrayList cache = getHookCache(ownerEmail)

			// todo : make sure hook exists and user has access
			ApiDescriptor apiObject = getApiDescriptor(hook.controller, hook.apiVersion, hook.action)
			if(apiObject){
				hooks.add(it)
			}
			cache.add(hook)
			return cache
		}catch(Exception e){
			throw new Exception("[HookCacheService :: setHookCache] : Exception - full stack trace follows:",e)
		}
	}

	// list of all user hooks
	ArrayList showUserHooks(){
		String email = principle.email()
		ArrayList cache = getHookCache(email)
		return cache
	}

	// admin function
	@CacheEvict(value="HookCache", key="#ownerEmail")
	public void evictSingleCacheValue(String cacheKey) {}
	LinkedHashMap deleteHook(String internalEndpoint){
		String email = principle.email()
		LinkedHashMap cache = getHookCache(email)
		if(cache){
			// get the hook for 'internalEndpoint'
			cache.remove("${ownerEmail}")
		}
		return cache
	}

	// admin function
	@CachePut(value='HookCache',key="#ownerEmail")
	LinkedHashMap lockHook(String internalEndpoint, String ownerEmail){
		ArrayList cache = getHookCache(ownerEmail)
		if(cache){
			// get the hook for 'internalEndpoint'
			hook.setIsActive(0)
			cache["${ownerEmail}"] = hook
		}
		return cache
	}

	// todo
	@CachePut(value='HookCache',key="#ownerEmail")
	LinkedHashMap resetHook(String internalEndpoint, String ownerEmail){
		ArrayList cache = getHookCache(ownerEmail)
		if(cache){
			// get the hook for 'internalEndpoint'
		}
		return cache
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	LinkedHashMap getHookCache(String ownerEmail) throws Exception{
		if(ownerEmail!=null) {
			try {
				//cacheManager.setTransactionAware(false);
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('HookCache')?.getNativeCache()
				// do check; check with put
				LinkedHashMap cache2 = temp.get(ownerEmail)?.getObjectValue()
				return cache2
			} catch (Exception e) {
				throw new Exception("[HookCacheService :: getHookCache] : no hooks found for '${ownerEmail}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[HookCacheService :: getHookCache] : no hooks found for '${ownerEmail}'.")
		}
		return [:]
	}


	/**
	 * Method to load the list of all object contained in the 'HookCache' cache
	 * @return A List of keys of all object names contained with the 'HookCache'
	 */
	ArrayList<String> getCacheKeys(){
		//logger.debug("getCacheKeys() : {}")
		//cacheManager.setTransactionAware(false);
		net.sf.ehcache.Ehcache temp = cacheManager.getCache('HookCache').getNativeCache()
		return temp.getKeys()

	}



	/**
	 * Given the data to be sent and 'service' for which hook is defined,
	 * will send data to all 'subscribers'
	 * @param String URI of local endpoint being hooked into
	 * @param String data to be sent to all subscribers
	 * @return
	 */
	private boolean send(String data, String internalEndpoint) {
		LinkedHashMap hooks = getHookByEndpoint(internalEndpoint)

		/*
		GrailsDomainClass dc = grailsApplication.getDomainClass('net.nosegrind.apiframework.Hook')
		def tempHook = dc.clazz.newInstance()
		def hooks = tempHook.find("from Hook where service=?",[service])
		*/

		withPool(this.cores) { pool ->

			HttpURLConnection myConn = null
			DataOutputStream os = null
			BufferedReader stdInput = null

			hooks[internalEndpoint].eachWithIndexParallel { hook, k ->
				String format = hook.format.toLowerCase()
				if (hook.retryAttempts >= hook.retryLimit) {
					data = [message: 'Number of attempts exceeded. Please reset hook via web interface']
				}

				try {
					URL hostURL = new URL(hook.forwardUrl)
					myConn = (HttpURLConnection) hostURL.openConnection()
					myConn.setRequestMethod("POST")
					myConn.setRequestProperty("Content-Type", "application/json")

					// 	NOTE : NOT REAL AUTHORIZATION : JUST A HASHED SECRET
					if (hook?.secret) {
						String secretHash = Hashing.murmur3_32().hashString(hook.secret, StandardCharsets.UTF_8).toString()
						myConn.setRequestProperty("Authorization", "${secretHash}")
					}

					myConn.setUseCaches(false)
					myConn.setDoInput(true)
					myConn.setDoOutput(true)
					myConn.setReadTimeout(15 * 1000)
					myConn.connect()

					OutputStreamWriter out = new OutputStreamWriter(myConn.getOutputStream())
					out.write(data)
					out.close()

					int code = myConn.getResponseCode()
					myConn.diconnect()

					return code
				} catch (Exception e) {
					try {
						Thread.sleep(15000)
					} catch (InterruptedException ie) {
						println(e)
					}
				} finally {
					if (myConn != null) {
						myConn.disconnect()
					}
				}
				return 400
			}
		}
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	ApiHook getApiHook(String ownerEmail, String controllername, String version, String action){
		//logger.debug("getApiCache(String) : {}",controllername)
		if(ownerEmail!=null && version!=null && controllername!=null && action!=null) {
			net.sf.ehcache.Ehcache temp = cacheManager?.getCache('HookCache')?.getNativeCache()
			ArrayList cache2 = temp.get(ownerEmail)?.getObjectValue()
			cache2.each(){
				if(it.apiVersion==version && it.controller==controllername && it.action==action){
					return it
				}
			}
			return null

		}else{
			return null
		}
		return null
	}

	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	ApiDescriptor getApiDescriptor(String controllername, String version, String action) throws Exception{
		//logger.debug("getApiCache(String) : {}",controllername)
		if(controllername!=null) {
			try {
				//cacheManager.setTransactionAware(false);
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('ApiCache')?.getNativeCache()
				// do check; check with put
				LinkedHashMap cache2 = temp.get(controllername)?.getObjectValue()
				def temp2 = cache2[version]
				ApiDescriptor apiObject = temp2[action]
				return apiObject
			} catch (Exception e) {
				throw new Exception("[ApiCacheService :: getApiCache] : no cache found for handler '${controllername}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[ApiCacheService :: getApiCache] : no cache found for 'NULL_HANDLER' '${controllername}'.")
		}
		return [:]
	}
}

