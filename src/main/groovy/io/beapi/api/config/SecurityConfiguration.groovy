package io.beapi.api.config;


import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.domain.service.UserService;
import io.beapi.api.filter.JwtRequestFilter;
import io.beapi.api.filter.RequestInitializationFilter;
//import io.beapi.api.filter.FilterChainExceptionHandler;
//import io.beapi.api.filter.CorsSecurityFilter;
import io.beapi.api.properties.ApiProperties;
import io.beapi.api.service.BootstrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import io.beapi.api.properties.ApiProperties;
import io.beapi.api.domain.service.AuthorityService;
import io.beapi.api.domain.service.UserAuthorityService;
import io.beapi.api.domain.service.UserService;

import java.io.IOException;


@Order(1000)
@Configuration
@EnableWebSecurity(debug=false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private RequestInitializationFilter requestInitializationFilter;

    @Autowired
    private FilterRegistrationBean requestInitializationFilterRegistration;

    //@Autowired
    //private FilterChainExceptionHandler filterChainExceptionHandler;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    protected AuthorityService authService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserAuthorityService uAuthService;

    @Autowired
    public SecurityConfiguration(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }


    // this registers filter with RequestMappingHandlerMapping
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<JwtRequestFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtRequestFilter());
        registrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER+1);
        //registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER-100)
        registrationBean.addUrlPatterns("/authenticate","/register","/error");
        return registrationBean;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    // websocket security
    /*
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/user/queue/errors").permitAll()
                .simpDestMatchers("/app/**").hasRole("ROLE_USER","ROLE_ADMIN")
                .simpSubscribeDestMatchers("/**").hasRole("ROLE_USER","ROLE_ADMIN")
                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
                .anyMessage().denyAll();
        return messages.build();
    }

     */

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors();
        //httpSecurity.authorizeRequests().antMatchers("/ws/**").permitAll();
        httpSecurity.authorizeRequests().antMatchers("/authenticate", "/register").permitAll().anyRequest().authenticated();
        httpSecurity.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        //httpSecurity.exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/error"));
        httpSecurity.addFilterAfter(jwtRequestFilter(), ExceptionTranslationFilter.class);
        httpSecurity.addFilterAfter(requestInitializationFilter, JwtRequestFilter.class);
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    public BootstrapService bootstrapService() throws IOException {
        return new BootstrapService(apiProperties, authService, userService, uAuthService, passwordEncoder);
    }
}