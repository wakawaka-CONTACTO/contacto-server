package org.kiru.chat.config.websocket;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        if (query != null) {
                            String[] params = query.split("&");
                            for (String param : params) {
                                String[] keyValue = param.split("=");
                                if (keyValue.length == 2) {
                                    if ("userId".equals(keyValue[0])) {
                                        attributes.put("userId", keyValue[1]);
                                    } else if ("accessToken".equals(keyValue[0])) {
                                        attributes.put("accessToken", keyValue[1]);
                                    }
                                }
                            }
                        }
                        if (!attributes.containsKey("userId")) {
                            throw new ContactoException(FailureCode.SOCKET_CONNECTED_FAILED);
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Exception exception) {
                        log.info(">>> afterHandshake OK");
                    }
                })
                .withSockJS();
    }
}