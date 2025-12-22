package io.beapi.api.controller


import io.beapi.api.utils.ErrorCodes
import org.springframework.security.authentication.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@RestController
public class GreetingController {
	
	//
	// curl -v -H "Content-Type: application/json" --request GET "http://localhost:8080/hello?name=owen"
	//
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public ResponseEntity<?> hello(@RequestParam("name") String name) {
		return ResponseEntity.ok("hello called "+name);
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


}
