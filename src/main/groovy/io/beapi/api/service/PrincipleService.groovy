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

import groovyx.gpars.*
import io.beapi.api.properties.ApiProperties
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import javax.annotation.PostConstruct

@Service
// @EnableConfigurationProperties([ApiProperties.class])
class PrincipleService {

	@Autowired
	ApiProperties apiProperties

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PrincipleService.class);

	public PrincipleService() {
		//this.apiProperties = apiProperties
	}

	Object principle(){
		//logger.debug("principle : {}")
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal()
		//return authentication.getPrinciple();
	}

	String authorities() {
		String authorities = 'permitAll'
		//logger.debug("authorities : {}")
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			authentication.getAuthorities().each() {
				if(it!='permitAll') {
					authorities = it.getAuthority()
				}
			}
		}catch(Exception e){
			throw new Exception("[PrincipleService :: authorities] : Exception - full stack trace follows:",e)
		}
		return authorities
	}

	String name() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}


	protected LinkedHashMap getPrincipal(){
		LinkedHashMap principalSecInfo = ['name':name(),'auths':authorities()]
		return principalSecInfo
	}

	boolean isSuperuser() {
		String authority = authorities()
		return authority==apiProperties.getSecurity().getSuperuserRole()
	}
}

