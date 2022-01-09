package org.eclipse.kura.dnomaid.iot.mqtt.api;

public interface IntMqttDevice {
	class MessageMqttDeviceException extends Exception {
		private static final long serialVersionUID = 1L;
	};
	
	void addMqttDevice(String typeDevice, String numberDevice, String aliasDevice) throws MessageMqttDeviceException;
	void deleteMqttDevice(String typeDevice, String numberDevice) throws MessageMqttDeviceException;

}
