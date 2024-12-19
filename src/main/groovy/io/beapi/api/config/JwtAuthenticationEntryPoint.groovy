package io.beapi.api.config;

import java.io.IOException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

	private static final long serialVersionUID = -7858869558953243875L;

	/* cors check for EVERTHING but 'homepage' which does not have an api...
	* BUT does set session.
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		if(request.getRequestURI()!="/error"){
			if(CorsUtils.isCorsRequest(request)!=true && !request.getMethod().equals("OPTIONS")) {
				println("JwtAuthenticationEntryPoint : #"+request.getRequestURI()+"#")
				//response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
				String message = "{\"timestamp\":\"" + System.currentTimeMillis() + "\",\"status\":\"" + HttpServletResponse.SC_UNAUTHORIZED + "\",\"error\":\"Unauthorized Access\",\"message\": \"UNAUTHORIZED ACCESS\",\"path\":\"" + request.getRequestURI() + "\"}";
				response.getWriter().write(message);
			}
		}
	}
}