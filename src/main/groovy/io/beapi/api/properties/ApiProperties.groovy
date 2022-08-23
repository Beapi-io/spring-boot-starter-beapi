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
import org.springframework.test.context.TestPropertySource;

//import lombok.Data;
import lombok.Getter
import lombok.Setter


@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix="api")
//@TestPropertySource(properties = ["apiConfigPath = ${user.home}/.boot/${env}/beapi_api.yaml"])
@PropertySource(value = "classpath:/beapi_api.yaml", factory = YamlPropertySourceFactory.class)
public class ApiProperties{

        private String name
        private Integer attempts
        private Integer procCores
        private String documentationUrl
        private ArrayList views
        private ArrayList reservedUris
        private ArrayList nonmappedEndpoint
        private String apiServer
        private ArrayList entities
        private Integer apichainLimit
        private Boolean postcrement
        private Boolean chainingEnabled
        private Boolean batchingEnabled
        private String encoding
        private String iostateDir
        private String serverType
        private Boolean parseValidRequestParams
        private Boolean autoTest

        ThrottleProps throttle = new ThrottleProps()
        WebhookProps webhook = new WebhookProps()
        SecurityProps security = new SecurityProps()
        BootstrapProps bootstrap = new BootstrapProps()

        String getName() { return name }
        Integer getAttempts() { return attempts }
        Integer getProcCores() { return procCores }
        String getDocumentationUrl() { return documentationUrl }
        ArrayList getViews() { return views }
        ArrayList getReservedUris() { return reservedUris }
        ArrayList getNonmappedEndpoint() { return nonmappedEndpoint }
        String getApiServer() { return apiServer }
        ArrayList getEntities() { return entities }
        Integer getApichainLimit() { return apichainLimit }
        Boolean getPostcrement() { return postcrement }
        Boolean getChainingEnabled() { return chainingEnabled }
        Boolean getBatchingEnabled() { return batchingEnabled }
        String getEncoding() { return encoding }
        String getIostateDir() { return iostateDir }
        String getServerType() { return serverType }
        Boolean getAutoTest() { return autoTest }
        Boolean getParseValidRequestParams(){ return parseValidRequestParams }


        void setName(String name) { this.name = name }
        void setAttempts(Integer attempts) { this.attempts = attempts }
        void setProcCores(Integer procCores) { this.procCores = procCores }
        void setDocumentationUrl(String documentationUrl) { this.documentationUrl = documentationUrl }
        void setViews(ArrayList views) { this.views = views }
        void setReservedUris(ArrayList reservedUris) { this.reservedUris = reservedUris }
        void setNonmappedEndpoint(ArrayList nonmappedEndpoint) { this.nonmappedEndpoint = nonmappedEndpoint }
        void setApiServer(String apiServer) { this.apiServer = apiServer }
        void setEntities(ArrayList entities) { this.entities = entities }
        void setApichainLimit(Integer apichainLimit) { this.apichainLimit = apichainLimit }
        void setPostcrement(Boolean postcrement) { this.postcrement = postcrement }
        void setChainingEnabled(Boolean chainingEnabled) { this.chainingEnabled = chainingEnabled }
        void setBatchingEnabled(Boolean batchingEnabled) { this.batchingEnabled = batchingEnabled }
        void setEncoding(String encoding) { this.encoding = encoding }
        void setIostateDir(String iostateDir) { this.iostateDir = iostateDir }
        void setServerType(String serverType) { this.serverType = serverType }
        void setAutoTest(Boolean autoTest) { this.autoTest = autoTest }
        void setParseValidRequestParams(Boolean parseValidRequestParams){ this.parseValidRequestParams = parseValidRequestParams}

        public ThrottleProps getThrottle(){ return this.throttle; }
        public WebhookProps getWebhook(){ return this.webhook; }
        public SecurityProps getSecurity(){ return this.security; }
        public BootstrapProps getBootstrap(){ return this.bootstrap; }

        public void setThrottle(ThrottleProps throttle){ this.throttle = throttle }
        public void setWebhook(WebhookProps webhook ){ this.webhook = webhook }
        public void setSecurity(SecurityProps security){ this.security = security }
        public void setBootstrap(BootstrapProps bootstrap){ this.bootstrap = bootstrap }


    public class ThrottleProps {
        private Boolean active
        private LinkedHashMap<String,Integer> rateLimit
        private LinkedHashMap<String,Integer> dataLimit
        // # in seconds (3600 = 60 minutes)
        private Integer expires


        public String getActive() { return this.active; }
        public String getRateLimit() { return this.rateLimit; }
        public Boolean getDataLimit() { return this.dataLimit; }
        public Integer getExpires() { return this.expires; }

        public void setActive(Boolean active) { this.active = active }
        public void setRateLimit(LinkedHashMap rateLimit) { this.rateLimit = rateLimit }
        public void setDataLimit(LinkedHashMap dataLimit) { this.dataLimit = dataLimit }
        public void setExpires(Integer expires) { this.expires = expires }


    }

    public class WebhookProps {
        private Boolean active
        private ArrayList services


        public Boolean getActive() { return this.active }
        public Boolean getServices() { return this.services }

        public void setActive(Boolean active) { this.active = active; }
        public void setServices(ArrayList services) { this.services = services; }


    }

    public class SecurityProps {
        private String superuserRole
        private String userRole
        private String anonRole
        private Set networkGroups
        private LinkedHashMap networkRoles
        private LinkedHashMap corsNetworkGroups
        private Set corsIncludeEnvironments
        private Set corsExcludeEnvironments


        public String getSuperuserRole(){ return this.superuserRole }
        public String getUserRole(){ return this.userRole}
        public String getAnonRole(){ return this.anonRole}
        public Set getNetworkGroups() { return this.networkGroups }
        public LinkedHashMap getNetworkRoles() { return this.networkRoles }
        public LinkedHashMap getCorsNetworkGroups() { return this.corsNetworkGroups }
        public Set getCorsIncludeEnvironments() { return this.corsIncludeEnvironments }
        public Set getCorsExcludeEnvironments() { return this.corsExcludeEnvironments }

        public void setSuperuserRole(String superuserRole){ this.superuserRole = superuserRole }
        public void setUserRole(String userRole){ this.userRole = userRole}
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
