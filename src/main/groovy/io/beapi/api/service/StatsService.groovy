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

import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import java.time.Instant

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class StatsService {


	StatsCacheService statsCacheService

	public StatsService(StatsCacheService statsCacheService) {
		this.statsCacheService = statsCacheService
	}

	private HttpServletRequest getRequest(){
		RequestAttributes requestAttributes = RCH.getRequestAttributes();
		if (requestAttributes == null) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
	}

	public void setStat(String status, String uri){
		try {
			Long timestamp = Instant.now().getEpochSecond();
			statsCacheService.setCache(status,uri,timestamp)
		}catch(Exception e){
			println("[StatsService :: setStat] " +e)
			println(e.getStackTrace())
		}
	}

	public LinkedHashMap getStats(){
		LinkedHashMap output = [:]
		ArrayList keys = statsCacheService.getCacheKeys()
		keys.each(){
			LinkedHashMap cache = statsCacheService.getStatsCache(it)
			output.put(it,cache)
			println(ouput)
		}
		return output
	}

}
