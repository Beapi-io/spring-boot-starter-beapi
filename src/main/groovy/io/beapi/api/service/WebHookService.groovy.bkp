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

import io.beapi.api.domain.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import io.beapi.api.repositories.HookRepository;
import io.beapi.api.domain.Hook;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static groovyx.gpars.GParsPool.withPool;
import javax.servlet.http.HttpServletRequest;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import groovy.transform.CompileStatic
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import org.springframework.beans.factory.ListableBeanFactory

@Service
public class WebHookService{

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	ApiCacheService apiCacheService
	PrincipleService principle;


	public WebHookService(ApiCacheService apiCacheService, PrincipleService principle) {
		this.principle = principle
		this.apiCacheService = apiCacheService
	}


	/**
	 * Given the data to be sent and 'service' for which hook is defined,
	 * will send data to all 'subscribers'
	 * @param String URI of local endpoint being hooked into
	 * @param String data to be sent to all subscribers
	 * @return
	 */
	private boolean send(String data, String service, String key) {
		def hooks = findByEnabledTrueAndService(service);
		//def hooks = Class.forName('net.nosegrind.apiframework.Hook').findAll("from Hook where is_enabled=true and service=?",[service])

		GParsPool.withPool(this.cores){ pool ->

			HttpURLConnection myConn = null;
			DataOutputStream os = null;
			BufferedReader stdInput = null;

			hooks.eachParallel(){ Hook hook ->
				String format = hook.format.toLowerCase();
				if (hook.attempts >= grailsApplication.config.apitoolkit.attempts) {
					data = [message:'Number of attempts exceeded. Please reset hook via web interface'];
				}else{
					String encData
					try{
						encData = encode(key,data)
					}catch(Exception e){
						println("###BAD ENCRYPTION FOR HOOK ###")
					}
					data = 	[message:encData];
				}


				try {
					URL hostURL = new URL(hook.callback.toString());
					myConn = (HttpURLConnection) hostURL.openConnection();
					myConn.setRequestMethod("POST");
					myConn.setRequestProperty("Content-Type", "application/json");
					if (hook?.authorization) {
						myConn.setRequestProperty("Authorization", "${hook.authorization}");
					}
					myConn.setUseCaches(false);
					myConn.setDoInput(true);
					myConn.setDoOutput(true);
					myConn.setReadTimeout(15 * 1000);

					myConn.connect();

					OutputStreamWriter out = new OutputStreamWriter(myConn.getOutputStream());
					out.write(data);
					out.close();

					int code = myConn.getResponseCode();
					myConn.diconnect();

					return code;
				} catch (Exception e) {
					try {
						Thread.sleep(15000);
					} catch (InterruptedException ie) {
						println(e);
					}
				} finally {
					if (myConn != null) {
						myConn.disconnect();
					}
				}
				return 400;
			}
		}
	}

	/**
	 * Given the data to be sent and 'service' for which hook is defined,
	 * will send error message to all 'subscribers'
	 * @param String URI of local endpoint being hooked into
	 * @param String data to be sent to all subscribers
	 * @return
	 */
	private boolean sendErr(String data, String service, String key) {
		def hooks = findByEnabledTrueAndService(service);
		//def hooks = grailsApplication.getClassForName('net.nosegrind.apiframework.Hook').findAll("from Hook where is_enabled=true and service=?",[service])

		hooks.each { hook ->
			String format = hook.format.toLowerCase();

			String encData
			try{
				encData = encode(key,data)
			}catch(Exception e){
				println("###BAD ENCRYPTION FOR HOOK ###")
			}
			String message = 	[message:encData];


			HttpURLConnection myConn= null;
			DataOutputStream os = null;
			BufferedReader stdInput = null;
			try{
				URL hostURL = new URL(hook.callback.toString());
				myConn= (HttpURLConnection)hostURL.openConnection();
				myConn.setRequestMethod("POST");
				myConn.setRequestProperty("Content-Type", "application/json");
				if(hook?.authorization) {
					myConn.setRequestProperty("Authorization", "${hook.authorization}");
				}
				myConn.setUseCaches(false);
				myConn.setDoInput(true);
				myConn.setDoOutput(true);
				myConn.setReadTimeout(15*1000);

				myConn.connect();

				OutputStreamWriter out = new OutputStreamWriter(myConn.getOutputStream());
				out.write(message);
				out.close();

				int code =  myConn.getResponseCode();
				myConn.disconnect();

				return code;
			}catch (Exception e){
				try{
					Thread.sleep(15000);
				}catch (InterruptedException ie){
					println(e);
				}
			} finally{
				if (myConn!= null){
					myConn.disconnect();
				}
			}
			return 400;
		}
	}

	/**
	 * Determines if object is valid domain class and return object as a Map
	 * @param Object an object assumed to be a domain object
	 * @return Data as a Map
	 */
	Map formatDomainObject(Object data){
		def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass","maps"];
		def newMap = [:];
		data.getProperties().each { key, val ->
			if (!nonPersistent.contains(key)) {
				if(grailsApplication.isDomainClass(val.getClass())){
					newMap.put key, val.id;
				}else{
					newMap.put key, val;
				}
			}
		}
		return newMap;
	}

	/*
	Map processMap(Map data,Map processor){
		processor.each() { key, val ->
			if(!val?.trim()){
				data.remove(key)
			}else{
				def matcher = "${data[key]}" =~ "${data[key]}"
				data[key] = matcher.replaceAll(val)
			}
		}
		return data
	}

	boolean validateUrl(String url){
		try {
			String[] schemes = ["http", "https"]
			UrlValidator urlValidator = new UrlValidator(schemes)
			if (urlValidator.isValid(url)) {
				return true
			} else {
				return false
			}
		}catch(Exception e){
			println(e)
		}
		return false
	}
	*/

	//byte[] hash = encode("secret", "Message")
	//String encodedData = hash.encodeBase64().toString()
	@groovy.transform.CompileStatic
	public String encode(String publicKey, String data) {
		// println("### ENCODE ###")

		byte[] byteArray1 = "abcd".getBytes()
		byte[] byteArray2 = "efgh".getBytes(StandardCharsets.US_ASCII)
		byte[] byteArray3 = "ijkl".getBytes("UTF-8")
		byte[] expected1 = [ 97, 98, 99, 100 ] as byte[]
		byte[] expected2 = [ 101, 102, 103, 104 ] as byte[]
		byte[] expected3 = [ 105, 106, 107, 108 ] as byte[]

		assert(Arrays.equals(expected1,byteArray1));
		assert(Arrays.equals(expected2,byteArray2));
		assert(Arrays.equals(expected3,byteArray3));

		final byte[]  rawKey = publicKey.getBytes(StandardCharsets.UTF_8) as byte[]
		//byte rawData = data.getBytes(StandardCharsets.UTF_8);

		//HashFunction hashFunction = Hashing.hmacSha256(rawKey);
		//HashCode saltedMessage = hashFunction.hashBytes(rawData);

		HashFunction hashFunc = Hashing.hmacSha256(rawKey);
		HashCode saltedMessage = hashFunc.hashString(data, StandardCharsets.UTF_8);
		return saltedMessage.toString();
	}

	public boolean isSignatureValid(String signature, String publicKey, String data) {
		String calculatedHash = encode(publicKey, data);
		//println calculatedHash.length
		//println signature.length
		return calculatedHash==signature
	}

	public ArrayList getHookServices(){
		LinkedHashMap<String, Object> cont = listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		String version = getVersion()
		String authority = principle.authorities()
		ArrayList services = []
		try {
			cont.each() { k, v ->

				def cache = apiCacheService?.getApiCache(k)

				// todo : if no action, default to apidoc/show/id
				if (cache) {

					ArrayList ignoreList = ['deprecated', 'defaultAction', 'testOrder']

					String apiVersion = cache['currentstable']
					ArrayList keyList = cache[apiVersion].keySet()
					ignoreList.intersect(keyList).each() { it -> keyList.remove(it) }

					//this.deprecated = temp['deprecated'] as List
					keyList.each() { action ->
						def temp = cache[apiVersion]
						def apiObject = temp[action]
						if (apiObject['hookRoles']) {
							if (apiObject['hookRoles'].contains(authority)) {
								services.add("${k}/${action}".toString())
							}
						}
					}
				}
			}
		}catch(Exception e){
			println("WebhookService Exception : "+e)
		}
		return services
	}


	private String getVersion() throws IOException {
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
}
