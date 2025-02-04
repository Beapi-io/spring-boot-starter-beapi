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
import org.springframework.context.annotation.PropertySources;
//import org.springframework.test.context.TestPropertySource;

//import lombok.Data;
//import lombok.Getter
//import lombok.Setter


@Configuration
//@Getter
//@Setter
@ConfigurationProperties(prefix="api")
//@PropertySource(value = "classpath:/beapi_api.yaml", factory = YamlPropertySourceFactory.class)
@PropertySources([
    @PropertySource(value="classpath:beapi_api.yaml", factory=YamlPropertySourceFactory.class),
        @PropertySource(value='file:${user.home}/.boot/${spring.profiles.active}/beapi_api.yaml', factory=YamlPropertySourceFactory.class)
])
//@PropertySource(value='file:${user.home}/.boot/${spring.profiles.active}/beapi_api.yaml', factory = YamlPropertySourceFactory.class)
public class ApiProperties{
        private String apiServer
        private Integer attempts = 5
        private Integer procCores = 8
        private String documentationUrl = 'http://orubel.github.io/Beapi-API-Framework/'
        private ArrayList reservedUris
        private Integer apichainLimit = 3
        private Boolean postcrement = false
        private Boolean chainingEnabled = true
        private Boolean batchingEnabled = true
        private String encoding = 'UTF-8'
        private String iostateDir = '.boot/.iostate'
        private ArrayList staticEndpoint = ['apidoc','openapi','connector','throttle','actuator']
        private ArrayList supportedFormats = ['JSON','XML']

        // todo : current master/slave (change to parent/child)
        private String serverType = 'slave'

        // note : can be nano/medium/large
        private String configType = 'large'

        // todo: change name of this to testing protocol (should be HTTPS in prod)
        private String testingProtocol = 'http'
        private Boolean autoTest = false

        private DbProps db = new DbProps()
        private ThrottleProps throttle = new ThrottleProps()
        private WebhookProps webhook = new WebhookProps()
        private SecurityProps security = new SecurityProps()
        private MailProps mail = new MailProps()
        private BootstrapProps bootstrap = new BootstrapProps()
        private CallbackProps callback = new CallbackProps()

        String getApiServer() { return apiServer }
        Integer getAttempts() { return attempts }
        Integer getProcCores() { return procCores }
        String getDocumentationUrl() { return documentationUrl }
        ArrayList getReservedUris() { return reservedUris }
        Integer getApichainLimit() { return apichainLimit }
        Boolean getPostcrement() { return postcrement }
        Boolean getChainingEnabled() { return chainingEnabled }
        Boolean getBatchingEnabled() { return batchingEnabled }
        String getEncoding() { return encoding }
        String getIostateDir() { return iostateDir }
        ArrayList getStaticEndpoint() { return staticEndpoint }
        String getServerType() { return serverType }
        String getConfigType() { return configType }
        Boolean getAutoTest() { return autoTest }
        ArrayList getSupportedFormats() { return supportedFormats }
        String getTestingProtocol() { return testingProtocol }

        void setApiServer(String apiServer) { this.apiServer = apiServer }
        void setAttempts(Integer attempts) { this.attempts = attempts }
        void setProcCores(Integer procCores) { this.procCores = procCores }
        void setReservedUris(ArrayList reservedUris) { this.reservedUris = reservedUris }
        void setApichainLimit(Integer apichainLimit) { this.apichainLimit = apichainLimit }
        void setPostcrement(Boolean postcrement) { this.postcrement = postcrement }
        void setChainingEnabled(Boolean chainingEnabled) { this.chainingEnabled = chainingEnabled }
        void setBatchingEnabled(Boolean batchingEnabled) { this.batchingEnabled = batchingEnabled }
        void setEncoding(String encoding) { this.encoding = encoding }
        void setIostateDir(String iostateDir) { this.iostateDir = iostateDir }
        void setServerType(String serverType) { this.serverType = serverType }
        void setConfigType(String configType) { this.configType = configType }
        void setAutoTest(Boolean autoTest) { this.autoTest = autoTest }
        void setSupportedFormats(ArrayList supportedFormats) { this.supportedFormats = supportedFormats }
        void setTestingProtocol(String testingProtocol) { this.testingProtocol = testingProtocol }

        public DbProps getDb(){ return this.db; }
        public ThrottleProps getThrottle(){ return this.throttle; }
        public WebhookProps getWebhook(){ return this.webhook; }
        public SecurityProps getSecurity(){ return this.security; }
        public MailProps getMail(){ return this.mail; }
        public BootstrapProps getBootstrap(){ return this.bootstrap; }
        public CallbackProps getCallback(){ return this.callback; }

        public void setThrottle(ThrottleProps throttle){ this.throttle = throttle }
        public void setWebhook(WebhookProps webhook ){ this.webhook = webhook }
        public void setSecurity(SecurityProps security){ this.security = security }
        public void setMail(MailProps mail){ this.mail = mail }
        public void setBootstrap(BootstrapProps bootstrap){ this.bootstrap = bootstrap }
        public void setCallback(CallbackProps callback){this.callback = callback }

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
        // whether throttle is on/off
        private Boolean active
        private LinkedHashMap<String,Integer> rateLimit
        private LinkedHashMap<String,Integer> dataLimit
        // how long until throttle limit/lock resets
        private Long staleSession


        public Boolean getActive() { return this.active; }
        public LinkedHashMap getRateLimit() { return this.rateLimit; }
        public LinkedHashMap getDataLimit() { return this.dataLimit; }
        public Long getStaleSession() { return this.staleSession; }

        public void setActive(Boolean active) { this.active = active }
        public void setRateLimit(LinkedHashMap rateLimit) { this.rateLimit = rateLimit }
        public void setDataLimit(LinkedHashMap dataLimit) { this.dataLimit = dataLimit }
        public void setStaleSession(Long staleSession) { this.staleSession = staleSession }
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
        private String superuserRole = 'ROLE_ADMIN'
        private String userRole = 'ROLE_USER'
        private String testRole = 'ROLE_USER'
        private String anonRole = 'ROLE_ANONYMOUS'
        private Set networkGroups = ['open','public','private']
        private LinkedHashMap networkRoles
        private ArrayList corsWhiteList = []

        public String getSuperuserRole(){ return this.superuserRole }
        public String getUserRole(){ return this.userRole}
        public String getTestRole(){ return this.testRole}
        public String getAnonRole(){ return this.anonRole}
        public Set getNetworkGroups() { return this.networkGroups }
        public LinkedHashMap getNetworkRoles() { return this.networkRoles }
        public ArrayList getCorsWhiteList() { return this.corsWhiteList }


        public void setSuperuserRole(String superuserRole){ this.superuserRole = superuserRole }
        public void setUserRole(String userRole){ this.userRole = userRole}
        public void setTestRole(String testRole){ this.testRole = testRole}
        public void setAnonRole(String anonRole){ this.anonRole = anonRole}
        public void setNetworkGroups(Set networkGroups) { this.networkGroups = networkGroups}
        public void setNetworkRoles(LinkedHashMap networkRoles) {
            if (networkRoles) {
                this.networkRoles = networkRoles
            }
        }
        public void setcorsWhiteList(ArrayList cors) { this.corsWhiteList = cors}

    }

    public class MailProps {
        private String host
        private Integer port
        private String username
        private String password
        private String validationCallback
        private String fromAddress
        private String senderName
        private boolean smtpAuth
        private boolean smtpStarttlsEnable
        private String testEmail

        public String getHost(){ return this.host}
        public Integer getPort(){ return this.port}
        public String getUsername(){ return this.username}
        public String getPassword(){ return this.password}
        public String getValidationCallback(){ return this.validationCallback}
        public String getFromAddress(){ return this.fromAddress}
        public String getSenderName(){ return this.senderName}
        public boolean getSmtpAuth(){ return this.smtpAuth}
        public boolean getSmtpStarttlsEnable(){ return this.smtpStarttlsEnable}
        public String getTestEmail(){ return this.testEmail}

        public void setHost(String host){ this.host = host}
        public void setPort(Integer port){ this.port = port}
        public void setUsername(String username){ this.username = username}
        public void setPassword(String password){ this.password = password}
        public void setValidationCallback(String validationCallback){
            if(validationCallback!=null||validationCallback!='') {
                this.validationCallback = validationCallback
            }else{
                throw new Exception("EXCEPTION: Validation Callback is NULL")
            }
        }
        public void setFromAddress(String fromAddress){ this.fromAddress = fromAddress}
        public void setSenderName(String senderName){ this.senderName = senderName}
        public void setSmtpAuth(boolean smtpAuth){ this.smtpAuth = smtpAuth}
        public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable){ this.smtpStarttlsEnable = smtpStarttlsEnable}
        public void setTestEmail(String testEmail){ this.testEmail = testEmail}
    }

    public class BootstrapProps {
        private LinkedHashMap superUser
        private LinkedHashMap testUser

        public LinkedHashMap getSuperUser(){ return this.superUser }
        public LinkedHashMap getTestUser(){ return this.testUser}

        public void setSuperUser(LinkedHashMap superUser){ this.superUser = superUser }
        public void setTestUser(LinkedHashMap testUser){ this.testUser = testUser}
    }

    public class CallbackProps {
        private String validation
        private String forgotPassword
        private String resetPassword

        public String getValidation(){ return this.validation }
        public String getForgotPassword(){ return this.forgotPassword }
        public String getResetPassword(){ return this.resetPassword }

        public void setValidation(String validation){ this.validation = validation }
        public void setForgotPassword(String forgotPassword){ this.forgotPassword = forgotPassword}
        public void setResetPassword(String resetPassword){ this.resetPassword = resetPassword}
    }
}
