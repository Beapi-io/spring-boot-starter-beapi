package io.beapi.api.controller


import io.beapi.api.domain.AuthenticationToken;
import io.beapi.api.domain.Authority;
import io.beapi.api.domain.JwtRequest;
import io.beapi.api.domain.JwtResponse;
import io.beapi.api.domain.UserAuthority;
import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.AuthenticationTokenService;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.service.JwtUserDetailsService;
import io.beapi.api.service.MailService
import io.beapi.api.service.PrincipleService;
import io.beapi.api.service.SessionService;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.utils.ErrorCodes;
import io.beapi.api.utils.JwtTokenUtil;
import io.beapi.api.domain.User;
import groovy.json.JsonSlurper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import net.bytebuddy.utility.RandomString;
import groovy.json.JsonSlurper

import java.util.*;
import java.time.Clock;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class JwtAuthenticationController {

	@Autowired ApiProperties apiProperties;
	@Autowired PasswordEncoder passwordEncoder;
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private JwtTokenUtil jwtTokenUtil;
	@Autowired private JwtUserDetailsService userDetailsService;
	@Autowired private SessionService sessionService;
	@Autowired private MailService mailService;
	@Autowired private UserService userService;
	@Autowired private UserAuthorityService uAuthService;
	@Autowired private AuthorityService authService;
	@Autowired private PrincipleService principal
	@Autowired private AuthenticationTokenService authTokenService
	@Autowired private HttpServletRequest request;
	@Autowired private HttpServletResponse response;
	@Autowired ApplicationContext ctx

	//@CrossOrigin
	@RequestMapping(value="/authenticate", consumes="application/json", produces="application/json", method = RequestMethod.POST)
	@Transactional(value="transactionManager",readOnly = true)
	public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		String username = authenticationRequest.getUsername();
		String password = authenticationRequest.getPassword();

		final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		try {
			authenticate(username,password,userDetails);
		} catch (LockedException e){
			// account locked
			//response.setContentType("application/json")
			String message = writeErrorResponse('423', request.getRequestURI())
			return new ResponseEntity<>(message, HttpStatus.LOCKED);
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
		System.out.println("### [JwtAuthenticationController :: register]")
		Authority auth = authService.findByAuthority(apiProperties.getSecurity().getUserRole());
		//User user = User.get(springSecurityService.principal.id)
		//User dupEmail = userService.findByEmail(user.getEmail())
		User dupUser = userService.findByUsername(user.getUsername())
		User dupUser2 = userService.findByEmail(user.getEmail())

		if(!dupUser || dupUser2) {
			String pass = user.getPassword()
			try {
				if (passwordCriteria(user.getPassword())) {
					user.setPassword(passwordEncoder.encode(pass));
					String randomCode = UUID.randomUUID().toString();
					user.setVerificationCode(randomCode);
					user.setAccountLocked(true);

					try {
						User usr = userService.save(user)

						if (usr.getEmail() == user.getEmail()) {
							UserAuthority uAuth = new UserAuthority();
							uAuth.setUser(user);
							uAuth.setAuthority(auth);
							uAuthService.save(uAuth);
						} else {
							return new ResponseEntity<>("User not saved. Please try again. : ", HttpStatus.UNPROCESSABLE_ENTITY);
						}

					} catch (Exception e) {
						return new ResponseEntity<>("Acct with this username/email already exists. Please try again. : " + e, HttpStatus.UNPROCESSABLE_ENTITY);
					}
				} else {
					return new ResponseEntity<>("Password must contain capitol, number and special character and be 8-characters or more. Please try again: " + e, HttpStatus.UNPROCESSABLE_ENTITY);
				}
			}catch(Exception e){
				println(e)
			}
		}else{
			return new ResponseEntity<>("Duplicate user. This username/email already exists. : " + e, HttpStatus.UNPROCESSABLE_ENTITY);
		}

		try{
			mailService.sendVerificationEmail(user,apiProperties.apiServer + apiProperties.callback.getValidation());
			return ResponseEntity.ok("A validation email was sent. Please check your inbox ");
		}catch(Exception e){
			return new ResponseEntity<>("Cannot send registration email : "+e, HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}


	/*
	### [validate user registration email] ###
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.GET)
	public RedirectView getVerification(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) {
		User user = userService.findByVerificationCode(id);
		try {
			if (user == null) {
				return new RedirectView("${apiProperties.getCallback().getValidation()}400");
			} else {
				if (user.getVerificationExpiry() == null) {
					user.setVerificationCode(null);
					user.setAccountLocked(false)
					user.setEmailVerified(true)
					userService.save(user)
					return new RedirectView("${apiProperties.getMail().getValidationCallback()}?200");
				} else {
					Clock clock = Clock.systemUTC();
					long unixTime = Instant.now(clock).getEpochSecond();
					if (user.getVerificationExpiry() < unixTime) {
						user.setVerificationCode(null);
						user.setAccountLocked(false)
						user.setEmailVerified(true)
						userService.save(user)
						return new RedirectView("${apiProperties.getCallback().getValidation()}?200");
					} else {
						// return response as ratelimit failure and do not continue with chain
						String msg = "Verification code has expired. Please request a new one."
						writeErrorResponse(response, '498', request.getRequestURI(), msg);
					}
				}
			}
		}catch(Exception e){
			throw new Exception("[JwtAuthenticationController :: validate] : Exception", e);
		}
	}

	/*
	Need to validate then forward (with id)
	 */
	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public RedirectView resetPassword(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id, @RequestBody String data) {
		//System.out.println("### [JwtAuthenticationController :: resetPassword]")
		def obj = new JsonSlurper().parseText(data)
		User user = userService.findByVerificationCode(id);
		try {
			if (user == null) {
				// DOES NOT MATCH KNOWN USER
				return new RedirectView("${apiProperties.getCallback().getValidation()}?400");
			} else {
				if (obj.password1==obj.password2){
					if(passwordCriteria(user.getPassword())) {
						user.setPassword(passwordEncoder.encode(obj.password1));
						String randomCode = UUID.randomUUID().toString();
						user.setVerificationCode(null);
						user.setAccountLocked(false);
						userService.save(user)
						return ResponseEntity.ok("Password successfully changed. Please login. ")
					}else{
						println("bad cirteria")
						return new ResponseEntity<>("Password must contain capitol, number and special character and be 8-characters or more. Please try again: " + e, HttpStatus.UNPROCESSABLE_ENTITY);
					}
				}else{
					return new ResponseEntity<>("Passwords don't match. Please try again: " + e, HttpStatus.UNPROCESSABLE_ENTITY);
				}
			}
		}catch(Exception e){
			throw new Exception("[JwtAuthenticationController :: validate] : Exception", e);
		}
	}

	// Reset password
	@RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
	public ResponseEntity<?> forgotPassword(HttpServletRequest request, HttpServletResponse response, @RequestBody String data) {
		//System.out.println("### [JwtAuthenticationController :: forgotPassword]")

		def obj = new JsonSlurper().parseText(data)
		User user = userService.findByUsername(obj.email)

		if (user) {
			String randomCode = UUID.randomUUID().toString();
			user.setVerificationCode(randomCode);
			user.setAccountLocked(true);

			if(userService.save(user)) {
				try{
					mailService.sendVerificationEmail(user, apiProperties.getCallback().getForgotPassword());
					return ResponseEntity.ok("A validation email was sent. Please check your inbox ");
				}catch(Exception e){
					return new ResponseEntity<>("Cannot send registration email : "+e, HttpStatus.UNPROCESSABLE_ENTITY);
				}
			}
		}
	}


	@Transactional(value="transactionManager",readOnly = true)
	//private void authenticate(UserDetails userDetails) throws Exception {
	private void authenticate(String username, String password,UserDetails userDetails) throws Exception, DisabledException, BadCredentialsException, LockedException {
		Objects.requireNonNull(userDetails.getUsername());
		Objects.requireNonNull(userDetails.getPassword());

		try {
			Collection grantedAuthorities = userDetails.getAuthorities();
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities));
			String ip = sessionService.getClientIpAddress()
			sessionService.setAttribute("ip",ip)
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		} catch (LockedException e){
			throw new LockedException("Account Locked", e);
		}
	}

	// Todo : Move to exchangeService??
	/**
	 * Standardized error handler for all interceptors; simplifies RESPONSE error handling in interceptors
	 * @param HttpServletResponse response
	 * @param String statusCode
	 * @return LinkedHashMap commonly formatted linkedhashmap
	 */
	protected String writeErrorResponse(String statusCode, String uri){
		String msg = ErrorCodes.codes[statusCode]['long']
		String message = "{\"timestamp\":\"${System.currentTimeMillis()}\",\"status\":\"${statusCode}\",\"error\":\"${ErrorCodes.codes[statusCode]['short']}\",\"message\": \"${msg}\",\"path\":\"${uri}\"}"
		return message
	}

	protected boolean passwordCriteria(password){
		Pattern pass = ~/(?=.*[A-Z])(?=.*[! @# $&*])(?=.*[0-9])(?=.*[a-z]).{8,}/
		Matcher match = pass.matcher(password)
		if (match.find()) {
			return true
		}else{
			return false
		}
	}
}
