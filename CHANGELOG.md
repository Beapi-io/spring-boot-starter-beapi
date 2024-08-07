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


 ## 0.9.x - (projected release 11/01/2023)
 
 ### Added
  - email registration & validation for SDK
  - (in dev) rate limiting
  - (in dev) session state storage with Apache Geode
 
 ### Changed

 ### Documentation
 - (TODO) setting apiServer in beapi_api.yml
 - (TODO) setting mail settings in beapi_api.yml for registration

 ### planned
  - (Feature) CLI scaffolding of controller/domain/etc for new projects
  - (Feature) CLI scaffolding of PROJECT (ie beapi new-project your.domain.project)
  - (Feature) CLI init of new project; inits and creates project env based on package name
  - (Feature) add random uuid
    - perhaps better, use 'SecretGenerator' for creating randomized UUID. That way we can use for need authentication or to rotate
    - ok, we can do rotation through 'caching'; UUID is only stored in cache allowing for dynamic generation & caching upon return
    - we can turn this into a class allowing people to extend this functionality into their own document stores/caches
  - (Feature) user management (also for frontend)
  - (DOCUMENTATION DEMO) stats tracking (with google or integrated Matamo : https://developer.matomo.org/api-reference/tracking-api)
  - (feature) integrate micrometer
  - (SDK) optional JMS service/config ??

## 1.0.x - (no release date yet)
 
 ### Added
 
 ### Changed

 ### Documentation
 - (TODO) setting apiServer in beapi_api.yml
 - (TODO) setting mail settings in beapi_api.yml for registration

 ### planned
  - (PRIORITY) Unit Tests - we have integration/functional testing but need to add unit as well
  - (PRIORITY) Extensibility: Alot of classes are not extensible when they need to be (ie almost all services)
  - Implement Automated Apidocs for PUBLIC endpoints using RequestMappingHandlerMapping
  - Finish internationalizing messages



