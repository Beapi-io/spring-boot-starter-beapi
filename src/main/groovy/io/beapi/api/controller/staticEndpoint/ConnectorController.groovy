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


import io.beapi.api.controller.BeapiRequestHandler
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.IoStateService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.header.*
import org.springframework.stereotype.Controller
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import groovy.json.JsonSlurper
import org.json.JSONObject

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("connector")
public class ConnectorController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ConnectorController.class);

	@Autowired
	ApiCacheService apiCacheService

	@Autowired
	IoStateService iostateService
	String authority

	List update(HttpServletRequest request, HttpServletResponse response) {
		//println("### connector/update ###")
		HashMap model = [:]
		iostateService.parseJson(this.params['IOSTATE']['NAME'],this.params['IOSTATE'])

		model = [NAME:this.params['IOSTATE']['NAME'],VERSION:'0']

		List returnData = [model]
		return returnData

	}

}