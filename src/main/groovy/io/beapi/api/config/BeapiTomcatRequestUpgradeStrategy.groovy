package io.beapi.api.config;

import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.context.Lifecycle;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.socket.WebSocketHandler;


public class BeapiTomcatRequestUpgradeStrategy extends TomcatRequestUpgradeStrategy implements Lifecycle {

	@Override
	public void start() {
		println("socket connection started...");
	}

	@Override
	public void stop() {
		println("socket connection stopped...");
	}


	@Override
	public boolean isRunning() {
		return true;
	}

}