package io.beapi.api.controller;

import io.beapi.api.domain.JwtRequest;
import io.beapi.api.domain.JwtResponse;

import io.beapi.api.service.JwtUserDetailsService;
import io.beapi.api.utils.JwtTokenUtil;
import io.beapi.api.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;



import org.springframework.stereotype.Controller;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@RequestMapping(value="/authenticate", consumes="application/json", produces="application/json", method = RequestMethod.POST)
	@Transactional(value="transactionManager",readOnly = true)
	public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		String username = authenticationRequest.getUsername();
		String password = authenticationRequest.getPassword();
		final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		try {
			authenticate(username,password,userDetails);
		} catch (Exception e) {
			// todo : fix, throwing 'INVALID CREDENTIALS' when they are valid(??)
			throw new Exception("Expired Token / Authentication Error", e);
		}
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new JwtResponse(token));
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json")
	@Transactional(value="transactionManager",readOnly = true)
	public ResponseEntity<User> saveUser(@RequestBody User user) throws Exception {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User newuser = userDetailsService.save(user);
		return ResponseEntity.ok(newuser);
	}

	@Transactional(value="transactionManager",readOnly = true)
	//private void authenticate(UserDetails userDetails) throws Exception {
	private void authenticate(String username, String password,UserDetails userDetails) throws Exception {
		Objects.requireNonNull(userDetails.getUsername());
		Objects.requireNonNull(userDetails.getPassword());

		try {
			Collection grantedAuthorities = userDetails.getAuthorities();
			//authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorities))
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}
