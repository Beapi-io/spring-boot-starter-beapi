package io.beapi.api.utils;

/*
Random Number Generation for 'secret' (used in JWT token and other HASHing)

This allows for each request/response be maintain by SESSION across the architecture (ie load balancer is session based).
Thus when generating a HASH for anything, each generated HASH is specific to THAT session on THAT application
Help to avoid session hijacking

The only way to regenerate SECRET is to restart application.

 */

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SecretGenerator {

	private String secret;

	public SecretGenerator(){
		Random random = ThreadLocalRandom.current();
		byte[] r = new byte[32]; //Means 2048 bit
		random.nextBytes(r);
		secret = Base64.getUrlEncoder().encodeToString(r)
	}

	public String getSecret() {
		return secret;
	}
}