package io.beapi.api.controller

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.JwtRequest;
import io.beapi.api.domain.JwtResponse
import io.beapi.api.domain.UserAuthority
import io.beapi.api.domain.service.AuthorityService
import io.beapi.api.domain.service.UserAuthorityService
import io.beapi.api.domain.service.UserService;
import io.beapi.api.service.JwtUserDetailsService
//import io.beapi.api.service.MailService
import io.beapi.api.utils.ErrorCodes;
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
import org.springframework.transaction.annotation.Transactional
import  org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import io.beapi.api.properties.ApiProperties;
import org.springframework.web.context.request.RequestContextHolder;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.beapi.api.utils.ErrorCodes
import org.springframework.web.context.request.ServletRequestAttributes;
import net.bytebuddy.utility.RandomString;

@RestController
@CrossOrigin
public class JwtAuthenticationController {

	@Autowired ApiProperties apiProperties;
	@Autowired PasswordEncoder passwordEncoder;
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private JwtTokenUtil jwtTokenUtil;
	@Autowired private JwtUserDetailsService userDetailsService;
	// @Autowired private MailService mailService;
	@Autowired private UserService userService;
	@Autowired private UserAuthorityService uAuthService;
	@Autowired private AuthorityService authService;
	@Autowired private HttpServletRequest request;
	//@Autowired private HttpServletResponse response;


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

		String  userAgent =   request.getHeader("User-Agent");
		String  user =   userAgent.toLowerCase();
		String os = jwtTokenUtil.getOs(userAgent);
		String browser = jwtTokenUtil.getBrowser(user, userAgent);
		String ip = jwtTokenUtil.getIp(request)

		final String token = jwtTokenUtil.generateToken(userDetails, os, browser, ip);
		return ResponseEntity.ok(new JwtResponse(token));
	}


	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json")
	//@Transactional(value="transactionManager")
	public ResponseEntity<User> saveUser(@RequestBody User user) throws Exception {

		Authority auth = authService.findByAuthority(apiProperties.getSecurity().getUserRole());

		//User dupEmail = userService.findByEmail(user.getEmail())
		//User dupUser = userService.findByUsername(user.getUsername())

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		String randomCode = RandomString.make(64);
		user.setRegistrationVerificationCode(randomCode);

		try {
			if (user && auth) {
				if (userService.save(user)) {
					UserAuthority uAuth = new UserAuthority();
					uAuth.setUser(user);
					uAuth.setAuthority(auth);
					uAuthService.save(uAuth);
				}

				//mailService.sendVerificationEmail(user);

				return ResponseEntity.ok(user);
			}
		}catch(Exception e){
			return new ResponseEntity<>("Acct with this username/email already exists. Please try again. : "+e, HttpStatus.UNPROCESSABLE_ENTITY);
		}

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

	// Todo : Move to exchangeService??
	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	protected void writeErrorResponse(HttpServletResponse response, String statusCode, String uri){
		response.setContentType("application/json")
		response.setStatus(Integer.valueOf(statusCode))
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${ErrorCodes.codes[statusCode]['long']}\",\"path\":\"${uri}\"}"
		response.getWriter().write(message)
		response.writer.flush()
	}

	// Todo : Move to exchangeService??
	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	protected void writeErrorResponse(HttpServletResponse response, String statusCode, String uri, String msg){
		response.setContentType("application/json")
		response.setStatus(Integer.valueOf(statusCode))
		if(msg.isEmpty()){
			msg = ErrorCodes.codes[statusCode]['long']
		}
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
		response.getWriter().write(message)
		response.writer.flush()
	}
}
