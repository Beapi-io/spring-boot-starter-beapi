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

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class CliService {

	@Value("\${sun.java.command}")
	private List<String> tempArgs;

	ApplicationContext ctx

	private String controllerArg
	private String connectorArg
	private String domainArg

	//Integer cores = Holders.grailsApplication.config.apitoolkit.procCores as Integer

	public CliService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	//void parse() {
	//	println(argsList)
	//}

	void parse() {
		Set<String> args = new HashSet<>(tempArgs);
		println(args)
		ArrayList validArgKeys = ['controller','connector','domain']
		ArrayList scaffoldKeys = ['controller','connector']
		ArrayList domainKey = ['domain']
		LinkedHashMap vars = [:]
		args.each(){
			println("test:"+it)
			ArrayList temp = it.split('=')
			println(temp[0].toLowerCase())
			if(validArgKeys.contains(temp[0].toLowerCase())){
				println("has valid arg : ${println(temp[0])}")
				if(temp[1] ==~ /[a-z][a-z0-9_]*(\.[a-z0-9_]+)+[0-9a-z_]/) {
					switch(temp[0].toLowerCase()){
						case 'controller':
							println('controllerMatch')
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
							println('domainMatch')
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

		//if(domainArg==null){
		//	error(1, "Missing valid domain value sent. Please try again.")
		//}

		//if(controllerArg==null && connectorArg==null){
		//	error(1, "Missing valid scaffold value sent (ie controller/connector). Please try again.")
		//}
		println("domain : "+domainArg)
		println("controller : "+controllerArg)
		println("connector : "+connectorArg)
	}

	// NOTE : This has to be called separately in the 'runner'
	public scaffold(ApplicationContext context){
		def entityManager = context.getBean('entityManagerFactory')
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		if(domainArg) {
			if (controllerArg) {
				for (EntityType tempEntityType : entities) {
					println(tempEntityType.getJavaType())
					println(tempEntityType.getName())
					//entityClasses.add(tempEntityType.getJavaType());
				}
				//createController()
			} else if (connectorArg) {
				//createConnector()
			} else {
				error(1, "Unrecognized arg. Please try again.")
			}
		}
	}

	private void createController(){
		// look for 'entity'' first and try to get match


		// next make sure controller does not exist

	}

	private void createDomain(){

	}

	private void createConnector(){

	}

	private void checkDirectory(contDir) {
		def ant = new AntBuilder()
		def cfile = new File(contDir)
		if (!cfile.exists()) {
			ant.mkdir(dir: contDir)
		}
		return
	}

	private boolean fileExists(String path){
		def cfile = new File(path)
		if (cfile.exists()) {
			return true
		}
		return false
	}

	private LinkedHashMap hibernateTypeConverter(String type){
		switch(type){
			case 'class org.hibernate.type.CharacterType':
				return ['Character':'java.lang.Character']
				break
			case 'class org.hibernate.type.NumericBooleanType':
			case 'class org.hibernate.type.YesNoType':
			case 'class org.hibernate.type.TrueFalseType':
			case 'class org.hibernate.type.BooleanType':
				return ['Boolean':'java.lang.Boolean']
				break
			case 'class org.hibernate.type.ByteType':
				return ['Byte':'java.lang.Byte']
				break
			case 'class org.hibernate.type.ShortType':
				return ['Short':'java.lang.Short']
				break
			case 'class org.hibernate.type.IntegerTypes':
				return ['Integer':'java.lang.Integer']
				break
			case 'class org.hibernate.type.LongType':
				return ['Long':'java.lang.Long']
				break
			case 'class org.hibernate.type.FloatType':
				return ['Float':'java.lang.Float']
				break
			case 'class org.hibernate.type.DoubleType':
				return ['Double':'java.lang.Double']
				break
			case 'class org.hibernate.type.BigIntegerType':
				return ['BigInteger':'java.math.BigInteger']
				break
			case 'class org.hibernate.type.BigDecimalType':
				return ['BigDecimal':'java.math.BigDecimal']
				break
			case 'class org.hibernate.type.TimestampType':
				return ['Timestamp':'java.sql.Timestamp']
				break
			case 'class org.hibernate.type.TimeType':
				return ['Time':'java.sql.Time']
				break
			case 'class org.hibernate.type.CalendarDateType':
			case 'class org.hibernate.type.DateType':
				return ['Date':'java.sql.Date']
				break
			case 'class org.hibernate.type.CalendarType':
				return ['Calendar':'java.util.Calendar']
				break
			case 'class org.hibernate.type.CurrencyType':
				return ['Currency':'java.util.Currency']
				break
			case 'class org.hibernate.type.LocaleType':
				return ['Locale':'java.util.Locale']
				break
			case 'class org.hibernate.type.TimeZoneType':
				return ['TimeZone':'java.util.TimeZone']
				break
			case 'class org.hibernate.type.UrlType':
				return ['URL':'java.net.URL']
				break
			case 'class org.hibernate.type.ClassType':
				return ['Class':'java.lang.Class']
				break
			case 'class org.hibernate.type.MaterializedBlobType':
			case 'class org.hibernate.type.BlobType':
				return ['Blob':'java.sql.Blob']
				break
			case 'class org.hibernate.type.ClobType':
				return ['Clob':'java.sql.Clob']
				break
			case 'class org.hibernate.type.PostgresUUIDType':
			case 'class org.hibernate.type.UUIDBinaryType':
				return ['UUID':'java.util.UUID']
				break
			case 'class org.hibernate.type.TextType':
			case 'class org.hibernate.type.StringType':
			default:
				return ['String':'java.lang.String']
				break
		}
	}

	private void error(int i, String msg){
		System.err << "${msg}"
		System.exit i
	}
}
