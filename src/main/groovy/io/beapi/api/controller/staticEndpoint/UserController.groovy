package io.beapi.api.controller.staticEndpoint;

import io.beapi.api.controller.BeapiRequestHandler;
import io.beapi.api.domain.Authority;
import io.beapi.api.domain.User;
import io.beapi.api.domain.UserAuthority;
import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.service.PrincipleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

@Controller("user")
public class UserController extends BeapiRequestHandler {

	@Autowired
	protected  PasswordEncoder passwordEncoder;

	@Autowired
	protected PrincipleService principle;

	@Autowired
	protected UserService userService;

	@Autowired
	private AuthorityService authService;

	@Autowired
	private UserAuthorityService uAuthService;



	public List<User> list(HttpServletRequest request, HttpServletResponse response){
			List<User> users = userService.getAllUsers();
			return users;
	}

	public User show(HttpServletRequest request, HttpServletResponse response){
			String username;
			if(principle.isSuperuser()){
				username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
			}else {
				username = principle.name();
			}

			User user = userService.findByUsername(username);

			if (Objects.nonNull(user)) {
				return user;
			}
			return null;
    }

	// admin can pass a role else defaults to 'ROLE_USER
	public User create(HttpServletRequest request, HttpServletResponse response){

			String role = this.params.get("role");
			Authority auth = authService.findByAuthority(role);

			User user = new User();
			user.setUsername(this.params.get("login"));
			user.setEmail(this.params.get("email"));
			user.setPassword(passwordEncoder.encode(this.params.get("password")));

			// todo : need rollback upon fail
			if(Objects.nonNull(userService.save(user))){
				UserAuthority uAuth = new UserAuthority();
				uAuth.setUser(user);
				uAuth.setAuthority(auth);
				uAuthService.save(uAuth);
			}
			return user;

	}



	public User updatePassword(HttpServletRequest request, HttpServletResponse response){

			String username;
			if(principle.isSuperuser()){
				username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
			}else {
				username = principle.name();
			}
			User user = userService.findByUsername(username);

			if (Objects.nonNull(user)) {
				user.setPassword(passwordEncoder.encode(this.params.get("password")));
				userService.save(user);
			}else{
				writeErrorResponse(response, "404", request.getRequestURI());
			}

			return user;
	}



	public User getByUsername(HttpServletRequest request, HttpServletResponse response){
		String username;
		if(principle.isSuperuser()){
			username = (Objects.nonNull(this.params.get("id")))? (this.params.get("id")):principle.name();
		}else {
			username = principle.name();
		}

		User user = userService.findByUsername(username);
		if (!Objects.nonNull(user)) {
			writeErrorResponse(response, "404", request.getRequestURI());
		}
		return user;
	}

	/*
	LinkedHashMap delete() {
		User user
		List prole
		try {
			user = User.get(params.id)
			if(user){
					prole = PersonRole.findAllByPerson(user)
					prole.each() {
						it.delete(flush: true, failOnError: true)
					}


					 // additional dependencies to be removed should be put here


					user.delete(flush: true, failOnError: true)
					return [person: [id: params.id.toLong()]]
			}else{
				render(status: 500,text:"Id " + params.id + " does not match record in database.")
			}
		}catch(Exception e){
			throw new Exception("[PersonController : delete] : Exception - full stack trace follows:",e)
		}
	}
*/

}
