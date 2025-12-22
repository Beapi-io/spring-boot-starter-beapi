package io.beapi.api.config


import io.beapi.api.filter.JwtRequestFilter;
import io.beapi.api.filter.RequestInitializationFilter;
//import io.beapi.api.filter.FilterChainExceptionHandler;
//import io.beapi.api.filter.CorsSecurityFilter;

import io.beapi.api.service.BootstrapService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.security.web.access.ExceptionTranslationFilter
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.domain.service.UserService
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import io.beapi.api.service.JwtUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter

import org.springframework.scheduling.annotation.EnableAsync;
import io.beapi.api.service.TraceService
import io.beapi.api.utils.JwtTokenUtil;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.method.HandlerMethod

@Order(1000)
@Configuration
@EnableAsync
@AutoConfigureAfter([PasswordConfig.class])
@EnableWebSecurity(debug=false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration{

    @Autowired private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired private ApiProperties apiProperties;
    @Autowired private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired private RequestInitializationFilter requestInitializationFilter;
    @Autowired private FilterRegistrationBean requestInitializationFilterRegistration;
    @Autowired protected AuthorityService authService;
    @Autowired protected UserService userService;
    @Autowired protected UserAuthorityService uAuthService;
    @Autowired protected JwtUserDetailsService jwtUserDetailsService

    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired protected TraceService traceService

    //@Autowired private FilterChainExceptionHandler filterChainExceptionHandler;

    @Autowired PasswordEncoder passwordEncoder;
    //private final PasswordEncoder passwordEncoder;
    String version


    public SecurityConfiguration() {
        AppMetadata metadata = new AppMetadata()
        this.version = metadata.getAppVersion()
        //this.passwordEncoder = passwordEncoder;
    }



/*
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(64000);
        return loggingFilter;
    }
 */

    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        ArrayList<String> publicUris = []
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
        for(Map.Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = item.getKey();
            //HandlerMethod method = item.getValue();

            for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
                publicUris.add(urlPattern);
            }
        }

        return new JwtRequestFilter(apiProperties, version, jwtTokenUtil, traceService, userService, publicUris);
    }


    // this registers filter with RequestMappingHandlerMapping
    //@Bean
    //@ConditionalOnMissingBean
    //public FilterRegistrationBean<JwtRequestFilter> jwtFilterRegistration() {
    //    FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();

        // should not have this with public apis
        //registrationBean.setFilter(jwtRequestFilter());

     //   registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+1);
        //registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
        //registrationBean.addUrlPatterns("/authenticate","/register","/error","/validate","/validate","/post-registration/good","/post-registration-bad")

     //   return registrationBean;
    //}


    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(jwtUserDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

/*
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors();
        httpSecurity.authorizeHttpRequests().antMatchers((String[])apiProperties.reservedUris).permitAll().anyRequest().authenticated();
        httpSecurity.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        //httpSecurity.exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/error"));
        httpSecurity.addFilterAfter(jwtRequestFilter(), ExceptionTranslationFilter.class);
        httpSecurity.addFilterAfter(requestInitializationFilter, JwtRequestFilter.class);
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true));
    }

 */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        ArrayList<String> publicUris = []
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
        for(Map.Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = item.getKey();
            HandlerMethod method = item.getValue();

            for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
                publicUris.add(urlPattern);
            }
        }

        httpSecurity.csrf().disable().cors();
        httpSecurity.authorizeHttpRequests().requestMatchers((String[])publicUris).permitAll().anyRequest().authenticated();
        httpSecurity.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        //httpSecurity.exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/error"));
        httpSecurity.addFilterAfter(jwtRequestFilter(), ExceptionTranslationFilter.class);
        httpSecurity.addFilterAfter(requestInitializationFilter, JwtRequestFilter.class);
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true));
        return httpSecurity.build();
    }

    public BootstrapService bootstrapService() throws IOException {
        return new BootstrapService(apiProperties, authService, userService, uAuthService, passwordEncoder);
    }

}