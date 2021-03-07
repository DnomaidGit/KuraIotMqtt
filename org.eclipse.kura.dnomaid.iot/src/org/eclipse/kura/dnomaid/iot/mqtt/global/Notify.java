/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.kura.dnomaid.iot.mqtt.global;

import org.eclipse.kura.dnomaid.iot.Iot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Notify {
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Iot.class);

    public static void printf(String text) {
    	System.out.println(text);
    	S_LOGGER.info(text);
    }
}
