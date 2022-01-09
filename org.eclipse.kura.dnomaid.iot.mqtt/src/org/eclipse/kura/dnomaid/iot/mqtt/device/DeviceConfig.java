package org.eclipse.kura.dnomaid.iot.mqtt.device;

import org.eclipse.kura.dnomaid.iot.mqtt.global.Constants.TypeDevice;

public class DeviceConfig {
	private TypeDevice typeDevice; 
	private String numberDevice;
	private String aliasDevice;
	
	public DeviceConfig(TypeDevice typeDevice, String numberDevice, String aliasDevice) {
		super();
		this.typeDevice = typeDevice;
		this.numberDevice = numberDevice;
		this.aliasDevice = aliasDevice;
	}
	public TypeDevice getTypeDevice() {
		return typeDevice;
	}
	public void setTypeDevice(TypeDevice typeDevice) {
		this.typeDevice = typeDevice;
	}
	public String getNumberDevice() {
		return numberDevice;
	}
	public void setNumberDevice(String numberDevice) {
		this.numberDevice = numberDevice;
	}	
	public String getAliasDevice() {
		return aliasDevice;
	}
	public void setAliasDevice(String aliasDevice) {
		this.aliasDevice = aliasDevice;
	}
	@Override
	public String toString() {
		return typeDevice.name()+"_"+numberDevice;
	}
	
}
