package org.kiru.gateway.config;

import org.kiru.gateway.jwt.JwtValidStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@EnableAutoConfiguration(exclude={RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class})
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;
    
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public ReactiveRedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        redisConfig.setPassword(password);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public ReactiveRedisOperations<String, JwtValidStatusDto> redisTemplate() {
        ReactiveRedisConnectionFactory rrcf = redisConnectionFactory();

        Jackson2JsonRedisSerializer<JwtValidStatusDto> serializer = new Jackson2JsonRedisSerializer<>(JwtValidStatusDto.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, JwtValidStatusDto> builder = RedisSerializationContext
                .newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, JwtValidStatusDto> context = builder.value(serializer).hashValue(serializer)
                .hashKey(serializer).build();

        return new ReactiveRedisTemplate<>(rrcf, context);
    }
}
