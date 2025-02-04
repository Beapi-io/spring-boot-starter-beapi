package io.beapi.api.utils;


import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
* Random Number Generation for 'secret' (used in JWT token and other HASHing)
*
* This allows for each request/response be maintain by SESSION across the architecture (ie load balancer is session based).
* Thus when generating a HASH for anything, each generated HASH is specific to THAT session on THAT application
* Help to avoid session hijacking
*
* The only way to regenerate SECRET is to restart application.
*
* @author Owen Rubel
*/
public class SecretGenerator {

	/**
	 * String holder for generated hash
	 */
	private String secret;

	/**
	* SecretGenerator class constructor.
	* @return instance of SecretGenerator
	* @see RequestInitializationFilter#processRequest
	*/
	public SecretGenerator(){
		Random random = ThreadLocalRandom.current();
		byte[] r = new byte[32]; //Means 2048 bit
		random.nextBytes(r);
		secret = Base64.getUrlEncoder().encodeToString(r)
	}

	/**
	* secret getter
	* @return String representing the generated hash
	*/
	public String getSecret() {
		return secret;
	}
}