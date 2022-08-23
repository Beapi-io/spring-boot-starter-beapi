package io.beapi.api.utils

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.annotation.Nonnull;

public class UriObject {

    private static final ArrayList CALL_TYPES = ['v','b','c','r','t']

    private String uri
    private String controller
    private String action
    private Integer callType
    private String version
    private String appVersion
    private String apiVersion
    private String id
    private boolean trace = false

    public UriObject(String uri, String version){
        setUri(uri)
        setVersion(version)

        ArrayList uriVars = uri.split('/')
        String tempVersion = uriVars[1].toLowerCase()

        setController(uriVars[2])
        setAction(uriVars[3])

        if(uriVars[4]){
            String id = URLDecoder.decode(uriVars[4], StandardCharsets.UTF_8.toString())
            setId(id)
        }

        // note : does not require stringbuilder as it only concats once (effectively) so this is faster
        String uriPattern = "/([v|b|c|r|t])(${version})-([0-9]+)|([v|b|c|r|t])(${version})-([0-9]+)|([v|b|c|r|t])(${version})|([v|b|c|r|t])(${version})/"
        Pattern pattern = Pattern.compile(uriPattern)
        Matcher vers = pattern.matcher(tempVersion)

        if(vers.find()){
            if(vers[0][1]) {
                setCallType(CALL_TYPES.indexOf(vers[0][1])+1)
                setAppVersion(vers[0][2])
                String apiVers = (vers[0][3])?vers[0][3]:'1'
                setApiVersion(apiVers)
            }else if(vers[0][7]){
                setCallType(CALL_TYPES.indexOf(vers[0][7])+1)
                setAppVersion(vers[0][8])
                setApiVersion('1')
            }
            setTrace()
        }else{
            throw new Exception("[ExchangeObject :: UriObject] : Bad Uri sent; could not parse URI '${uri}'")
        }
    }

    private setUri(String uri){ this.uri = new String(uri)}
    public String getUri() { return this.uri }

    private setController(String controller){ this.controller=new String(controller) }
    public String getController() { return controller }

    private setAction(String action){ this.action=new String(action) }
    public String getAction() { return action }

    private setCallType(Integer callType){ this.callType=new Integer(callType) }
    public Integer getCallType() { return callType }

    private setVersion(String version){ this.version=new String(version) }
    public String getVersion() { return version }

    private setAppVersion(String appVersion){ this.appVersion=new String(appVersion) }
    public String getAppVersion() { return appVersion }

    private setApiVersion(String apiVersion){ this.apiVersion=new String(apiVersion) }
    public String getApiVersion() { return apiVersion }

    private setId(String id){ this.id=new String(id) }
    public String getId() { return id }

    private setTrace(){
        if(this.callType==5){this.trace=true}
    }

    public boolean isTrace() { return trace }

}
