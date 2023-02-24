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


import io.beapi.api.properties.ApiProperties
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory


@Service
class CliService {

	@Autowired
	ApiProperties apiProperties

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PrincipleService.class);

	public CliService() {
		//this.apiProperties = apiProperties
	}

	LinkedHashMap parse(ArrayList args) {
		ArrayList validArgKeys = ['controller','connector','domain']
		LinkedHashMap vars = [:]
		try{
			args.each(){
				ArrayList temp = it.split('=')
				if(validArgKeys.contains(temp[0])){
					if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]/) {
						vars[temp[0]] = temp[1]
					}else{
						throw new Exception("Invalid package name. Package name for '"+temp[0]+"' is not recognized as a valid package name", e)
					}
				}else{
					throw new Exception('Invalid ARG sent. Please provide ARG values of \'controller/connector\' and \'domain\'.', e)
				}
			}
		} catch (Exception e) {
			System.err << e
			System.exit 1
		}
		return vars
	}

}

