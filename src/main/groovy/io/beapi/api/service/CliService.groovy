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
package io.beapi.api.service

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.ServletRequestAttributes
import static groovyx.gpars.GParsPool.withPool
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class CliService {

	@Value("\${sun.java.command}")
	private List<String> argsList;

	ApplicationContext ctx

	//Integer cores = Holders.grailsApplication.config.apitoolkit.procCores as Integer

	public CliService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}


	static transactional = false
	
	//void parse() {
	//	println(argsList)
	//}

	void parse() {
		ArrayList args = argsList.split(' ')
		args.remove(0)
		ArrayList validArgKeys = ['controller','connector','domain']
		ArrayList scaffoldKeys = ['controller','connector']
		ArrayList domainKey = ['domain']
		LinkedHashMap vars = [:]
		args.each(){
			ArrayList temp = it.split('=')
			if(validArgKeys.contains(temp[0].toLowerCase())){
				if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]/) {
					switch(temp[0].toLowerCase()){
						case 'controller':
							if(controllerArg!=null){
								error(1, "'controller' value has already been set. Please try again.")
							}else{
								controllerArg = temp[1]
							}
							break
						case 'connector':
							if(connectorArg!=null){
								error(1, "'connector' value has already been set. Please try again.")
							}else{
								connectorArg = temp[1]
							}
							break
						case 'domain':
							if(domainArg!=null){
								error(1, "'domain' value has already been set. Please try again.")
							}else{
								domainArg = temp[1]
							}
							break
						default:
							error(1, "Unrecognized arg. Please try again.")
					}
				}else{
					error(1, "Invalid package name. Package name for '"+temp[0]+"' is not recognized as a valid package name")
				}
			}else{
				error(1, "Invalid ARG sent. Please provide ARG values of 'controller/connector' and 'domain'.")
			}
		}

		if(domainArg==null){
			error(1, "Missing valid domain value sent. Please try again.")
		}

		if(controllerArg==null && connectorArg==null){
			error(1, "Missing valid scaffold value sent (ie controller/connector). Please try again.")
		}
		println("domain : "+domainArg)
		println("controller : "+controllerArg)
		println("connector : "+connectorArg)
	}

}
