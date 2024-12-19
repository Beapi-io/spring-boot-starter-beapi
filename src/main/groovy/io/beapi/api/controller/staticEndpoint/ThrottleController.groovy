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
import org.slf4j.LoggerFactory
import org.springframework.security.web.header.*
import org.springframework.stereotype.Controller
import org.springframework.beans.factory.annotation.Autowired
import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
//import io.beapi.api.service.ThrottleService
import javax.servlet.http.HttpSession

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("throttle")
public class ThrottleController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ThrottleController.class);

	//@Autowired
	//ThrottleService throttle

	LinkedHashMap dump(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false)
		println("### session? "+session)
		println(request.getSession().getId())
		println(session.getId())
		//String id = (session==null)?request.getSession().getId():session.getId()

		//throttle.dump()
		return [:]
	}


}
