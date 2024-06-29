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
import io.beapi.api.domain.service.AuthorityService
import io.beapi.api.properties.ApiProperties

import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;

import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.domain.service.UserAuthorityService;

import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.info.BuildProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.*;
import java.util.stream.StreamSupport;
//import org.springframework.security.crypto.password.PasswordEncoder;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;

import java.security.SecureRandom;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class BootstrapService {

	ApiProperties apiProperties;
	private AuthorityService authService;
	private UserService userService;
	private UserAuthorityService uAuthService;
	private PasswordEncoder passwordEncoder;

	public BootstrapService(ApiProperties apiProperties, AuthorityService authService, UserService userService, UserAuthorityService uAuthService, PasswordEncoder passwordEncoder) {
		this.apiProperties = apiProperties
		this.authService = authService
		this.userService = userService
		this.passwordEncoder = passwordEncoder
		this.uAuthService = uAuthService
		this.passwordEncoder = passwordEncoder
	}

	public void bootstrapAll(){
		bootstrapAuthorities()
		bootstrapSuperUser()
		bootstrapTestUser()
	}

	public void bootstrapAuthorities(){
		ArrayList<String> roles = new ArrayList();
		roles.add(apiProperties.getSecurity().getSuperuserRole());
		roles.add(apiProperties.getSecurity().getTestRole());

		List<Authority> auth = authService.findAll();
		ArrayList<String> authroles = new ArrayList();
		for(Authority it:auth){
			authroles.add(it.getAuthority());
		}

		for(String role:roles){
			if(!authroles.contains(role)){
				Authority newAuth = new Authority();
				newAuth.setAuthority(role);
				authService.save(newAuth);
			}
		}
	}


	public void bootstrapSuperUser(){
		LinkedHashMap superUser = apiProperties.getBootstrap().getSuperUser();
		Authority adminAuth = authService.findByAuthority(apiProperties.getSecurity().getSuperuserRole());
		User sUser = userService.findByEmail(superUser['email']);

		if(null==sUser){ sUser = userService.findByUsername(superUser['login'])}

		if(Objects.nonNull(sUser)){
			// UPDATE SUPERUSER
			sUser.setUsername(superUser.get("login").toString());
			sUser.setEmail(superUser.get("email").toString());
			sUser.setPassword(passwordEncoder.encode(superUser['password']));
			userService.save(sUser);
		}else{
			// CREATE NEW SUPERUSER
			sUser = new User();
			ArrayList<Authority> auths1 = new ArrayList();
			auths1.add(adminAuth);

			sUser.setUsername(superUser.get("login").toString());
			sUser.setEmail(superUser.get("email").toString());
			sUser.setPassword(passwordEncoder.encode(superUser.get("password").toString()));

			// todo : need rollback upon fail
			if(Objects.nonNull(userService.save(sUser))) {
				//auths1.each() {
				for(Authority it : auths1){
					UserAuthority uAuth1 = new UserAuthority();
					uAuth1.setUser(sUser);
					uAuth1.setAuthority(it);
					uAuthService.save(uAuth1);
				}
			}
		}
	}

	public void bootstrapTestUser(){
		Authority testAuth = authService.findByAuthority(apiProperties.getSecurity().getUserRole());
		LinkedHashMap testUser = apiProperties.getBootstrap().getTestUser();
		User tUser = userService.findByEmail(testUser.get("email").toString());
		if(null==tUser){ tUser = userService.findByUsername(testUser.get("login").toString()); }

		if(Objects.nonNull(tUser)){
			// UPDATE TESTUSER
			tUser.setUsername(testUser.get("login").toString());
			tUser.setEmail(testUser.get("email").toString());
			tUser.setPassword(passwordEncoder.encode(testUser.get("password").toString()));
			userService.save(tUser);
		}else{
			// CREATE NEW TESTUSER
			tUser = new User();
			ArrayList<Authority> auths2 = new ArrayList();
			auths2.add(testAuth);

			tUser.setUsername(testUser.get("login").toString());
			tUser.setEmail(testUser.get("email").toString());
			tUser.setPassword(passwordEncoder.encode(testUser.get("password").toString()));

			// todo : need rollback upon fail
			if(Objects.nonNull(userService.save(tUser))){
				//auths2.each() {
				for(Authority it: auths2){
					UserAuthority uAuth = new UserAuthority();
					uAuth.setUser(tUser);
					uAuth.setAuthority(it);
					uAuthService.save(uAuth);
				}
			}
		}
	}

}
