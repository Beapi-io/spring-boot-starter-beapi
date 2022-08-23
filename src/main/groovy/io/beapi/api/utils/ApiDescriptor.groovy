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
import javax.validation.constraints.Size
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 *
 * Api Object used for caching all data associated with endpoint
 * @author Owen Rubel
 *
 * @see ApiCommLayer
 * @see BatchInterceptor
 * @see ChainInterceptor
 *
 */

class ApiDescriptor implements Serializable{

	boolean empty = false
	String defaultAction = ""
	//ArrayList testOrder
	//ArrayList deprecated

	String handler

	//@NotNull
	String networkGrp

	//@NotNull
	//@Pattern(regexp = "GET|POST|PUT|DELETE", flags = Pattern.Flag.CASE_INSENSITIVE)
	String method
	LinkedHashSet pkeys
	LinkedHashSet fkeys
	ArrayList roles
	ArrayList batchRoles
	ArrayList hookRoles

	//@NotNull
	//@Size(max = 200)
	String name

	//@Size(max = 1000)
    String description

	LinkedHashMap doc
    LinkedHashMap<String,ParamsDescriptor> receives
    LinkedHashMap<String,ParamsDescriptor> returns
	LinkedHashMap cachedResult
	//LinkedHashMap stats

	ApiDescriptor(String handler, String networkGrp, String method, LinkedHashSet pkeys, LinkedHashSet fkeys, ArrayList roles,String name, String description, LinkedHashMap receives, LinkedHashMap returns) {
		this.handler = handler
		this.networkGrp = networkGrp
		this.method = method
		this.pkeys=pkeys
		this.fkeys=fkeys
		this.roles=roles
		this.name=name
		this.description=description
		this.receives=receives as LinkedHashMap
		this.returns=returns as LinkedHashMap
	}

	public String getHandler() {
		return this.handler;
	}

	public String getMethod() {
		return this.method;
	}

	public String getNetworkGrp() {
		return this.networkGrp;
	}

	public LinkedHashMap getPkeys() {
		return this.pkeys
	}

	public LinkedHashMap getFkeys() {
		return this.fkeys
	}

	public ArrayList getRoles() {
		return this.roles
	}

	public ArrayList getBatchRoles() {
		return this.batchRoles
	}

	public ArrayList getHookRoles() {
		return this.hookRoles
	}

	public String getName() {
		return this.name;
	}

	public String getDesc() {
		return this.description;
	}

	public LinkedHashMap getDoc() {
		return this.doc
	}

	public LinkedHashMap getReceives() {
		return this.receives
	}

	public LinkedHashMap getReturns() {
		return this.returns
	}

	public LinkedHashMap getCachedResult() {
		return this.cachedResult
	}

	public LinkedHashMap toLinkedHashMap() {
		return [handler:this.handler,networkGrp: this.networkGrp, method: this.method, pkeys: this.pkeys, fkeys: this.fkeys, roles: this.roles, name: this.name, description: this.description, receives: this.receives, returns: this.returns]
	}

}
