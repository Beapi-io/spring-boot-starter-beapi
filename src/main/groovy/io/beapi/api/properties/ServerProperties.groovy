package io.beapi.api.properties

import io.beapi.api.properties.yaml.factory.YamlPropertySourceFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "server")
@PropertySource(value = "file:\${user.home}/.boot/\${spring.profiles.active}/beapi_server.yaml", factory = YamlPropertySourceFactory.class)
public class ServerProperties {


    public NanoProps nano = new NanoProps()
    public MediumProps medium = new MediumProps()
    public LargeProps large = new LargeProps()

    public NanoProps getNano(){ return this.nano; }
    public MediumProps getMedium(){ return this.medium; }
    public LargeProps getLarge(){ return this.large; }

    public void setNano(NanoProps nano){ this.nano = nano }
    public void setMedium(MediumProps medium){ this.medium = medium }
    public void setLarge(LargeProps large){ this.large = large }


    public static class NanoProps {
        private Integer backgroundProcessorDelay;
        private Integer maxThreads;
        private Integer minSpareThreads;
        private Integer maxConnections;
        private String uriEncoding;
        private Boolean compression;
        private ArrayList compressableMimeTypes;

        public Integer getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public Integer getMaxThreads() { return maxThreads; }
        public Integer getMinSpareThreads() { return minSpareThreads; }
        public Integer getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public Boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(Integer backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(Integer maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(Integer minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(Boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }


    public static class MediumProps {
        private Integer backgroundProcessorDelay;
        private Integer maxThreads;
        private Integer minSpareThreads;
        private Integer maxConnections;
        private String uriEncoding;
        private Boolean compression;
        private ArrayList compressableMimeTypes;

        public Integer getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public Integer getMaxThreads() { return maxThreads; }
        public Integer getMinSpareThreads() { return minSpareThreads; }
        public Integer getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public Boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(Integer backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(Integer maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(Integer minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(Boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }

    public static class LargeProps {
        private Integer backgroundProcessorDelay;
        private Integer maxThreads;
        private Integer minSpareThreads;
        private Integer maxConnections;
        private String uriEncoding;
        private Boolean compression;
        private ArrayList compressableMimeTypes;

        public Integer getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public Integer getMaxThreads() { return maxThreads; }
        public Integer getMinSpareThreads() { return minSpareThreads; }
        public Integer getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public Boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(Integer backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(Integer maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(Integer minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(Boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }

}
