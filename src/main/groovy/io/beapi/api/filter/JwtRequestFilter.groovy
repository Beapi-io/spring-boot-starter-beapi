package io.beapi.api.filter;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.service.UserService
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.JwtUserDetailsService
import io.beapi.api.service.LinkRelationService
import io.beapi.api.utils.JwtTokenUtil;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod

import org.springframework.context.annotation.PropertySource;
import io.beapi.api.properties.ApiProperties;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import io.jsonwebtoken.ExpiredJwtException;
import io.beapi.api.utils.ErrorCodes;
import java.io.*;
import java.util.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.cors.CorsUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory
import org.springframework.web.servlet.HandlerMapping;
import io.beapi.api.service.PrincipleService
import java.util.concurrent.CompletableFuture
import java.util.regex.Matcher
import java.util.regex.Pattern;
import org.springframework.web.util.WebUtils

import javax.servlet.http.Cookie

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
	String markerText = "DEVNOTES";
	Marker devnotes = MarkerFactory.getMarker(markerText);

	@Autowired private UserService userService;
	@Autowired Environment env;
	@Autowired private JwtUserDetailsService userDetails;
	@Autowired private JwtTokenUtil jwtTokenUtil;

	private ApiProperties apiProperties;
	private String version


	public JwtRequestFilter(ApiProperties apiProperties, String version) {
		this.apiProperties = apiProperties
		this.version = version
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		println("### JwtRequestFilter > "+request.getRequestURI())

// TESTING
//println("Header : "+request.getHeader("Content-Type"))
//Enumeration<String> headerEnums = request.getHeaderNames();
//while (headerEnums.hasMoreElements()) {
//	String elementName = headerEnums.nextElement();
//	String elementValue = request.getHeader(elementName);
//	println(elementName+"="+elementValue)
//}

		/*
		* NOTE : If system detects a 'public' api, it will run 'RESPONSE' through filters for CORS handling
		* This is why some endpoints will show filters being used for RESPONSE as well as REQUEST
		* Totally normal.
		 */
		//println("### isCORS? "+CorsUtils.isCorsRequest(request))
		//println("### request method : "+request.getMethod())

		if(CorsUtils.isCorsRequest(request)==true && request.getMethod()=="OPTIONS"){
			chain.doFilter(request, response);
		}else{
			Pattern p = ~/[v|b|c|r]${version}/
			Matcher match = p.matcher(request.getRequestURI()[1..4])
			if (match.find()) {
				String username = null;
				String jwtToken = null;
				String uri = request.getRequestURI();

				final String requestTokenHeader = request.getHeader("Authorization");
				// TODO : make sure they are not logging in/ logging out else will throw logger.warn message
				if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
					jwtToken = requestTokenHeader.substring(7);
					if(jwtToken!='null') {
						try {
							username = jwtTokenUtil.getUsernameFromToken(jwtToken.replaceAll("\\s+", ""));
						} catch (IllegalArgumentException e) {
							println("IllegalArgumentException found " + e);
						} catch (ExpiredJwtException e) {
							println("ExpiredJwtException found " + e);
						} catch (io.jsonwebtoken.SignatureException e) {
							println("ExpiredJwtException found " + e);
							// old token / no token
						}

						//Map tokenHeader = jwtTokenUtil.getHeaders(jwtToken)

						// check session token == cookie token
						if (!validateTokenHeaders(jwtToken, request)) {
							logger.warn("tokenHeaders do not match (possible token hijack or using token at different location/browser)");
							writeErrorResponse(request, response, '403', request.getRequestURI(), "Invalid Token or Token is empty")
						}

						// todo : need to build in a check to make sure the JWT timeout is always HIGHER than session expiry


					}else{
						// no token
					}
				} else {
					logger.warn("JWT Token does not begin with Bearer String");
				}

				// Once we get the token validate it.
				if (!apiProperties.getReservedUris().contains(uri)) {
					if (username != null) {
						UserDetails userDetails = loadUserByUsername(username);

						// if token is valid configure Spring Security to manually set
						// authentication
						if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
							try {
								UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
								usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
								SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

								// check cookie against existing session
								if(request.getSession().getId()==WebUtils.getCookie(request, 'JSESSIONID')?.getValue()) {
									chain.doFilter(request, response)
								}else{
									//println("session does not match: "+WebUtils.getCookie(request, 'JSESSIONID')?.getValue())
									logger.warn("session does not match");
									writeErrorResponse(request, response, '403', request.getRequestURI(), "Invalid session or session cookie not sent")
								}

							} catch (Exception ignored) {
								//ignored.printStackTrace();
							}
							// After setting the Authentication in the context, we specify
							// that the current user is authenticated. So it passes the
							// Spring Security Configurations successfully.
						}

					} else {
						writeErrorResponse(request, response, '403', request.getRequestURI(),"no username/authentication for " + request.getRequestURI())
					}
				}else{
					println(apiProperties.getReservedUris())
					// fix for errorController
					try {
						chain.doFilter(request, response);
					} catch (Exception ignored) {
						//println("ignoring stacktrace")
						//ignored.printStackTrace();
					}
				}

			}else{
				// ### fix for public api
				try {
					chain.doFilter(request, response);
				} catch (Exception ignored) {
					//println("ignoring jwtrequestfilter stacktrace")
					//ignored.printStackTrace();
				}
			}

		}
	}


	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//logger.debug("JwtRequestFilter > loadUserByUsername(String) : {}");

		User user = userService.findByUsername(username);

		if (!Objects.nonNull(user)) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}

		List<Authority> authorities = user.getAuthorities();


		HashSet<SimpleGrantedAuthority> updatedAuthorities = new HashSet();
		//authorities.each(){ auth ->
		for(Authority auth: authorities){
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth.getAuthority());
			//SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth);
			updatedAuthorities.add(authority);
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), updatedAuthorities);
	}

	public boolean validateTokenHeaders(String jwtToken, HttpServletRequest request){
		//println("validateTokenHeaders...")
		String  userAgent =   request.getHeader("User-Agent");
		String  user =   userAgent.toLowerCase();
		String os = jwtTokenUtil.getOs(userAgent);
		String browser = jwtTokenUtil.getBrowser(user, userAgent);
		String ip = jwtTokenUtil.getIp(request)

		Map tokenHeader = jwtTokenUtil.getHeaders(jwtToken)
		if(os!=tokenHeader.os){
			println("BAD MATCH [os] : ${os} != ${tokenHeader.os}")
			return false
		}
		if(browser!=tokenHeader.browser){
			println("BAD MATCH [browser] : ${browser} != ${tokenHeader.browser}")
			return false
		}
		if(ip!=tokenHeader.origin){
			println("BAD MATCH [ip] : ${ip} != ${tokenHeader.origin}")
			return false
		}

		return true;
	}

	// Todo : Move to exchangeService??
	/**
	 * so even though response has 'sendError' which send to 'errorController', we don't want to travel that
	 * far to have to send the 'response'.
	 *
	 * Since we are at the FRONT of the request/response call flow (as we are just entering the securityChain)
	 * we can merely return the 'response' and avoid continuing the chain if an error occurs
	 *
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, String statusCode, String uri){
		response.setContentType("application/json")
		response.setStatus(Integer.valueOf(statusCode));
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${ErrorCodes.codes[statusCode]['long']}\",\"path\":\"${uri}\"}"
		response.getWriter().write(message)
		response.writer.flush()
		//request.getRequestDispatcher("/error").forward(request, response);
	};

	// Todo : Move to exchangeService??
	/**
	 * so even though response has 'sendError' which send to 'errorController', we don't want to travel that
	 * far to have to send the 'response'.
	 *
	 * Since we are at the FRONT of the request/response call flow (as we are just entering the securityChain)
	 * we can merely return the 'response' and avoid continuing the chain if an error occurs
	 *
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, String statusCode, String uri, String msg){
		response.setContentType("application/json")
		response.setStatus(Integer.valueOf(statusCode));
		if (msg.isEmpty()) {
			msg = ErrorCodes.codes[statusCode]['long']
		}
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
		response.getWriter().write(message)
		response.writer.flush()
		//request.getRequestDispatcher("/error").forward(request, response);
	};
}
