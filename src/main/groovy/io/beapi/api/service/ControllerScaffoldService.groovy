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
import org.springframework.stereotype.Service
import javax.persistence.metamodel.EntityType

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern

/*
Scaffolding for building out integration tests
 */
// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Service
public class ControllerScaffoldService {

	@Value("\${api.iostateDir}")
	private connectorDir


	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	@Autowired
	ApiCacheService apiCacheService

	private boolean domainFound = false;

	/*
	 * BOOTSTRAP DATA
	 */
	String realClassName
	String logicalClassName
	String packageName
	String format
	String createData
	String updateData

	public ControllerScaffoldService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	// type = [groovy,java]
	void scaffoldController(String domainArg, String serviceArg, String type='groovy') {
		println("scaffolding controller...")
		def entityManager = ctx.getBean('entityManagerFactory')
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

		format = (type == null)?'groovy':type

		LinkedHashMap values = [:]
		for (EntityType tempEntityType : entities) {
			if (!domainFound) {

				if (tempEntityType.getJavaType().getCanonicalName() == domainArg) {
					domainFound = true

					def canName = tempEntityType.getJavaType().getCanonicalName()
					logicalClassName = tempEntityType.getJavaType().getSimpleName()
					realClassName = logicalClassName.toLowerCase()
					packageName = canName-".${logicalClassName}"

					// todo
					createData = getCreateData()
					// todo
					updateData = getUpdateData()

					writeController()
					error(0, "")
				}
			}
		}
	}


	/*
	* TODO : createData needs to be populated
	* TODO : updateData needs to be populated
	*
	* get from apicache based on realClassName (I think)
	*
	*  TODO : DETECT IF FILE EXISTS
	 */
	private void writeController(){
		String tmpPackage = packageName -".domain"
		String packageDirName = tmpPackage.replaceAll('\\.','/');

		Map templateAttributes = [
				packageName     : "${packageName}",
				realClassName   : "${realClassName}",
				logicalClassName: "${logicalClassName}",
				createData      : getCreateData(),
				updateData      : getUpdateData(),
		]

		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)

		String inPath = "io/beapi/api/templates/Controller.groovy.template"
		String outPath = "${System.getProperty("user.dir")}/src/main/${format}/${packageDirName}/controller/${logicalClassName}Controller.${format}"

		if (starter.isFile() && starter.name.endsWith("jar")) {
			JarFile jar = new JarFile(starter)

/*
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				if (entry.getName()) {
					System.out.println("File : " + entry.getName());
				}
			}
 */

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
			def template = engine.createTemplate(templateFile).make(templateAttributes)

			String controller = template.toString()

			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))
			writer.write(controller)
			writer.close()
		}
	}

	private String getCreateData(){
		String version = getVersion()
		ArrayList<String> test = apiCacheService.getCacheKeys()
		test.each(){
			println(it)
		}

		def apiObject = apiCacheService.getApiDescriptor(realClassName, version, 'create')
		LinkedHashMap receives = apiObject?.getReceivesList()
		Set createList = receives['permitAll']

		String output = ""
		createList.each(){
			output += realClassName+".setName(this.params.get(\""+it+"\"));\n"
		}

		return output
	}

	private String getUpdateData(){
		String version = getVersion()
		def apiObject = apiCacheService.getApiDescriptor(realClassName, version, 'update')
		LinkedHashMap returns = apiObject?.getReturnsList()
		Set updateList = receives['permitAll']

		String output = ""
		updateList.each(){
			output += realClassName+".setName(this.params.get(\""+it+"\"));\n"
		}

		return output
	}

	private String getVersion(){
		ClassLoader classLoader = getClass().getClassLoader();
		URL incoming = classLoader.getResource("META-INF/build-info.properties")

		String version
		if (incoming != null) {
			Properties properties = new Properties();
			properties.load(incoming.openStream());
			version = properties.getProperty('build.version')
		}
		return version
	}

	private void error(int i, String msg) {
		if (msg != "") {
			System.err << "[ERROR] ${msg}"
		}
		System.exit i
	}

}
