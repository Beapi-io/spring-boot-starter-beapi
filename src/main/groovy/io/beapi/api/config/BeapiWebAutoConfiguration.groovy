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

import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.TraceExchangeService
import io.beapi.api.service.TraceService
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

import org.springframework.stereotype.Controller

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
import org.springframework.boot.web.servlet.FilterRegistrationBean

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContext

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.BeansException


//@EnableWebMvc
//@ConditionalOnBean(name = ["principleService","apiCacheService"])
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
//@ConditionalOnClass(RequestMappingHandlerMapping.class)
//@Import(EnableWebMvcConfiguration.class)
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter([org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
public class BeapiWebAutoConfiguration implements WebMvcConfigurer, BeanFactoryAware{


	@Autowired
	ApplicationContext context;

	@Autowired
	PrincipleService principleService

	@Autowired
	ApiCacheService apiCacheService

	@Autowired
	ExchangeService exchangeService

	@Autowired
	BatchExchangeService batchService

	@Autowired
	ChainExchangeService chainService

	@Autowired
	TraceExchangeService traceExchangeService

	@Autowired
	private ApiProperties apiProperties

	@Autowired
	TraceService traceService


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





	//@Bean
	//@ConditionalOnBean(name = ["principle","apiCacheService"])
	//BeapiResponseBodyAdvice beapiResponseAdvice(){
	//	return new BeapiResponseBodyAdvice(principleService,apiCacheService)
	//}

	//@Override
	//public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
	//	messageConverters.add(mappingJackson2HttpMessageConverter());
	//	messageConverters.add(new MappingJackson2HttpMessageConverter());
	//}


	//public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
	//	MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
	//	jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
	//	return jsonConverter;
	//}


	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:message");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public LocalValidatorFactoryBean getValidator() {
		LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
		bean.setValidationMessageSource(messageSource());
		return bean;
	}

	@Bean
	@ConditionalOnMissingBean
	public FilterRegistrationBean<RequestInitializationFilter> requestInitializationFilter() {
		FilterRegistrationBean<RequestInitializationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new RequestInitializationFilter(principleService, apiProperties, apiCacheService, this.version, context));
		registrationBean.setOrder(0)
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	//@Bean
	//public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
	//	return new HttpRequestHandlerAdapter();
	//}



	/*
	* Also create ApiInfo Object
	 */
	@Bean

	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		Map<String, Object> urlMap = new LinkedHashMap<>();

		LinkedHashMap<String, Object> cont = this.listableBeanFactory.getBeansWithAnnotation(Controller.class)
		cont.each() { k, v ->

			String controllerName = k
			String controller = controllerName.minus('Controller')


			if(!apiProperties.nonmappedEndpoint.contains(controller)) {
				ArrayList methodNames = []
				for (Method method : v.getClass().getDeclaredMethods()) {
					methodNames.add(method.getName())
				}

				def cache = apiCacheService.getApiCache(controller)
				if (cache) {
					cache.each() { k2, v2 ->

						if (!['values', 'currentstable', 'cacheversion'].contains(k2)) {
							// 1.  get method from cache and check arraylist for matching method
							// 2. if match, create mapping else move on to next 'cont' name
							for (Map.Entry<Integer, Object> entry : v2.entrySet()) {


								/*
							* mapping need to include 4 'callTypes' for load balancing:
							* v : regular api call
							* b : batching call
							* c : chain call
							* r : resource call
							*
							* This allows us the ability to move different call to different servers (should we want/need)
							* so they do not affect 'regular calls' (ie 'v' callType)
							 */

								String action = entry.getKey()
								if (methodNames.contains(action)) {
									String url1 = "/v${this.version}/${controller}/${action}/**" as String
									urlMap.put(url1, v);
									String url2 = "/v${this.version}-${k2}/${controller}/${action}/**" as String
									urlMap.put(url2, v);

									if (apiProperties.batchingEnabled) {
										String batch1 = "/b${this.version}/${controller}/${action}/**" as String
										urlMap.put(batch1, v);
										String batch2 = "/b${this.version}-${k2}/${controller}/${action}/**" as String
										urlMap.put(batch2, v);
										String batch3 = "/b${this.version}/${controller}/${action}/" as String
										urlMap.put(batch3, v);
										String batch4 = "/b${this.version}-${k2}/${controller}/${action}/" as String
										urlMap.put(batch4, v);
									}

									if (apiProperties.chainingEnabled) {
										String chain1 = "/c${this.version}/${controller}/${action}/**" as String
										urlMap.put(chain1, v);
										String chain2 = "/c${this.version}-${k2}/${controller}/${action}/**" as String
										urlMap.put(chain2, v);
										String chain3 = "/c${this.version}/${controller}/${action}/" as String
										urlMap.put(chain3, v);
										String chain4 = "/c${this.version}-${k2}/${controller}/${action}/" as String
										urlMap.put(chain4, v);
									}

/*
									String res1 = "/r${this.version}/${controller}/${action}/**" as String
									urlMap.put(res1, v);
									String res2 = "/r${this.version}-${k2}/${controller}/${action}/**" as String
									urlMap.put(res2, v);
									String res3 = "/r${this.version}/${controller}/${action}/" as String
									urlMap.put(res3, v);
									String res4 = "/r${this.version}-${k2}/${controller}/${action}/" as String
									urlMap.put(res4, v);

 */


									String trace1 = "/t${this.version}/${controller}/${action}/**" as String
									urlMap.put(trace1, v);
									String trace2 = "/t${this.version}-${k2}/${controller}/${action}/**" as String
									urlMap.put(trace2, v);
									String trace3 = "/t${this.version}/${controller}/${action}/" as String
									urlMap.put(trace3, v);
									String trace4 = "/t${this.version}-${k2}/${controller}/${action}/" as String
									urlMap.put(trace4, v);

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
				new ApiInterceptor(exchangeService, batchService, chainService, traceExchangeService, principleService, apiProperties)
		})
		mapping.setApplicationContext(context);
		//resourceCache.putAllResources(urlSet);

		return mapping;
	}


}



