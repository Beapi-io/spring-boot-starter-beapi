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
 *
 * ##### USAGE ####
 * for scaffolding a connector : gradle  scaffold -Pargs="connector=<domain.package.name>>"
 * for scaffolding a connector : gradle  scaffold -Pargs="domain=demo.application.domain.Company"

 *
 */
package io.beapi.api.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.File

import groovy.text.Template
import groovy.text.GStringTemplateEngine

import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Value;
import javax.persistence.EntityManager
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.Attribute
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Controller
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.annotation.Annotation;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class CliService {

	@Value("\${sun.java.command}")
	private List<String> argsString

	@Autowired
	ConnectorScaffoldService connScaffoldService

	@Autowired
	TestScaffoldService testScaffoldService

	public CliService() {}

	static transactional = false

	void parse() {
		//println("#### ARGS : "+argsString)

		ArrayList args = argsString[0].split(" ")
		//println("#### PARSE ARGS : "+args)

		if(args.size()>0) {
			args.remove(0)
			ArrayList validArgKeys = ['connector','help','test']
			LinkedHashMap temp = [:]
			args.each() {
				ArrayList z = it.split('=')
				temp.put(z[0],z[1])
			}

			ArrayList keys = temp.keySet()
			keys.removeAll(validArgKeys);

			if(args.size()>0 && keys.size()==0 && keys.isEmpty()) {
				temp.each() { k, v ->
					if (validArgKeys.contains(k.toLowerCase())) {
							switch (k.toLowerCase()) {
								case 'connector':
									if (v ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
										if (v.isEmpty()) {
											error(1, "'domain' value cannot be NULL/empty. Please try again.")
										} else {
											connScaffoldService.scaffoldConnector(v)
										}
									}else{
										error(1, "Invalid package. Package value for '" + k + "' is not recognized as a valid Domain package name")
									}
									break
								case 'test':
									if (v ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
										if (v.isEmpty()) {
											error(1, "'controller' value cannot be NULL/empty. Please try again.")
										} else {
											testScaffoldService.scaffoldTest(v)
										}
									}else{
										error(1, "Invalid package. Package value for '" + k + "' is not recognized as a valid Controller package name")
									}
									break
								case 'help':
									usage()
							}
					}
				}
			}
		}
	}

	private void usage(){
		println("""
##########################################################################################
USAGE: gradle scaffold -Pargs="<option>=<associated package name>"

ex. gradle scaffold -Pargs="connector=demo.application.domain.Company"
ex. gradle scaffold -Pargs="test=demo.application.controller.Company"
ex. gradle scaffold -Pargs="help"

[ARGS]
connector = <an associated entity/domain>
-- scaffolds connectors

test = <an associated controller>
-- scaffolds integration tests for endpoints.

help
-- CLI usage and help information

##########################################################################################
""")
		error(0, "")
	}

	private void error(int i, String msg) {
		if (msg != "") {
			System.err << "[ERROR] ${msg}"
		}
		System.exit i
	}
}
