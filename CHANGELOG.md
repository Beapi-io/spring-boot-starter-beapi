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


## 0.6.X-SNAPSHOT

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


 ## 0.6 - (projected release Feb/2023)
 
### todo
    - automated webhooks
    - rate limiting
    - automated I/O State generation
    - functional test scaffolding
    - automated controller scaffolding
    - UI/UX tools (maybe)
      - build out demo application as an SDK(???) 

