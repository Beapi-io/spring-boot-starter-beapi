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
import io.beapi.api.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import io.beapi.api.domain.service.StatService
import io.beapi.api.domain.Stat

/**
 * A class for caching Stats of processed api calls and returning them
 * @author Owen Rubel
 */
// NOTE : CALLTYPE = 4
@Service
//@EnableConfigurationProperties([ApiProperties.class])
class StatsCacheService {

	@Autowired
	private CacheManager cacheManager;


	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TraceCacheService.class);


	public StatsCacheService(CacheManager cacheManager) {
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
	@CacheEvict(value='StatsCache', allEntries = true)
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
			throw new Exception("[StatsCacheService :: flushApiCache] : Error :",e)
		}
	}

	public void flushCache() throws Exception{
		try{
			cacheManager.getCache('Stats').clear()
		}catch(Exception e){
			throw new Exception("[StatsCacheService :: flushCache] : Exception - full stack trace follows:",e)
		}
	}


	@CachePut(value='StatsCache',key="#status")
	LinkedHashMap  setCache(String status, String uri, Long now) throws Exception{
		//println("### [StatsCacheService :: setCache]")
		LinkedHashMap cache = getStatsCache(status)
		// get currentdate
		Date date = new Date(now * 1000);

		if (cache) {

			if(cache["${uri}"]) {
				Date cachedate = new Date(cache["${uri}"][1] * 1000)

				if (cachedate.getDay() != date.getDay()) {
					try {
						// getCachKeys
						ArrayList keys = getCacheKeys()
						keys.each() {
							Stat stat = new Stat()
							stat.setStatusCode(it)
							LinkedHashMap cache2 = getStatsCache(it)
							cache2.each() { k, v ->
								Date cacheDate2 = new Date(v[1] * 1000)

								if (cacheDate2.getDay() != date.getDay()) {
									stat.setDate(v[1]);
									stat.setUrl(k);
									stat.setCount(v[0]);
								}
							}
							//dump to DB

							// foreach cache, insetr the rows for all uris where day of month == date.getDayOfMonth()
						}
					} catch (Exception e) {
						println("[StatsCacheService :: setCache1 ] exception : " + e.getStackTrace())
					}

					// wipe cache where timestamp day != today

					println("bad days")
					// increment
					try {
						if (cache["${uri}"]) {
							cache["${uri}"][0]++
						} else {
							cache["${uri}"] = [1, now]
						}
					} catch (Exception e) {
						println("[StatsCacheService :: setCache2 ] exception : " + e.getStackTrace())
					}
				} else {
					try {
						if (cache["${uri}"]) {
							cache["${uri}"][0]++
						} else {
							cache["${uri}"] = [1, now]
						}
					} catch (Exception e) {
						println("[StatsCacheService :: setCache3 ] exception : " + e.getStackTrace())
					}
				}
			}else{
				cache["${uri}"] = [1,now]
			}
		} else {
			cache["${uri}"] = [1,now]
		}
		return cache
	}


	/**
	 * Method to get the 'ApiCache' cache object
	 * @param String controller name for designated endpoint
	 * @return A LinkedHashMap of Cached data associated with controllername
	 */
	//@Cacheable(value='ApiCache',key="#controllername",sync=false)
	LinkedHashMap getStatsCache(String status) throws Exception{
		//logger.debug("getStatsCache(String) : {}",status)

		if(status!=null) {
			try {
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('StatsCache')?.getNativeCache()
				LinkedHashMap cache = temp?.get(status)?.getObjectValue()
				if(cache){
					return cache
				}
			} catch (Exception e) {
				throw new Exception("[StatsCacheService :: getStatsCache] : no cache found for handler '${status}'. full stack trace follows:", e)
			}
		}else{
			throw new Exception("[StatsCacheService :: getStatsCache] : no cache found for 'NULL_HANDLER' '${status}'.")
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
		net.sf.ehcache.Ehcache temp = cacheManager.getCache('StatsCache').getNativeCache()
		return temp.getKeys()

	}

}
