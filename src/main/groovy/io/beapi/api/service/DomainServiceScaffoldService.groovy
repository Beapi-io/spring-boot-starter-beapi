package io.beapi.api.service;

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

@Service
public class DomainServiceScaffoldService {



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
	String repoPackage
	String repoName

	public DomainServiceScaffoldService(ApplicationContext applicationContext) {
		this.ctx = applicationContext
	}

	static transactional = false

	// type = [groovy,java]
	void scaffoldDomainService(String domainArg, String repoArg, String type='groovy') {
		println("scaffolding domain service...")
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
					domainPackage = tempEntityType.getJavaType().getPackage().getName()
					repoPackage = repoArg;
					ArrayList tmp = repoArg.split('.')
					repoName = tmp[tmp.size() - 1]

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
				repoPackage   : "${repoPackage}",
				logicalClassName: "${logicalClassName}",
				repoName: "${repoName}",
		]

		String starterDir = new File(getClass().protectionDomain.codeSource.location.path).path
		def starter = new File(starterDir)

		String inPath = "io/beapi/api/templates/DomainService.java.template"
		String outPath = "${System.getProperty("user.dir")}/src/main/${format}/${packageDirName}/service/${logicalClassName}Service.${format}"

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
}
