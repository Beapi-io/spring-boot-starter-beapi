package io.beapi.api.properties

import io.beapi.api.properties.yaml.factory.YamlPropertySourceFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ConfigurationProperties(prefix = "server")
@PropertySources([
        @PropertySource(value="classpath:beapi_server.yaml", factory=YamlPropertySourceFactory.class),
        @PropertySource(value = "file:\${user.home}/.boot/\${spring.profiles.active}/beapi_server.yaml", factory = YamlPropertySourceFactory.class)
])
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
        private int backgroundProcessorDelay;
        private int maxThreads;
        private int minSpareThreads;
        private int maxConnections;
        private String uriEncoding;
        private boolean compression;
        private ArrayList compressableMimeTypes;

        public int getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public int getMaxThreads() { return maxThreads; }
        public int getMinSpareThreads() { return minSpareThreads; }
        public int getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(int backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(int minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }


    public static class MediumProps {
        private int backgroundProcessorDelay;
        private int maxThreads;
        private int minSpareThreads;
        private int maxConnections;
        private String uriEncoding;
        private boolean compression;
        private ArrayList compressableMimeTypes;

        public int getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public int getMaxThreads() { return maxThreads; }
        public int getMinSpareThreads() { return minSpareThreads; }
        public int getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(int backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(int minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }

    public static class LargeProps {
        private int backgroundProcessorDelay;
        private int maxThreads;
        private int minSpareThreads;
        private int maxConnections;
        private String uriEncoding;
        private boolean compression;
        private ArrayList compressableMimeTypes;

        public int getBackgroundProcessorDelay() { return backgroundProcessorDelay; }
        public int getMaxThreads() { return maxThreads; }
        public int getMinSpareThreads() { return minSpareThreads; }
        public int getMaxConnections() { return maxConnections; }
        public String getUriEncoding() { return uriEncoding; }
        public boolean getCompression() { return compression; }
        public ArrayList getCompressableMimeTypes() { return compressableMimeTypes; }

        void setBackgroundProcessorDelay(int backgroundProcessorDelay) { this.backgroundProcessorDelay = backgroundProcessorDelay; }
        void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }
        void setMinSpareThreads(int minSpareThreads) { this.minSpareThreads = minSpareThreads; }
        void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }
        void setCompression(boolean compression) { this.compression = compression; }
        void setCompressableMimeTypes(ArrayList compressableMimeTypes) { this.compressableMimeTypes = compressableMimeTypes; }
    }

}
