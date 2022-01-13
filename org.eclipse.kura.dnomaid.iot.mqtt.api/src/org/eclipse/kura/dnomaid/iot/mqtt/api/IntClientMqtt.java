package org.eclipse.kura.dnomaid.iot.mqtt.api;

public interface IntClientMqtt {
	class MessageException extends Exception {
		private static final long serialVersionUID = 1L;
	};

	boolean isConnected()throws MessageException;
	
	int numberRelay() throws MessageException;
		
	void publishRelay(String alias, String message) throws MessageException;
		
}
