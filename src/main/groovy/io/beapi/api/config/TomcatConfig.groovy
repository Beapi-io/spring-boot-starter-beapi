package io.beapi.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http11.Http11NioProtocol
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.server.Compression;
import org.springframework.beans.factory.annotation.Autowired;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.properties.ServerProperties;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import org.apache.coyote.http2.Http2Protocol;
//import lombok.extern.slf4j.Slf4j;

//@Slf4j
@Configuration
public class TomcatConfig  {

    @Autowired
    protected ApiProperties apiProperties;

    @Autowired
    protected ServerProperties serverProperties;

    protected Boolean compression;
    protected Integer maxThreads;
    protected Integer minSpareThreads;
    protected Integer maxConnections;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {

            @Override
            public void customize(TomcatServletWebServerFactory factory) {
                String configType = apiProperties.getConfigType().toLowerCase();
                ArrayList configTypes = new ArrayList();
                configTypes.add("nano");
                configTypes.add("medium");
                configTypes.add("large");

                if(!configTypes.contains(configType)){
                    //todo : throw better error/exception
                    System.out.println("not a recognizable configType");
                }else {
                    switch(configType){
                        case "nano":
                            compression = serverProperties.nano.getCompression();
                            maxThreads = serverProperties.nano.getMaxThreads();
                            minSpareThreads = serverProperties.nano.getMinSpareThreads();
                            maxConnections = serverProperties.nano.getMaxConnections();
                            break;
                        case "medium":
                            compression = serverProperties.medium.getCompression();
                            maxThreads = serverProperties.medium.getMaxThreads();
                            minSpareThreads = serverProperties.medium.getMinSpareThreads();
                            maxConnections = serverProperties.medium.getMaxConnections();
                            break;
                        case "large":
                            compression = serverProperties.large.getCompression();
                            maxThreads = serverProperties.large.getMaxThreads();
                            minSpareThreads = serverProperties.large.getMinSpareThreads();
                            maxConnections = serverProperties.large.getMaxConnections();
                            break;
                    }

                    Compression comp = new Compression();
                    comp.setEnabled(compression);
                    factory.setCompression(comp);
                    factory.setUriEncoding(StandardCharsets.UTF_8);
                    factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {

                        @Override
                        public void customize(Connector connector) {
                            connector.addUpgradeProtocol(new Http2Protocol());

                            // No significant diff between protocols
                            //AbstractHttp11Protocol<?> httpHandler = ((AbstractHttp11Protocol<?>) connector.getProtocolHandler());
                            Http11NioProtocol httpHandler = (Http11NioProtocol) connector.getProtocolHandler();

                            httpHandler.setMaxKeepAliveRequests(-1);
                            httpHandler.setRejectIllegalHeader(true);
                            httpHandler.setMaxThreads(maxThreads);
                            httpHandler.setMinSpareThreads(minSpareThreads);
                            httpHandler.setMaxConnections(maxConnections);
                            //httpHandler.setUseKeepAliveResponseHeader(true);
                            //httpHandler.setKeepAliveTimeout(keepAliveTimeout);
                        }
                    });
                }
            }

        };
    }

}
