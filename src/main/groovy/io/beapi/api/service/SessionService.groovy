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
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpServletRequest

@Service
public class SessionService {

	HttpServletRequest request

	public SessionService() {}

	private HttpServletRequest getRequest(){
		RequestAttributes requestAttributes = RCH.getRequestAttributes();
		if (requestAttributes == null) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
	}

	public boolean sessionExists(){
		HttpServletRequest request = getRequest()
		HttpSession session = request.getSession(false);
		if (session != null) {
			return true
		} else {
			return false
		}
	}

	public List listAttributes(){
		HttpServletRequest request = getRequest()
		HttpSession session = request.getSession();
		Enumeration<String> attributes = session.getAttributeNames();
		List atts = []
		while (attributes.hasMoreElements()) {
			String attribute = (String) attributes.nextElement();
			atts.add(attribute);
		}
		return atts
	}

	public void setAttribute(String att,Object val){
		try {
			HttpServletRequest request = getRequest()
			HttpSession session = request.getSession();
			session.setAttribute(att, val)
			session.setMaxInactiveInterval(10*60);
		}catch(Exception e){
			throw new Exception("[SessionService :: seAttribute] : Exception - full stack trace follows:", e)
		}
	}

	public Object getAttribute(String att){
		try{
			HttpServletRequest request = getRequest()
			HttpSession session = request.getSession();
			return session.getAttribute(att)
		}catch(Exception e){
			throw new Exception("[SessionService :: geAttribute] : Exception - full stack trace follows:", e)
		}
	}

	protected String getClientIpAddress() {
		HttpServletRequest request = getRequest()
		String[] IP_HEADER_CANDIDATES = ["X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"];
		for (String header : IP_HEADER_CANDIDATES) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		return request.getRemoteAddr();
	}
}
