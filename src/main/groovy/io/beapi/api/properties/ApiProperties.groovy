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
package io.beapi.api.properties;

import io.beapi.api.properties.yaml.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
//import org.springframework.test.context.TestPropertySource;

//import lombok.Data;
//import lombok.Getter
//import lombok.Setter


@Configuration
//@Getter
//@Setter
@ConfigurationProperties(prefix="api")
//@PropertySource(value = "classpath:/beapi_api.yaml", factory = YamlPropertySourceFactory.class)
@PropertySource(value='file:${user.home}/.beapi/${spring.profiles.active}/beapi_api.yaml', factory = YamlPropertySourceFactory.class)
public class ApiProperties{

        private String name
        private Integer attempts
        private Integer procCores
        private String documentationUrl
        private ArrayList views
        private ArrayList reservedUris
        private ArrayList publicEndpoint
        private ArrayList entities
        private Integer apichainLimit
        private Boolean postcrement
        private Boolean chainingEnabled
        private Boolean batchingEnabled
        private String encoding
        private String iostateDir
        private ArrayList staticEndpoint
        private ArrayList supportedFormats
        private String serverType
        private String protocol
        private Boolean parseValidRequestParams
        private Boolean autoTest


        private DbProps db = new DbProps()
        private ThrottleProps throttle = new ThrottleProps()
        private WebhookProps webhook = new WebhookProps()
        private SecurityProps security = new SecurityProps()
        private BootstrapProps bootstrap = new BootstrapProps()

        String getName() { return name }
        Integer getAttempts() { return attempts }
        Integer getProcCores() { return procCores }
        String getDocumentationUrl() { return documentationUrl }
        ArrayList getViews() { return views }
        ArrayList getReservedUris() { return reservedUris }
        ArrayList getPublicEndpoint() { return publicEndpoint }
        ArrayList getEntities() { return entities }
        Integer getApichainLimit() { return apichainLimit }
        Boolean getPostcrement() { return postcrement }
        Boolean getChainingEnabled() { return chainingEnabled }
        Boolean getBatchingEnabled() { return batchingEnabled }
        String getEncoding() { return encoding }
        String getIostateDir() { return iostateDir }
        ArrayList getStaticEndpoint() { return staticEndpoint }
        String getServerType() { return serverType }
        Boolean getAutoTest() { return autoTest }
        ArrayList getSupportedFormats() { return supportedFormats }
        String getProtocol() { return protocol }
        Boolean getParseValidRequestParams(){ return parseValidRequestParams }


        void setName(String name) { this.name = name }
        void setAttempts(Integer attempts) { this.attempts = attempts }
        void setProcCores(Integer procCores) { this.procCores = procCores }
        void setDocumentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl }
        void setViews(ArrayList views) { this.views = views }
        void setReservedUris(ArrayList reservedUris) { this.reservedUris = reservedUris }
        void setPublicEndpoint(ArrayList publicEndpoint) { this.publicEndpoint = publicEndpoint }
        void setEntities(ArrayList entities) { this.entities = entities }
        void setApichainLimit(Integer apichainLimit) { this.apichainLimit = apichainLimit }
        void setPostcrement(Boolean postcrement) { this.postcrement = postcrement }
        void setChainingEnabled(Boolean chainingEnabled) { this.chainingEnabled = chainingEnabled }
        void setBatchingEnabled(Boolean batchingEnabled) { this.batchingEnabled = batchingEnabled }
        void setEncoding(String encoding) { this.encoding = encoding }
        void setIostateDir(String iostateDir) { this.iostateDir = iostateDir }
        void setStaticEndpoint(ArrayList staticEndpoint){ this.staticEndpoint = staticEndpoint }
        void setServerType(String serverType) { this.serverType = serverType }
        void setAutoTest(Boolean autoTest) { this.autoTest = autoTest }
        void setSupportedFormats(ArrayList supportedFormats) { this.supportedFormats = supportedFormats }
        void setProtocol(String protocol) { this.protocol = protocol }
        void setParseValidRequestParams(Boolean parseValidRequestParams){ this.parseValidRequestParams = parseValidRequestParams}

        public DbProps getDb(){ return this.db; }
        public ThrottleProps getThrottle(){ return this.throttle; }
        public WebhookProps getWebhook(){ return this.webhook; }
        public SecurityProps getSecurity(){ return this.security; }
        public BootstrapProps getBootstrap(){ return this.bootstrap; }

        public void setThrottle(ThrottleProps throttle){ this.throttle = throttle }
        public void setWebhook(WebhookProps webhook ){ this.webhook = webhook }
        public void setSecurity(SecurityProps security){ this.security = security }
        public void setBootstrap(BootstrapProps bootstrap){ this.bootstrap = bootstrap }

    public class DbProps {
        private String dataSourceBeanName
        private String localContainerEntityManagerFactoryBeanName
        private String localSessionFactoryBeanName
        private String platformTransactionManagerBeanName
        private ArrayList entityPackages

        public String getDataSourceBeanName() { return this.dataSourceBeanName }
        public String getLocalContainerEntityManagerFactoryBeanName() { return this.localContainerEntityManagerFactoryBeanName; }
        public String getLocalSessionFactoryBeanName() { return this.localSessionFactoryBeanName; }
        public String getPlatformTransactionManagerBeanName() { return this.platformTransactionManagerBeanName; }
        public String getEntityPackages() { return this.entityPackages.join(', '); }

        public void setDataSourceBeanName(String dataSourceBeanName) { this.dataSourceBeanName = dataSourceBeanName }
        public void setLocalContainerEntityManagerFactoryBeanName(String localContainerEntityManagerFactoryBeanName) { this.localContainerEntityManagerFactoryBeanName = localContainerEntityManagerFactoryBeanName }
        public void setLocalSessionFactoryBeanName(String localSessionFactoryBeanName) { this.localSessionFactoryBeanName = localSessionFactoryBeanName }
        public void setPlatformTransactionManagerBeanName(String platformTransactionManagerBeanName) { this.platformTransactionManagerBeanName = platformTransactionManagerBeanName }
        public void setEntityPackages(ArrayList entityPackages) { this.entityPackages = entityPackages }
    }

    public class ThrottleProps {
        private Boolean active
        private LinkedHashMap<String,Integer> rateLimit
        private LinkedHashMap<String,Integer> dataLimit
        // # in seconds (3600 = 60 minutes)
        private Integer expires


        public Boolean getActive() { return this.active; }
        public LinkedHashMap getRateLimit() { return this.rateLimit; }
        public LinkedHashMap getDataLimit() { return this.dataLimit; }
        public Integer getExpires() { return this.expires; }

        public void setActive(Boolean active) { this.active = active }
        public void setRateLimit(LinkedHashMap rateLimit) { this.rateLimit = rateLimit }
        public void setDataLimit(LinkedHashMap dataLimit) { this.dataLimit = dataLimit }
        public void setExpires(Integer expires) { this.expires = expires }
    }

    public class WebhookProps {
        private Boolean active
        private ArrayList<String> services

        public Boolean getActive() { return this.active }
        public ArrayList<String> getServices() { return this.services }

        public void setActive(Boolean active) { this.active = active; }
        public void setServices(ArrayList<String> services) { this.services = services; }
    }


    public class SecurityProps {
        private String superuserRole
        private String userRole
        private String testRole
        private String anonRole
        private Set networkGroups
        private LinkedHashMap networkRoles
        private LinkedHashMap corsNetworkGroups
        private Set corsIncludeEnvironments
        private Set corsExcludeEnvironments


        public String getSuperuserRole(){ return this.superuserRole }
        public String getUserRole(){ return this.userRole}
        public String getTestRole(){ return this.testRole}
        public String getAnonRole(){ return this.anonRole}
        public Set getNetworkGroups() { return this.networkGroups }
        public LinkedHashMap getNetworkRoles() { return this.networkRoles }
        public LinkedHashMap getCorsNetworkGroups() { return this.corsNetworkGroups }
        public Set getCorsIncludeEnvironments() { return this.corsIncludeEnvironments }
        public Set getCorsExcludeEnvironments() { return this.corsExcludeEnvironments }

        public void setSuperuserRole(String superuserRole){ this.superuserRole = superuserRole }
        public void setUserRole(String userRole){ this.userRole = userRole}
        public void setTestRole(String testRole){ this.testRole = testRole}
        public void setAnonRole(String anonRole){ this.anonRole = anonRole}
        public void setNetworkGroups(Set networkGroups) { this.networkGroups = networkGroups}
        public void setNetworkRoles(LinkedHashMap networkRoles) { this.networkRoles = networkRoles}
        public void setCorsNetworkGroups(LinkedHashMap corsNetworkGroups) { this.corsNetworkGroups = corsNetworkGroups}
        public void setCorsIncludeEnvironments(Set corsIncludeEnvironments) { this.corsIncludeEnvironments = corsIncludeEnvironments}
        public void setCorsExcludeEnvironments(Set corsExcludeEnvironments) { this.corsExcludeEnvironments = corsExcludeEnvironments}
    }

    public class BootstrapProps {
        private LinkedHashMap superUser
        private LinkedHashMap testUser

        public LinkedHashMap getSuperUser(){ return this.superUser }
        public LinkedHashMap getTestUser(){ return this.testUser}

        public void setSuperUser(LinkedHashMap superUser){ this.superUser = superUser }
        public void setTestUser(LinkedHashMap testUser){ this.testUser = testUser}
    }
}
