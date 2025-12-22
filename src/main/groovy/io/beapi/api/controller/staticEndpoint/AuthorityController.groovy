package io.beapi.api.controller.staticEndpoint;

import io.beapi.api.controller.BeapiRequestHandler;
import io.beapi.api.domain.Authority;
import io.beapi.api.domain.service.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.List;
import java.util.Objects;


@Controller("authority")
public class AuthorityController extends BeapiRequestHandler {

	@Autowired
	private AuthorityService authService;

	public List<Authority> list(HttpServletRequest request, HttpServletResponse response){
		List<Authority> auth = authService.findAll();
		return auth;
	}

	public Authority create(HttpServletRequest request, HttpServletResponse response){
		println("authority/create called ...")
		String authority = this.params?.get("authority");
		println("auth : "+authority)
		if(authority) {
			Authority auth = authService.findByAuthority(authority);
			if (!Objects.nonNull(auth)) {
				println("auth not found")
				Authority newAuth = new Authority(); ;
				newAuth.setAuthority(authority);
				if(authService.save(newAuth)){
					println("data returned")
					println(newAuth)
					return newAuth
				}
			}
		}
		return null;
	}

/*
	LinkedHashMap update(){
		try{
			User user
			if(isSuperuser() && params?.id){
				user = User.get(params?.id?.toLong())
			}else{
				user = User.get(springSecurityService.principal.id)
			}
			if(user){
				user.username = params.username
				user.password = params.password
				user.email = params.email

				if(isSuperuser()){
					user.enabled = params.enabled
				}

				if(!user.save(flush:true,failOnError:true)){
					user.errors.allErrors.each { println(it) }
				}
				return [person:user]
			}else{
				render(status: 500,text:"Id does not match record in database.")
			}
		}catch(Exception e){
			throw new Exception("[PersonController : update] : Exception - full stack trace follows:",e)
		}
	}
*/


}
