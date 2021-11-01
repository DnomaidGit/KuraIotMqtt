package org.eclipse.kura.dnomaid.iot.mqtt.client;

import org.eclipse.kura.dnomaid.iot.mqtt.global.ConnectionConstants;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Notify;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Status;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;


public class Connection {
	private static Connection instance = null;
	private MqttClient client;
	private MqttConnectOptions conOpt;
	private String uri;
	private String server;
	private int port;
	private String clientId;
	private int publishQos;
	private int subscribeQos;
	private boolean ssl;
	private boolean retained;
	private String messageLWT;
	private String publishTopic;
	private String subscribeTopic;

  //Constructor
	public Connection(){
		uri = "";
		server = "";
		port = 0;
		clientId = "";
		publishQos = 0;
		subscribeQos = 0;
		ssl = false;
		retained = false;
		messageLWT = "";
		publishTopic = "";
		subscribeTopic = "";
	}
  //----------------
	public synchronized static Connection getInstance() {
	    if (instance == null) {
	      instance = new Connection();
	    }
	    return instance;
	}
	public MqttClient createClient() throws MqttException{
	    server = ConnectionConstants.getInst().getServer();
	    port = ConnectionConstants.getInst().getPort();
	    ssl = ConnectionConstants.getInst().isSsl();	    
	    if (ssl) {
	    	ConnectionConstants.getInst().setUri("ssl://"+server+":"+port);
	    }
	    else {
	    	ConnectionConstants.getInst().setUri("tcp://"+server+":"+port);
	    }
	    uri = ConnectionConstants.getInst().getUri();
	    clientId = ConnectionConstants.getInst().getClientId();
		client = new MqttClient(uri, clientId);
	    return client;
	}
	public MqttConnectOptions createConnectionOptions() {
		messageLWT = ConnectionConstants.getInst().getMessageLWT();
		publishTopic = ConnectionConstants.getInst().getPublishTopic();
		publishQos = ConnectionConstants.getInst().getPublishQos();
		retained = ConnectionConstants.getInst().isRetained();
		conOpt = ConnectionConstants.getInst().getConOpt();
		// last will message
        if ((!messageLWT.equals(Status.EMPTY)) || (!publishTopic.equals(Status.EMPTY))) {
        	conOpt.setWill(publishTopic, messageLWT.getBytes(), publishQos, retained);
        }
		return conOpt;
	}
	public void createSubscribeTopic() {
		subscribeTopic = ConnectionConstants.getInst().getSubscribeTopic();
		subscribeQos = ConnectionConstants.getInst().getSubscribeQos();
		retained = ConnectionConstants.getInst().isRetained();
	}	
	public void createPublishTopic() {
		publishTopic = ConnectionConstants.getInst().getPublishTopic();
		publishQos = ConnectionConstants.getInst().getPublishQos();
		retained = ConnectionConstants.getInst().isRetained();	
	}	
	public void addAction(String action) {Notify.printf(action); }  
//Only value read 
	public MqttClient getClient() { return client; }
	public MqttConnectOptions getConnectionOptions() { return conOpt; }
	public String getClientId() { return clientId; }
	public String getServer() { return server; }
	public String getSubscribeTopic() {return subscribeTopic;}
	public int getSubscribeQos() {return subscribeQos;}
	public boolean isRetained() {return retained;}
	public String getPublishTopic() {return publishTopic;}
	public int getPublishQos() {return publishQos;}
}
