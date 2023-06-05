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
import org.springframework.beans.factory.annotation.Autowired
import io.beapi.api.properties.ApiProperties
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("properties")
public class PropertiesController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PropertiesController.class);

	@Autowired
	ApiProperties apiProperties;

	@Autowired
	IoStateService iostateService
	String authority

	/*
	LinkedHashMap getProperties(HttpServletRequest request, HttpServletResponse response) {

		return returnData
	}

	LinkedHashMap getThrottle(HttpServletRequest request, HttpServletResponse response) {

		return returnData
	}

	 */


	// this is what we need to fix
	LinkedHashMap webhookProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		Boolean active = apiProperties.getWebhook().getActive();
		ArrayList<String> services = apiProperties.getWebhook().getServices();
		temp.put("active", active);
		temp.put("service", services);

		LinkedHashMap hook = new LinkedHashMap<>();
		hook.put("webhook", temp);
		return hook;
	}

	/*
	LinkedHashMap getSecurity(HttpServletRequest request, HttpServletResponse response) {

		return returnData
	}

	 */
}
