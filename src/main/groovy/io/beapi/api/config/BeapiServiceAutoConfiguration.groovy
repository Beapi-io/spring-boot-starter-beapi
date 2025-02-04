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
import io.beapi.api.service.BootstrapService
import io.beapi.api.service.ChainExchangeService
import io.beapi.api.service.ConnectorScaffoldService
import io.beapi.api.service.ErrorService
import io.beapi.api.service.LinkRelationService
import io.beapi.api.service.SessionService
import io.beapi.api.service.StatsCacheService
import io.beapi.api.service.StatsService

import io.beapi.api.service.ExchangeService
import io.beapi.api.service.ThrottleService
import io.beapi.api.service.TraceExchangeService
//import io.beapi.api.service.EndpointMappingService
//import io.beapi.api.service.IoStateService
import io.beapi.api.service.PrincipleService
import io.beapi.api.service.TraceCacheService
//import io.beapi.api.service.HookCacheService
import io.beapi.api.service.TraceService
import io.beapi.api.service.CliService
//import io.beapi.api.service.WebHookService

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


@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter([BeapiEhCacheAutoConfiguration.class])
@AutoConfigureBefore([BeapiWebAutoConfiguration.class])
public class BeapiServiceAutoConfiguration {

	@Autowired protected ApplicationContext applicationContext;
	@Autowired protected TraceCacheService traceCacheService
	@Autowired protected ApiCacheService apiCacheService
	@Autowired protected StatsCacheService statsCacheService
	@Autowired protected ApiProperties apiProperties

	public BeapiServiceAutoConfiguration() {}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='appVersion')
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


	@Bean(name='errorService')
	@ConditionalOnMissingBean
	public ErrorService errorService() throws IOException {
		return new ErrorService();
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='principleService')
	@ConditionalOnMissingBean
	public PrincipleService principleService() throws IOException {
		return new PrincipleService();
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='cliService')
	@ConditionalOnMissingBean
	public CliService cliService() throws IOException {
		return new CliService();
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='connScaffoldService')
	@ConditionalOnMissingBean
	public ConnectorScaffoldService connScaffoldService() throws IOException {
		return new ConnectorScaffoldService(applicationContext);
	}


	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='linkRelationService')
	@ConditionalOnMissingBean
	public LinkRelationService linkRelationService() throws IOException {
		return new LinkRelationService(apiCacheService, principleService());
	}

	/*
	@Bean(name='webHookService')
	@ConditionalOnMissingBean
	public WebHookService webHookService() throws IOException {
		return new WebHookService(apiCacheService, principleService());
	}
	 */



	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='traceService')
	@ConditionalOnMissingBean
	public TraceService traceService() throws IOException {
		return new TraceService(traceCacheService)
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='traceExchangeService')
	@ConditionalOnMissingBean
	public TraceExchangeService traceExchangeService() throws IOException {
		return new TraceExchangeService(apiCacheService, traceService())
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='statsService')
	@ConditionalOnMissingBean
	public StatsService statsService() throws IOException {
		return new StatsService(statsCacheService)
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	@Bean(name='throttleService')
	@ConditionalOnMissingBean
	public ThrottleService throttleService() throws IOException {
		return new ThrottleService(apiProperties, sessionService)
	}


}
