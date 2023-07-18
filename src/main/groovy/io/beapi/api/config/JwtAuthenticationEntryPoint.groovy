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

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		if(CorsUtils.isCorsRequest(request)!=true && !request.getMethod().equals("OPTIONS")) {
			//response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			String message = "{\"timestamp\":\"" + System.currentTimeMillis() + "\",\"status\":\"" + HttpServletResponse.SC_UNAUTHORIZED + "\",\"error\":\"Unauthorized Access\",\"message\": \"UNAUTHORIZED ACCESS\",\"path\":\"" + request.getRequestURI() + "\"}";
			response.getWriter().write(message);
		}
	}
}