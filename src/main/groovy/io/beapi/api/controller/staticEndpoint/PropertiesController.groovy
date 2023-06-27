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


	LinkedHashMap getAll(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("name",apiProperties.getName());
		temp.put("attempts",apiProperties.getAttempts());
		temp.put("procCores",apiProperties.getProcCores());
		temp.put("documentationUrl",apiProperties.getDocumentationUrl());
		temp.put("views",apiProperties.getViews());
		temp.put("reservedUris",apiProperties.getReservedUris());
		temp.put("publicEndpoint",apiProperties.getPublicEndpoint());
		temp.put("entities",apiProperties.getEntities());
		temp.put("apichainLimit",apiProperties.getApichainLimit());
		temp.put("postcrement",apiProperties.getPostcrement());
		temp.put("chainingEnabled",apiProperties.getChainingEnabled());
		temp.put("batchingEnabled",apiProperties.getBatchingEnabled());
		temp.put("encoding",apiProperties.getEncoding());
		temp.put("iostateDir",apiProperties.getIostateDir());
		temp.put("staticEndpoint",apiProperties.getStaticEndpoint());
		temp.put("supportedFormats",apiProperties.getSupportedFormats());
		temp.put("serverType",apiProperties.getServerType());
		temp.put("protocol",apiProperties.getProtocol());
		temp.put("parseValidRequestParams",apiProperties.getParseValidRequestParams());
		temp.put("autoTest",apiProperties.getAutoTest());

		LinkedHashMap throttle = new LinkedHashMap<>();
		throttle.put("active", apiProperties.getThrottle().getActive());
		throttle.put("rateLimit", apiProperties.getThrottle().getRateLimit());
		throttle.put("dataLimit", apiProperties.getThrottle().getDataLimit());
		throttle.put("expires", apiProperties.getThrottle().getExpires());
		temp.put("throttle", throttle);

		LinkedHashMap hook = new LinkedHashMap<>();
		hook.put("active", apiProperties.getWebhook().getActive());
		hook.put("service", apiProperties.getWebhook().getServices());
		temp.put("webhook", hook);

		LinkedHashMap sec = new LinkedHashMap<>();
		sec.put("superuserRole",apiProperties.getSecurity().getSuperuserRole());
		sec.put("userRole",apiProperties.getSecurity().getUserRole());
		sec.put("testRole",apiProperties.getSecurity().getTestRole());
		sec.put("anonRole",apiProperties.getSecurity().getAnonRole());
		sec.put("networkGroups",apiProperties.getSecurity().getNetworkGroups());
		sec.put("networkRoles",apiProperties.getSecurity().getNetworkRoles());
		sec.put("corsNetworkGroups",apiProperties.getSecurity().getCorsNetworkGroups());
		sec.put("corsIncludeEnvironments",apiProperties.getSecurity().getCorsIncludeEnvironments());
		sec.put("corsExcludeEnvironments",apiProperties.getSecurity().getCorsExcludeEnvironments());
		temp.put("security", sec);

		return temp;
	}

	LinkedHashMap getProperties(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
        temp.put("name",apiProperties.getName());
        temp.put("attempts",apiProperties.getAttempts());
        temp.put("procCores",apiProperties.getProcCores());
        temp.put("documentationUrl",apiProperties.getDocumentationUrl());
        temp.put("views",apiProperties.getViews());
        temp.put("reservedUris",apiProperties.getReservedUris());
        temp.put("publicEndpoint",apiProperties.getPublicEndpoint());
        temp.put("entities",apiProperties.getEntities());
        temp.put("apichainLimit",apiProperties.getApichainLimit());
        temp.put("postcrement",apiProperties.getPostcrement());
        temp.put("chainingEnabled",apiProperties.getChainingEnabled());
        temp.put("batchingEnabled",apiProperties.getBatchingEnabled());
        temp.put("encoding",apiProperties.getEncoding());
        temp.put("iostateDir",apiProperties.getIostateDir());
        temp.put("staticEndpoint",apiProperties.getStaticEndpoint());
        temp.put("supportedFormats",apiProperties.getSupportedFormats());
        temp.put("serverType",apiProperties.getServerType());
        temp.put("protocol",apiProperties.getProtocol());
        temp.put("parseValidRequestParams",apiProperties.getParseValidRequestParams());
        temp.put("autoTest",apiProperties.getAutoTest());
		return temp;
	}


	LinkedHashMap throttleProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("active", apiProperties.getThrottle().getActive());
		temp.put("rateLimit", apiProperties.getThrottle().getRateLimit());
		temp.put("dataLimit", apiProperties.getThrottle().getDataLimit());
		temp.put("expires", apiProperties.getThrottle().getExpires());

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("throttle", temp);
		return map
	}

	// this is what we need to fix
	LinkedHashMap webhookProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		Boolean active = apiProperties.getWebhook().getActive();
		ArrayList<String> services = apiProperties.getWebhook().getServices();
		temp.put("active", active);
		temp.put("service", services);

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("webhook", temp);
		return map
	}

	LinkedHashMap securityProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("superuserRole",apiProperties.getSecurity().getSuperuserRole());
		temp.put("userRole",apiProperties.getSecurity().getUserRole());
		temp.put("testRole",apiProperties.getSecurity().getTestRole());
		temp.put("anonRole",apiProperties.getSecurity().getAnonRole());
		temp.put("networkGroups",apiProperties.getSecurity().getNetworkGroups());
		temp.put("networkRoles",apiProperties.getSecurity().getNetworkRoles());
		temp.put("corsNetworkGroups",apiProperties.getSecurity().getCorsNetworkGroups());
		temp.put("corsIncludeEnvironments",apiProperties.getSecurity().getCorsIncludeEnvironments());
		temp.put("corsExcludeEnvironments",apiProperties.getSecurity().getCorsExcludeEnvironments());

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("security", temp);
		return map
	}
}
