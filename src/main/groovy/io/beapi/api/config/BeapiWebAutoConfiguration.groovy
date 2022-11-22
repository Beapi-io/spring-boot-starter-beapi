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
package io.beapi.api.config


//import io.beapi.api.filter.CorsSecurityFilter
import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.TraceExchangeService
//import io.beapi.api.service.TraceService
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

import io.beapi.api.filter.RequestInitializationFilter
import io.beapi.api.interceptor.ApiInterceptor
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.context.MessageSource
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContext
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.BeansException
import org.springframework.boot.autoconfigure.security.SecurityProperties;





@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter([org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
public class BeapiWebAutoConfiguration implements WebMvcConfigurer, BeanFactoryAware{


	@Autowired
	private ApplicationContext context;

	@Autowired
	PrincipleService principleService

	@Autowired
	private ApiCacheService apiCacheService

	@Autowired
	private ExchangeService exchangeService

	@Autowired
	private BatchExchangeService batchService

	@Autowired
	private ChainExchangeService chainService

	@Autowired
	private TraceExchangeService traceExchangeService

	@Autowired
	ApiProperties apiProperties


	String version

	private ListableBeanFactory listableBeanFactory;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BeapiWebAutoConfiguration.class);

	public BeapiWebAutoConfiguration() {
		this.version = getVersion()
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.listableBeanFactory = (ListableBeanFactory) beanFactory;
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

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:message");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	//@Bean
	//public LocalValidatorFactoryBean getValidator() {
	//	LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
	//	bean.setValidationMessageSource(messageSource());
	//	return bean;
	//}


	@Bean
	@ConditionalOnMissingBean
	public FilterRegistrationBean<RequestInitializationFilter> requestInitializationFilter() {
		FilterRegistrationBean<RequestInitializationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new RequestInitializationFilter(principleService, apiProperties, apiCacheService, this.version, context));
		registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+1)
		//registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}


	@Bean(name='simpleUrlHandlerMapping')
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {

		Map<String, Object> urlMap = new LinkedHashMap<>();
		Map<String, CorsConfiguration> corsMap = new LinkedHashMap<>();

		LinkedHashMap<String, Object> cont = this.listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		cont.each() { k, v ->

			String controller = k
			def cache = apiCacheService.getApiCache(controller)
			if(cache) {
				if (!apiProperties.publicEndpoint.contains(controller)) {

					ArrayList methodNames = []
					for (Method method : v.getClass().getDeclaredMethods()) { methodNames.add(method.getName()) }

					cache.each() { k2, v2 ->
						if (!['values', 'currentstable', 'cacheversion','networkGrp','networkGrpRoles'].contains(k2)) {
							for (Map.Entry<Integer, Object> entry : v2.entrySet()) {
								String action = entry.getKey()

								// if IO State 'action' does not match a KNOWN controller/method, do not map
								if (methodNames.contains(action)) {

									String path = "${controller}/${action}" as String
									urlMap += createControllerMappings(path, k2, v)

								} else {
									logger.debug("simpleUrlHandlerMapping() : {}", "Connector URI '${action}' for connector '${controller}' does not match any given method. Try ${methodNames}")
								}
							}
						}
					}

				}
			}
		}


		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.registerHandlers(urlMap)
		mapping.setUrlMap(urlMap);
		mapping.setOrder(1);
		mapping.setInterceptors(new Object[]{
				new ApiInterceptor(exchangeService, batchService, chainService, traceExchangeService, apiProperties)
		})
		mapping.setApplicationContext(context);
		//resourceCache.putAllResources(urlSet);
		//mapping.setCorsConfigurations(corsMap);

		return mapping;
	}


	/*
	* mapping needs to include 4 'callTypes' for load balancing:
	* v : regular api call
	* b : batching call
	* c : chain call
	* r : resource call
	*
	* This allows us the ability to move different call to different servers (should we want/need)
	* so they do not affect 'regular calls' (ie 'v' callType)
	*/
	private Map createControllerMappings(String path, String apiVersion, Object obj){
		Map<String, Object> urlMap = new LinkedHashMap<>();
		List url = ["/v${this.version}/${path}/**" as String, "/v${this.version}/${path}/" as String, "/v${this.version}-${apiVersion}/${path}/**" as String, "/v${this.version}-${apiVersion}/${path}/" as String]
		url.each() { urlMap.put(it, obj); }

		if (apiProperties.batchingEnabled) {
			List batchUrl = ["/b${this.version}/${path}/**" as String, "/b${this.version}/${path}/" as String, "/b${this.version}-${apiVersion}/${path}/**" as String, "/b${this.version}-${apiVersion}/${path}/" as String]
			batchUrl.each() { urlMap.put(it, obj); }
		}

		if (apiProperties.chainingEnabled) {
			List chainUrl = ["/c${this.version}/${path}/**" as String, "/c${this.version}/${path}/" as String, "/c${this.version}-${apiVersion}/${path}/**" as String, "/c${this.version}-${apiVersion}/${path}/" as String]
			chainUrl.each() { urlMap.put(it, obj); }
		}

		List traceUrl = ["/t${this.version}/${path}/**" as String, "/t${this.version}/${path}/" as String, "/t${this.version}-${apiVersion}/${path}/**" as String, "/t${this.version}-${apiVersion}/${path}/" as String]
		traceUrl.each() { urlMap.put(it, obj); }

		return urlMap
	}


	/*
	* mapping for CORS; takes keySet from 'createMappings' and uses it to create CORS mappings
	 */
	/*
	private Map createCorsMappings(Map corsMap, Set paths,String networkGrp) {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		LinkedHashMap corsNetworkGroups = apiProperties.security.corsNetworkGroups

		ArrayList networkGrps = []
		corsNetworkGroups[networkGrp].each { k,v ->
			networkGrps.add(v)
		}

		corsConfiguration.setAllowedOrigins(networkGrps);
		paths.each(){
			corsMap.put(it, corsConfiguration);
		}

		return corsMap
	}

	@Bean
	CorsConfiguration corsConfig() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		LinkedHashMap corsNetworkGroups = apiProperties.security.corsNetworkGroups

		ArrayList networkGrps = []
		corsNetworkGroups[networkGrp].each { k,v ->
			networkGrps.add(v)
		}

		corsConfiguration.setAllowedOrigins(networkGrps);

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

 */


}



