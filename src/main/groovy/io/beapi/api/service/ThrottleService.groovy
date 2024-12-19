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

import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.SessionService
import io.beapi.api.utils.ApiDescriptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct


@Service
//@EnableConfigurationProperties([ApiProperties.class])
class ThrottleService{


	private ApiProperties apiProperties
	private SessionService sessionService

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ThrottleCacheService.class);


	public ThrottleService(ApiProperties apiProperties, SessionService sessionService) {
		this.apiProperties = apiProperties
		this.sessionService = sessionService
		//this.version = version
	}


	/**
	 * Sets cached variables for user rate limiting / data limiting
	 * ex ['timestamp': currentTime, 'currentRate': 1, 'currentData':contentLength,'locked': false, 'expires': expires]
	 * @param String userId of user being rate limited
	 * @param LinkedHashMap cache of properties used in rate limiting
	 * @return A LinkedHashMap of Cached data associated with userId
	 */

	void initThrottle() throws Exception{
		try {
			Integer throttleCurrentCnt = 1
			Long throttleExpiryTime = (System.currentTimeMillis()) + apiProperties.throttle.staleSession
			sessionService.setAttribute('throttleCurrentCnt', throttleCurrentCnt)
			sessionService.setAttribute('throttleExpiryTime', throttleExpiryTime.toLong())
		}catch(Exception e){
			throw new Exception("[ThrottleService :: initThrottle] : Exception - full stack trace follows:", e)
		}

	}

	/**
	 * increments the rate limit in the cache associated with user id
	 * @param String userId of user being rate limited
	 * @return A LinkedHashMap of Cached data associated with userId
	 */
	void incrementThrottle(String role) throws Exception {
		//try {
			// check to see if it is initialized
			if (sessionService.getAttribute('throttleExpiryTime')) {
				checkExpiry()
				if(!pastLimit(role)){
					// increment count
					int cnt = sessionService.getAttribute('throttleCurrentCnt')
					sessionService.setAttribute('throttleCurrentCnt', cnt+1)
				}else{
					if(sessionService.getAttribute('throttleExpiryTime')<System.currentTimeMillis()){
						Long throttleExpiryTime = (System.currentTimeMillis()) + apiProperties.throttle.staleSession
						int cnt = sessionService.getAttribute('throttleCurrentCnt')
						sessionService.setAttribute('throttleCurrentCnt', cnt+1)
						sessionService.setAttribute('throttleExpiryTime', throttleExpiryTime.toLong())
					}else {
						throw new Exception("[ThrottleService :: pastLimit] : ROLE: [${role}] has exceeded rate limit.")
					}
				}
			} else {
				//check timelimit
				initThrottle()
			}
		//}catch(Exception e){
		//	throw new Exception("[ThrottleService :: incrementThrottle] : Exception - full stack trace follows:", e)
		//}
	}

	protected void checkExpiry() throws Exception {
		try{
			Long now = System.currentTimeMillis() / 1000L
			Long expiry
			expiry = sessionService.getAttribute('throttleExpiryTime').toLong()
			if(now>expiry){
				sessionService.setAttribute('throttleExpiryTime',now + apiProperties.throttle.staleSession)
			}
		}catch(Exception e){
			throw new Exception("[ThrottleService :: checkExpiry] : Exception - full stack trace follows:", e)
		}
	}

	protected boolean pastLimit(String role) throws Exception {
		try{
			int cnt = sessionService.getAttribute('throttleCurrentCnt')
			if(role==apiProperties.getSecurity().getSuperuserRole()){
				return false
			}else {
				if (cnt >= apiProperties.throttle.rateLimit[role]) {
					return true
				}else{
					return false
				}
			}
		}catch(Exception e){
			throw new Exception("[ThrottleService :: checkLimit] : Exception - full stack trace follows:", e)
		}
	}

}
