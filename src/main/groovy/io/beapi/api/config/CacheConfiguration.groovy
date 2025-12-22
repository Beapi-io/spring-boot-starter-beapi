package io.beapi.api.config;

import com.github.benmanes.caffeine.cache.Caffeine
import io.beapi.api.service.ApiCacheService;
import io.beapi.api.service.IoStateService
import io.beapi.api.service.SessionService
import io.beapi.api.service.StatsCacheService
import io.beapi.api.service.TraceCacheService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import com.github.benmanes.caffeine.cache.CacheLoader
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

//import org.springframework.cache.CacheManager

import javax.cache.spi.CachingProvider
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.jsr107.Eh107Configuration
import org.ehcache.config.units.MemoryUnit
import org.ehcache.config.builders.ExpiryPolicyBuilder
import javax.cache.Caching;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.core.spi.time.TickingTimeSource;

import org.slf4j.LoggerFactory

@Configuration
@EnableCaching
public class CacheConfiguration {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

	// CAFFEINE CACHE CONFIG
	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager manager = new CaffeineCacheManager();
		manager.registerCustomCache("ApiCache", Caffeine.newBuilder().build());
		manager.registerCustomCache("StatsCache", Caffeine.newBuilder().maximumSize(500).expireAfterAccess(10, TimeUnit.MINUTES).build());
		manager.registerCustomCache("Trace", Caffeine.newBuilder().maximumSize(50).expireAfterAccess(10, TimeUnit.MINUTES).build());
		return manager;
	}

	@Bean(name='sessionService')
	@ConditionalOnMissingBean
	public SessionService sessionService() throws IOException {
		return new SessionService();
	}

	@Bean
	@ConditionalOnMissingBean
	public ApiCacheService apiCacheService() throws IOException { return new ApiCacheService(cacheManager()); }

	@Bean
	@ConditionalOnMissingBean
	public TraceCacheService traceCacheService() throws IOException { return new TraceCacheService(cacheManager()); }

	@Bean
	@ConditionalOnMissingBean
	public StatsCacheService statsCacheService() throws IOException {
		return new StatsCacheService(cacheManager());
	}


}

