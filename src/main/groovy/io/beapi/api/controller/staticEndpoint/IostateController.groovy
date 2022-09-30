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
package io.beapi.api.controller.staticEndpoint


import groovyx.gpars.*
import io.beapi.api.controller.BeapiRequestHandler
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import io.beapi.api.service.IoStateService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.header.*
import org.springframework.stereotype.Controller
import org.json.JSONObject
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("iostate")
public class IostateController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IostateController.class);

	@Autowired
	ApiCacheService apiCacheService


	@Autowired
	IoStateService iostateService

	String authority


	List update(HttpServletRequest request, HttpServletResponse response) {
		println("### iostate/update ###")
		HashMap model = [:]

		iostateService.parseJson(this.params['NAME'],this.params)

		def cache = apiCacheService.getApiCache(this.params['NAME'])

		cache.keySet()[0]

		model = [NAME:this.params['NAME'],VERSION:cache.keySet()[0]]
		//webhookService.postData('Iostate', model,'update')

		List returnData = [model]
		
		return returnData

	}

}
