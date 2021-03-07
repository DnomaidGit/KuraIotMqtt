package org.eclipse.kura.dnomaid.iot.mqtt.client;

import org.eclipse.kura.dnomaid.iot.mqtt.client.Connection;
import org.eclipse.kura.dnomaid.iot.mqtt.device.Devices;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Status;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCallbackHandler implements MqttCallback {
  private boolean knownTopic = false;
  public MqttCallbackHandler() {
  }
  @Override
  public void connectionLost(Throwable cause) {
    if (cause != null) {
      Connection c = Connection.getInstance();
      Status.getInst().changeConnectionStatus(Status.ConnectionStatus.DISCONNECTED);
      Object[] args = new Object[2];
      args[0] = c.getClientId();
      args[1] = c.getServer();
      String message = "connectionLost: "+ args;
      c.addAction(message + " -> " + cause.toString());
    }
  }
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    Connection c = Connection.getInstance();
    String messagePayload = new String(message.getPayload());
    String[] args = new String[2];
    args[0] = "::>Message recieved: " +messagePayload;
    args[1] = " topic:"+topic+";qos:"+message.getQos()+";retained:"+message.isRetained();
    c.addAction(args[0] + args[1]);
    knownTopic = false;    
    Devices.getInst().getDevices().stream().forEach(a->{    	
    	a.getTopics().stream().forEach(b->{
    		if(topic.equals(b.getName())){
    			if(b.updateValueTopic(messagePayload)){
    				knownTopic = true;
    				System.out.println("Update::>"+b.getName());    		
    			}
    		}	
		});    
    }); 
    if(!knownTopic)System.out.println("Unknown topic::>"+topic);
  }
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) { }
}
