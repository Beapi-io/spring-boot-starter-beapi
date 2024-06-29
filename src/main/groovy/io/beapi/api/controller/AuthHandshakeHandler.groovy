package io.beapi.api.controller;


import io.beapi.api.service.PrincipleService;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import io.beapi.api.service.PrincipleService;
import org.springframework.security.core.userdetails.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.http.server.ServletServerHttpRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


//@Slf4j
public class AuthHandshakeHandler extends DefaultHandshakeHandler {

	@Autowired
	private PrincipleService principleService;

	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		System.out.println("### AuthHandshakeHandler > determine User ###");

		ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
		HttpServletRequest httpRequest = servletServerHttpRequest.getServletRequest();

		Principal principal = httpRequest.getUserPrincipal();
		if (principal == null) {

			Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
			try {
				SimpleGrantedAuthority authority = new SimpleGrantedAuthority(principleService.authorities());
				authorities.add(authority);
			} catch (Exception e) {
				System.out.println("Exception thrown : " + e);
			}

			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principleService.principle(), null, authorities);
			usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			principal = usernamePasswordAuthenticationToken
		}else{
			println("HAS PRINCIPAL")
		}

		return principal;


	}
}
