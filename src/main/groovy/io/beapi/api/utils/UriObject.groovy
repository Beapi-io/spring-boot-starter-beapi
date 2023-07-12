package io.beapi.api.utils

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.annotation.Nonnull;

// [callType, sent appVersion, default appVersion(for comparison), apiVersion, controller, action, trace, id]
public class UriObject {

    private static final ArrayList CALL_TYPES = ['v','b','c','r','t']

    private Integer callType
    private String appVersion
    private String defaultAppVersion
    private String apiVersion
    private String controller
    private String action=""
    private boolean trace = false
    private String id

    public UriObject(String uri, String version){
            Integer callType
            boolean trace = false

            ArrayList uriVars = uri.split('/')
            String tempVersion = uriVars[1].toLowerCase()

            switch(tempVersion){
                case ~/([v|b|c|t])(${version})-([0-9]+)/:
                case ~/([v|b|c|t])(${version})/:
                    def m = Matcher.lastMatcher
                    callType = (CALL_TYPES.indexOf(m[0][1])+1)

                    setCallType(callType)
                    setAppVersion(m[0][2])
                    setDefaultAppVersion(version)
                    setApiVersion(((m[0][3])?m[0][3]:'1'))
                    setController(uriVars[2].toString())
                    setAction(uriVars[3].toString())

                    //if(callType==5){ trace=true }
                    setTrace()

                    if(uriVars[4]){ setId(URLDecoder.decode(uriVars[4], StandardCharsets.UTF_8.toString())) }
                    break
            }
    }

    private setUri(String uri){ this.uri = new String(uri)}
    public String getUri() { return this.uri }

    public setController(String controller){ this.controller=new String(controller) }
    public String getController() { return controller }

    public setAction(String action){
        if(action){ this.action=action }
    }
    public String getAction() { return action }

    private setCallType(Integer callType){ this.callType=new Integer(callType) }
    public Integer getCallType() { return callType }

    private setVersion(String version){ this.version=new String(version) }
    public String getVersion() { return version }

    private setAppVersion(String appVersion){ this.appVersion=new String(appVersion) }
    public String getAppVersion() { return appVersion }

    private setDefaultAppVersion(String defaultAppVersion){ this.defaultAppVersion=new String(defaultAppVersion) }
    public String getDefaultAppVersion() { return defaultAppVersion }

    private setApiVersion(String apiVersion){ this.apiVersion=new String(apiVersion) }
    public String getApiVersion() { return apiVersion }

    public setId(String id){ this.id=new String(id) }
    public String getId() { return id }
    public boolean hasId() {
        if(this.id){ return true }else{ return false }
    }

    private setTrace(){
        if(this.callType==5){this.trace=true}
    }

    public boolean isTrace() { return trace }

}
