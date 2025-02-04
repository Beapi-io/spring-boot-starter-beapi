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
 * for scaffolding a connector : gradle  scaffold -Pargs="connector=<domain.package.name>"
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
	RepositoryScaffoldService repoScaffoldService

	@Autowired
	ConnectorScaffoldService connScaffoldService

	@Autowired
	ControllerScaffoldService contScaffoldService

	@Autowired
	DomainServiceScaffoldService domservScaffoldService

	ArrayList validArgKeys = ['connector','help','controller','repository','domainservice']

	public CliService() {}

	static transactional = false

	void parse() {
		parseArgs(argsString.toString(), validArgKeys)
		/*
		ArrayList args = argsString[0].split(" ")
		//println("#### PARSE ARGS : "+args)


		if(args.size()>0) {
			args.remove(0)

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
									}
									connScaffoldService.scaffoldConnector(v)
								}else{
									error(1, "Invalid package. Package value for '" + k + "' is not recognized as a valid Domain package name")
								}
								break
							case 'controller':
								// test for comma then split and test each value separately
								println("###"+argsString+"###")
								Pattern p = ~/(.+)\,(.+)/
								Matcher match = p.matcher(v)
								if (match.find()) {
									if(match[0][1] !=~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
										//error: unrecognized format for domain
										error(1, "Invalid package. Package value for '" + match[0][1] + "' is not recognized as a valid Domain package name")
									}
									if(match[0][2] !=~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
										//error: unrecognized format for service
										error(1, "Invalid package. Package value for '" + match[0][2] + "' is not recognized as a valid Service package name")
									}
									if (match[0][1].isEmpty() || match[0][2].isEmpty()) {
										error(0, "'domain' and 'service' values are required and cannot be NULL/empty. Please try again.")
									}
									contScaffoldService.scaffoldController(match[0][1], match[0][2])
									System.exit 0
								}else{
									//error: badly formatted args; requires comma between 'domain' and 'service'
									error(1, "Badly Formatted ARGs. Args require comma between 'domain' and 'service'")
								}
								break
							case 'test':
								if (v ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
									if (v.isEmpty()) {
										error(0, "'controller' value cannot be NULL/empty. Please try again.")
									}
									testScaffoldService.scaffoldTest(v)
								}else{
									error(0, "Invalid package. Package value for '" + k + "' is not recognized as a valid Controller package name")
								}
								break
							case 'help':
								usage()
						}
					}
				}
			}
		}

		 */
	}

	private void usage(){
		println("""
##########################################################################################
USAGE: gradle scaffold -Pargs="<option>=<associated package name>"

# SCAFFOLD CONNECTOR #
gradle scaffold -Pargs="connector=<domain name>"
ex. gradle scaffold -Pargs="connector=demo.application.domain.Company"

# SCAFFOLD CONTROLLER #
ex. gradle scaffold -Pargs="<domain name>, <service name>, java"
ex. gradle scaffold -Pargs="controller=demo.application.domain.Company, demo.application.service.CompanyService, java"

# SCAFFOLD REPOSITORY #
gradle scaffold -Pargs="repository=<domain name>"
ex. gradle scaffold -Pargs="repository=demo.application.domain.Company, java"

# SCAFFOLD DOMAIN SERVICE #
gradle scaffold -Pargs="repository=<domain name>"
ex. gradle scaffold -Pargs="domainservice=demo.application.domain.Company, io.beapi.api.repositories.CompanyRepository, java"

# HELP #
ex. gradle scaffold -Pargs="help"

[ARGS]
connector = <an associated entity/domain>
-- scaffolds connector

controller = <an associated entity/domain>,<the entity/domain service>,<output format>
-- scaffolds controller

repository = <an associated entity/domain>,<output format>
-- scaffolds repository

domainservice = <an associated entity/domain>,<the entity/domain service>,<output format>
-- scaffolds domain service for repository


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
		System.exit 0
	}

	// parse args from argString
	private ArrayList parseArgs(String argString, ArrayList validArgKeys){

		ArrayList matches = []
		Pattern pattern = ~/\[[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_] (connector|Connector|CONNECTOR|controller|CONTROLLER|Controller|repository|Repository|REPOSITORY|domainService|DomainService|domainservice|DOMAINSERVICE)=(.+)\]/

		Matcher match = pattern.matcher(argString)
		if (match.find()) {
			if(!validArgKeys.contains(match[0][2])) {
				//error: unrecognized format for domain
				error(1, "Invalid argument: "+match[0][2]+". Valid arguments for scaffolding are : "+validArgKeys)
			}else{
				if(match[0][2]){

					switch(match[0][2]){
						case 'connector':
							if(match[0][3] ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/){
								if (match[0][3].isEmpty()) {
									error(1, "'domain' value cannot be NULL/empty. Please try again.")
								}
								connScaffoldService.scaffoldConnector(match[0][3])
							}else{
								error(1, "Invalid package. Package value for '" + k + "' is not recognized as a valid Domain package name")
							}
							break

						case 'repository':
							println("case repository")
							Pattern p = ~/(.+)\,(.+)/
							println(match[0][3])
							Matcher match2 = p.matcher(match[0][3])
							if (match2.find()) {
								String arg1 = match2[0][1].trim()
								String arg3 = match2[0][2].trim()
								println(arg1)
								println(arg3)


								if(arg1 ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

								}else{
									//error: unrecognized format for domain
									println("[repo] invalid package : "+match2[0][1])
									error(1, "Invalid package. Package value for '" + arg1 + "' is not recognized as a valid Domain package name")
								}

								if(['java','groovy'].contains(arg3)) {
									println("pass : "+arg3)
								}else{

									//error: unrecognized format for service
									println("arg3 : "+arg3)
									println("class : "+arg3.class)
									error(1, "Invalid format. '" + arg3 + "' is not recognized as a valid format. Choose between [java,groovy]")
								}

								repoScaffoldService.scaffoldRepository(arg1,arg3)
							}else{
								println("[repo] invalid package : "+match2[0][1])
								error(1, "Invalid args. '" + match[0][3] + "' is not recognized set of arguments. Use 'help' to see the proper args.")
							}
							break

						case 'controller':
							// test for comma then split and test each value separately
							println("controller")
							Pattern p = ~/(.+)\,(.+)\,(.+)/

							Matcher match2 = p.matcher(match[0][3])
							if (match2.find()) {
								String arg1 = match2[0][1].trim()
								String arg2 = match2[0][2].trim()
								String arg3 = match2[0][3].trim()

								if(arg1 ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

								}else{
									//error: unrecognized format for domain
									error(1, "Invalid package. Package value for '" + match2[0][1] + "' is not recognized as a valid Domain package name")
								}

								if(arg2 ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

								}else{
									//error: unrecognized format for service
									error(1, "Invalid package. Package value for '" + match2[0][2] + "' is not recognized as a valid Service package name")
								}

								if(arg3 ==~ /(java|groovy)/) {

								}else{
									//error: unrecognized format for service
									error(1, "Invalid package. Package value for '" + match2[0][2] + "' is not recognized as a valid Service package name")
								}
								if (arg1.isEmpty() || arg2.isEmpty()) {
									error(1, "'domain' and 'service' values are required and cannot be NULL/empty. Please try again.")
								}

								contScaffoldService.scaffoldController(arg1, arg2, arg3)
							}
							break
						case 'domainservice':
							// test for comma then split and test each value separately
							println("controller")
							Pattern p = ~/(.+)\,(.+)\,(.+)/

							Matcher match2 = p.matcher(match[0][3])
							if (match2.find()) {
								String arg1 = match2[0][1].trim()
								String arg2 = match2[0][2].trim()
								String arg3 = match2[0][3].trim()

								if(arg1 ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

								}else{
									//error: unrecognized format for domain
									error(1, "Invalid package. Package value for '" + match2[0][1] + "' is not recognized as a valid Domain package name")
								}

								if(arg2 ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {

								}else{
									//error: unrecognized format for service
									error(1, "Invalid package. Package value for '" + match2[0][2] + "' is not recognized as a valid Service package name")
								}

								if(arg3 ==~ /(java|groovy)/) {

								}else{
									//error: unrecognized format for service
									error(1, "Invalid package. Package value for '" + match2[0][2] + "' is not recognized as a valid Service package name")
								}
								if (arg1.isEmpty() || arg2.isEmpty()) {
									error(1, "'domain' and 'service' values are required and cannot be NULL/empty. Please try again.")
								}

								domservScaffoldService.scaffoldDomainService(arg1, arg2, arg3)
							}
							break

						case 'help':
							usage()
					}
				}
			}
			if(match[0][2] !=~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
				//error: unrecognized format for service
				error(1, "Invalid package. Package value for '" + match[0][2] + "' is not recognized as a valid Service package name")
			}
			if (match[0][1].isEmpty() || match[0][2].isEmpty()) {
				error(1, "'domain' and 'service' values are required and cannot be NULL/empty. Please try again.")
			}
		}else{
			 //error(0, "'Invalid Arg. Please try again.")
		}
	}

}
