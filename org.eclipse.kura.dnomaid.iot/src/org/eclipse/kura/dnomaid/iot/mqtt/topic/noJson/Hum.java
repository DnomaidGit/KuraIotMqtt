package org.eclipse.kura.dnomaid.iot.mqtt.topic.noJson;

public class Hum {
	private String name = "Hum";
	private String Hum;

	public String getHum() {
		return Hum;
	}

	public void setHum(String temp) {
		Hum = temp;
	}

	@Override
	public String toString() {
		return name;
	}

}
