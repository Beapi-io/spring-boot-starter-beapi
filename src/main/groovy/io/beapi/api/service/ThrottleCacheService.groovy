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


@Service
class ThrottleCacheService{

	@Autowired
	private CacheManager cacheManager;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ThrottleCacheService.class);


	public ThrottleCacheService(CacheManager cacheManager) {
		this.cacheManager = cacheManager
		//this.version = version
	}


	/**
	 * Sets cached variables for user rate limiting / data limiting
	 * ex ['timestamp': currentTime, 'currentRate': 1, 'currentData':contentLength,'locked': false, 'expires': expires]
	 * @param String userId of user being rate limited
	 * @param LinkedHashMap cache of properties used in rate limiting
	 * @return A LinkedHashMap of Cached data associated with userId
	 */
	//@org.springframework.cache.annotation.CachePut(value="Throttle",key="#userId")
	@CachePut(value="Throttle",key="#userId")
	LinkedHashMap setThrottleCache(String userId, LinkedHashMap input) throws Exception{
		try{
			def cache = cacheManager.getCache('Throttle');
			cache.put(userId,input);
			return input;
		}catch(Exception e){
			throw new Exception("[ThrottleCacheService :: setThrottleCache] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * increments the rate limit in the cache associated with user id
	 * @param String userId of user being rate limited
	 * @return A LinkedHashMap of Cached data associated with userId
	 */
	//@org.springframework.cache.annotation.CachePut(value="Throttle",key="#userId")
	@CachePut(value="Throttle",key="#userId")
	LinkedHashMap incrementThrottleCache(String userId) throws Exception{
		try{
			def cache = getLimitCache(userId)
			cache['rateLimitCurrent']++

			// send via webhook to subscribing services
			List servers = grailsApplication.config.apitoolkit.apiServer

			return cache
		}catch(Exception e){
			throw new Exception("[ThrottleCacheService :: incrementThrottleCache] : Exception - full stack trace follows:",e)
		}
	}

	/**
	 * returns the rate limit cache associated with given user id
	 * @param String userId of user being rate limited
	 * @return A LinkedHashMap of Cached data associated with userId
	 */
	LinkedHashMap getThrottleCache(String userId) throws Exception{
		if(userId!=null) {
			try{
				net.sf.ehcache.Ehcache temp = cacheManager?.getCache('Throttle')?.getNativeCache()
				LinkedHashMap cache = temp?.get(userId)?.getObjectValue()
				return cache
			}catch(Exception e){
				throw new Exception("[ThrottleCacheService :: getThrottleCache] : Exception - full stack trace follows:",e)
			}
		}else{
			throw new Exception("[ThrottleCacheService :: getThrottleCache] : no cache found for 'NULL_HANDLER' '${userId}'.")
		}
		return [:]
	}

	// do the check locally in service for rate limit
	boolean throttleRate(String userId){

	}

	/*
	// do the check locally in service for data limit
	boolean throttleData(String userId){

	}
	 */
}
