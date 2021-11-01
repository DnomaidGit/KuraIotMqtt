package org.eclipse.kura.dnomaid.iot.mqtt.schedule;

public class ScheduleRelay {
	private int id;
	private String name="Scheduled";
	private String propertyRelay="Relay";
    private String propertyCmnd="Cmnd";;
    private String propertyHour="Hour";
    private String propertyMinute="Minute";

    private String valueRelay;
    private String valueCmnd;
    private String valueHour;
    private String valueMinute;
        
	public ScheduleRelay(int id) {
		super();
		this.id = id;
		this.name = id + this.name;
		this.propertyRelay = this.name + this.propertyRelay; 
		this.propertyCmnd = this.name + this.propertyCmnd;
		this.propertyHour = this.name + this.propertyHour;
		this.propertyMinute = this.name + this.propertyMinute;		
	}

	public String getValueRelay() {
		return valueRelay;
	}

	public void setValueRelay(String valueRelay) {
		this.valueRelay = valueRelay;
	}

	public String getValueCmnd() {
		return valueCmnd;
	}

	public void setValueCmnd(String valueCmnd) {
		this.valueCmnd = valueCmnd;
	}

	public String getValueHour() {
		return valueHour;
	}

	public void setValueHour(String valueHour) {
		this.valueHour = valueHour;
	}

	public String getValueMinute() {
		return valueMinute;
	}

	public void setValueMinute(String valueMinute) {
		this.valueMinute = valueMinute;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPropertyRelay() {
		return propertyRelay;
	}

	public String getPropertyCmnd() {
		return propertyCmnd;
	}

	public String getPropertyHour() {
		return propertyHour;
	}

	public String getPropertyMinute() {
		return propertyMinute;
	}

	@Override
	public String toString() {
		return id+name;
	}
	
}