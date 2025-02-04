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
package io.beapi.api.controller.staticEndpoint

import io.beapi.api.controller.BeapiRequestHandler
import io.beapi.api.service.StatsCacheService
import io.beapi.api.service.StatsService
import org.slf4j.LoggerFactory

import org.springframework.security.web.header.*
import org.springframework.stereotype.Controller

import javax.json.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.CacheManager

import javax.sql.DataSource
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;

import javax.servlet.http.HttpSession;

//import java.lang.management.ManagementFactory
import java.lang.management.*

// todo: rename as ExchangeService : encompasses both request/response methods for interceptor
@Controller("actuator")
public class ActuatorController extends BeapiRequestHandler{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ActuatorController.class);

	@Autowired
	StatsCacheService statsCacheService

	@Autowired
	List<HttpSession> getActiveSessions

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	@Autowired
	private DataSource getDataSource

	@Autowired
	protected Environment env;

	@Autowired
	protected CacheManager cacheManager

	@Autowired
	BuildProperties buildProperties;

	@Value("\${spring.application.name}")
	protected String appName;

	@Value("\${spring.application.version}")
	protected String appVersion

	//@Value("\${logging.file.name}")
	protected String logFile = System.getProperty("logging.file.name")

	//@Value("\${logging.file.path}")
	protected String logFilePath = System.getProperty("logging.file.path")

	LinkedHashMap env(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("attempts",apiProperties.getAttempts());
		temp.put("procCores",apiProperties.getProcCores());
		temp.put("documentationUrl",apiProperties.getDocumentationUrl());
		temp.put("reservedUris",apiProperties.getReservedUris());
		temp.put("apichainLimit",apiProperties.getApichainLimit());
		temp.put("postcrement",apiProperties.getPostcrement());
		temp.put("chainingEnabled",apiProperties.getChainingEnabled());
		temp.put("batchingEnabled",apiProperties.getBatchingEnabled());
		temp.put("encoding",apiProperties.getEncoding());
		temp.put("iostateDir",apiProperties.getIostateDir());
		temp.put("staticEndpoint",apiProperties.getStaticEndpoint());
		temp.put("supportedFormats",apiProperties.getSupportedFormats());
		temp.put("serverType",apiProperties.getServerType());
		temp.put("autoTest",apiProperties.getAutoTest());

		LinkedHashMap throttle = new LinkedHashMap<>();
		throttle.put("active", apiProperties.getThrottle().getActive());
		throttle.put("rateLimit", apiProperties.getThrottle().getRateLimit());
		throttle.put("dataLimit", apiProperties.getThrottle().getDataLimit());
		throttle.put("staleSession (mins)", apiProperties.getThrottle().getStaleSession());
		temp.put("throttle", throttle);

		LinkedHashMap hook = new LinkedHashMap<>();
		hook.put("active", apiProperties.getWebhook().getActive());
		hook.put("service", apiProperties.getWebhook().getServices());
		temp.put("webhook", hook);

		LinkedHashMap sec = new LinkedHashMap<>();
		sec.put("superuserRole",apiProperties.getSecurity().getSuperuserRole());
		sec.put("userRole",apiProperties.getSecurity().getUserRole());
		sec.put("testRole",apiProperties.getSecurity().getTestRole());
		sec.put("anonRole",apiProperties.getSecurity().getAnonRole());
		sec.put("networkGroups",apiProperties.getSecurity().getNetworkGroups());
		sec.put("networkRoles",apiProperties.getSecurity().getNetworkRoles());
		temp.put("security", sec);

		return temp;
	}

	LinkedHashMap getProperties(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
        temp.put("attempts",apiProperties.getAttempts());
        temp.put("procCores",apiProperties.getProcCores());
        temp.put("documentationUrl",apiProperties.getDocumentationUrl());
        temp.put("reservedUris",apiProperties.getReservedUris());
        temp.put("apichainLimit",apiProperties.getApichainLimit());
        temp.put("postcrement",apiProperties.getPostcrement());
        temp.put("chainingEnabled",apiProperties.getChainingEnabled());
        temp.put("batchingEnabled",apiProperties.getBatchingEnabled());
        temp.put("encoding",apiProperties.getEncoding());
        temp.put("iostateDir",apiProperties.getIostateDir());
        temp.put("staticEndpoint",apiProperties.getStaticEndpoint());
        temp.put("supportedFormats",apiProperties.getSupportedFormats());
        temp.put("serverType",apiProperties.getServerType());
        temp.put("autoTest",apiProperties.getAutoTest());

		return temp;
	}


	LinkedHashMap throttleProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("active", apiProperties.getThrottle().getActive());
		temp.put("rateLimit", apiProperties.getThrottle().getRateLimit());
		temp.put("dataLimit", apiProperties.getThrottle().getDataLimit());
		temp.put("expires", apiProperties.getThrottle().getExpires());

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("throttle", temp);
		return map
	}

	// this is what we need to fix
	LinkedHashMap webhookProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		Boolean active = apiProperties.getWebhook().getActive();
		ArrayList<String> services = apiProperties.getWebhook().getServices();
		temp.put("active", active);
		temp.put("service", services);

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("webhook", temp);
		return map
	}

	LinkedHashMap securityProps(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("superuserRole",apiProperties.getSecurity().getSuperuserRole());
		temp.put("userRole",apiProperties.getSecurity().getUserRole());
		temp.put("testRole",apiProperties.getSecurity().getTestRole());
		temp.put("anonRole",apiProperties.getSecurity().getAnonRole());
		temp.put("networkGroups",apiProperties.getSecurity().getNetworkGroups());
		temp.put("networkRoles",apiProperties.getSecurity().getNetworkRoles());

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("security", temp);
		return map
	}

	LinkedHashMap health(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp1 = new LinkedHashMap<>();
		temp1.put("name", appName);
		temp1.put("version", appVersion);
		temp1.put("java_version", System.getProperty("java.version"))

		LinkedHashMap temp2 = new LinkedHashMap<>();
		temp2.put("name", buildProperties.getName());
		temp2.put("time", buildProperties.getTime().toString());
		temp2.put("version", buildProperties.getVersion());
		temp2.put("group", buildProperties.getGroup());
		temp2.put("artifact", buildProperties.getArtifact());

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("application", temp1);
		map.put("build", temp2);

		return map
	}

	LinkedHashMap caches(HttpServletRequest request, HttpServletResponse response) {
		def caches
		caches = cacheManager.getCacheNames()

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("caches", caches);

		return map
	}

	LinkedHashMap beans(HttpServletRequest request, HttpServletResponse response) {
		//println("### beans called")
		ArrayList<String> beans = this.listableBeanFactory.getBeanDefinitionNames()

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("beans", beans);

		return map
	}

	LinkedHashMap logging(HttpServletRequest request, HttpServletResponse response) {
		//println("### logging called")
		String logName = (logFile)?:"beapi.log"
		String logPath = (logFilePath)?:System.getProperty("user.dir")

		String logOutput="";
		try {
			File log = new File("${logPath}/${logName}");
			FileInputStream input = new FileInputStream(log);
			byte[] bf = new byte[(int)log.length()];
			input.read(bf);
			logOutput = new String(bf, "UTF-8");
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("logging", logOutput);

		return map
	}

	LinkedHashMap db(HttpServletRequest request, HttpServletResponse response) {
		//println("### db called")

		/*
		try {
			getActiveSessions.each() {
				println("sessions : ${it} / " + it.getAttribute('user')+" / " + it.getAttribute('ip'))
			}
		}catch(Exception e){
			println("### [ActuactorController :: db] Exception : "+e)
		}
		 */

		LinkedHashMap info = new LinkedHashMap<>();
		info.put("info",getDataSource.getConnection().getMetaData().getURL())

		ArrayList tableList = []

		DatabaseMetaData metaData = getDataSource.getConnection().getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, 'TABLE');

		while (tables.next()) {
			tableList.add(tables.getString("Table_NAME"));
		}
		info.put("tables",tableList)

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("db", info);

		return map
	}

	LinkedHashMap sessions(HttpServletRequest request, HttpServletResponse response) {
		//println("### sessions called")

		LinkedHashMap temp = new LinkedHashMap<>();
		LinkedHashMap sessvals = new LinkedHashMap<>();
		try {
			String currentSession
			getActiveSessions.each() {
				sessvals.put('ip',(String)it.getAttribute('ip'))
				sessvals.put('user',(String)it.getAttribute('user'))
				sessvals.put('throttleCurrentCnt',(String)it.getAttribute('throttleCurrentCnt'))
				sessvals.put('throttleExpiryTime',(String)it.getAttribute('throttleExpiryTime'))

				temp.put('session',(String)it)
				temp.put('values',sessvals)
			}
		}catch(Exception e){
			println("### [ActuactorController :: sessions] Exception : "+e)
		}

		LinkedHashMap map = [:]
		if(temp) {
			map.put("sessions", temp);
		}

		return map
	}

	LinkedHashMap metrics(HttpServletRequest request, HttpServletResponse response) {
		//println("### metrics called")

		ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		CompilationMXBean compile = ManagementFactory.getCompilationMXBean()
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean()
		ThreadMXBean threads = ManagementFactory.getThreadMXBean()
		RuntimeMXBean runtime =	ManagementFactory.getRuntimeMXBean()

		LinkedHashMap temp = new LinkedHashMap<>();

		temp.put("classes", classLoading.getLoadedClassCount())
		temp.put("classes.loaded", classLoading.getTotalLoadedClassCount())
		temp.put("classes.unloaded", classLoading.getUnloadedClassCount())

		temp.put("compiler", compile.getName())
		temp.put("compiler.time", compile.getTotalCompilationTime())

		temp.put("heap", mem.getHeapMemoryUsage())
		temp.put("nonheap", mem.getNonHeapMemoryUsage())

		temp.put("mem", Runtime.getRuntime().freeMemory())
		temp.put("mem.free", Runtime.getRuntime().freeMemory())
		temp.put("processors", os.getAvailableProcessors())
		temp.put("threads", threads.getDaemonThreadCount())
		temp.put("threads.peak", threads.getPeakThreadCount())
		temp.put("threads.totalStarted", threads.getThreadCount())

		temp.put("uptime", runtime.getUptime())
		temp.put("instance.uptime", runtime.getUptime())


		LinkedHashMap map = [:]
		map.put("metrics", temp);

		return map
	}

	LinkedHashMap stats(HttpServletRequest request, HttpServletResponse response) {
		//println("### stats called")
		ArrayList keys = statsCacheService.getCacheKeys()
		LinkedHashMap tmp = new LinkedHashMap<>();
		keys.each(){
			LinkedHashMap cache = statsCacheService.getStatsCache(it)
			if(cache){
				tmp[it] = cache
			}
		}

		LinkedHashMap map = new LinkedHashMap<>();
		map.put("stats", tmp);

		return map
	}

	LinkedHashMap info(HttpServletRequest request, HttpServletResponse response) {
		LinkedHashMap temp = new LinkedHashMap<>();
		temp.put("status", "UP");

		return temp
	}
}
