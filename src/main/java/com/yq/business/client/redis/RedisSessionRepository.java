package com.yq.business.client.redis;

import com.yq.business.client.Consts;
import com.yq.business.client.FlushMode;
import com.yq.business.client.web.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by alexqdjay on 2017/9/3.
 */
public class RedisSessionRepository implements SessionRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSessionRepository.class);

    private RedisTemplate redisTemplate;
    private FlushMode flushMode;
    private int maxExpireSecond = 1800; // 默认过期时间30min

    public RedisSessionRepository(RedisTemplate redisTemplate) {
        this(redisTemplate, FlushMode.LAZY);
    }

    public RedisSessionRepository(RedisTemplate redisTemplate, FlushMode flushMode) {
        this(redisTemplate, flushMode, 1800);
    }

    public RedisSessionRepository(RedisTemplate redisTemplate, FlushMode flushMode, int maxExpireSecond) {
        this.redisTemplate = redisTemplate;
        this.flushMode = flushMode;
        this.maxExpireSecond = maxExpireSecond;
    }

    @Override
    public Map<String, Object> getSessionAttributesById(String sessionId) {
        try {
            Map<String, Object> hmget = redisTemplate.hmget(sessionIdKey(sessionId));
            if (hmget !=null){
                Long now = System.currentTimeMillis();
                if ((now - (Long)hmget.get("lastAccessTime"))/1000> (Integer)hmget.get("maxInactiveInterval")){
                    return null;
                }
            }
            return hmget;
        } catch (Exception e) {
            LOG.error("get session attributes error msg:{}", e.getMessage());
            return null;
        }
    }

    @Override
    public void saveAttributes(String sessionId, Map<String, Object> attributes) {
        redisTemplate.hmset(sessionIdKey(sessionId), attributes, maxExpireSecond);
    }

    private String sessionIdKey(String sessionId) {
        return String.format(Consts.RedisFields.FIELDS_PREFIX, sessionId);
    }

    @Override
    public FlushMode getFlushMode() {
        return flushMode;
    }

    @Override
    public void removeSession(String sessionId) {
        redisTemplate.delete(sessionIdKey(sessionId));
    }
}
