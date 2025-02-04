package io.beapi.api.properties

import io.beapi.api.properties.yaml.factory.YamlPropertySourceFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@Configuration
@ConfigurationProperties(prefix = "cache")
@PropertySources([
        @PropertySource(value = "file:\${user.home}/.boot/\${spring.profiles.active}/beapi_cache.yaml", factory = YamlPropertySourceFactory.class)
])
public class CacheProperties {

    public ApiProps api = new ApiProps()
    public HookProps hook = new HookProps()
    public StatsProps stats = new StatsProps()
    public TraceProps trace = new TraceProps()

    public ApiProps getApi(){ return this.api; }
    public HookProps getHook(){ return this.hook; }
    public StatsProps getStats(){ return this.stats; }
    public TraceProps getTrace(){ return this.trace; }

    public void setApi(ApiProps api){ this.api = api }
    public void setHook(HookProps hook){ this.hook = hook }
    public void setStats(StatsProps stats){ this.stats = stats }
    public void setTrace(TraceProps trace){ this.trace = trace }


    public static class ApiProps {
        private Long diskExpiryThreadIntervalSeconds
        private Integer maxElementsInMemory
        private Integer maxElementsOnDisk
        private Integer maxEntriesLocalHeap
        private Integer maxEntriesLocalDisk
        private String memoryStoreEvictionPolicy

        public Long getDiskExpiryThreadIntervalSeconds(){ return diskExpiryThreadIntervalSeconds }
        public Integer getMaxElementsInMemory(){ return maxElementsInMemory }
        public Integer getMaxElementsOnDisk(){ return maxElementsOnDisk }
        public Integer getMaxEntriesLocalHeap(){ return maxEntriesLocalHeap }
        public Integer getMaxEntriesLocalDisk(){ return maxEntriesLocalDisk }
        public String getMemoryStoreEvictionPolicy(){ return memoryStoreEvictionPolicy }

        void setDiskExpiryThreadIntervalSeconds(Long diskExpiryThreadIntervalSeconds){ this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds }
        void setMaxElementsInMemory(Integer maxElementsInMemory){ this.maxElementsInMemory = maxElementsInMemory }
        void setMaxElementsOnDisk(Integer maxElementsOnDisk){ this.maxElementsOnDisk = maxElementsOnDisk }
        void setMaxEntriesLocalHeap(Integer maxEntriesLocalHeap){ this.maxEntriesLocalHeap = maxEntriesLocalHeap }
        void setMaxEntriesLocalDisk(Integer maxEntriesLocalDisk){ this.maxEntriesLocalDisk = maxEntriesLocalDisk }
        void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy){ this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy }
    }

    public static class HookProps {
        private Long diskExpiryThreadIntervalSeconds
        private Long maxElementsInMemory
        private Integer maxElementsOnDisk
        private Integer maxEntriesLocalHeap
        private Integer maxEntriesLocalDisk
        private String memoryStoreEvictionPolicy

        public Long getDiskExpiryThreadIntervalSeconds(){ return diskExpiryThreadIntervalSeconds }
        public Integer getMaxElementsInMemory(){ return maxElementsInMemory }
        public Integer getMaxElementsOnDisk(){ return maxElementsOnDisk }
        public Integer getMaxEntriesLocalHeap(){ return maxEntriesLocalHeap }
        public Integer getMaxEntriesLocalDisk(){ return maxEntriesLocalDisk }
        public String getMemoryStoreEvictionPolicy(){ return memoryStoreEvictionPolicy }

        void setDiskExpiryThreadIntervalSeconds(Long diskExpiryThreadIntervalSeconds){ this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds }
        void setMaxElementsInMemory(Integer maxElementsInMemory){ this.maxElementsInMemory = maxElementsInMemory }
        void setMaxElementsOnDisk(Integer maxElementsOnDisk){ this.maxElementsOnDisk = maxElementsOnDisk }
        void setMaxEntriesLocalHeap(Integer maxEntriesLocalHeap){ this.maxEntriesLocalHeap = maxEntriesLocalHeap }
        void setMaxEntriesLocalDisk(Integer maxEntriesLocalDisk){ this.maxEntriesLocalDisk = maxEntriesLocalDisk }
        void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy){ this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy }
    }

    public static class StatsProps {
        private Long diskExpiryThreadIntervalSeconds
        private Long maxElementsInMemory
        private Integer maxElementsOnDisk
        private Integer maxEntriesLocalHeap
        private Integer maxEntriesLocalDisk
        private String memoryStoreEvictionPolicy

        public Long getDiskExpiryThreadIntervalSeconds(){ return diskExpiryThreadIntervalSeconds }
        public Integer getMaxElementsInMemory(){ return maxElementsInMemory }
        public Integer getMaxElementsOnDisk(){ return maxElementsOnDisk }
        public Integer getMaxEntriesLocalHeap(){ return maxEntriesLocalHeap }
        public Integer getMaxEntriesLocalDisk(){ return maxEntriesLocalDisk }
        public String getMemoryStoreEvictionPolicy(){ return memoryStoreEvictionPolicy }

        void setDiskExpiryThreadIntervalSeconds(Long diskExpiryThreadIntervalSeconds){ this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds }
        void setMaxElementsInMemory(Integer maxElementsInMemory){ this.maxElementsInMemory = maxElementsInMemory }
        void setMaxElementsOnDisk(Integer maxElementsOnDisk){ this.maxElementsOnDisk = maxElementsOnDisk }
        void setMaxEntriesLocalHeap(Integer maxEntriesLocalHeap){ this.maxEntriesLocalHeap = maxEntriesLocalHeap }
        void setMaxEntriesLocalDisk(Integer maxEntriesLocalDisk){ this.maxEntriesLocalDisk = maxEntriesLocalDisk }
        void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy){ this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy }
    }

    public static class TraceProps {
        private Long diskExpiryThreadIntervalSeconds
        private Integer maxEntriesLocalHeap
        private Integer maxEntriesLocalDisk
        private Integer timeToLiveSeconds
        private Integer timeToIdleSeconds
        private String memoryStoreEvictionPolicy

        public Long getDiskExpiryThreadIntervalSeconds(){ return diskExpiryThreadIntervalSeconds }
        public Integer getMaxEntriesLocalHeap(){ return maxEntriesLocalHeap }
        public Integer getMaxEntriesLocalDisk(){ return maxEntriesLocalDisk }
        public Integer getTimeToLiveSeconds(){ return timeToLiveSeconds }
        public Integer getTimeToIdleSeconds(){ return timeToIdleSeconds }
        public String getMemoryStoreEvictionPolicy(){ return memoryStoreEvictionPolicy }


        void setDiskExpiryThreadIntervalSeconds(Long diskExpiryThreadIntervalSeconds){ this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds }
        void setMaxEntriesLocalHeap(Integer maxEntriesLocalHeap){ this.maxEntriesLocalHeap = maxEntriesLocalHeap }
        void setMaxEntriesLocalDisk(Integer maxEntriesLocalDisk){ this.maxEntriesLocalDisk = maxEntriesLocalDisk }
        void setTimeToLiveSeconds(Integer timeToLiveSeconds){ this.timeToLiveSeconds = timeToLiveSeconds }
        void setTimeToIdleSeconds(Integer timeToIdleSeconds){ this.timeToIdleSeconds = timeToIdleSeconds }
        void setMemoryStoreEvictionPolicy(String memoryStoreEvictionPolicy){ this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy }
    }
}
