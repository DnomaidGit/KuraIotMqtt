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

public interface ConnectionDefaults extends Constants {
  String URI = "tcp://";
  String SERVER = "192.168.1.39";
  int PORT = 1883;
  String CLIENT_ID = "ClientRashberry"+ID;
  boolean CLEAN_SESSION = true;
  int PUBLISH_QOS = 0;
  int SUBSCRIBE_QOS = 1;
  int TIME_OUT = 30000;
  int KEEP_ALIVE = 15000;
  boolean SSL = false;
  String SSL_PASSWORD = "mqtttest";
  boolean RETAINED = true;
  String USERNAME = "guest";
  String PASSWORD = "test12";
  String MESSAGE_LWT =CLIENT_ID+" disconnected!";
  String PUBLISH_TOPIC =CMND_PREFIX;
  String SUBSCRIBE_TOPIC =ID+"/#";
}
