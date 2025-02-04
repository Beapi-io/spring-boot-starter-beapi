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
package io.beapi.api.utils

import java.io.Serializable
//import javax.validation.constraints.Size
//import javax.validation.constraints.NotNull
//import javax.validation.constraints.Pattern

/**
 *
 * Api Object used for caching all IOState data associated with an endpoint
 * @author Owen Rubel
 *
 * @see ApiCommLayer
 * @see BatchInterceptor
 * @see ChainInterceptor
 *
 */
class ApiDescriptor implements Serializable{

	boolean empty = false
	String type


	//@NotNull

	/**
	 * String representing networkGrp this endpoint belongs
	 */
	String networkGrp

	/**
	 * boolean representing whether to cache the response or not
	 */
	boolean updateCache

	/**
	 * LinkedHashMap representing key/values of ROLES and their associated rate limit
	 */
	LinkedHashMap<String,Integer> rateLimit = [:]

	/**
	 * String representing request method (GET|POST|PUT|DELETE)
	 */
	String method

	/**
	 * HashSet representing primary keys
	 */
	HashSet pkeys

	/**
	 * HashSet representing foreign keys
	 */
	HashSet fkeys

	/**
	 * Set representing allKeys
	 */
	Set keyList

	/**
	 * ArrayList representing all ROLES associated with this endpoint
	 */
	ArrayList roles

	/**
	 * ArrayList representing ROLES which can use this endpoint to run batches
	 */
	ArrayList batchRoles

	/**
	 * ArrayList representing ROLES which can use this endpoint as a webhook
	 */
	ArrayList hookRoles


	/**
	 * String representing endpoint name(matches how to call via
	 */
	String name

	/**
	 * LinkedHashMap containing key/value representation of ABAC request data as a ParamDescriptor
	 */
    LinkedHashMap<String,ParamsDescriptor> receives

	/**
	 * Set containing all 'receives' ParamDescriptor keys
	 * @see receives
	 */
	Set receivesKeys

	/**
	 * LinkedHashMap containing key/value representation of ABAC response data as a ParamDescriptor
	 */
    LinkedHashMap<String,ParamsDescriptor> returns

	/**
	 * Set representing all 'returns' ParamDescriptor keys
	 * @see returns
	 */
	Set returnsKeys

	/**
	 * LinkedHashMap representing key/values representing ROLE and its receivesKeys
	 * @see receivesKeys
	 */
	LinkedHashMap<String,ArrayList> receivesList

	/**
	 * LinkedHashMap representing key/values representing ROLE and its returnsKeys
	 * @see returnsKeys
	 */
	LinkedHashMap<String,ArrayList> returnsList

	/**
	 * LinkedHashMap representing the current cached data for this endpoint for each ROLE
	 */
	LinkedHashMap cachedResult
	//LinkedHashMap stats

	/**
	 * ApiDescriptor Constructor
	 * @param networkGrp String representing the networkGrp for this endpoint
	 * @param method String representing request method
	 * @param pkeys HashSet representing primaryKeys
	 * @param fkeys HashSet representing foreignKeys
	 * @param roles ArrayList representing all roles used with this endpoint
	 * @param name String representing name/controller
	 * @param receives LinkedHashMap containing key/value representation of ABAC request data as a ParamDescriptor
	 * @param receivesList LinkedHashMap representing key/values representing ROLE and its receivesKeys
	 * @param returns LinkedHashMap containing key/value representation of ABAC response data as a ParamDescriptor
	 * @param returnsList LinkedHashMap representing key/values representing ROLE and its returnsKeys
	 * @param keyList Set representing allKeys
	 * @param updateCache boolean representing whether to cache the response or not
	 * @param rateLimit LinkedHashMap representing key/values of ROLES and their associated rate limit
	 */
	ApiDescriptor(String networkGrp, String method, HashSet pkeys, HashSet fkeys, ArrayList roles,String name, LinkedHashMap receives, LinkedHashMap receivesList, LinkedHashMap returns, LinkedHashMap returnsList, Set keyList, boolean updateCache, LinkedHashMap<String,String> rateLimit) {
		try {
			this.networkGrp = networkGrp
			this.method = method
			this.pkeys = pkeys
			this.fkeys = fkeys

			this.updateCache = updateCache

			// unlimited is represented as -1; mainly for admins
			// we only use string for the config; using integers in final object
			// is faster than strings
			if(!rateLimit.isEmpty()){
				rateLimit.each(){ k, v ->
					if(v.equals("*")){
						this.rateLimit.add(k, -1)
					}else{
						this.rateLimit.add(k, (Integer)v)
					}
				}
			}


			//this.keyList = pkeys+fkeys
			this.keyList = keyList

			this.roles = roles
			this.name = name

			this.receives = receives as LinkedHashMap
			this.receivesList = receivesList as LinkedHashMap
			this.receivesList.each() { it ->
				if (keyList.contains(it)) {
					receivesKeys.add(it)
				}
			}

			this.returns = returns as LinkedHashMap
			this.returnsList = returnsList as LinkedHashMap
			this.returnsList.each() { it ->
				if (keyList.contains(it)) {
					returnsKeys.add(it)
				}
			}
		}catch(Exception e){
			throw new Exception("[ApiDescriptor :: init] : Exception. full stack trace follows:", e)
		}
	}

	/**
	 * Method getter.
	 * @return String representing the request method
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * NetworkGrp getter.
	 * @return String representing the NetworkGrp
	 */
	public String getNetworkGrp() {
		return this.networkGrp;
	}

	/**
	 * keyList getter.
	 * @return String representing the all DB keys f
	 * @see ParamsDescriptor#keyType
	 */
	public Set getKeyList() {
		return this.keyList
	}

	/**
	 * roles getter.
	 * @return String representing the all Roles for this endpoint
	 */
	public ArrayList getRoles() {
		return this.roles
	}

	/**
	 * batchRoles getter.
	 * @return String representing the all batching Roles for this endpoint
	 */
	public ArrayList getBatchRoles() {
		return this.batchRoles
	}

	/**
	 * hookRoles getter.
	 * @return String representing the all hook Roles for this endpoint
	 */
	public ArrayList getHookRoles() {
		return this.hookRoles
	}

	/**
	 * name getter.
	 * @return String representing the controller for this endpoint
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @deprecated
	 */
	public boolean receivesRoleExists(String role){
		Set keys = this.receives.keySet()
		return keys.contains(role)
	}

	/**
	 * receives getter.
	 * @return LinkedHashMap containing key/value representation of ABAC request data as a ParamDescriptor
	 */
	public LinkedHashMap getReceives() {
		return this.receives
	}

	/**
	 * receivesKeys getter.
	 * @return LinkedHashMap representing key/values representing ROLE and its receivesKeys
	 */
	public Set getReceivesKeys(){
		return this.receivesKeys
	}

	/**
	 * receivesList getter.
	 * @return LinkedHashMap representing key/values representing ROLE and its receivesKeys
	 */
	public LinkedHashMap getReceivesList() {
		return this.receivesList
	}

	/**
	 * returns getter.
	 * @return LinkedHashMap containing key/value representation of ABAC response data as a ParamDescriptor
	 */
	public LinkedHashMap getReturns() {
		return this.returns
	}

	/**
	 * returnsKeys getter.
	 * @return LinkedHashMap representing key/values representing ROLE and its returnsKeys
	 */
	public Set getReturnsKeys(){
		return this.returnsKeys
	}

	/**
	 * returnsList getter.
	 * @return LinkedHashMap representing key/values representing ROLE and its returnsKeys
	 */
	public LinkedHashMap getReturnsList() {
		return this.returnsList
	}

	/**
	 * cachedResult getter
	 * @return LinkedHashMap representing the existing cachedResult for clients ROLE
	 */
	public LinkedHashMap getCachedResult() {
		return this.cachedResult
	}

	/**
	 * LinkedHashMap represention of the instance
	 * @return
	 */
	public LinkedHashMap toLinkedHashMap() {
		return [networkGrp: this.networkGrp, method: this.method, roles: this.roles, name: this.name, receives: this.receives, receivesList: this.receivesList, returns: this.returns, returnsList: this.returnsList]
	}

	/**
	 * updateCache getter.
	 * @return boolean representing whether to cache the response or not
	 */
	public boolean getUpdateCache() {
		return this.updateCache
	}

	/**
	 * rateLimit getter.
	 * @return LinkedHashMap representing key/values of ROLES and their associated rate limit
	 */
	public LinkedHashMap getRateLimit() {
		return this.rateLimit
	}
}
