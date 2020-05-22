package com.yq.business.client.config.boot;
import com.yq.business.client.jvm.MapSessionRepository;
import com.yq.business.client.redis.RedisSessionRepository;
import com.yq.business.client.redis.RedisTemplate;
import com.yq.business.client.redis.Serializer;
import com.yq.business.client.web.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import redis.clients.jedis.JedisPool;

/**
 * Created by alexqdjay on 2018/3/17.
 */
@Configuration
@EnableConfigurationProperties(SimpleSessionProperties.class)
public class SimpleSessionConfig {

    @Autowired
    private SimpleSessionProperties simpleSessionProperties;

    @Autowired(required = false)
    private Serializer serializer;
    private SessionRepository sessionRepository;

    @Bean
    @ConditionalOnMissingBean(JedisPool.class)
    public JedisPool jedisPool() {
        SimpleSessionProperties.Redis redis = simpleSessionProperties.getRedis();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(redis.getMaxIdle());
        poolConfig.setMinIdle(redis.getMinIdle());
        poolConfig.setMaxTotal(redis.getMaxActive());
        poolConfig.setMaxWaitMillis(redis.getMaxWait());
        poolConfig.setTestWhileIdle(redis.isTestWhileIdle());
        poolConfig.setTimeBetweenEvictionRunsMillis(redis.getTimeBetweenEvictionRunsMillis());
        poolConfig.setBlockWhenExhausted(redis.isBlockWhenExhausted());
        poolConfig.setMinEvictableIdleTimeMillis(redis.getMinEvictableIdleTimeMillis());
        poolConfig.setNumTestsPerEvictionRun(redis.getNumTestsPerEvictionRun());

        if (redis.getPassword() == null) {
            return new JedisPool(poolConfig, redis.getHost(),
                redis.getPort(), redis.getTimeout());
        } else {
            return new JedisPool(poolConfig, redis.getHost(),
                    redis.getPort(), redis.getTimeout(), redis.getPassword());
        }
    }

    @Bean
    @ConditionalOnProperty(name = "simplesession.store", havingValue = "redis", matchIfMissing = true)
    public SessionRepository redisSessionRepository(JedisPool jedisPool) {
        RedisTemplate redisTemplate = new RedisTemplate(jedisPool);
        if (serializer != null) {
            redisTemplate.setSerializer(serializer);
        }
        return new RedisSessionRepository(redisTemplate,
                simpleSessionProperties.getFlushMode(), simpleSessionProperties.getMaxExpireSecond());
    }

    @Bean
    @ConditionalOnProperty(name = "simplesession.store", havingValue = "map", matchIfMissing = false)
    public SessionRepository mapRepository() {
        return new MapSessionRepository();
    }


    private SessionStrategy sessionStrategy() {
        return SessionStrategy.valueOf(simpleSessionProperties.getStrategy());
    }

    private SimpleSessionFilter simpleSessionFilter(SessionRepository sessionRepository) {
        SimpleSessionFilter simpleSessionFilter =  new SimpleSessionFilter(sessionRepository);
        simpleSessionFilter.setSessionStrategy(sessionStrategy());
        return simpleSessionFilter;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean(SessionRepository sessionRepository) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(simpleSessionFilter(sessionRepository));
        filterRegistrationBean.addUrlPatterns(simpleSessionProperties.getFilterPath());
        filterRegistrationBean.setName("simpleSessionFilter");
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

}
