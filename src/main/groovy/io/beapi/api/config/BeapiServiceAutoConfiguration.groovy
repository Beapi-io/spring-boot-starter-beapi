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

import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.BatchExchangeService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ExchangeService
import io.beapi.api.service.TraceExchangeService
//import io.beapi.api.service.EndpointMappingService
//import io.beapi.api.service.IoStateService
//import io.beapi.api.properties.ApiProperties

import io.beapi.api.service.PrincipleService
import io.beapi.api.service.TraceCacheService
import io.beapi.api.service.TraceService

import io.beapi.api.service.CliService

//import io.beapi.api.filter.RequestInitializationFilter
//import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
//import org.springframework.boot.info.BuildProperties
//import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.ApplicationContext


//@ConditionalOnBean(name = ["principle","apiCacheService"])
//@ConditionalOnClass( name = ['io.beapi.api.interceptor.ApiFrameworkInterceptor'] )
//@EnableWebSecurity(debug=true)
//@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
//@AutoConfigureAfter([org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class,BeapiEhCacheAutoConfiguration.class])
@AutoConfigureAfter([BeapiEhCacheAutoConfiguration.class])
@AutoConfigureBefore([BeapiWebAutoConfiguration.class])
public class BeapiServiceAutoConfiguration {

	//private final org.springframework.boot.autoconfigure.info.ProjectInfoProperties properties;

	@Autowired
	private ApiProperties apiProperties

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	TraceCacheService traceCacheService

	@Autowired
	ApiCacheService apiCacheService

	public BeapiServiceAutoConfiguration() {}



	@Bean
	String appVersion() throws IOException {
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

	@Bean(name='principleService')
	@ConditionalOnMissingBean
	public PrincipleService principleService() throws IOException {
		return new PrincipleService();
	}

	@Bean
	@ConditionalOnMissingBean
	public CliService cliService() throws IOException {
		return new CliService(applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public ExchangeService exchangeService() throws IOException {
		return new ExchangeService(apiCacheService);
	}


	@Bean
	@ConditionalOnMissingBean
	public BatchExchangeService batchService() throws IOException {
		return new BatchExchangeService(apiCacheService, applicationContext);
	}


	@Bean
	@ConditionalOnMissingBean
	public ChainExchangeService chainService() throws IOException {
		return new ChainExchangeService(apiCacheService, applicationContext);
	}

	@Bean(name='traceService')
	@ConditionalOnMissingBean
	public TraceService traceService() throws IOException {
		return new TraceService(traceCacheService)
	}

	@Bean(name='traceExchangeService')
	@ConditionalOnMissingBean
	public TraceExchangeService traceExchangeService() throws IOException {
		return new TraceExchangeService(apiCacheService, traceService())
	}



}
