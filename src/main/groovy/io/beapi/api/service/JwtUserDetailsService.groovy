package io.beapi.api.service;

import io.beapi.api.domain.Authority
import io.beapi.api.domain.UserAuthority
import io.beapi.api.domain.service.UserAuthorityService
import io.beapi.api.repositories.UserAuthorityRepository;
import io.beapi.api.repositories.UserRepository;
import io.beapi.api.domain.User;
import io.beapi.api.domain.service.UserService;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.web.context.annotation.RequestScope;
import java.util.*;

import java.util.concurrent.CompletableFuture;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private UserService userService;

	@Autowired
	UserAuthorityService userauthService;

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);


	@RequestScope
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//logger.debug("loadUserByUsername(String) : {}");

		User user = userService.findByUsername(username);

		if (!Objects.nonNull(user)) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}

		List<Authority> authorities = user.getAuthorities();

		// TODO : loop through authorities and assign as simpleGrantedAuth
		HashSet<SimpleGrantedAuthority> updatedAuthorities = new HashSet();
		//authorities.each(){ auth ->
		for(Authority auth:authorities){
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth.getAuthority());
			//SimpleGrantedAuthority authority = new SimpleGrantedAuthority(auth);
			updatedAuthorities.add(authority);
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), updatedAuthorities);
	}

	//@Override
	public User save(User usr) {
		// TODO Auto-generated method stub
		//return userrepo.save(usr);

		try {
			userService.save(usr);
			return usr;
		}catch(Exception e){
			println(e)
		}
	}

	public UserAuthority save(UserAuthority userauth) {
		// TODO Auto-generated method stub
		return userauthService.save(userauth);
	}
}