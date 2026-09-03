package com.ng_doanh.hr_management_system.notification.config;

import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetailsService;
import com.ng_doanh.hr_management_system.common.security.JwtTokenProvider;
import com.ng_doanh.hr_management_system.common.security.RedisTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTokenBlacklistService tokenBlacklistService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker to send messages to subscribers
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages that are bound for methods annotated with @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific queues
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections, enabling SockJS fallback options
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = null;

                    // 1. Try to extract token from Authorization native header
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String bearer = authHeaders.getFirst();
                        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                            token = bearer.substring(7);
                        }
                    }

                    // 2. Fallback to "token" query parameter or header
                    if (!StringUtils.hasText(token)) {
                        List<String> tokenHeaders = accessor.getNativeHeader("token");
                        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                            token = tokenHeaders.getFirst();
                        }
                    }

                    if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                        if (tokenBlacklistService.isBlacklisted(token)) {
                            log.warn("WebSocket CONNECT rejected: Token is blacklisted");
                            return null;
                        }

                        String username = jwtTokenProvider.getUsernameFromToken(token);
                        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        log.info("WebSocket authenticated user: {}", username);
                    } else {
                        log.warn("WebSocket CONNECT attempt without valid JWT token");
                    }
                }

                return message;
            }
        });
    }
}
