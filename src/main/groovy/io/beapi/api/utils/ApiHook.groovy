package io.beapi.api.utils

import io.beapi.api.service.ApiCacheService

import java.nio.charset.StandardCharsets
import com.google.common.hash.Hashing
import org.springframework.beans.factory.annotation.Autowired

import java.util.regex.Matcher

/**
 * Class is used for instantiating Webhook object
 *
 * @author Owen Rubel
 * @see HookCacheService
 */
class ApiHook implements Serializable{

	@Autowired
	ApiCacheService apiCacheService
	// todo : create function to check roles for endpoint for EVERY function; (ie hasAuthority() )

	/**
	 * String representing version number of the application
	 */
	private String appVersion

	/**
	 * String representing version for the requested api endpoint
	 */
	private String apiVersion

	/**
	 * String representing controller for the requested api endpoint
	 */
	private String controller

	/**
	 * String representing action for the requested api endpoint
	 */
	private String action

	/**
	 * Supported formats for the requested api endpoint
	 */
	private ArrayList formats = ['JSON','XML']

	/**
	 * String representing forwardURI for Hook
	 */
	private String forwardUrl

	/**
	 * String representing format for Hook
	 */
	private String format = 'JSON'

	/**
	 * String representing secret for Hook
	 */
	private String secretHash

	/**
	 * boolean representing if Hook is active
	 */
	private boolean isActive = 0

	/**
	 * Long representing number of attempts to send made so far
	 */
	private Long retryAttempts = 0

	/**
	 * integer representing number of attempts allowed for sending the hook
	 */
	private int retryLimit = 5

	/**
	 * UriObject class constructor.
	 * @param appVersion
	 * @param apiVersion
	 * @param controller
	 * @param action
	 * @param forwardUrl
	 * @param format
	 * @param secretHash
	 * @return instance of ApiHook
	 */
	ApiHook(String appVersion, String apiVersion, String controller, String action, String forwardUrl, String format, String secretHash) {
		// check hookEndpointExists
		this.appVersion = appVersion
		if(apiVersion) {
			this.apiVersion = apiVersion
		}
		this.controller = controller
		this.action = action
		this.forwardUrl = forwardUrl
		this.format = formats.contains(format)?format:'JSON'
		this.isActive = false
		this.secretHash = secretHash
		this.retryAttempts=0
	}

	/**
	 * Method for returning appVersion associated with Hook
	 * @return  String representing the appVersion for the Hook
	 */
	public Long getAppVersion() {
		return this.appVersion;
	}

	/**
	 * Method for returning apiVersion associated with Hook
	 * @return  String representing the apiVersion for the Hook
	 */
	public Long getApiVersion() {
		return this.apiVersion;
	}

	/**
	 * Method for returning controller associated with Hook
	 * @return  String representing the controller for the Hook
	 */
	public Long getController() {
		return this.controller;
	}

	/**
	 * Method for returning action/method associated with Hook
	 * @return  String representing the action/method for the Hook
	 */
	public Long getAction() {
		return this.action;
	}

	/**
	* for easy comparison of URI against users hooks
	* have to store this way so we can check 'apiVersions'
	 * @param uri String representing URI associated with Hook
	 * @return boolean representing if URI is valid or not
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

	/**
	 * Method for returning URI associated with Hook
	 * @return  String representing the URI for the Hook
	 */
	public String getUri(){
		String version = (this.apiVersion)?"/${this.appVersion}-${this.apiVersion}":"/${this.appVersion}"
		return "/${version}/${this.controller}/${this.action}".toString()
	}

	/**
	 * Method for returning forward URL
	 * @return  String representing the forwardUrl for the Hook
	 */
	public String getForwardUrl() {
		return this.forwardUrl;
	}

	/**
	 * Method for returning format used with webhook
	 * @return  String representing the format for the Hook
	 */
	public String getFormat() {
		return this.format;
	}

	/**
	 * Method for returning secret used with webhook
	 * @return  String representing the secret for the Hook
	 */
	public String getSecretHash() {
		return this.secretHash
	}

	/**
	 * Method for returning whether Hook is active or not
	 * @return  boolean representing whether Hook is active or not
	 */
	public boolean getIsActive() {
		return this.isActive
	}

	/**
	* Method which sets Hook activity(true/false) if given valid 'secret'
	 * @param isActive boolean representing whether Hook is active or not
	 * @param secret String representing the hash for the Hook
	 */
	public void setIsActive(boolean isActive, String secret) {
		String hash = Hashing.murmur3_32().hashBytes(secret.getBytes(StandardCharsets.UTF_8)).toString()
		if(this.secretHash == hash){
			this.isActive=isActive
		}
	}


	/**
	 * Method which resets Hook attempts if given a valid secret
	 * @param secret String representing the hash for the Hook
	 */
	public void resetRetryAttempts(String secret) {
		String hash = Hashing.murmur3_32().hashBytes(secret.getBytes(StandardCharsets.UTF_8)).toString()
		if(this.secretHash == hash){
			this.retryAttempts = 0
		}
	}

	/**
	 * For converting the object to a LinkedHashMap for usability
	 * @return LinkedHashMap representing the APIHook
	 */
	public LinkedHashMap toLinkedHashMap() {
		return [appVersion:this.appVersion, apiVersion:this.apiVersion, controller:this.controller, action:this.action, forwardUrl:this.forwardUrl, formats:this.format, isActive:this.isActive, secretHash:this.secretHash, retryAttempts:this.retryAttempts]
	}





}
