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

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service

import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.EntityType
import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang3.StringUtils

import groovy.text.Template
import groovy.text.GStringTemplateEngine

/*
Part of CLI : Used to scaffold IOState files

NOTE: Can only be used/tested as an starter artifact due to the way templates are referenced in the compiled JAR
 */
@Service
public class ConnectorScaffoldService {

	@Value("\${api.iostateDir}")
	private connectorDir


	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	private boolean domainFound = false;


	/*
	 * BOOTSTRAP DATA
	 */
	String logicalClassName
	String realClassName
	String packageName
	LinkedHashMap data = [:]
	String allAtts = ''
	String createAtts = ''

	//private Object obj;
	//String dirPath
	//String templateDir = "${System.getProperty('user.dir')}/src/main/groovy/templates/"
	List variables = []
	Set<Attribute<?,?>> attributes = []

	public ConnectorScaffoldService(ApplicationContext applicationContext) {
		println("### [ConnectorScaffoldService ] ###")
		this.ctx = applicationContext
	}

	//static transactional = false

	void scaffoldConnector(String domainArg) {
		//println("### [ConnectorScaffoldService :: scaffoldConnector] ###")
		def entityManager = ctx.getBean('entityManagerFactory')
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

		String values = ""
		for (EntityType tempEntityType : entities) {
			if (!domainFound) {

				if (tempEntityType.getJavaType().getCanonicalName() == domainArg) {


					domainFound = true

					// todo : should this be camelCase????
					logicalClassName = tempEntityType.getJavaType().getCanonicalName() - (tempEntityType.getJavaType().getPackage().getName() + ".")

					realClassName = logicalClassName.toLowerCase()
					packageName = (tempEntityType.getJavaType().getPackage().getName() - ".${realClassName}")
					this.data['realName'] = realClassName

					attributes = tempEntityType.getAttributes()
					attributes.each(){ it5 ->

						if(it5.getName()!="version") {
							allAtts += "\"${it5.getName()}\","
							if (it5.getName() != "id") {
								createAtts += "\"${it5.getName()}\","
							}
						}

						values += """
			"${it5.getName()}": {
				"type" : "${it5.getJavaType().getSimpleName()}",
				"mockData": ""
			},"""
					}

					data['allAtts'] = StringUtils.substring(allAtts, 0, allAtts.length() - 1);
					data['createAtts'] = StringUtils.substring(createAtts, 0, createAtts.length() - 1);
				}
			}
		}
		data['attList'] = values


		// todo : create method to check if controller exists
		// if exists, fill out 'URI' json
		// else template URI json using params from values
		String uris = createUriAtts(realClassName)

		if (domainFound) {
			if (connectorDir) {
				createConnector(this.data)
			}
		} else {
			error(1, "Entity name '${domainArg}' not found. Please try again.")
		}
	}

	protected String createUriAtts(String controllerName){
		//println("### [ConnectorScaffoldService :: createUriAtts] ###")
		ArrayList ignoreList = [
				'setMetaClass',
				'getMetaClass',
				'handleRequest',
				'formatMap',
				'formatEntity',
				'convertModel',
				'parseResponseParams',
				'writeErrorResponse',
				'invokeMethod',
				'getProperty',
				'setProperty',
				'wait',
				'notify',
				'notifyAll',
				'getClass',
				'hashCode',
				'toString',
				'equals','wait',
				'getTraceService',
				'setTraceService'
		]

		// todo: create json String from 'variables' arraylist for use below
		String varString = createVarString(variables)

		String uris = ""
		LinkedHashMap<String, Object> cont = listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		cont.each() { k, v ->
			if(k == controllerName){

				Method[] actions = v.getClass().getMethods()
				// get methods as 'actions'
				actions.each() { it4 ->
					try {
						if (!ignoreList.contains(it4.getName())) {
							String method = ""
							String req
							String resp
							Pattern listPattern = Pattern.compile("list|listBy")
							Pattern getPattern = Pattern.compile("get|getBy|show|showBy|enable")
							Pattern postPattern = Pattern.compile("create|make|generate|build|save|new")
							Pattern putPattern = Pattern.compile("edit|update")
							Pattern deletePattern = Pattern.compile("delete|deleteBy|disable|disableBy|destroy|kill|reset|resetBy")

							Matcher getm = getPattern.matcher(it4.getName())
							if (getm.find()) {
								method = 'GET'
								req = "[\"id\"]"
								resp = varString
							}

							Matcher listm = listPattern.matcher(it4.getName())
							if (listm.find()) {
								method = 'GET'
								resp = varString
							}

							if (method.isEmpty()) {
								Matcher postm = postPattern.matcher(it4.getName())
								if (postm.find()) {
									method = 'POST'
									req = varString
									resp = "[\"id\"]"
								}
							}

							if (method.isEmpty()) {
								Matcher putm = putPattern.matcher(it4.getName())
								if (putm.find()) {
									method = 'PUT'
									req = varString
									resp = "[\"id\"]"
								}
							}

							if (method.isEmpty()) {
								Matcher delm = deletePattern.matcher(it4.getName());
								if (delm.find()) {
									method = 'DELETE'
									req = "[\"id\"]"
									resp = "[\"id\"]"
								}
							}

						}
					}catch(Exception e){
						println("No URIs : "+e)
					}
				}
			}else{
				println("NO CONTROLLER for ${controllerName}")
			}

		}
		return uris
	}

	private String createVarString(ArrayList variables){
		//println("### [ConnectorScaffoldService :: createVarString] ###")
		String varString = "["
		int inc = 1
		variables.each(){
			varString += "\"${it}\""
			if(inc!=variables.size()){
				varString += ","
				inc++
			}
		}
		varString += "]"
		return varString
	}

	private String createAttList(LinkedHashMap values){
		//println("### [ConnectorScaffoldService :: createAttList] ###")
		String json = ""
		int inc=1
		values.each() { k, v ->
json += """
\t\t\t"${k}": {"""
			if (v.key == 'PKEY') {
json += """
\t\t\t\t"key": "${v.key}","""
			} else if (v.key == 'FKEY') {
json += """
\t\t\t\t"key": "${v.key}",
\t\t\t\t\t"references": "","""
			}

json += """
\t\t\t\t"type": "${entityTypeConverter(v.type)}",
\t\t\t\t"description": \"\",
\t\t\t\t"mockData": \"\","""

			if(v.constraints){
json += """
\t\t\t\t\"constraints\": {\"order\":${inc},\"isNullable\":${v.constraints.nullable}, \"isUnique\":${v.constraints.unique}},"""
			}else{
json += """
\t\t\t\t\"constraints\": {\"order\":${inc}},"""
			}

json += """
\t\t\t},"""
			inc++
		}

		return json
	}

	protected void createConnector(LinkedHashMap data){
		//println("### [ConnectorScaffoldService :: createConnector] ###")
		String connectorPath = "${System.getProperty('user.home')}/${connectorDir}"
		if(!dirExists(connectorPath)){
			// need to create path
			error(1, "The 'iostateDir' in your 'beapi_api.yml' file is not porperly defined as the directory does not exist. Please check and try again.")
		}


		//writeConnector("templates/Connector.json.template", "${System.getProperty('user.home')}/${connectorDir}/${logicalClassName}.json", data)

		writeConnector("io/beapi/api/templates/Connector.json.template", "${System.getProperty('user.home')}/${connectorDir}/${logicalClassName}.json", data)
		error(0, "")
	}

	protected boolean dirExists(String path) {
		boolean exists = false
		//def ant = new AntBuilder()
		def file = new File(path)
		if (file.exists()) {
			exists = true
			return exists
		}
		return exists
	}

	protected boolean fileExists(String path){
		def cfile = new File(path)
		if (cfile.exists()) {
			return true
		}
		return false
	}

	protected void error(int i, String msg) {
		if (msg != "") {
			System.err << "[ERROR] ${msg}"
		}
		System.exit 0
	}


	void writeConnector(String inPath, String outPath, LinkedHashMap attribs){
		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)

		if (starter.isFile() && starter.name.endsWith("jar")) {
			JarFile jar = new JarFile(starter)

			//Enumeration<JarEntry> entries = jar.entries()
			//for (JarEntry entry : entries) {
			//	System.out.println(entry.getName());
			//}

			JarEntry entry = jar.getEntry(inPath)

			InputStream inStream = jar.getInputStream(entry)

			try {
				OutputStream out = new FileOutputStream(outPath)
				int c
				while ((c = inStream.read()) != -1) {
					out.write(c)
				}
				inStream.close()
				out.close()
				jar.close()

				def templateFile = new File(outPath)
				def engine = new groovy.text.GStringTemplateEngine()
				def template = engine.createTemplate(templateFile).make(attribs)

				String controller = template.toString()

				BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))
				writer.write(controller)
				writer.close()
			}catch(Exception e){
				println("exception : "+e)
			}
		}else{
			// throw error
			error(1, "Project is trying to use CLI but does not use 'spring-boot-starter-beapi'. Please include the starter to use this service.");
		}
	}


	String entityTypeConverter(String type){
		switch(type){
			case 'char':
			case 'java.lang.Character':
				return 'Character'
				break
			case 'bool':
			case 'java.lang.Boolean':
				return 'Boolean'
				break
			case 'byte':
			case 'java.lang.Byte':
				return 'Byte'
				break
			case 'short':
			case 'java.lang.Short':
				return 'Short'
				break
			case 'int':
			case 'java.lang.Integer':
				return 'Integer'
				break
			case 'long':
			case 'java.lang.Long':
				return 'Long'
				break
			case 'float':
			case 'java.lang.Float':
				return 'Float'
				break
			case 'double':
			case 'java.lang.Double':
				return 'Double'
				break
			case 'java.math.BigInteger':
				return 'BigInteger'
				break
			case 'java.math.BigDecimal':
				return 'BigDecimal'
				break
			case 'java.sql.Timestamp':
				return 'Timestamp'
				break
			case 'java.sql.Date':
			case 'java.util.Date':
				return 'Date'
				break
			case 'java.util.Currency':
				return 'Currency'
				break
			case 'java.util.Locale':
				return 'Locale'
				break
			case 'java.util.TimeZone':
				return 'TimeZone'
				break
			case 'java.net.URL':
				return 'URL'
				break
			case 'java.util.UUID':
				return 'UUID'
				break
			case 'java.lang.String':
			default:
				return 'String'
				break
		}
	}
}
