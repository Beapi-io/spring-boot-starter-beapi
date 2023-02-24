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

	LinkedHashMap parse(String args) {

		/*
		try {
			if (options.d) {
				if (options.controller) {
					try {
						createController()
					} catch (Exception e) {
						throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
					}
				}

				if (options.domain) {
					try {
						createDomain()
					} catch (Exception e) {
						throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
					}
				}

				if (options.connector) {
					try {
						createConnector()
					} catch (Exception e) {
						throw new Exception('Scaffold Value must not be NULL. Please provide ARG value of \'-controller/domain/connector\'.', e)
					}
				}
			} else {
				throw new Exception('Method (-d/--domainname) is REQUIRED for Beapi to work. Please try again.\n')
			}
		} catch (Exception e) {
			System.err << e
			System.exit 1
		}

		 */
	}

}

