package com.yq.business.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by alexqdjay on 2017/8/20.
 */
public class LogHolder {

    public static final Logger LOGGER = LoggerFactory.getLogger("com.yq.business.client.LOGGER");

    private LogHolder() {
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
