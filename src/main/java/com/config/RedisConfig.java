package com.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 	redis配置类
 */
@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport{

	/*
	 * 	SpringBoot中RedisTemplate默认采用的的是JDK自带的序列化策略，存储对象会出现乱码
	 * 	StringRedisTemplate默认采用的是String的序列化策略，所以不受影响
	 * 	所以这里重新给RedisTemplate修改为JSON序列化的方式，对象需要实现序列化接口
	 */
	@SuppressWarnings({ "rawtypes", "deprecation", "unused" })
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		RedisSerializer<String> redisSerializer=new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
				new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om=new ObjectMapper();
		
		om.setVisibility(PropertyAccessor.ALL,JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setConnectionFactory(redisConnectionFactory);
//		template.setDefaultSerializer(jackson2JsonRedisSerializer);
		
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(jackson2JsonRedisSerializer);
		
		template.setEnableTransactionSupport(true); //打开事务支持
		return template;
	}
	
	/*
	 * 	配置redis缓存对象，对redis的键值做缓存序列化处理
	 */
	@SuppressWarnings("deprecation")
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisSerializer<String> redisSerializer=new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
				new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om=new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL,JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		RedisCacheConfiguration config=RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofSeconds(600))
				.serializeKeysWith
					(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
				.serializeValuesWith
					(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.disableCachingNullValues();
		RedisCacheManager cacheManager=RedisCacheManager.builder(redisConnectionFactory)
				.cacheDefaults(config)
				.build();
		return cacheManager;
	}
}
