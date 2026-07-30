package io.beapi.api.service

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.beapi.api.utils.ApiDescriptor
import io.beapi.api.properties.ApiProperties
import io.beapi.api.service.ApiCacheService


public class ApiTestService {

    private ApiProperties apiProperties
    private ApiCacheService apiCacheService;
    String version

    @Value("\${server.address}")
    String serverAddress;

    public ApiTestService(ApiProperties apiProperties, ApiCacheService apiCacheService, @Value(value = "version")String version)  throws Exception {
        this.version = version
        this.apiProperties = apiProperties
        this.apiCacheService = apiCacheService
    }

    public String getTestingProtocol(){
        return apiProperties.getTestingProtocol();
    }

    public Integer getRateLimit(String role){
        return apiProperties.getThrottle().getRateLimit()[role]
    }

    public ApiDescriptor getApiObject(String controller, String apiVersion, String action){
        LinkedHashMap cache = apiCacheService.getApiCache(controller);
        return cache?."${apiVersion}"?."${action}"
    }

    public LinkedHashMap getTestUser(){
        return apiProperties.getBootstrap().getTestUser()
    }

    public LinkedHashMap getSuperUser(){
        return apiProperties.getBootstrap().getSuperUser()
    }

    public String getSuperuserRole(){
        apiProperties.getSecurity().getSuperuserRole()
    }

    public String getAppVersion(){
        return this.version;
    }

    private Set getResponseData(String auth, ApiDescriptor apiObject){
        Set returnsList = []
        apiObject?.returns?."${auth}".each() { it2 -> returnsList.add(it2.name) }
        apiObject?.returns?."permitAll".each() { it2 -> returnsList.add(it2.name) }
        return returnsList
    }

    private Set getResponseData(String auth, String controller, String apiVersion, String action){
        Set returnsList = []
        ApiDescriptor apiObject = getApiObject(controller, apiVersion, action)
        apiObject?.returns?."${auth}".each() { it2 -> returnsList.add(it2.name) }
        apiObject?.returns?."permitAll".each() { it2 -> returnsList.add(it2.name) }
        return returnsList
    }
}