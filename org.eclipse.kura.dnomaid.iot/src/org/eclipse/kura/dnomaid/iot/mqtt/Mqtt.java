package org.eclipse.kura.dnomaid.iot.mqtt;

import org.eclipse.kura.dnomaid.iot.mqtt.client.ActionListener;
import org.eclipse.kura.dnomaid.iot.mqtt.client.Connection;
import org.eclipse.kura.dnomaid.iot.mqtt.client.MqttCallbackHandler;
import org.eclipse.kura.dnomaid.iot.mqtt.global.ConnectionConstants;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Status;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class Mqtt {
private Connection connection;
private ActionListener actionListener;
private MqttClient client;
private MqttConnectOptions conOpt;

public void connection() {
    connection = Connection.getInstance();
    Status.getInst().changeConnectionStatus(Status.ConnectionStatus.CONNECTING);        
    String[] additionalArgs = new String[2];
    additionalArgs[0] = " ClientId:"+ConnectionConstants.getInst().getClientId();
    additionalArgs[1] = ";Server:"+ConnectionConstants.getInst().getServer();
    actionListener = new ActionListener(ActionListener.Action.CONNECT, additionalArgs);
    boolean doConnect = true;
        try {
            client = Connection.getInstance().createClient();
            conOpt = Connection.getInstance().createConnectionOptions();
            client.setCallback(new MqttCallbackHandler());
        }
        catch (Exception e) {
            doConnect = false;
            actionListener.onFailure(null, e);
        }
    if (doConnect) {
        try {	            	
            client.connect(conOpt);
            actionListener.onSuccess(null);
        }
        catch (MqttSecurityException e) {
            actionListener.onFailure(null, e);
        }
        catch (MqttException e) {
            actionListener.onFailure(null, e);
        }
    }	        
}
public void disconnection() {
 	Status.getInst().changeConnectionStatus(Status.ConnectionStatus.DISCONNECTING);
    String[] additionalArgs = new String[2];
    additionalArgs[0] = " ClientId:"+connection.getClientId();
    additionalArgs[1] = ";Server:"+connection.getServer();
    try {
        actionListener = new ActionListener(ActionListener.Action.DISCONNECT, additionalArgs);                
    	connection.getClient().disconnect();
    	actionListener.onSuccess(null);
    } catch (MqttException e) {
    	actionListener.onFailure(null, e);
    }
}	 
public void subscribe() {
    connection = Connection.getInstance();
	connection.createSubscribeTopic();
    String[] additionalArgs = new String[3];
    additionalArgs[0] = " Topic:"+connection.getSubscribeTopic();
    additionalArgs[1] = ";Qos:" + connection.getPublishQos();
    additionalArgs[2] = ";Retained:" + connection.isRetained();
    try {
        actionListener = new ActionListener(ActionListener.Action.SUBSCRIBE, additionalArgs);                
        connection.getClient().subscribe(connection.getSubscribeTopic(), connection.getSubscribeQos());                
        actionListener.onSuccess(null);              
    }
    catch (MqttSecurityException e) {
    	actionListener.onFailure(null, e);
    }
    catch (MqttException e) {
    	actionListener.onFailure(null, e);
    }	        
}	    
public void unsubscribe() {
    String[] additionalArgs = new String[3];
    additionalArgs[0] = " Topic:"+connection.getSubscribeTopic();
    additionalArgs[1] = ";Qos:" + connection.getPublishQos();
    additionalArgs[2] = ";Retained:" + connection.isRetained();
    try {
        actionListener = new ActionListener(ActionListener.Action.UNSUBSCRIBE, additionalArgs);                
        connection.getClient().unsubscribe(connection.getSubscribeTopic());                
        actionListener.onSuccess(null);              
    }
    catch (MqttSecurityException e) {
    	actionListener.onFailure(null, e);
    }
    catch (MqttException e) {
    	actionListener.onFailure(null, e);
    }	        
}	    
public void publish(String topic, String message) {
    connection.createPublishTopic();
    String[] additionalArgs = new String[4];
    additionalArgs[0] = " Message:" + message;
    additionalArgs[1] = ";Topic:" + topic;
    additionalArgs[2] = ";Qos:" + connection.getPublishQos();
    additionalArgs[3] = ";Retained:" + connection.isRetained();
    try {
        actionListener = new ActionListener(ActionListener.Action.PUBLISH, additionalArgs);                
        connection.getClient().publish(topic, message.getBytes(), connection.getPublishQos(), connection.isRetained());
        actionListener.onSuccess(null);
    } catch (MqttException e) {
    	actionListener.onFailure(null, e);
    }
}

}


