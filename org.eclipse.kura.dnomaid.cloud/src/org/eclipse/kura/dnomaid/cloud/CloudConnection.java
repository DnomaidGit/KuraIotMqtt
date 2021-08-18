/*******************************************************************************
 * Copyright (c) 2011, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech and/or its affiliates
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.dnomaid.cloud;

import static java.util.Objects.nonNull;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.cloudconnection.listener.CloudConnectionListener;
import org.eclipse.kura.cloudconnection.listener.CloudDeliveryListener;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.publisher.CloudPublisher;
import org.eclipse.kura.cloudconnection.subscriber.CloudSubscriber;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraPosition;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudConnection implements ConfigurableComponent, CloudSubscriberListener, CloudConnectionListener, CloudDeliveryListener {

    private static final Logger S_LOGGER = LoggerFactory.getLogger(CloudConnection.class);
    private static final String ALIAS_APP_ID = "CloudDnomaid"; //

    private CloudPublisher cloudPublisher;

    private CloudSubscriber cloudSubscriber;

    private ScheduledExecutorService worker;
    private ScheduledFuture<?> handle;

    private float valueTest;
    private Map<String, Object> properties;
    private static boolean ENABLE;

    private CloudConnectionOptions cloudConnectionOptions;

    public void setCloudPublisher(CloudPublisher cloudPublisher) {
        this.cloudPublisher = cloudPublisher;
        this.cloudPublisher.registerCloudConnectionListener(CloudConnection.this);
        this.cloudPublisher.registerCloudDeliveryListener(CloudConnection.this);
    }

    public void unsetCloudPublisher(CloudPublisher cloudPublisher) {
        this.cloudPublisher.unregisterCloudConnectionListener(CloudConnection.this);
        this.cloudPublisher.unregisterCloudDeliveryListener(CloudConnection.this);
        this.cloudPublisher = null;
    }

    public void setCloudSubscriber(CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber = cloudSubscriber;
        this.cloudSubscriber.registerCloudSubscriberListener(CloudConnection.this);
        this.cloudSubscriber.registerCloudConnectionListener(CloudConnection.this);
    }

    public void unsetCloudSubscriber(CloudSubscriber cloudSubscriber) {
        this.cloudSubscriber.unregisterCloudSubscriberListener(CloudConnection.this);
        this.cloudSubscriber.unregisterCloudConnectionListener(CloudConnection.this);
        this.cloudSubscriber = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        loggerInfo("Activating "+ ALIAS_APP_ID +"...");

        // start worker
        this.worker = Executors.newSingleThreadScheduledExecutor();

        this.properties = properties;
        dumpProperties("Activate", properties);

        this.cloudConnectionOptions = new CloudConnectionOptions(properties);

        doUpdate();

        loggerInfo("Activating "+ ALIAS_APP_ID +"... Done.");
    }

    protected void deactivate(ComponentContext componentContext) {
        loggerInfo("Deactivating "+ ALIAS_APP_ID +"...");

        // shutting down the worker and cleaning up the properties
        this.worker.shutdown();

        S_LOGGER.info("Deactivating "+ ALIAS_APP_ID +"... Done.");
    }

    public void updated(Map<String, Object> properties) {
        S_LOGGER.info("Updated "+ ALIAS_APP_ID +"...");

        // store the properties received
        this.properties = properties;
        dumpProperties("Update", properties);

        this.cloudConnectionOptions = new CloudConnectionOptions(properties);
        ENABLE = (Boolean)properties.get("Enable");
        // try to kick off a new job
        doUpdate();
        S_LOGGER.info("Updated "+ ALIAS_APP_ID +"... Done.");
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    /**
     * Dump properties in stable order
     *
     * @param properties
     *            the properties to dump
     */
    private static void dumpProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
        }
    }

    /**
     * Called after a new set of properties has been configured on the service
     */
    private void doUpdate() {
        // cancel a current worker handle if one if active
        if (this.handle != null) {
            this.handle.cancel(true);
        }

        // reset the temperature to the initial value
        this.valueTest = this.cloudConnectionOptions.getTestValueInitial();

        // schedule a new worker based on the properties of the service
        int pubrate = this.cloudConnectionOptions.getPublishRate();
        this.handle = this.worker.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
            	if(ENABLE)doPublish();
            }
        }, 0, pubrate, TimeUnit.MILLISECONDS);
    }

    /**
     * Called at the configured rate to publish the next temperature measurement.
     */
    private void doPublish() {
        // Increment the simulated temperature value
        float valueTestIncr = this.cloudConnectionOptions.getTestValueIncrement();
        this.valueTest += valueTestIncr;

        // Allocate a new payload
        KuraPayload payload = new KuraPayload();

        // Timestamp the message
        payload.setTimestamp(new Date());

        // Add the temperature as a metric to the payload
        payload.addMetric("Test value", this.valueTest);
        S_LOGGER.info("Test value: {}", this.valueTest);

        
        // Publish the message
        try {
            if (nonNull(this.cloudPublisher)) {
                KuraMessage message = new KuraMessage(payload);
                String messageId = this.cloudPublisher.publish(message);
                S_LOGGER.info("Published to message: {} with ID: {}", message, messageId);
            }
        } catch (Exception e) {
            S_LOGGER.error("Cannot publish: ", e);
        }
    }

    private void logReceivedMessage(KuraMessage msg) {
        KuraPayload payload = msg.getPayload();
        Date timestamp = payload.getTimestamp();
        if (timestamp != null) {
            S_LOGGER.info("Message timestamp: {}", timestamp.getTime());
        }

        KuraPosition position = payload.getPosition();
        if (position != null) {
            S_LOGGER.info("Position latitude: {}", position.getLatitude());
            S_LOGGER.info("         longitude: {}", position.getLongitude());
            S_LOGGER.info("         altitude: {}", position.getAltitude());
            S_LOGGER.info("         heading: {}", position.getHeading());
            S_LOGGER.info("         precision: {}", position.getPrecision());
            S_LOGGER.info("         satellites: {}", position.getSatellites());
            S_LOGGER.info("         speed: {}", position.getSpeed());
            S_LOGGER.info("         status: {}", position.getStatus());
            S_LOGGER.info("         timestamp: {}", position.getTimestamp());
        }

        byte[] body = payload.getBody();
        if (body != null && body.length != 0) {
            S_LOGGER.info("Body lenght: {}", body.length);
        }

        if (payload.metrics() != null) {
            for (Entry<String, Object> entry : payload.metrics().entrySet()) {
                S_LOGGER.info("Message metric: {}, value: {}", entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void onConnectionEstablished() {
        S_LOGGER.info("Connection established");
    }

    @Override
    public void onConnectionLost() {
        S_LOGGER.warn("Connection lost!");
    }

    @Override
    public void onMessageArrived(KuraMessage message) {
        logReceivedMessage(message);
    }

    @Override
    public void onDisconnected() {
        S_LOGGER.warn("On disconnected");
    }

    @Override
    public void onMessageConfirmed(String messageId) {
        S_LOGGER.info("Confirmed message with id: {}", messageId);
    }
    
    
    private void loggerInfo (String message) {
		// var/log/kura-console.log
		System.out.println("::"+ALIAS_APP_ID+"::"+message);
		// var/log/kura.log
		S_LOGGER.info("::"+ALIAS_APP_ID+"::"+message);		
	}    	
}
