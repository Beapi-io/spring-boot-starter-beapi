package io.beapi.api.config

import io.beapi.api.service.PrincipleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;

public class SocketChannelInterceptor extends ChannelInterceptorAdapter {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SocketChannelInterceptor.class);
	private String markerText = "DEVNOTES";
	private Marker devnotes = MarkerFactory.getMarker(markerText);

	@Autowired
	private PrincipleService principleService;

	@Override
	public boolean preReceive(MessageChannel channel) {
		logger.info("preReceive");
		println("prereceive...")
		return super.preReceive(channel);
	}

	@Override
	public Message<?> preSend(org.springframework.messaging.Message<?> message, MessageChannel channel) {

		logger.info("preSend");
		println("presend...")



		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		StompCommand command = accessor.getCommand();

		List tokenList = accessor.getNativeHeader("X-Authorization");
		println("inboundChannel tokens :"+tokenList)
		//accessor.removeNativeHeader("X-Authorization");

		List bearer = accessor.getNativeHeader("Authorization");
		println("inboundChannel tokens2 :"+bearer)
		//accessor.removeNativeHeader("Authorization");

		switch(command){
			case StompCommand.SUBSCRIBE:
				println('SUBSCRIBING...')
				break;
			case StompCommand.DISCONNECT:
				println('DISCONNECTING...')
				break;
			default:
				println("${command.toString()}...")
				break;
		}

		return super.preSend(message, channel);
	}

	@Override
	public void afterSendCompletion(org.springframework.messaging.Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		logger.info("afterSendCompletion");
		println("afterSendCompletion");

		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		StompCommand command = accessor.getCommand();

		switch(command){
			case StompCommand.SUBSCRIBE:
				println('SUBSCRIBING...')
				break;
			case StompCommand.DISCONNECT:
				println('DISCONNECTING...')
				break;
			default:
				println("${command.toString()}...")
				break;
		}
		super.afterSendCompletion(message, channel, sent, ex);

		/*
		if (StompCommand.SUBSCRIBE.equals(command)) {

		}

		if (StompCommand.DISCONNECT.equals(command)) {

		}

		 */

		super.afterSendCompletion(message, channel, sent, ex);
	}


	/**
	 * Instantiate an object for retrieving the STOMP headers
	 */
	private StompHeaderAccessor readHeaderAccessor(Message<?> message) {
		final StompHeaderAccessor accessor = getAccessor(message);
		if (accessor == null) {
			throw new AuthenticationCredentialsNotFoundException("Fail to read headers.");
		}
		return accessor;
	}

	private String readSessionId(StompHeaderAccessor accessor) {
		return ofNullable(accessor.getMessageHeaders().get(SESSION_KEY_HEADER))
				.orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Session header not found")).toString();
	}

	private String readAuthKeyHeader(StompHeaderAccessor accessor) {
		final String authKey = accessor.getFirstNativeHeader(API_KEY_HEADER);
		if (authKey == null || authKey.trim().isEmpty())
			throw new AuthenticationCredentialsNotFoundException("Auth Key Not Found");
		return authKey;
	}

	private String readWebSocketIdHeader(StompHeaderAccessor accessor) {
		final String wsId = accessor.getFirstNativeHeader(WS_ID_HEADER);
		if (wsId == null || wsId.trim().isEmpty())
			throw new AuthenticationCredentialsNotFoundException("Web Socket ID Header not found");
		return wsId;
	}

	StompHeaderAccessor getAccessor(Message<?> message) {
		return MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	}
}
