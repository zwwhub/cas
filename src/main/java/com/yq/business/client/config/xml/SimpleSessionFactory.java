package com.yq.business.client.config.xml;

import com.yq.business.client.FlushMode;
import com.yq.business.client.redis.RedisSessionRepository;
import com.yq.business.client.redis.RedisTemplate;
import com.yq.business.client.redis.Serializer;
import com.yq.business.client.web.SessionRepository;
import com.yq.business.client.web.SessionStrategy;
import com.yq.business.client.web.SimpleSessionFilter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.JedisPool;

/**
 * Created by alexqdjay on 2018/3/17.
 */
public class SimpleSessionFactory implements FactoryBean<SimpleSessionFilter>, InitializingBean {

    private JedisPool jedisPool;

    private String strategy = "cookie";

    private String flushMode = "LAZY";

    private SessionRepository sessionRepository;

    private Serializer serializer;

    @Override
    public SimpleSessionFilter getObject() throws Exception {
        SimpleSessionFilter simpleSessionFilter = new SimpleSessionFilter(sessionRepository);
        simpleSessionFilter.setSessionStrategy(SessionStrategy.valueOf(strategy));
        return simpleSessionFilter;
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleSessionFilter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void setStrategy(String sessionStrategy) {
        this.strategy = sessionStrategy;
    }

    public void setSessionRepository(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (sessionRepository == null) {
            if (jedisPool == null) {
                throw new IllegalArgumentException("jedisPool must not be null");
            }
            FlushMode fm = FlushMode.valueOf(flushMode);
            RedisTemplate redisTemplate = new RedisTemplate(jedisPool);
            if (serializer != null) {
                redisTemplate.setSerializer(serializer);
            }
            sessionRepository = new RedisSessionRepository(redisTemplate, fm);
        }
    }
}
