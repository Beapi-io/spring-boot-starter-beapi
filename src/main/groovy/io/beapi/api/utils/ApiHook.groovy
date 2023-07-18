package io.beapi.api.utils

import io.beapi.api.service.ApiCacheService

import java.nio.charset.StandardCharsets
import com.google.common.hash.Hashing
import org.springframework.beans.factory.annotation.Autowired

import java.util.regex.Matcher

class ApiHook implements Serializable{

	@Autowired
	ApiCacheService apiCacheService
	// todo : create function to check roles for endpoint for EVERY function; (ie hasAuthority() )

	private String appVersion
	private String apiVersion
	private String controller
	private String action
	private ArrayList formats = ['JSON','XML']
	private String forwardUrl
	private String format = 'JSON'
	private String secretHash
	private boolean isActive = 0
	// increment for 404's when sending hook
	private Long retryAttempts = 0
	// retry attempt limit
	private int retryLimit = 5

	ApiHook(String appVersion, String apiVersion, String controller, String action, String forwardUrl, String format, String internalEndpoint, String secretHash) {
		// check hookEndpointExists
		this.appVersion = appVersion
		if(apiVersion) {
			this.apiVersion = apiVersion
		}
		this.controller = controller
		this.action = action
		this.forwardUrl = forwardUrl
		this.format = formats.contains(format)?format:'JSON'
		//this.internalEndpoint = internalEndpoint
		this.isActive = false
		this.secretHash = secretHash
		this.retryAttempts=0
	}

	public Long getAppVersion() {
		return this.appVersion;
	}

	public Long getApiVersion() {
		return this.apiVersion;
	}

	public Long getController() {
		return this.controller;
	}

	public Long getAction() {
		return this.action;
	}

	/*
	* for easy comparison of URI against users hooks
	* have to store this way so we can check 'apiVersions'
	 */
	public boolean compareUri(String uri){
		String tempUri
		if(this.apiVersion) {
			if(uri == "/${this.appVersion}-${this.apiVersion}/${this.controller}/${this.action}".toString()){
				return true
			}
		}else{
			if(uri == "/${this.appVersion}/${this.controller}/${this.action}".toString()){
				return true
			}
			if(uri == "/${this.appVersion}-1/${this.controller}/${this.action}".toString()){
				return true
			}
		}
		return false
	}

	public String getUri(){
		String version = (this.apiVersion)?"/${this.appVersion}-${this.apiVersion}":"/${this.appVersion}"
		return "/${version}/${this.controller}/${this.action}".toString()
	}

	public String getForwardUrl() {
		return this.forwardUrl;
	}

	public String getFormat() {
		return this.format;
	}

	public String getInternalEndpoint() {
		return this.internalEndpoint
	}

	public String getSecretHash() {
		return this.secretHash
	}

	public boolean getIsActive() {
		return this.isActive
	}

	/*
	* must respond via https with secret to compare against secretHash
	* 	HashCode hash = murmur3_32().hashBytes(secret.getBytes(Charsets.UTF_8));
	*	assertEquals(hashUtf8, murmur3_32().newHasher().putBytes(str.getBytes(Charsets.UTF_8)).hash());
	*	assertEquals(hashUtf8, murmur3_32().hashString(str, Charsets.UTF_8));
	*	assertEquals(hashUtf8, murmur3_32().newHasher().putString(str, Charsets.UTF_8).hash());
	 */
	public void setIsActive(boolean isActive, String secret) {
		String hash = Hashing.murmur3_32().hashBytes(secret.getBytes(StandardCharsets.UTF_8)).toString()
		if(this.secretHash == hash){
			this.isActive=isActive
		}
	}

	/*
	* must respond via https with secret to compare against secretHash
	* 	HashCode hash = murmur3_32().hashBytes(secret.getBytes(Charsets.UTF_8));
	*	assertEquals(hashUtf8, murmur3_32().newHasher().putBytes(str.getBytes(Charsets.UTF_8)).hash());
	*	assertEquals(hashUtf8, murmur3_32().hashString(str, Charsets.UTF_8));
	*	assertEquals(hashUtf8, murmur3_32().newHasher().putString(str, Charsets.UTF_8).hash());
	 */
	public void resetRetryAttempts(String secret) {
		String hash = Hashing.murmur3_32().hashBytes(secret.getBytes(StandardCharsets.UTF_8)).toString()
		if(this.secretHash == hash){
			this.retryAttempts = 0
		}
	}

	public LinkedHashMap toLinkedHashMap() {
		return [appVersion:this.appVersion, apiVersion:this.apiVersion, controller:this.controller, action:this.action, forwardUrl:this.forwardUrl, formats:this.format, isActive:this.isActive, secretHash:this.secretHash, retryAttempts:this.retryAttempts]
	}





}
