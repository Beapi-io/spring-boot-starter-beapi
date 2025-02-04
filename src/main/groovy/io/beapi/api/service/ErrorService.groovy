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

import io.beapi.api.utils.ErrorCodes
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.RequestContextUtils;
import java.lang.reflect.Field
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.beapi.api.service.StatsService
import org.springframework.beans.factory.annotation.Autowired

@Service
public class ErrorService {

	@Autowired
	StatsService statsService

	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	private void writeErrorResponse(HttpServletRequest request,HttpServletResponse response, int statusCode){
		println("errorservice :: writeerrorresponse")
		String uri = request.getRequestURI()
		Locale tmp = RequestContextUtils.getLocale(request);
		String lang = (tmp)?tmp.getLanguage():"en"

		try{
			ArrayList keys = []
			Field[] fields = ErrorCodes.getDeclaredFields();
			for (Field field : fields) {
				if(!['$staticClassInfo', '__$stMC', 'metaClass'].contains(field.getName())){
					keys.add(field.getName());
				}
			}
			lang = (keys.contains(lang))?lang:"en"
		}catch(Exception e){
			println("### [BeapiRequestHandler :: writeErrorResponse1] exception1  : "+e)
		}

		try{
			statsService.setStat((String)statusCode,uri)
		}catch(Exception e){
			println("### [BeapiRequestHandler :: writeErrorResponse1] exception2  : "+e)
		}

		try{
			response.setContentType("application/json")
			response.setStatus(statusCode)
			String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${ErrorCodes."$lang"[statusCode.toString()]['long']}\",\"path\":\"${uri}\"}"
			response.sendError(statusCode,message)
			response.flushBuffer()
		}catch(Exception e){
			println("### [BeapiRequestHandler :: writeErrorResponse1] exception3  : "+e)
		}
	};

	// Todo : Move to exchangeService??
	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, int statusCode, String msg){
		println("errorservice :: writeerrorresponse2")

		String uri = request.getRequestURI()
		Locale tmp = RequestContextUtils.getLocale(request);
		String lang = (tmp)?tmp.getLanguage():"en"

		ArrayList keys = []
		Field[] fields = ErrorCodes.getDeclaredFields();
		for (Field field : fields) {
			if(!['$staticClassInfo', '__$stMC', 'metaClass'].contains(field.getName())){
				keys.add(field.getName());
			}
		}
		lang = (keys.contains(lang))?lang:"en"


		// stat recording
		try{
			statsService.setStat((String)statusCode,uri)
		}catch(Exception e){
			println("### [BeapiRequestHandler :: writeErrorResponse2] exception2 : "+e)
		}


		response.setContentType("application/json")
		response.setStatus(statusCode)
		if(msg.isEmpty()){ msg = ErrorCodes."$lang"[statusCode.toString()]['long'] }
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes."$lang"[statusCode.toString()]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
		response.sendError(statusCode,message)
		response.flushBuffer()

	};

}
