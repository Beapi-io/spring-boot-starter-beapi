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

import groovy.json.JsonOutput
import groovyx.gpars.*
import io.beapi.api.properties.ApiProperties
import io.beapi.api.utils.ApiDescriptor
import net.sf.ehcache.Cache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

/**
 * A class for caching processed api calls and returning them
 * @author Owen Rubel
 */
// NOTE : CALLTYPE = 4
@Service
//@EnableConfigurationProperties([ApiProperties.class])
class TraceCacheService {

	@Autowired
	private CacheManager cacheManager;


	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TraceCacheService.class);


	public TraceCacheService(CacheManager cacheManager) {
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
	@CacheEvict(value='Trace', allEntries = true)
	void flushAllApiCache() throws Exception{
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
			throw new Exception("[TraceCacheService :: flushApiCache] : Error :",e)
		}


	}

	public void flushCache() throws Exception{
		try{
			cacheManager.getCache('Trace').clear()
		}catch(Exception e){
			throw new Exception("[TraceCacheService :: getTraceCache] : Exception - full stack trace follows:",e)
		}
	}

/*
	boolean flushApiCache(String controllername){
		def cache = setApiCache(controllername,[:])
	}
*/

	@CachePut(value='Trace',key="#uri")
	LinkedHashMap putTraceCache(String uri) throws Exception{
		try{
			return cache
		}catch(Exception e){
			throw new Exception("[TraceCacheService :: putTraceCache] : Exception - full stack trace follows:",e)
		}
	}


	// issue here
	@CachePut(value='Trace',key="#uri")
	LinkedHashMap setTraceMethod(String uri, LinkedHashMap input) throws Exception{
		try{
			def cache = cacheManager.getCache('Trace');
			cache.put(uri,input);
			return input;
		}catch(Exception e){
			throw new Exception("[TraceCacheService :: setTraceMethod] : Exception - full stack trace follows:",e)
		}
	}


	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	LinkedHashMap getTraceCache(String uri) throws Exception{
		//logger.debug("getTraceCache(String) : {}",uri)

		if(uri!=null) {
			try {
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('Trace')?.getNativeCache()
				LinkedHashMap cache = temp?.get(uri)?.getObjectValue()
				return cache
			} catch (Exception e) {
				throw new Exception("[TraceCacheService :: getTraceCache] : no cache found for handler '${uri}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[TraceCacheService :: getTraceCache] : no cache found for 'NULL_HANDLER' '${uri}'.")
		}
		return [:]
	}

	/**
	 * Method to load the list of all object contained in the 'ApiCache' cache
	 * @return A List of keys of all object names contained with the 'ApiCache'
	 */
	ArrayList getCacheKeys(){
		//logger.debug("getCacheKeys() : {}")
		//cacheManager.setTransactionAware(false);
		net.sf.ehcache.Ehcache temp = cacheManager.getCache('Trace').getNativeCache()
		return temp.getKeys()

	}

}
