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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.io.File
import groovy.text.GStringTemplateEngine
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Value;
import javax.persistence.EntityManager
import javax.persistence.metamodel.EntityType
import javax.persistence.metamodel.Attribute
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.stereotype.Controller
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class CliService {

	@Value("\${sun.java.command}")
	private List<String> argsString;

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	private String controllerArg
	private String connectorArg
	private String domainArg

	private boolean connectorFound = false
	private boolean controllerFound = false
	private boolean domainFound = false;

	private LinkedHashMap createData = [:]
	private LinkedHashMap updateData = [:]

	/*
	 * BOOTSTRAP DATA
	 */
	String realName
	String realPackageName
	private LinkedHashMap data = [:]
	private Object obj;
	String dirPath

	//Integer cores = Holders.grailsApplication.config.apitoolkit.procCores as Integer

	public CliService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	void parse() {
		ArrayList args = argsString[0].split(" ")
		args.remove(0)

		ArrayList validArgKeys = ['controller','connector','domain']
		ArrayList scaffoldKeys = ['controller','connector']
		ArrayList domainKey = ['domain']
		LinkedHashMap vars = [:]
		args.each(){

			ArrayList temp = it.split('=')
			if(validArgKeys.contains(temp[0].toLowerCase())){

				if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-zA-Z0-9_]+)+[0-9a-z_]/) {
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

		if(domainArg) {
			def entityManager = ctx.getBean('entityManagerFactory')
			Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
			for (EntityType tempEntityType : entities) {
				if(!domainFound){
					if (tempEntityType.getJavaType().getCanonicalName() == domainArg) {

						println("domain : "+tempEntityType.getJavaType())
						println("domain : "+tempEntityType.getJavaType().getCanonicalName())
						println("domain : "+tempEntityType.getJavaType().getName())
						println("domain : "+tempEntityType.getJavaType().getPackage().getName())
						println("domain : "+tempEntityType.getDeclaredAttributes())

						domainFound = true

						//entityType.getSimpleName().toLowerCase().concat("s");

						// todo : should this be camelCase????
						realPackageName =  tempEntityType.getJavaType().getCanonicalName() - (tempEntityType.getJavaType().getPackage().getName()+".")
						realName = realPackageName.toLowerCase()
						println(realPackageName)
						println(realName)

						data[realName] = [:]
						data[realName]['className'] = realPackageName
						data[realName]['values'] = [:]

						Field[] fields = tempEntityType.getJavaType().getDeclaredFields()
						fields.each(){

							String attName = it.getName()
							println("${it.getType().getCanonicalName()} / ${attName}")

							Annotation anno = it.getAnnotation(javax.persistence.Column.class);
							LinkedHashMap constraints = [:]

							String keyType = null
							String reference = null
							if(attName!='serialVersionUID') {
								Attribute att = tempEntityType.getDeclaredAttribute(attName)
								if (att.isAssociation()) {
									keyType = (['id', 'ID'].contains(attName)) ? 'PKEY' : 'FKEY'
									if (keyType == 'FKEY') {
										reference = att.getJavaType().getSimpleName()
									}
								}
							}

							if(anno!=null){
								constraints['nullable'] = anno.nullable()
								constraints['unique'] = anno.unique()
								if(keyType){
									if(reference){
										//FKEY
										data[realName]['values'][it.getType().getName()] = ['key':keyType,'reference':reference,'type':it.getType().getCanonicalName(),'constraints':constraints,'description':'<put your description here>','mockData':'<put your mock data here>']
									}else{
										//PKEY
										data[realName]['values'][it.getType().getName()] = ['key':keyType,'type':it.getType().getCanonicalName(),'constraints':constraints,'description':'<put your description here>','mockData':'<put your mock data here>']
									}
								}else{
									data[realName]['values'][it.getType().getName()] = ['type':it.getType().getCanonicalName(),'constraints':constraints,'description':'<put your description here>','mockData':'<put your mock data here>']
								}
							}else{
								data[realName]['values'][it.getType().getName()] = ['type':it.getType().getCanonicalName(),'constraints':null,'description':'<put your description here>','mockData':'<put your mock data here>']
							}
						}
					}
				}
			}

			if (domainFound) {
				if (controllerArg) {
					createController(data)
				} else if (connectorArg) {
					createConnector(data)
				}
			}else{
				error(1, "Entity name '${domainArg}' not found. Please try again.")
			}
		}
	}

	private boolean isFkey(){

	}

	private void createController(){
		println("### creating controller...")
		Map<String, Object> controllers = listableBeanFactory.getBeansWithAnnotation(Controller.class)

		// check to see if it exists
		controllers.each(){ k, v ->
			if(!connectorFound) {
				if (v.getClass().getPackage().getName() == controllerArg) {
					connectorFound = true
					obj = v
					data[realName]['packageName'] = controllerArg
				}
			}
		}

		// check directory structure (in case this is FIRST controller)
		String controllerPath = controllerArg.replaceAll("\\.","/");
		if(!controllerFound && !dirExists(controllerPath)){
			error(42, "Sent controller class did not match any existing package using the 'Controller' annotation NOR directory structure. Please try again with the full package.")
		}

		//start scaffold process

		error(42, "")
	}

	private void createConnector(){
		println("### creating connector...")
		error(42, "")

	}

	private boolean dirExists(String path) {
		boolean exists = false

		// src/main/groovy
		String groovyPath = System.getProperty("user.dir")+"/src/main/groovy/${path}"
		//def ant = new AntBuilder()
		def gfile = new File(groovyPath)
		if (gfile.exists()) {
			exists = true
			dirPath = groovyPath
			return exists
		}

		// src/main/java
		String javaPath = System.getProperty("user.dir")+"/src/main/java/${path}"
		def jfile = new File(javaPath)
		if (jfile.exists()) {
			exists = true
			dirPath = javaPath
			return exists
		}
		return exists
	}

	private boolean fileExists(String path){
		def cfile = new File(path)
		if (cfile.exists()) {
			return true
		}
		return false
	}

	private void error(int i, String msg) {
		if (msg != "") {
			System.err << "${msg}"
		}
		System.exit i
	}


	LinkedHashMap entityTypeConverter(String type){
		switch(type){
			case 'char':
			case 'java.lang.Character':
				return ['Character': type]
				break
			case 'bool':
			case 'java.lang.Boolean':
				return ['Boolean':'java.lang.Boolean']
				break
			case 'byte':
			case 'java.lang.Byte':
				return ['Byte':'java.lang.Byte']
				break
			case 'short':
			case 'java.lang.Short':
				return ['Short':'java.lang.Short']
				break
			case 'int':
			case 'java.lang.Integer':
				return ['Integer':'java.lang.Integer']
				break
			case 'long':
			case 'java.lang.Long':
				return ['Long':'java.lang.Long']
				break
			case 'float':
			case 'java.lang.Float':
				return ['Float':'java.lang.Float']
				break
			case 'double':
			case 'java.lang.Double':
				return ['Double':'java.lang.Double']
				break
			case 'java.math.BigInteger':
				return ['BigInteger':'java.math.BigInteger']
				break
			case 'java.math.BigDecimal':
				return ['BigDecimal':'java.math.BigDecimal']
				break
			case 'java.sql.Timestamp':
				return ['Timestamp':'java.sql.Timestamp']
				break
			case 'java.sql.Date':
			case 'java.util.Date':
				return ['Date':'java.util.Date']
				break
			case 'java.util.Calendar':
				return ['Calendar':'java.util.Calendar']
				break
			case 'java.util.Currency':
				return ['Currency':'java.util.Currency']
				break
			case 'java.util.Locale':
				return ['Locale':'java.util.Locale']
				break
			case 'java.util.TimeZone':
				return ['TimeZone':'java.util.TimeZone']
				break
			case 'java.net.URL':
				return ['URL':'java.net.URL']
				break
			case 'java.util.UUID':
				return ['UUID':'java.util.UUID']
				break
			case 'java.lang.String':
			default:
				return ['String':'java.lang.String']
				break
		}
	}
}
