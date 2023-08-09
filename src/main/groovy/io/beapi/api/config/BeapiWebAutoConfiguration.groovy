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
import io.beapi.api.service.LinkRelationService
import io.beapi.api.service.ThrottleCacheService
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
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter([org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
public class BeapiWebAutoConfiguration implements WebMvcConfigurer, BeanFactoryAware{


	@Autowired
	private ApplicationContext context;

	@Autowired
	protected LinkRelationService linkRelationService

	@Autowired
	private PrincipleService principleService

	@Autowired
	private ApiCacheService apiCacheService

	@Autowired
	private ThrottleCacheService throttleCacheService

	@Autowired
	private ExchangeService exchangeService

	@Autowired
	private BatchExchangeService batchService

	@Autowired
	private ChainExchangeService chainService

	@Autowired
	private TraceExchangeService traceExchangeService

	@Autowired
	protected ApiProperties apiProperties


	String version

	private ListableBeanFactory listableBeanFactory;

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BeapiWebAutoConfiguration.class);

	public BeapiWebAutoConfiguration() {
		this.version = getVersion()
	}

/*
	@Override
	public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping("/**")
            .allowedOrigins("http://localhost","http://test.nosegrind.net")
            .allowedMethods("*")
			.allowedHeaders("*")
			.exposedHeaders("*")
            .allowCredentials(true);
	}
*/


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
	public RequestInitializationFilter requestInitializationFilter() {
		return new RequestInitializationFilter(linkRelationService, principleService, apiProperties, apiCacheService, this.version, context);
	}


	/*
	@Bean
	public CorsSecurityFilter corsSecurityFilter() {
		return new CorsSecurityFilter(apiProperties, apiCacheService);
	}
	 */


	@Bean
	@ConditionalOnMissingBean
	public FilterRegistrationBean<RequestInitializationFilter> requestInitializationFilterRegistration() {
		FilterRegistrationBean<RequestInitializationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(requestInitializationFilter());
		registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+2)
		//registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
		//registrationBean.addUrlPatterns("/v*/**","/b*/**","/c*/**","/r*/**");
		return registrationBean;
	}






	// todo : have to do similar method for HandlerMappingRequestMapping
	@Bean(name='simpleUrlHandlerMapping')
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		Map<String, Object> urlMap = new LinkedHashMap<>();

		LinkedHashMap<String, Object> cont = this.listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		cont.each() { k, v ->

			//println(k)
			//println(v)
			//println(v.getClass().getName())

			if(!['beapiErrorController','jwtAuthenticationController'].contains(k)) {
				String controller = k
				def cache = apiCacheService.getApiCache(controller)
				if (cache) {
					if (!apiProperties.publicEndpoint.contains(controller)) {

						ArrayList methodNames = []
						for (Method method : v.getClass().getDeclaredMethods()) {
							methodNames.add(method.getName())
						}

						cache.each() { k2, v2 ->
							if (!['values', 'currentstable', 'cacheversion', 'networkGrp', 'networkGrpRoles'].contains(k2)) {

								for (Map.Entry<Integer, Object> entry : v2.entrySet()) {
									String action = entry.getKey()

									// if IO State 'action' does not match a KNOWN controller/method, do not map
									if (methodNames.contains(action)) {
										//String path = "${controller}/${action}" as String
										urlMap += createControllerMappings(controller, action, k2, v)
									} else {
										logger.debug("simpleUrlHandlerMapping() : {}", "Connector URI '${action}' for connector '${controller}' does not match any given method. Try ${methodNames}")
									}
								}
							}
						}
					}
				}
			}
		}

		Map<String, CorsConfiguration> corsMap = new LinkedHashMap<>();

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList("http://localhost","http://localhost:80","http://localhost:8080", "http://127.0.0.1","http://127.0.0.1:80", "http://test.nosegrind.net","http://test.nosegrind.net/","http://test.nosegrind.net:8080"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.addExposedHeader("Access-Control-Allow-Headers");
		config.setAllowCredentials(true);
		config.setAllowedMethods(Arrays.asList("HEAD","GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setExposedHeaders(Arrays.asList("*"));

		urlMap.each{ k,v ->
			corsMap.put(k, config);
		}

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		//mapping.registerHandlers(urlMap)
		mapping.setUrlMap(urlMap);
		mapping.setOrder(Integer.MAX_VALUE - 5);
		try {
			mapping.setInterceptors(new Object[]{new ApiInterceptor(throttleCacheService, exchangeService, batchService, chainService, traceExchangeService, apiProperties)})
		}catch(Exception e){
			println("Bad Interceptor : "+e)
		}
		mapping.setApplicationContext(context);
		//resourceCache.putAllResources(urlSet);
		try {
			mapping.setCorsConfigurations(corsMap);
		}catch(Exception e){
			println("Bad Corsconfig : "+e)
		}
		return mapping;
	}

	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handler = super.requestMappingHandlerMapping();
		//now i have a handle on the handler i can lower it's priority
		//in the super class implementation this is set to 0
		handler.setOrder(Integer.MAX_VALUE);
		return handler;
	}

	protected Map<String, CorsConfiguration> getCorsConfigurations() {
		CorsRegistry registry = new CorsRegistry()
		registry.addMapping("/**")
				.allowedOrigins("http://localhost","http://test.nosegrind.net","http://test.nosegrind.net/")
				.allowedMethods("*")
				.allowedHeaders("*")
				.exposedHeaders("*")
				.allowCredentials(true);
		return registry.getCorsConfigurations();
	}




    @Bean
    CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList("http://localhost", "http://localhost:80", "http://localhost:8080", "http://127.0.0.1", "http://127.0.0.1:80", "http://test.nosegrind.net", "http://test.nosegrind.net/", "http://test.nosegrind.net:8080"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.addExposedHeader("Access-Control-Allow-Headers");
		config.setAllowCredentials(true);
		config.setAllowedMethods(Arrays.asList("HEAD","GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setExposedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
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
	private Map createControllerMappings(String controller, String action, String apiVersion, Object obj) {
		String path = "${controller}/${action}" as String
		Map<String, Object> urlMap = new LinkedHashMap<>();

		try {

			List url = ["/v${this.version}/${path}/**" as String, "/v${this.version}/${path}/" as String, "/v${this.version}-${apiVersion}/${path}/**" as String, "/v${this.version}-${apiVersion}/${path}/" as String, "/v${this.version}/${controller}/**" as String, "/v${this.version}/${controller}/" as String, "/v${this.version}-${apiVersion}/${controller}/**" as String, "/v${this.version}-${apiVersion}/${controller}/" as String]
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

		}catch(Exception e) {
			println("### BeapiWebAutoConfiguration > CreateControllerMappings : Exception : "+e)
		}
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



