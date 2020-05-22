package com.yq.business.client.web;

import com.yq.business.client.FlushMode;

import java.util.Map;

/**
 * Created by alexqdjay on 2017/8/20.
 */
public interface SessionRepository {

    Map<String, Object> getSessionAttributesById(String sessionId);

    void saveAttributes(String sessionId, Map<String, Object> attributes);

    FlushMode getFlushMode();

    void removeSession(String sessionId);
}
