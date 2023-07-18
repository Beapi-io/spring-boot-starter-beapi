package io.beapi.api.filter;

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.service.UserService;

import io.beapi.api.utils.JwtTokenUtil;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private UserService userService;

	@Autowired
	Environment env;

	@Autowired
	private ApiProperties apiProperties;


	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
		//System.out.println("### JwtRequestFilter...");

		if(CorsUtils.isCorsRequest(request)==true && request.getMethod().equals("OPTIONS")){
			chain.doFilter(request, response);
		}else{
			final String requestTokenHeader = request.getHeader("Authorization");

			String username = null;
			String jwtToken = null;
			String uri = request.getRequestURI();


			// TODO : make sure they are not logging in/ logging out else will throw logger.warn message
			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
				jwtToken = requestTokenHeader.substring(7);
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken.replaceAll("\\s+", ""));
				} catch (IllegalArgumentException e) {
					System.out.println("Exception found " + e);
				} catch (ExpiredJwtException e) {
					System.out.println("Exception found " + e);
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

							//chain.doFilter(request, response)
						} catch (Exception ignored) {
							//ignored.printStackTrace();
						}
						// After setting the Authentication in the context, we specify
						// that the current user is authenticated. So it passes the
						// Spring Security Configurations successfully.
					}
				} else {
					// no username/authentication for " + request.getRequestURI());

					String statusCode = "401";
					response.setContentType("application/json");
					response.setStatus(Integer.valueOf(statusCode));
					LinkedHashMap code = ErrorCodes.codes.get(statusCode);
					String message = "{\"timestamp\":\""+System.currentTimeMillis()+"\",\"status\":\""+statusCode+"\",\"error\":\""+code.get("short")+"\",\"message\": \""+code.get("long")+"\",\"path\":\""+request.getRequestURI()+"\"}";
					response.getWriter().write(message);
					//response.getWriter().flush();
				}
			}

			// fix for errorController
			try {
				chain.doFilter(request, response);
			} catch (Exception ignored) {
				ignored.printStackTrace();
				String statusCode = "401";
				response.setContentType("application/json");
				response.setStatus(Integer.valueOf(statusCode));
				LinkedHashMap code = ErrorCodes.codes.get(statusCode);
				String message = "{\"timestamp\":\""+System.currentTimeMillis()+"\",\"status\":\""+statusCode+"\",\"error\":\""+code.get("short")+"\",\"message\": \""+code.get("long")+"\",\"path\":\""+request.getRequestURI()+"\"}";
				response.getWriter().write(message);
				//response.getWriter().flush();
			}
		}
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.debug("loadUserByUsername(String) : {}");

		User user = userService.findByUsername(username);

		if (!Objects.nonNull(user)) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}

		List<Authority> authorities = user.getAuthorities();

		// TODO : loop through authorities and assign as simpleGrantedAuth
		HashSet<SimpleGrantedAuthority> updatedAuthorities = new HashSet();
		//authorities.each(){ auth ->
		for(Authority auth: authorities){
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth.getAuthority());
			//SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth);
			updatedAuthorities.add(authority);
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), updatedAuthorities);
	}

}
