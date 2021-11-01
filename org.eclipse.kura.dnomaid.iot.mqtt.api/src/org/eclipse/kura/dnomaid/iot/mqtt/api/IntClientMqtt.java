package org.eclipse.kura.dnomaid.iot.mqtt.api;

public interface IntClientMqtt {
	class MessageException extends Exception {
		private static final long serialVersionUID = 1L;
	};

	boolean isConnected()throws MessageException;
	
	int numberRelay() throws MessageException;
	
	String nameRelay(int number) throws MessageException;
	
	void publish(String relay, String message) throws MessageException;

}
