package org.eclipse.kura.dnomaid.iot.mqtt.global;

import org.eclipse.kura.dnomaid.iot.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notify {
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Scheduled.class);

    public static void printf(String text) {
    	System.out.println(text);
    	S_LOGGER.info(text);
    }
}
