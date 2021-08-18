/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Eurotech
 *
 *******************************************************************************/
package org.eclipse.kura.dnomaid.cloud;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;

final class CloudConnectionOptions {

    // Cloud Application identifier
    private static final String DEFAULT_CLOUD_PUBLISHER_PID = "";
    private static final String DEFAULT_CLOUD_SUBSCRIBER_PID = "";
    private static final int DEFAULT_PUBLISH_RATE = 1000;
    private static final float DEFAULT_TESTVALUE_INITIAL = 10;
    private static final float DEFAULT_TESTVALUE_INCREMENT = 0.1f;

    // Publishing Property Names
    private static final String PUBLISH_RATE_PROP_NAME = "publish.rate";
    private static final String VALUE_INITIAL_PROP_NAME = "test.value.initial";
    private static final String VALUE_INCREMENT_PROP_NAME = "test.value.increment";
    private static final String CLOUD_PUBLISHER_PROP_NAME = "CloudPublisher.target";
    private static final String CLOUD_SUBSCRIBER_PROP_NAME = "CloudSubscriber.target";

    private final Map<String, Object> properties;

    CloudConnectionOptions(final Map<String, Object> properties) {
        requireNonNull(properties);
        this.properties = properties;
    }

    String getCloudPublisherPid() {
        String cloudPublisherPid = DEFAULT_CLOUD_PUBLISHER_PID;
        Object configCloudPublisherPid = this.properties.get(CLOUD_PUBLISHER_PROP_NAME);
        if (nonNull(configCloudPublisherPid) && configCloudPublisherPid instanceof String) {
            cloudPublisherPid = (String) configCloudPublisherPid;
        }
        return cloudPublisherPid;
    }
    
    String getCloudSubscriberPid() {
        String cloudSubscriberPid = DEFAULT_CLOUD_SUBSCRIBER_PID;
        Object configCloudSubscriberPid = this.properties.get(CLOUD_SUBSCRIBER_PROP_NAME);
        if (nonNull(configCloudSubscriberPid) && configCloudSubscriberPid instanceof String) {
            cloudSubscriberPid = (String) configCloudSubscriberPid;
        }
        return cloudSubscriberPid;
    }

    int getPublishRate() {
        int publishRate = DEFAULT_PUBLISH_RATE;
        Object rate = this.properties.get(PUBLISH_RATE_PROP_NAME);
        if (nonNull(rate) && rate instanceof Integer) {
            publishRate = (int) rate;
        }
        return publishRate;
    }

    float getTestValueInitial() {
        float testValueInitial = DEFAULT_TESTVALUE_INITIAL;
        Object testValue = this.properties.get(VALUE_INITIAL_PROP_NAME);
        if (nonNull(testValue) && testValue instanceof Float) {
        	testValueInitial = (float) testValue;
        }
        return testValueInitial;
    }

    float getTestValueIncrement() {
        float testValueIncrement = DEFAULT_TESTVALUE_INCREMENT;
        Object testValue = this.properties.get(VALUE_INCREMENT_PROP_NAME);
        if (nonNull(testValue) && testValue instanceof Float) {
        	testValueIncrement = (float) testValue;
        }
        return testValueIncrement;
    }
    
}