![alt text](https://github.com/orubel/logos/blob/master/beapi_logo_large.png)
# Beapi Spring Boot Starter

**Current Stable Version** - 0.5.1

**Springboot Version** - 2.6.2 (or greater)

**JVM** - 17 (contact if you need a build for a lower version)

**License** - [Reciprocal Public License](https://en.wikipedia.org/wiki/Reciprocal_Public_License)

**Documentation** - [https://beapi-io.github.io/spring-boot-starter-beapi/](https://beapi-io.github.io/spring-boot-starter-beapi/)

**Configuration Files** - https://github.com/orubel/spring-boot-starter-beapi-config (Note : Move these into your 'iostateDir' location as found in your demo-application/src/main/resources/beapi_api.yaml file)

---

**Beapi abstracts all RULES for API endpoints** so they can be **shared/syncronized with all services** in a distributed API architecture **without requiring restarts of all servers** to do so.

In current architectures, DATA for endpoints is bound to FUNCTIONALITY ( see [Cross Cutting Concern](https://en.wikipedia.org/wiki/Cross-cutting_concern) ) through things like 'annotations'; this makes it so that you have to **duplicate this DATA everywhere**(see OpenApi) as said data is hardcoded into functionality via those annotations.

By abstracting it into an externally **reloadable file**, things like ROLES for endpoints can be easily adjusted without requiring a restart of the service. Plus using functionality like webhooks, one can synchronize all services from a MASTER server. This allows for changes to API endpoint DATA on a distributed API architecture without restarting services.

Additionally, this creates new patterns like [automated batching](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-1) and '[Api Chaining&reg;](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-3) '

---

**Gradle Implementation**
```
dependencies {
    ...
    implementation 'io.beapi:spring-boot-starter-beapi:0.5.1'
    ...
}
 ```

**Getting a Token and calling your api** - 

Using the [Java-demo implementation](https://github.com/Beapi-io/beapi-java-demo), this will get you your BEARER token to use in your calls/app:
```
curl -v -H "Content-Type: application/json" -X POST -d '{"username":"admin","password":"@6m!nP@s5"}' http://localhost:8080/authenticate
```

Then call your api normally:
```
curl -v -H "Content-Type: application/json" -H "Authorization: Bearer {your_token_here}" --request GET "http://localhost:8080/v{appVersion}/user/show/5"
```

---

## 0.5 (released: 8/31/2022)
 - API AUTOMATION
    - localized api caching (do first for benchmarking) 
    - [automated versioning for urlmapping](https://beapi-io.github.io/spring-boot-starter-beapi/adv_config.html#section-1)
    - [automated batching](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-1)
    - [api chaining&reg;](https://beapi-io.github.io/spring-boot-starter-beapi/advanced.html#section-3) 
      - 'blankchain'
      - 'prechain'
      - 'postchain'
    - [separated I/O State for sharing/synchronizing of all endpoint rules](https://beapi-io.github.io/spring-boot-starter-beapi/adv_config.html#section-2)
    - built-in performance tracing (partial)
  - SECURITY
    - network groups
    - role checking
    - automated resolution for [API3:2019 Excessive Data Exposure](https://github.com/OWASP/API-Security/blob/master/2019/en/src/0xa3-excessive-data-exposure.md).
  - DEMO APPLICATION (separate application - link TB provided)
    - JWT token creation and authentication 

## 0.5.1 (released: 9/3/2022)
 - stability fix
 - fixed issue with filter double calling handlerinterceptor
 - removal of attributes before resetting when chaining


 ## 0.6 - ACTIVE CHANGELOG
 - reflection for controllers/handlers added so they no longer need to be added to I/O State files
   - corrected issue with reflection handler defaulting to alphabetically first handler if not found (bug in simpleUrlHandlerMapping)
   - stripped out staticMapping and using internal Controllers
 - adding automated role-based apidocs; role based so user only sees docs they have access too (OWASP API COMPLIANT)
 - updating mapping to automate CORS whitelisting from beapi_api.yml
   - simplifying mapping in config
 - (IMPLEMENTING) adding in handling for ROLE_ANONYMOUS
   - (IMPLEMENTING) 'open' networkGrp accessible by persons without 'token'; principle assigned 'ROLE_ANONYMOUS'
 
 ## 0.6 - (projected release Feb/2023)
  - API AUTOMATION
    - automated apidocs (DONE)
    - automated webhooks
    - io state reloading
    - rate limiting
    - automated CORS; uses whitelisted IP 'networkGrps' (see previous implementation)
 - BOOTSTRAPPING
    - automated I/O State generation
    - functional test scaffolding
    - automated controller scaffolding
 - SCALABILITY
    - extend DispatcherServlet for all front controller functionality (ie RequestInitializationFilter)
 - REPORTING
    - stats reporting
 - 3RD PARTY TOOLS
    - properties file/service/endpoint for 3rd party/local oauth implementations 
 - UI
    - UI/UX tools (maybe)
      - build out demo application as an SDK(???) 

---

# Q&A
- **Why Not bind the endpoints to the 'MODEL'?**
    - API return data may be a mixture of two tables (ie JOIN), a file, a link to another api, etc. By binding to your model, you are asking it to become the 'business logic','communication logic' as well as 'data handling' and limits what your API can return. This breaks rules of AOP, Separation of Control' and over complicates your build and testing. This also makes your entire environment slower and harder to scale.
- **Why require a cache?**
    - It is complex to build dynamic properties and properties that represent MULTIPLE MAPS in a SINGLE PROPERTIES FILE. Cache is hence the simpler solution. Also many developers do not understand proper caching techniques (with API's).
- **Why not just use @RequestMapping, @GetMapping, etc?**
    - The RequestMapping annotations create a HARD CODED 'rules' to functionality; you cannot update these 'rules' while the server is running and these cannot be synchronized across multiple servers. Updating endpoint RULES is often necessary but requires a restart because these bindings are hardcoded. 
    - By abstracting this data from the functionality, we are better able to make LIVE CHANGES TO ENDPOINT RULES (when functionality hasn't changed). So for example if we want to disable privileges or change how a certain ROLE accesses endpoints, we can do that on the fly without taking down servers.
- **Why can't 'API Chaining(R)' have more than ONE UNSAFE method in the chain?**
    - FIRST, You can only send ONE METHOD with a chain; you cannot send a PUT and POST method in the same call. But you can just default every other call to a SAFE call (ie GET) as long as client has AUTHORITY to the endpoint. SECOND, since it can only have one UNSAFE METHOD, you can only send ONE DATASET. We made it to be as simple as possible while encompassing the most commonly used calls thus simplifying the processing and the client call.
- **Isn't it BAD to send form data with a GET request? I thought you could only send URI encoded data??**
    - Per W3C guidelines : 'A client SHOULD NOT generate content in a GET request unless it is made directly to an origin server that has previously indicated, in or out of band, that such a request has a purpose and will be adequately supported'. API Chaining(tm) is that direct connection with purpose. It provides the necessary backend checks and limits what can be sent.

   

