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
import io.beapi.api.properties.CacheProperties
import io.beapi.api.service.HookCacheService
import io.beapi.api.service.IoStateService
import io.beapi.api.service.ThrottleCacheService
import io.beapi.api.service.TraceCacheService
import net.sf.ehcache.config.CacheConfiguration

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.CacheResolver
import org.springframework.cache.interceptor.KeyGenerator

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.info.BuildProperties

import net.sf.ehcache.config.DiskStoreConfiguration
//import net.sf.ehcache.config.PersistenceConfiguration
//import net.sf.ehcache.config.PersistenceConfiguration.Strategy;

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;


import io.beapi.api.service.ApiCacheService




@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter(org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class)
@AutoConfigureBefore([BeapiWebAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
public class BeapiEhCacheAutoConfiguration implements CachingConfigurer{

    @Autowired
    private CacheProperties cacheProperties;

    @Autowired
    private ApiProperties apiProperties;

    private HashMap evictPolicy = [
            'LRU':net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU,
            'LFU':net.sf.ehcache.store.MemoryStoreEvictionPolicy.LFU,
            'FIFO':net.sf.ehcache.store.MemoryStoreEvictionPolicy.FIFO
    ]

    public BeapiEhCacheAutoConfiguration() {}

    @Bean
    @Override
    public CacheManager cacheManager() {
        EhCacheCacheManager cacheManager = new EhCacheCacheManager(ehCacheManager())
        cacheManager.setTransactionAware(false);
        return cacheManager
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiCacheService apiCacheService() throws IOException { return new ApiCacheService(cacheManager()); }

    @Bean
    @ConditionalOnMissingBean
    public HookCacheService hookCacheService() throws IOException { return new HookCacheService(cacheManager()); }

    @Bean
    @ConditionalOnMissingBean
    public TraceCacheService traceCacheService() throws IOException { return new TraceCacheService(cacheManager()); }

    @Bean
    @ConditionalOnMissingBean
    public ThrottleCacheService throttleCacheService() throws IOException { return new ThrottleCacheService(cacheManager()); }

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() throws Exception{
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "false")

        // user by name cache
        try {
            DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration()
            diskStoreConfiguration.setPath("/tmp")


            // ApiCache
            CacheProperties.ApiProps api = cacheProperties.getApi()
            CacheConfiguration cacheConfig1 = new CacheConfiguration()
            cacheConfig1.setName("ApiCache")
            cacheConfig1.eternal(true)
            cacheConfig1.overflowToDisk(true)
            // cacheConfig1.diskPersistent(true)
            cacheConfig1.diskExpiryThreadIntervalSeconds(api.getDiskExpiryThreadIntervalSeconds())
            cacheConfig1.setMaxElementsInMemory(api.getMaxElementsInMemory())
            cacheConfig1.setMaxElementsOnDisk(api.getMaxElementsOnDisk())
            cacheConfig1.maxEntriesLocalHeap(api.getMaxEntriesLocalHeap())
            cacheConfig1.maxEntriesLocalDisk(api.getMaxEntriesLocalDisk())
            cacheConfig1.memoryStoreEvictionPolicy(evictPolicy[api.getMemoryStoreEvictionPolicy()])


            // (Enterprise ehcache only)
            //PersistenceConfiguration persistConfig = new PersistenceConfiguration();
            //persistConfig.strategy(Strategy.LOCALRESTARTABLE);

            // HookCache
            CacheProperties.HookProps hook = cacheProperties.getHook()
            CacheConfiguration cacheConfig2 = new CacheConfiguration()
            cacheConfig2.setName("HookCache")
            cacheConfig2.eternal(true)
            cacheConfig2.overflowToDisk(true)
            //cacheConfig2.diskPersistent(true)
            //cacheConfig2.persistence(persistConfig);
            cacheConfig2.diskExpiryThreadIntervalSeconds(hook.getDiskExpiryThreadIntervalSeconds())
            cacheConfig2.setMaxElementsInMemory(hook.getMaxElementsInMemory())
            cacheConfig2.setMaxElementsOnDisk(hook.getMaxElementsOnDisk())
            cacheConfig2.maxEntriesLocalHeap(hook.getMaxEntriesLocalHeap())
            cacheConfig2.maxEntriesLocalDisk(hook.getMaxEntriesLocalDisk())
            cacheConfig2.memoryStoreEvictionPolicy(evictPolicy[hook.getMemoryStoreEvictionPolicy()])

            // Throttle
            CacheProperties.ThrottleProps throttle = cacheProperties.getThrottle()
            CacheConfiguration cacheConfig3 = new CacheConfiguration()
            cacheConfig3.setName("Throttle")
            cacheConfig3.eternal(false)
            cacheConfig3.overflowToDisk(true)
            cacheConfig3.diskExpiryThreadIntervalSeconds(throttle.getDiskExpiryThreadIntervalSeconds())
            cacheConfig3.maxEntriesLocalHeap(throttle.getMaxEntriesLocalHeap())
            cacheConfig3.maxEntriesLocalDisk(throttle.getMaxEntriesLocalDisk())
            cacheConfig3.maxEntriesLocalDisk(throttle.getTimeToLiveSeconds())
            cacheConfig3.maxEntriesLocalDisk(throttle.getTimeToIdleSeconds())
            cacheConfig3.memoryStoreEvictionPolicy(evictPolicy[throttle.getMemoryStoreEvictionPolicy()])

            // Trace
            CacheProperties.TraceProps trace = cacheProperties.getTrace()
            CacheConfiguration cacheConfig4 = new CacheConfiguration()
            cacheConfig4.setName("Trace")
            cacheConfig4.eternal(false)
            cacheConfig4.overflowToDisk(false)
            cacheConfig4.diskExpiryThreadIntervalSeconds(trace.getDiskExpiryThreadIntervalSeconds())
            cacheConfig4.maxEntriesLocalHeap(trace.getMaxEntriesLocalHeap())
            cacheConfig4.maxEntriesLocalDisk(trace.getMaxEntriesLocalDisk())
            cacheConfig4.maxEntriesLocalDisk(trace.getTimeToLiveSeconds())
            cacheConfig4.maxEntriesLocalDisk(trace.getTimeToIdleSeconds())
            cacheConfig4.memoryStoreEvictionPolicy(evictPolicy[trace.getMemoryStoreEvictionPolicy()])


            net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration()
            config.addCache(cacheConfig1)
            config.addCache(cacheConfig2)
            config.addCache(cacheConfig3)
            config.addCache(cacheConfig4)
            //config.addDiskStore(diskStoreConfiguration)

            return net.sf.ehcache.CacheManager.newInstance(config)
        }catch(Exception e){
            throw new Exception("[EhCacheAutoConfiguration ] : ERROR - ",e)
        }
    }


    @Override
    public CacheResolver cacheResolver() {
        return null
    }

    @Override
    public KeyGenerator keyGenerator() {
        return null
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return null
    }

    @Bean
    @ConditionalOnBean(name = ["cacheManager"])
    @ConditionalOnMissingBean
    public IoStateService ioService() throws IOException {
        return new IoStateService(apiProperties, applicationContext, apiCache, appVersion());
    }
}
