package org.eclipse.kura.dnomaid.iot.mqtt.topic.noJson;

public class POWER {
	private String name = "POWER";
	private String POWER;

	public String getPOWER() {
		return POWER;
	}

	public void setPOWER(String pOWER) {
		POWER = pOWER;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
