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
//import io.beapi.api.service.ApiCacheService
import io.beapi.api.service.IoStateService
import io.beapi.api.service.TraceCacheService
import net.sf.ehcache.config.CacheConfiguration
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
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
//import org.springframework.stereotype.Component;


//import org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Build
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
//import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration

import io.beapi.api.service.ApiCacheService

//import java.beans.beancontext.BeanContext


@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties([ApiProperties.class])
@AutoConfigureAfter(org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class)
@AutoConfigureBefore([BeapiWebAutoConfiguration.class,BeapiServiceAutoConfiguration.class])
public class BeapiEhCacheAutoConfiguration  implements CachingConfigurer {

    @Autowired
    private ApiProperties apiProperties;

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
    public TraceCacheService traceCacheService() throws IOException { return new TraceCacheService(cacheManager()); }

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "false")

        // user by name cache
        try {
            DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration()
            diskStoreConfiguration.setPath("/tmp")

            // ApiCache
            CacheConfiguration cacheConfig1 = new CacheConfiguration()
            cacheConfig1.setName("ApiCache")
            cacheConfig1.eternal(true)
            cacheConfig1.overflowToDisk(true)
            cacheConfig1.diskExpiryThreadIntervalSeconds(120)
            cacheConfig1.setMaxElementsInMemory(10000)
            cacheConfig1.setMaxElementsOnDisk(100000)
            cacheConfig1.maxEntriesLocalHeap(10000)
            cacheConfig1.maxEntriesLocalDisk(100000)
            //cacheConfig1.timeToLiveSeconds(0)
            //cacheConfig1.timeToIdleSeconds(3000)
            cacheConfig1.memoryStoreEvictionPolicy(net.sf.ehcache.store.MemoryStoreEvictionPolicy.FIFO)


            // Stats
            /*
            CacheConfiguration cacheConfig2 = new CacheConfiguration()
            cacheConfig2.setName("Stats")
            cacheConfig2.eternal(false)
            cacheConfig2.overflowToDisk(true)
            cacheConfig2.diskExpiryThreadIntervalSeconds(120)
            cacheConfig2.maxEntriesLocalHeap(1000)
            cacheConfig2.maxEntriesLocalDisk(5000)
            cacheConfig2.timeToLiveSeconds(120)
            cacheConfig2.timeToIdleSeconds(0)
            cacheConfig2.memoryStoreEvictionPolicy(net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU)
            */

            // Throttle
            /*
            CacheConfiguration cacheConfig3 = new CacheConfiguration()
            cacheConfig3.setName("Throttle")
            cacheConfig3.eternal(false)
            cacheConfig3.overflowToDisk(true)
            cacheConfig3.diskExpiryThreadIntervalSeconds(120)
            cacheConfig3.maxEntriesLocalHeap(1000)
            cacheConfig3.maxEntriesLocalDisk(5000)
            cacheConfig3.timeToLiveSeconds(120)
            cacheConfig3.timeToIdleSeconds(0)
            cacheConfig3.memoryStoreEvictionPolicy(net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU)
            */

            // Trace
            CacheConfiguration cacheConfig4 = new CacheConfiguration()
            cacheConfig4.setName("Trace")
            cacheConfig4.eternal(false)
            cacheConfig4.overflowToDisk(false)
            cacheConfig4.diskExpiryThreadIntervalSeconds(30)
            cacheConfig4.maxEntriesLocalHeap(1000)
            cacheConfig4.maxEntriesLocalDisk(500)
            cacheConfig4.timeToLiveSeconds(0)
            cacheConfig4.timeToIdleSeconds(30)
            cacheConfig4.memoryStoreEvictionPolicy(net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU)



            net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration()
            config.addCache(cacheConfig1)
            //config.addCache(cacheConfig2)
            //config.addCache(cacheConfig3)
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
