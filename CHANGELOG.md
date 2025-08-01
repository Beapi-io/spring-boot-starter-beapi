# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).


## 0.5 (released: 8/31/2022)

### Added
- [API AUTOMATION]
- localized api caching (do first for benchmarking)
- automated versioning for urlmapping
- automated batching
- api chaining®
- separated I/O State for sharing/synchronizing of all endpoint rules
- built-in performance tracing (partial)
- [SECURITY]
- network groups
- role checking
- automated resolution for API3:2019 Excessive Data Exposure.


## 0.5.1 (released: 9/3/2022)

### Changed
- fixed issue with filter calling handlerinterceptor more than once
- removal of attributes before resetting when chaining


## 0.6.0-STABLE (released: 3/6/2023)

### Added
- reflection for controllers/handlers added so they no longer need to be added to I/O State files
  - corrected issue with reflection handler defaulting to alphabetically first handler if not found (bug in simpleUrlHandlerMapping)
  - stripped out staticMapping and using internal Controllers
- added CORS functionality
- updating mapping to automate CORS whitelisting per networkGrp; this allows for separate frontends for each 'backend' environment thus separating api environments by networkgrp on front/backend
- adding automated role-based apidocs; role based so user only sees docs they have access too (OWASP API COMPLIANT)
- added IO state reloading functionality
- lack of 'action' for mapping will default to 'apidocs/show' for only THAT controller; this makes it so if you dont know what you are calling, it provides fast/automated lookup
- apicalls with no 'action' will send back apidocs for endpoints for that controller that TOKEN has access to
- added receivesList/returnsList to ApiDescriptor
- added 'getApiDescriptor()' to IoStateService

### Changed
- simplifying mapping in config
- removing resource handling for file uploads except for supported mimetypes (JSON/XML); not a file server
- allowing api data to be sent as FILE; can simplify batching and complex calls to put JSON/XML all in FILE to send, detect and auto-parse into params (which we then check to see if they match expected request data for endpoint)
- performance improvements
- fixed issue with pkeys/fkeys not being stored in cache properly
- improvements to ApiDescriptor; better checking for request/response keys
- renamed nonmappedEndpoint to 'publicEndpoints'

### Additional
- releasing beginning SDK frontend with this release


 ## 0.6.5 - (released: 6/6/2023)
 
 ### Added
 - [AUTOMATION]
 - Added CLI service for reading in args in order to scaffold 
 - Added templates for scaffolded connectors
 - Scaffolding for connectors/IO State; allows users to scaffold connectors based on data from an existing entity/controller
 - additional tests for JPA/Hooks
 - (SDK) automated functional tests for ALL API's; moved to SDK
 - (SDK) automated webhooks (in progress)
 
 ### Changed
 - changing default 'iostateDir' to '.beapi/.iostate' for uniqueness
 - removed 'apiServer' from properties
 - adding 'protocol' to properties
 - updated tests in beapi-java-demo to use 'protocol' property; this way protocol can be set per env without changing tests
 - enable bootstrapping of multiple superusers/testusers through properties
 - simplify bootstrapping
 - added testRole to properties
 - updated 'user' domain; added columns for webhook monitoring
 - fixed RequestInitializationFilter to properly return errors
 - fixed CORS/JWT issue
 
 ## 0.7.0 - (released 07/21/2023)
 
 ### Added
 - PropertiesController and properties IOState for showing Application properties from config as API (not changing them though; that requires restart)
 - encoding for webhooks
 - tests for hooks
 - tests for properties
 - default properties being read from starter config
 - updating of Connector/IO-State on-the-fly added; allows changing all rules for apis without restart
 - added configurable server properties for tomcat server
 - integrating data components from demo project into starter to make it more of a API framework extending Springboot (as a starter should)
 - integrating security components from demo project into starter to make it more of a API framework extending Springboot (as a starter should)
 - added 'convention over config'; demo will be much simpler now going forward (as will docs)
 - added default for 'beapi_server' in case user forgets server properties; default server properties
 - added default for 'beapi_api' in case user forgets api properties; default api properties
 - connector scaffolding
 
 ### Changed
 - stability fixes
 - cleaned up/simplified user config
 - no longer need to added config to project; will read from .boot/{env}
 - moved DataSourceProperties config to starter
 - converted uriList/uList to UriObject for better usage/testing
 - moving core repositories/domains from demo project to starter
 - moving core security from demo project into starter
 - consolidated 'bootstrap' functionality into a service; only requires one line to bootstrap project from properties files

 ## 0.7.1 - (released 07/22/2023)

 ### Changed
 - stability fixes; cleanup


 ## 0.8.61 - (projected release 11/01/2023)
 
 ### Added
 - tests for LinkRelations
 - automating HATEOS LinkRelations (if there are any) via passing of 'X-LINK-RELATIONS' header
 - added 'devnotes' (run with log level 'warn') which will let you know what went wrong and how to fix in the log
 - hooked up  beginning Trace service/functionality
 - integrating randomly generated 'secret'; changes with every startup of the app. Initialized by app and stored as bean with private variable. This will mean every deployed application will have a separate way of handling HASHING BUT... since proxy/gateway/load balancer maintain session (and ROUTING based on session), no server has to have the same 'secret' for hashing. This secures each environment from being vulnerable to session hijacking since you would have to supply a separate hash for each different deployed server.
 - added os/browser/ip information as headers to JWT token
 - added jwt header checks to avoid session hijacking
 - caching added to batching/chaining functionality
 - adding back in webhook functionality
 - updating IO State to include caching and ratelimiting config options
 - added toggle to turn cache on/off per endpoint
 - added tests for registration
 
 ### Changed
 - cleanup
 - fixed issue with SimpleUrlMapping throwing error when 'action' not sent
 - upgrading to Groovy 4.0.8 for tests
 - 'sealing' extended classes in call flow
 - speed/scale optimizations
 - removed functionality for bad uri to default to apidocs; can be exploited by DOS attacks.
 - minimized required libs
 - unset cached data upon unsafe method call
 - modified gradle to provide templates for scaffolding
 - updating config for cors changes; importing cors config from properties
 - adding functionality/tests so registration cleanly rejects duplicates
 - adding try/catch block to domain services to throw clean exception

 ### todo
  - (DOCS) separate DEMO docs from STARTER docs; keep in this project, easier to maintain
  - (DOCS) documentation for scaffolding functionality


 ## 0.9.x - (released 12/19/2024)
 
 ### Added
  - email registration & validation for SDK
  - newly registered user cannot get token until after they validate [tested]
  - added tests for registration
  - added sessionService
  - added rate limiting
    - added tests for rate limiting
  - automate documentation for public static apis (login, error, etc)
    - after much back-and-forth, these can be hardcoded since we don't have/support 'public' apis; that requires separate less-secure product that anyone can provide. Our product is aimed at a secure SDK.
  - check registration valdation did/didn't expire; set time limit on how long they have to respond before validation expires (usually 10 minutes) (RegistrationVerificationExpiry)
    - test with mail, api and frontend
    - test with mail, api and frontend
  - (TODO) (in dev) adding 'resetToken' for resetting password. need 'forgotPassword' functionality (with email verification) and 'passwordResetForward' property for beapi_api.yaml
    - test with mail, api and frontend
    - session timeout
    - connector/iostate scaffolding
    - controller scaffolding

 ### Changed
 - FIXED BUG #125  : writeErrorResponse can be bypassed and still return response
 - updated functional tests to save/pass session cookie
 - optimized error reporting for responses
 - renamed ThrottleCacheService to just be ThrottleService now that we reverted RateLimit Changes
 - tests only running in 'dev' now; will not run in 'prod'
 - changed 'registrationVerificationCode' to 'verificationCode' to be more genereic and general purpose
 - updated userDetails
 - newly registered user cannot get token until they verify acct via email
 - separated 'callbacks' from beapi-api 'mail' section into their own section as we add more callbacks.
 - added Forgot pasword functionality for validating by email
 - added 'register' functionality with email validation
   
 ### Documentation (todo)
 - document rate limiting
 - add setting 'server.servlet.session.cookie.http-only' in application.properties; needs to be 'true' in prod environment

### Planned
- prior to release, update spring-boot-starter-beapi-config and sql file



## 1.0.x - (planned 02.05.2025)
 
 ### Added
 - (done) trace functionality
 - (done) statistics reporting/logging
 - (done) cli for scaffolding repository
 - (done) CLI to scaffold service for domains
 - (done) refreshToken functionality
 - (done) internationalizing messages for all errors/exceptions (start with chinese, spanish, french, german in that order)
 
 ### Changed
 - (done) add OpenApi format for docs; needs to be separate class/controller
 - (done) added UPDATECACHE to all IO State files
 - test webhooks and finalize functionality
 - (done) add username to session
 - (done) documented adding public endpoints
 - (done) documented refreshToken
 - (done) added actuator endpoints to documentation

### moved to next version
 - (backburner) [session management](https://stackoverflow.com/questions/49539076/how-can-i-get-a-list-of-all-sessions-in-spring)
 - (backburner)support XML content-type and parsing; test


## 2.x

 ### Added
 - added additional tests

 ### Changed
- convert caching from ehcache2 to caffeine; ehcache 2 no longer supported.
- convert javax imports to jakarta
- [conversion to springboot 3.x requires complete rewrite](https://docs.openrewrite.org/recipes/java/spring/boot3/upgradespringboot_3_0)
- convert security configurer to not use WebSecurityConfigurerAdapter(deprecated)
- upgraded tests for 3.0; no longer using HttpClient
- upgraded documentation for 1.X
- (todo)upgrade documentation for 2.X



