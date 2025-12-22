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




import io.beapi.api.service.ErrorService
import org.springframework.boot.context.properties.ConfigurationProperties

import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.LinkRelationService
import io.beapi.api.service.MailService
import io.beapi.api.service.SessionService


import io.beapi.api.service.ThrottleService
import io.beapi.api.service.TraceExchangeService
import io.beapi.api.utils.SecretGenerator
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
//import io.beapi.api.service.TraceService
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

import io.beapi.api.filter.RequestInitializationFilter
import io.beapi.api.interceptor.ApiInterceptor
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.PrincipleService
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
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "webconfig")
//@EnableAsync
@ConditionalOnWebApplication
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter([org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
//public class BeapiWebAutoConfiguration implements WebMvcConfigurer{
public class BeapiWebAutoConfiguration{


	@Autowired private ApplicationContext context;
	@Autowired protected LinkRelationService linkRelationService
	@Autowired private PrincipleService principleService
	@Autowired private ErrorService errorService
	@Autowired private ApiCacheService apiCacheService

	@Autowired private ExchangeService exchangeService
	@Autowired private BatchExchangeService batchService
	@Autowired private SessionService sessionService
	@Autowired private ChainExchangeService chainService
	@Autowired private TraceExchangeService traceExchangeService
	@Autowired private ThrottleService throttleService
	@Autowired protected ApiProperties apiProperties

	List publicEndpoint =  ['jwtAuthentication','beapiError']
	String version


	private LinkedHashMap<String, Object> controllers
	private LinkedHashMap<String, Object> restControllers
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BeapiWebAutoConfiguration.class);

	public BeapiWebAutoConfiguration() {
		AppMetadata metadata = new AppMetadata()
		this.version = metadata.getAppVersion()
	}

	// strictly for public endpoints
	/*
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		converters.add(mappingJackson2HttpMessageConverter);
		converters.add(new StringHttpMessageConverter());
	}
	 */


	/**
	 *
	 * @return
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:message");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}




	@Bean(name='mailSender')
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(apiProperties.getMail().getHost());
		mailSender.setPort(apiProperties.getMail().getPort());
		mailSender.setUsername("${apiProperties.getMail().getUsername()}");
		mailSender.setPassword("${apiProperties.getMail().getPassword()}");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.port", apiProperties.getMail().getPort());
		props.put("mail.debug", "true");


		//props.put("mail.transport.protocol", "smtp");
		//props.put("mail.smtp.auth", apiProperties.getMail().getSmtpAuth());
		//props.put("mail.smtp.ssl.enable", "true");
		/*
		props.put("mail.smtp.tls.enable", "true");
		props.put("mail.smtp.starttls.enable", "true");

		props.put("mail.smtp.socketFactory.port", apiProperties.getMail().getPort());
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.debug", "true");
		 */


		return mailSender;
	}

	/**
	 *
	 * @return
	 */
	@Bean(name='mailService')
	public MailService mailService() {
		return new MailService();
	}

	/**
	 *
	 * @return
	 */
	@Bean(name='secretGenerator')
	public SecretGenerator secretGenerator() {
		return new SecretGenerator();
	}

	/*
	@Bean
	public CorsSecurityFilter corsSecurityFilter() {
		return new CorsSecurityFilter(apiProperties, apiCacheService);
	}
	 */

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='exchangeService')
	@ConditionalOnMissingBean
	public ExchangeService exchangeService() throws IOException {
		return new ExchangeService(errorService, linkRelationService(), apiCacheService);
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='batchService')
	@ConditionalOnMissingBean
	public BatchExchangeService batchService() throws IOException {
		return new BatchExchangeService(errorService, apiCacheService, context);
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='chainService')
	@ConditionalOnMissingBean
	public ChainExchangeService chainService() throws IOException {
		return new ChainExchangeService(errorService,apiCacheService, context);
	}

	public LinkedHashMap<String, Object> getRestControllers(){
		return restControllers
	}

	@Bean
	public void setRestControllers(){
		restControllers = context.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestController.class)
	}

	public LinkedHashMap<String, Object> getControllers(){
		return controllers
	}

	@Bean
	public void setControllers(){
		controllers = context.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
	}

	/**
	 *
	 * @return
	 */
	@Bean(name='simpleUrlHandlerMapping')
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		Map<String, Object> urlMap = new LinkedHashMap<>();

		// get list of 'restful controllers' (for checking routing)
		ArrayList restKeys = restControllers.keySet();

		//LinkedHashMap<String, Object> cont = this.listableBeanFactory.getBeansWithAnnotation(org.springframework.stereotype.Controller.class)
		controllers.each() { k, v ->

			if(!restKeys.contains(k)) {
				String controller = k

				def cache = apiCacheService.getApiCache(controller)
				if (cache) {
					if (!publicEndpoint.contains(controller)) {

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
		List origins = getAllowedOrigins()

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
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
			mapping.setInterceptors(new Object[]{new ApiInterceptor(errorService, exchangeService, batchService, chainService, traceExchangeService, apiProperties)})
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

	// CORS whitelisted origins
	private List getAllowedOrigins() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL incoming = classLoader.getResource("META-INF/build-info.properties")

		List origins
		if (incoming != null) {
			Properties properties = new Properties();
			properties.load(incoming.openStream());
			origins = properties.getProperty('api.security.corsWhiteList')
		}
		return origins
	}

	// setOrder for this mapping so it is evaluated FIRST before other HandlerMappings (ie RequestMappingHandlerMapping)
	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handler = super.requestMappingHandlerMapping();
		handler.setOrder(Integer.MAX_VALUE);
		return handler;
	}


	/**
	 *
	 * @return
	 */
	@Bean
	public RequestInitializationFilter requestInitializationFilter() {
		return new RequestInitializationFilter(throttleService, linkRelationService, principleService, apiProperties, apiCacheService, sessionService, version, this.context);
	}

	/**
	 *
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public FilterRegistrationBean requestInitializationFilterRegistration() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(requestInitializationFilter());
		registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+2)
		registrationBean.addUrlPatterns("/v*/**","/b*/**","/c*/**","/t*/**");
		return registrationBean;
	}

/*
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(
				apiProperties.security.corsWhiteList
		);
		config.setAllowedHeaders(Arrays.asList("*"));
		config.addExposedHeader("Access-Control-Allow-Headers");
		config.setAllowCredentials(true);
		config.setAllowedMethods(Arrays.asList("HEAD","GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setExposedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

 */


	/**
	 *
	 * 	mapping needs to include 4 'callTypes' for load balancing:
	 * 	v : regular api call
	 * 	b : batching call
	 * 	c : chain call
	 * 	r : resource call
	 *
	 * 	This allows us the ability to move different call to different servers (should we want/need)
	 * 	so they do not affect 'regular calls' (ie 'v' callType)
	 *
	 * @param controller
	 * @param action
	 * @param apiVersion
	 * @param obj
	 * @return
	 */
	private Map createControllerMappings(String controller, String action, String apiVersion, Object obj) {
		String path = "${controller}/${action}" as String
		Map<String, Object> urlMap = new LinkedHashMap<>();

		try {

			List url = [
					"/v${this.version}/${path}/**" as String,
					"/v${this.version}/${path}/" as String,
					"/v${this.version}/${path}?**" as String,
					"/v${this.version}-${apiVersion}/${path}/**" as String,
					"/v${this.version}-${apiVersion}/${path}/" as String,
					"/v${this.version}-${apiVersion}/${path}?**" as String,
					"/v${this.version}/${controller}/**" as String,
					"/v${this.version}/${controller}/" as String,
					"/v${this.version}-${apiVersion}/${controller}/**" as String,
					"/v${this.version}-${apiVersion}/${controller}/" as String
			]
			url.each() { urlMap.put(it, obj); }

			/*
			* assign batch endpoints
			 */
			if (apiProperties.batchingEnabled) {
				List batchUrl = ["/b${this.version}/${path}/**" as String, "/b${this.version}/${path}/" as String, "/b${this.version}-${apiVersion}/${path}/**" as String, "/b${this.version}-${apiVersion}/${path}/" as String]
				batchUrl.each() { urlMap.put(it, obj); }
			}

			/*
			* assign chaining endpoints
			 */
			if (apiProperties.chainingEnabled) {
				List chainUrl = ["/c${this.version}/${path}/**" as String, "/c${this.version}/${path}/" as String, "/c${this.version}-${apiVersion}/${path}/**" as String, "/c${this.version}-${apiVersion}/${path}/" as String]
				chainUrl.each() { urlMap.put(it, obj); }
			}

			/*
			* assign trace endpoints
			 */
			List traceUrl = ["/t${this.version}/${path}/**" as String, "/t${this.version}/${path}/" as String, "/t${this.version}-${apiVersion}/${path}/**" as String, "/t${this.version}-${apiVersion}/${path}/" as String]
			traceUrl.each() { urlMap.put(it, obj); }

		}catch(Exception e) {
			println("### BeapiWebAutoConfiguration > CreateControllerMappings : Exception : "+e)
		}
		return urlMap
	}

}



