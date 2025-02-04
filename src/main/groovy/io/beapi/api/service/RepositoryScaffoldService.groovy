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
public class RepositoryScaffoldService {


	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private ApplicationContext ctx

	@Autowired
	ApiCacheService apiCacheService

	private boolean domainFound = false;

	/*
	 * BOOTSTRAP DATA
	 */

	String logicalClassName
	String packageName
	String domainPackage
	String format

	public RepositoryScaffoldService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	// type = [groovy,java]
	void scaffoldRepository(String domainArg, String type='groovy') {
		println("scaffolding repository...")
		def entityManager = ctx.getBean('entityManagerFactory')
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

		format = (type == null)?'java':type

		LinkedHashMap values = [:]
		for (EntityType tempEntityType : entities) {
			println("has entities")
			if (!domainFound) {
				println("no domain : "+tempEntityType.getJavaType().getCanonicalName() +" / "+domainArg)

				if (tempEntityType.getJavaType().getCanonicalName() == domainArg) {
					domainFound = true
					println("domain found")
					def canName = tempEntityType.getJavaType().getCanonicalName()
					logicalClassName = tempEntityType.getJavaType().getSimpleName()
					packageName = canName-".${logicalClassName}"

println("###########################")
println(logicalClassName)
println(format)

					domainPackage = tempEntityType.getJavaType().getPackage().getName()

					writeRepository()
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
	private void writeRepository(){
		println("writing repository...")
		println(format+"/"+this.format)
		String tmpPackage = packageName -".domain"
		String packageDirName = tmpPackage.replaceAll('\\.','/');

		LinkedHashMap templateAttributes = [
				packageName     : "${packageName}",
				domainPackage   : "${domainPackage}",
				logicalClassName: "${logicalClassName}"
		]

		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)

		String inPath = "io/beapi/api/templates/Repository.java.template"
		String outPath = "${System.getProperty("user.dir")}/src/main/${format}/${packageDirName}/repositories/${logicalClassName}Repository.${format}"

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
			def template = engine.createTemplate(templateFile).make(templateAttributes)

			String controller = template.toString()

			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))
			writer.write(controller)
			writer.close()
		}
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
