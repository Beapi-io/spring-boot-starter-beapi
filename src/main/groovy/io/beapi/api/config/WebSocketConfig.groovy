package io.beapi.api.config

import io.beapi.api.controller.AuthHandshakeHandler
import io.beapi.api.domain.Authority
import io.beapi.api.domain.User
import io.beapi.api.utils.JwtTokenUtil;
import io.beapi.api.service.PrincipleService
import io.beapi.api.utils.ErrorCodes
import io.beapi.api.utils.JwtTokenUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;

import org.springframework.security.authorization.AuthenticatedAuthorizationManager;

import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptorAdapter
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import java.security.Principal;
import io.beapi.api.service.PrincipleService;
import org.springframework.messaging.Message;

import org.springframework.web.socket.server.RequestUpgradeStrategy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/*
* websockets and stomp cannot send HTTP headers
* keep this in kind when configuring
 */


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


	//@Autowired
	//private JwtTokenUtil jwtTokenUtil

	private final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

	@PostConstruct
	public void init() {
		this.taskExecutor.setCorePoolSize(2);
		this.taskExecutor.setAllowCoreThreadTimeOut(true);
	}

	@Bean
	public SocketChannelInterceptor socketChannelInterceptor(){
		return new SocketChannelInterceptor();
	}


	//@Override
	//public void configureClientInboundChannel(ChannelRegistration registration) {
	//	ChannelRegistration channelRegistration = registration.setInterceptors(socketChannelInterceptor());
	//	super.configureClientInboundChannel(channelRegistration);
	//}



	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		//config.enableSimpleBroker("/topic");
		config.enableSimpleBroker("/chat");
		config.setApplicationDestinationPrefixes("/app").enableSimpleBroker("/topic");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		//WebSocketTransportHandler webSocketTransportHandler = new WebSocketTransportHandler(handshakeHandler);
		//registry.addEndpoint("/websocket").setAllowedOriginPatterns("*").withSockJS().setTransportHandlers(webSocketTransportHandler).setInterceptors(httpSessionHandshakeInterceptor());


		// initial connect
		registry.addEndpoint("/chat").setAllowedOrigins("*");

		//registry.addEndpoint("/chat").setAllowedOriginPatterns("http://localhost:80")
		registry.addEndpoint("/chat").setAllowedOrigins("*").setHandshakeHandler(new AuthHandshakeHandler())
	}



	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		ChannelRegistration channelRegistration = registration.setInterceptors(socketChannelInterceptor());
		channelRegistration.taskExecutor(taskExecutor);
		registration.taskExecutor(taskExecutor);

		//registration.taskExecutor().corePoolSize(4).maxPoolSize(6).keepAliveSeconds(60);
		//registration.setInterceptors(socketChannelInterceptor());
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.taskExecutor(taskExecutor);
	}


	@PreDestroy
	public void onShutdown() {
		taskExecutor.shutdown();
	}

	//@Override
	//public void registerWebSocketHandlers(MessageBrokerRegistry registry) {
	//	registry.addHandler(teacherMonitorHandler, "/chat");
	//}


/*
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.setInterceptors(new ChannelInterceptorAdapter() {
			org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message,  MessageChannel channel) {
				StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

				List tokenList = accessor.getNativeHeader("X-Authorization");
				println("inboundChannel tokens :"+tokenList)
				accessor.removeNativeHeader("X-Authorization");

				List bearer = accessor.getNativeHeader("Authorization");
				println("inboundChannel tokens2 :"+bearer)
				accessor.removeNativeHeader("Authorization");


				String token = null;
				if(tokenList != null && tokenList.size > 0) {
					token = tokenList.get(0);
				}

				println("### has token...")
				String jwtToken = requestTokenHeader.substring(7);
				String username
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken.replaceAll("\\s+", ""));
					println("user : "+username)
				} catch (IllegalArgumentException e) {
					System.out.println("Exception found " + e);
				} catch (ExpiredJwtException e) {
					System.out.println("Exception found " + e);
				}

				if (username != null) {
					UserDetails userDetails = loadUserByUsername(username);

					// if token is valid configure Spring Security to manually set
					// authentication
					if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
						try {
							UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
							usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

							//chain.doFilter(request, response)
						} catch (Exception ignored) {
							//ignored.printStackTrace();
						}
						// After setting the Authentication in the context, we specify
						// that the current user is authenticated. So it passes the
						// Spring Security Configurations successfully.
					}
				} else {
					// no username/authentication for " + request.getRequestURI());
					String statusCode = "403";
					response.setContentType("application/json");
					response.setStatus(Integer.valueOf(statusCode));
					LinkedHashMap code = ErrorCodes.codes.get(statusCode);
					String message = "{\"timestamp\":\""+System.currentTimeMillis()+"\",\"status\":\""+statusCode+"\",\"error\":\""+code.get("short")+"\",\"message\": \""+code.get("long")+"\",\"path\":\""+request.getRequestURI()+"\"}";
					response.getWriter().write(message);
					//response.getWriter().flush();
				}


				Principal principle = token == null ? null : [...];

				if (accessor.messageType == SimpMessageType.CONNECT) {
					userRegistry.onApplicationEvent(SessionConnectedEvent(this, message, yourAuth));
				} else if (accessor.messageType == SimpMessageType.SUBSCRIBE) {
					userRegistry.onApplicationEvent(SessionSubscribeEvent(this, message, yourAuth));
				} else if (accessor.messageType == SimpMessageType.UNSUBSCRIBE) {
					userRegistry.onApplicationEvent(SessionUnsubscribeEvent(this, message, yourAuth));
				} else if (accessor.messageType == SimpMessageType.DISCONNECT) {
					userRegistry.onApplicationEvent(SessionDisconnectEvent(this, message, accessor.sessionId, CloseStatus.NORMAL));
				}

				accessor.setUser(principleService.principle());

				// not documented anywhere but necessary otherwise NPE in StompSubProtocolHandler!
				accessor.setLeaveMutable(true);


			}
		})
	}
*/


/*
	@Bean
	public MappingJackson2MessageConverter mappingJackson2MessageConverter(ObjectMapper objectMapper) {
		MappingJackson2MessageConverter jacksonMessageConverter = new MappingJackson2MessageConverter();
		jacksonMessageConverter.setObjectMapper(objectMapper);
		jacksonMessageConverter.setSerializedPayloadClass(String.class);
		jacksonMessageConverter.setStrictContentTypeMatch(true);
		return jacksonMessageConverter;
	}
 */


}



