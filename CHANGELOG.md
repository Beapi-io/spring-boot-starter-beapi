# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## 0.6.X-SNAPSHOT

### Added
- reflection for controllers/handlers added so they no longer need to be added to I/O State files
  - corrected issue with reflection handler defaulting to alphabetically first handler if not found (bug in simpleUrlHandlerMapping)
  - stripped out staticMapping and using internal Controllers
- added CORS functionality
- updating mapping to automate CORS whitelisting per networkGrp; this allows for separate frontends for each 'backend' environment thus separating api environments by networkgrp on front/backend
- adding automated role-based apidocs; role based so user only sees docs they have access too (OWASP API COMPLIANT)
- 
### Changed
- simplifying mapping in config
- removing resource handling for file uploads except for supported mimetypes (JSON/XML); not a file server
- allowing api data to be sent as FILE; can simplify batching and complex calls to put JSON/XML all in FILE to send, detect and auto-parse into params (which we then check to see if they match expected request data for endpoint)


