package io.beapi.api.controller.staticEndpoint;

import io.beapi.api.domain.Hook;
import io.beapi.api.domain.service.HookService;
import io.beapi.api.domain.User;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.service.PrincipleService;
import io.beapi.api.controller.BeapiRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.service.WebhookService;

@Controller("hook")
public class HookController extends BeapiRequestHandler{

	@Autowired
	ApiProperties apiProperties;

	@Autowired
	WebhookService webhookService;

	@Autowired
	PrincipleService principle;

	@Autowired
	private HookService hookService;

	@Autowired
	private UserService userService;


	public List<Hook> list(HttpServletRequest request, HttpServletResponse response){
		User user = userService.findByUsername(principle.name());
		List<Hook> hooks = new ArrayList();
		if (Objects.nonNull(user)) {
			hooks = hookService.findByUser(user);
		}
		return hooks;
	}

	public Hook show(HttpServletRequest request, HttpServletResponse response){
		//int id = Integer.parseInt(this.params.get("id"));
		Long id = Long.valueOf(this.params.get("id"));
		Optional<Hook> hook = hookService.findById(id);
		if (Objects.nonNull(hook)) {
			return hook.get();
		} else {
			return new Hook();
		}
	}

	public Hook create(HttpServletRequest request, HttpServletResponse response){
		User user = userService.findByUsername(principle.name());

		Hook hook = hookService.findByServiceAndUser(this.params.get("service"),user);

		if (Objects.nonNull(hook)) {
			writeErrorResponse(response, "400", request.getRequestURI(),"URL EXISTS: PLEASE CHECK YOUR REGISTERED WEBHOOKS TO MAKE SURE THIS IS NOT A DUPLICATE.");
		}else {
			//if(!hookService.validateUrl(params.url.toString())){
			//	render(status: 400,text:"BAD PROTOCOL: URL MUST BE FULLY QUALIFIED DOMAIN NAME (OR IP ADDRESS) FORMATTED WITH HTTP/HTTPS. PLEASE TRY AGAIN.")
			//}
			try{
				hook = new Hook();
				hook.setUser(user);
				hook.setService(this.params.get("service"));
				Long now = Instant.now().getEpochSecond();
				hook.setDateCreated(now);
				hook.setLastModified(now);
				hook.setCallback(this.params.get("callback"));
				hook.setAuthorization(this.params.get("authorization"));
				hookService.save(hook);
				return hook;
			}catch(Exception e){
				writeErrorResponse(response, "400", request.getRequestURI(), "INVALID/MALFORMED DATA: PLEASE SEE DOCS FOR THIS ENDPOINT AND PLEASE TRY AGAIN.");
			}
		}
		return new Hook();
	}

	public Hook update(HttpServletRequest request, HttpServletResponse response){
		User user = userService.findByUsername(principle.name());
		Hook hook = hookService.findByServiceAndUser(this.params.get("service"),user);

		if (Objects.nonNull(hook)) {
			//if(!hookService.validateUrl(params.url.toString())){
			//	render(status: 400,text:"BAD PROTOCOL: URL MUST BE FULLY QUALIFIED DOMAIN NAME (OR IP ADDRESS) FORMATTED WITH HTTP/HTTPS. PLEASE TRY AGAIN.")
			//}

			hook.setUser(user);
			hook.setService(this.params.get("service"));
			hook.setDateCreated(Instant.now().getEpochSecond());
			hook.setCallback(this.params.get("callback"));
			hook.setAuthorization(this.params.get("authorization"));

			if (Objects.nonNull(hookService.save(hook))) {
				return hook;
			} else {
				writeErrorResponse(response, "400", request.getRequestURI(), "INVALID/MALFORMED DATA: PLEASE SEE DOCS FOR 'JSON' FORMED STRING AND PLEASE TRY AGAIN.");
			}
		}else {
			writeErrorResponse(response, "400", request.getRequestURI(),"HOOK DOES NOT EXIST: PLEASE CHECK YOUR REGISTERED WEBHOOKS.");
		}
		return new Hook();
	}

	public LinkedHashMap delete(HttpServletRequest request, HttpServletResponse response){

		//int id = Integer.parseInt(this.params.get("id"));
		Long id = Long.valueOf(this.params.get("id"));
		Optional<Hook> hook = hookService.findById(id);
		if (Objects.nonNull(hook)) {
			hookService.deleteById(id);
			LinkedHashMap<String,Long> result = new LinkedHashMap<String, Long>();
			result.put("id",id);
			return result;
		} else {
			writeErrorResponse(response, "400", request.getRequestURI(),"HOOK DOES NOT EXIST: PLEASE CHECK YOUR REGISTERED WEBHOOKS.");
		}

		return new LinkedHashMap();
	}

	/*
	HashMap reset() {
		def user = loggedInUser()

		Hook webhookInstance = Hook.findByIdAndUser(params.id, user)
		if(!webhookInstance){
			writeErrorResponse(response, "400", request.getRequestURI(),"WEBHOOK NOT FOUND: NO WEBHOOK WITH THAT ID FOUND BELONGING TO CURRENT USER.");
		}

		webhookInstance.attempts = 0;

		if (webhookInstance.save(flush: true)) {
			return [hook:[id:params.id]]
		}
	}
	 */

	public List getFormats(HttpServletRequest request, HttpServletResponse response){
		return apiProperties.getSupportedFormats();
	}





	/*
	void postData(String service, String data, List hookRoles,String method) {
		if (hookRoles.size() < 0) {
			String msg = "The hookRoles in your IO State for " + params.controller + "is undefined.";
			sendError(msg, service);
		}else if(method=="GET") {
			String msg = "Webhooks are not applicable with GET method. Please check your IO State file as to what endpoints you are using webhooks with.";
			sendError(msg, service);
		}else{
			send(data, service);
		}
	}
	 */

	public LinkedHashMap getHookServices(HttpServletRequest request, HttpServletResponse response){
		//System.out.println("### Hook/getHookService called...");
		ArrayList newList = webhookService.getHookServices();
		LinkedHashMap<String,ArrayList> output = new LinkedHashMap();
		output.put("hookServices",newList);
		//System.out.println(output);
		return output;
	}

}
