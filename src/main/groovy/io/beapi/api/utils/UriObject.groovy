package io.beapi.api.utils

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.annotation.Nonnull;

// [callType, sent appVersion, default appVersion(for comparison), apiVersion, controller, action, trace, id]
/**
 * Class is used as a 'holder' for request metadata as parsed from the URI.
 * This makes it so we only have to parse URI once.
 * @author Owen Rubel
 * @see RequestInitializationFilter#processRequest
 */
public class UriObject {

    /**
     * list of calltypes that can be sent with version (ie v0.1)
     */
    private static final ArrayList CALL_TYPES = ['v','b','c','t']

    /**
     * numeric representation of the calltype
     */
    private Integer callType

    /**
     * String representing version number of the application
     */
    private String appVersion

    /**
     * String representing default version number of the application
     */
    private String defaultAppVersion

    /**
     * String representing version (parsed from the URI)
     */
    private String apiVersion

    /**
     * String representing controller (parsed from the URI)
     */
    private String controller

    /**
     * String representing action/method (parsed from the URI)
     */
    private String action=""

    /**
     * boolean representing whether request is 'trace' callType
     */
    private boolean trace = false

    /**
     * String representing id sent with URI
     */
    private String id

    /**
    * UriObject class constructor.
    * @param  uri The URI passed during the request (ex /v0.1/controller/method )
    * @param  version The default application version (note: version number is a representation of application version)
    * @return instance of UriObject
    * @see RequestInitializationFilter#processRequest
    */
    public UriObject(String uri, String version){

            /**
             * Numeric representation of the callType character. (ex 1=v, 2=b, 3=c, 4=t)
             */
            Integer callType
            //boolean trace = false


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

    /**
    * Uri setter.
    * @param  uri String representing the URI passed during the request (ex /v0.1/controller/method )
    */
    private setUri(String uri){ this.uri = new String(uri)}

    /**
    * Uri getter.
    @return String representing the uri parameter
    */
    public String getUri() { return this.uri }

    /**
    * controller setter.
    * @param  controller String representing the controller name parsed from the sent URI
    */
    public setController(String controller){ this.controller=new String(controller) }

    /**
    * controller getter.
    @return String representing the controller parameter
    */
    public String getController() { return controller }

    /**
    * action setter.
    * @param  action String representing the method name parsed from the URI
    */
    public setAction(String action){
        if(action){ this.action=action }
    }

    /**
    * action getter.
    @return String representing the action parameter (ie the method)
    */
    public String getAction() { return action }

    /**
    * callType setter.
    * @param  callType Integer representing a numeric representation of the call type passed with version in the URI (ex 1=v, 2=b, 3=c, 4=t)
    */
    private setCallType(Integer callType){ this.callType=new Integer(callType) }

    /**
    * callType getter.
    @return Integer representing the callType (ex 1=v, 2=b, 3=c, 4=t)
    */
    public Integer getCallType() { return callType }

    /**
    * version setter.
    * @param  version String representing the version version number parsed from the sent URI
    */
    private setVersion(String version){ this.version=new String(version) }

    /**
    * version getter.
    @return String representing the version sent in the Uri (ex 0.1)
    */
    public String getVersion() { return version }

    /**
    * appVersion setter.
    * @param appVersion String representing version number of the application
    */
    private setAppVersion(String appVersion){ this.appVersion=new String(appVersion) }

    /**
    * appVersion getter.
    * @return  String representing the version number of the application
    */
    public String getAppVersion() { return appVersion }

    /**
    * default appVersion setter.
    * @param  defaultAppVersion String representing the default application version
    */
    private setDefaultAppVersion(String defaultAppVersion){ this.defaultAppVersion=new String(defaultAppVersion) }

    /**
    * default appVersion getter.
    * @return  String representing the default version number (set in IO State files)
    */
    public String getDefaultAppVersion() { return defaultAppVersion }

    /**
    * apiVersion setter.
    * @param  apiVersion String representing the version parsed from the URI
    */
    private setApiVersion(String apiVersion){ this.apiVersion=new String(apiVersion) }

    /**
    * apiVersion getter.
    * @return String representing the version parsed from the URI
    */
    public String getApiVersion() { return apiVersion }

    /**
    * ID setter.
    *
    * @param id String representing the ID passed in the URI, if one was passed
    */
    public setId(String id){ this.id=new String(id) }

    /**
    * ID getter.
    *
    * @return String representing the ID passed in the URI, if one was passed
    */
    public String getId() { return id }

    /**
    * Method to check if ID was passed with URI from parsed URI data
    *
    * @return Returns 'True' if URI has an ID, else 'false'
    */
    public boolean hasId() {
        if(this.id){ return true }else{ return false }
    }

    /**
    * Method to set 'trace' boolean (for trace detection)
    */
    private setTrace(){
        if(this.callType.equals(4)){this.trace=true}
    }

    /**
    * Method to determine if call is a 'trace'
    *
    * @return Returns 'True'/'false' based on whether trace call was sent
    * @see setTrace
    */
    public boolean isTrace() { return trace }

}
