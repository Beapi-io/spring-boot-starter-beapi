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

import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.RequestDispatcher
import org.springframework.web.context.request.RequestAttributes

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class TraceService{


	TraceCacheService traceCacheService

	public TraceService(TraceCacheService traceCacheService) {
		this.traceCacheService = traceCacheService
	}

	private HttpServletRequest getRequest(){
		RequestAttributes requestAttributes = RCH.getRequestAttributes();
		if (requestAttributes == null) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
	}

	public void startTrace(String className, String methodName, String sessionId){
		Long mStart = System.nanoTime()

		LinkedHashMap cache = traceCacheService.getTraceCache(sessionId)
		if(cache == null){
			cache = [:]
			cache.calls = [:]
		}

		String loc = "${className}/${methodName}".toString()

		Long order = (cache['calls'])?cache['calls'].size()+1:1
		cache['calls']["${order}"] = [:]
		cache['calls']["${order}"][loc] = [:]
		cache['calls']["${order}"][loc]['start'] = mStart
		cache['calls']["${order}"][loc]['stop'] = 0

		def temp = traceCacheService.setTraceMethod(sessionId, cache)

		//println('63 : '+uri)
		//LinkedHashMap cache2 = traceCacheService.getTraceCache(uri)
	}


	public LinkedHashMap endTrace(String className, String methodName, String sessionId) {
		LinkedHashMap cache = traceCacheService.getTraceCache(sessionId)

		String loc = "${className}/${methodName}".toString()
		Long order = (cache['calls']?.size())?cache['calls']?.size():0

		cache['calls']["${order}"][loc]['stop'] = System.nanoTime()
		return traceCacheService.setTraceMethod(sessionId, cache)
	}

	public LinkedHashMap endAndReturnTrace(String className, String methodName, String sessionId){

		LinkedHashMap returnCache = endTrace(className, methodName,sessionId)
		LinkedHashMap newTrace = processTrace(returnCache)
		traceCacheService.flushCache()
		return newTrace
	}

	private LinkedHashMap processTrace(LinkedHashMap cache){
		LinkedHashMap newTrace = [:]
		newTrace['elapsedTime'] = 0

		cache['calls'].sort{ a, b -> b.key <=> a.key }
		cache['calls'].each() { it ->
			Long startTime = 0
			Long stopTime = 0
			it.value.each() { it2 ->
				String loc = it2.key

				if(startTime==0){ startTime=it2.value['start'] }
				stopTime = it2.value['stop']

				newTrace[it.key] = ['time': getElapsedTime(it2.value['start'], it2.value['stop']), 'loc': loc]
				if(!['ProfilerInterceptor/before','ProfilerInterceptor/after'].contains(loc)) {
					newTrace['elapsedTime'] += newTrace[it.key]['time']
				}
			}

			//newTrace['elapsedTime'] = getElapsedTime(startTime,stopTime)
		}
		return newTrace
	}

	private Long getElapsedTime(Long startTime, Long stopTime){
		Long elapsedTime = stopTime - startTime
		if(elapsedTime>=0) {
			return elapsedTime
		}else{
			throw new Exception("[TraceService :: getElapsedTime] : Exception - stopTime is less that startTime")
		}
	}
}
