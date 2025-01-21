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
    /**
     * Configures the message broker for WebSocket communication.
     *
     * @param config the MessageBrokerRegistry to be configured for message routing
     *
     * @see MessageBrokerRegistry
     *
     * This method sets up the message broker with the following configurations:
     * - Enables simple broker for destinations with prefixes "/topic" and "/queue"
     * - Sets the application destination prefix to "/app"
     * - Sets the user destination prefix to "/user"
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registers STOMP endpoints for WebSocket communication in the chat application.
     *
     * This method configures a WebSocket endpoint at "/chat-websocket" with the following characteristics:
     * - Allows connections from any origin
     * - Includes a custom handshake interceptor for authentication and user identification
     * - Supports SockJS fallback for browsers that do not support WebSocket
     *
     * The handshake interceptor performs the following actions:
     * - Extracts 'userId' and 'accessToken' from the connection query parameters
     * - Validates the presence of 'userId' in the connection attributes
     * - Throws a {@link ContactoException} if 'userId' is missing
     *
     * @param registry The {@link StompEndpointRegistry} used to register WebSocket endpoints
     * @throws ContactoException if the user ID is not provided during the WebSocket connection
     */
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