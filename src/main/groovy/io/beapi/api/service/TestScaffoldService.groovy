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

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class TestScaffoldService {

	@Value("\${api.iostateDir}")
	private connectorDir


	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	ApiCacheService apiCacheService

	private boolean controllerFound = false;
	private boolean connectorFound = false;
	private connectorName

	/*
	 * BOOTSTRAP DATA
	 */
	String realName
	String realPackageName
	private LinkedHashMap data = [:]
	private Object obj;
	String fileName
	String dirPath
	String templateDir = "${System.getProperty('user.dir')}/src/main/groovy/templates/"
	List variables = []

	public TestScaffoldService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	void scaffoldTest(String controllerArg) {
			// make sure there is a 'controller' & 'connector'
		LinkedHashMap<String, Object> cont = listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		cont.each() { k, v ->
			if (v.getClass().getCanonicalName() == controllerArg) {
				//println("${v} / ${controllerArg}  ")
				//println("CName : "+v.getClass().getCanonicalName())
				fileName = v.getClass().getSimpleName()+".json"
				controllerFound=true
			}
		}

		if(controllerFound){
			// check for existence of 'connector'
			String connectorPath = "${System.getProperty('user.home')}/${connectorDir}/${fileName}"
			if(fileExists(connectorPath)){
				// get data for template

				// make sure all mockdata exists for this
				// get cached data object


				// createTest(data)
			}else{
				error(1, "Connector file '"+connectorPath+"' not found. Please check and try again.")
			}
		}else{
			error(1, "Controller for Package Name '"+controllerArg+"' Not Found. Please check and try again.")
		}
		error(0, "")
	}


/*
	private void createTest(LinkedHashMap data){
		String connectorPath = "${System.getProperty('user.home')}/${connectorDir}"
		if(!dirExists(connectorPath)){
			// need to create path
			error(1, "The 'iostateDir' in your 'beapi_api.yml' file is not porperly defined as the directory does not exist. Please check and try again.")
		}

		writeConnector("templates/Connector.json.template", "${System.getProperty('user.home')}/${connectorDir}/${realPackageName}.json", data)

		error(0, "")
	}
 */

	private boolean dirExists(String path) {
		boolean exists = false
		//def ant = new AntBuilder()
		def file = new File(path)
		if (file.exists()) {
			exists = true
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
			System.err << "[ERROR] ${msg}"
		}
		System.exit i
	}


	void writeTest(String inPath, String outPath, LinkedHashMap attribs){
		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)
		if (starter.isFile() && starter.name.endsWith("jar")) {
			JarFile jar = new JarFile(starter)
			JarEntry entry = jar.getEntry(inPath)

			InputStream inStream = jar.getInputStream(entry)

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
		}else{
			// throw error
			error(1, "Project is trying to use CLI but does not use 'spring-boot-starter-beapi'. Please include the starter to use this service.");
		}
	}

}
