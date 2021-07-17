package org.eclipse.kura.dnomaid.iot.mqtt.device;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.eclipse.kura.dnomaid.iot.mqtt.global.Constants;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.TopicJson;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.TopicNoJson;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.json.AqaraTempJson;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.json.SonoffSNZB02Json;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.json.TuyaZigBeeSensorJson;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.json.XiaomiZNCZ04LM;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.noJson.POWER;
import org.eclipse.kura.dnomaid.iot.mqtt.topic.noJson.Set;

public class Devices implements Constants {	
    private ArrayList<DeviceConfig> DevicesConfig;
	private ArrayList<Device> Devices;

    private static Devices myGlobal = null;
    public  static synchronized Devices getInst() {
        if (myGlobal==null) {
            myGlobal=new Devices();
        }
        return myGlobal;
    }    
    Devices(){
    	DevicesConfig  = new ArrayList<>();
		Devices  = new ArrayList<>();
    }
    
    public void newDevice(TypeDevice typeDevice, String numberDevice){
    	DevicesConfig.add(new DeviceConfig(typeDevice, numberDevice));
    	selectDevice(typeDevice, numberDevice);
    }
    public void deleteDevice(DeviceConfig deviceConfig){     	
		for (int i = 0; i < getDevices().size(); ++i) {
			if (deviceConfig.toString().equals(getDevices().get(i).toString())){
				getDevices().remove(i);
			}
		}
		for (int i = 0; i < getDevicesConfig().size(); ++i) {
			if (deviceConfig.toString().equals(getDevicesConfig().get(i).toString())){
				getDevicesConfig().remove(i);
			}
		}
    }
    
    public ArrayList<DeviceConfig> getDevicesConfig() {return DevicesConfig;}
	public ArrayList<Device> getDevices() {return Devices;}
	public ArrayList<Device> getRelays() {
		ArrayList<Device> filterList = (ArrayList<Device>) getDevices().stream()
				  .filter(c -> c.getGroupList().equals(GroupList.Relay) 
						  || c.getGroupList().equals(GroupList.RelaySensorClimate))
				  .collect(Collectors.toList()); 		
		return filterList;		
		}
	public ArrayList<Device> getSensorsClimate() {
		ArrayList<Device> filterList = (ArrayList<Device>) getDevices().stream()
				  .filter(c -> c.getGroupList().equals(GroupList.SensorClimate) 
						  || c.getGroupList().equals(GroupList.RelaySensorClimate))
				  .collect(Collectors.toList()); 		
		return filterList;
		}
	public String getPublishTopicRelay(Integer numberRelay) {
		String PublishTopicRelay = "PublishTopic01Relay??";
		if(numberRelay>0&getRelays().size()>=numberRelay) {
			PublishTopicRelay = getRelays().get(numberRelay-1).getTopics().get(1).getName();
		}
		return PublishTopicRelay;
	}    
	
	private void selectDevice (TypeDevice typeDevice, String numberDevice){
		String nametopic01 = "";
		String nametopic02 = "";
		GroupList groupList;
		TypeGateway typeGateway;
		TopicNoJson topicNoJson01;
		TopicNoJson topicNoJson02;
		TopicJson topicJson01;
		Device device;
		
		switch (typeDevice) {
		case SonoffS20:
			typeGateway = TypeGateway.Router_1;
			groupList = GroupList.Relay;
			nametopic01 = groupList+"_1"+"/POWER";
			nametopic02 = nametopic01;			
			topicNoJson01 = new TopicNoJson(STAT_PREFIX, nametopic01, new POWER());
			topicNoJson02 = new TopicNoJson(CMND_PREFIX, nametopic02, new POWER());
			device = createDevice(typeGateway, typeDevice, numberDevice, groupList, topicNoJson01, topicNoJson02);		
	    	Devices.add(device);
			break;
		case SonoffSNZB02:
			typeGateway = TypeGateway.CC2531_1;
			groupList = GroupList.SensorClimate;
			nametopic01 = groupList+"_1";
			topicJson01 = new TopicJson(STAT_PREFIX, nametopic01, new SonoffSNZB02Json());
			device = createDevice(typeGateway, typeDevice, numberDevice, groupList, topicJson01);	
	    	Devices.add(device);
			break;
		case AqaraTemp:
			typeGateway = TypeGateway.CC2531_1;
			groupList = GroupList.SensorClimate;
			nametopic01 = groupList+"_1";
			topicJson01 = new TopicJson(STAT_PREFIX, nametopic01, new AqaraTempJson());
			device = createDevice(typeGateway, typeDevice, numberDevice, groupList, topicJson01);	
	    	Devices.add(device);
			break;
		case TuyaZigBeeSensor:
			typeGateway = TypeGateway.CC2531_1;
			groupList = GroupList.SensorClimate;
			nametopic01 = groupList+"_1";
			topicJson01 = new TopicJson(STAT_PREFIX, nametopic01, new TuyaZigBeeSensorJson());
			device = createDevice(typeGateway, typeDevice, numberDevice, groupList, topicJson01);	
	    	Devices.add(device);
			break;
		case XiaomiZNCZ04LM:
			typeGateway = TypeGateway.CC2531_1;
			groupList = GroupList.RelaySensorClimate;
			nametopic01 = groupList+"_1";
			nametopic02 = nametopic01+"/set";
			topicJson01 = new TopicJson(MIX_PREFIX, nametopic01, new XiaomiZNCZ04LM());
			topicNoJson02 = new TopicNoJson(MIX_PREFIX, nametopic02, new Set());
			device = createDevice(typeGateway, typeDevice, numberDevice, groupList, topicJson01, topicNoJson02);		
	    	Devices.add(device);
			break;
		default:
			break;
		}
		
	}
	
	private Device createDevice(TypeGateway gateway, TypeDevice typeDevice, String numberDevice, GroupList groupList, TopicNoJson topic01, TopicNoJson topic02){
		Device device = new Device(gateway,typeDevice,numberDevice,groupList);
		device.addTopic(topic01);
		device.addTopic(topic02);
		return device;
	}	
	private Device createDevice(TypeGateway gateway, TypeDevice typeDevice, String numberDevice, GroupList groupList, TopicJson topic01){
		Device device = new Device(gateway,typeDevice,numberDevice,groupList);
		device.addTopic(topic01);		
		return device;
	}
	private Device createDevice(TypeGateway gateway, TypeDevice typeDevice, String numberDevice, GroupList groupList, TopicJson topic01, TopicNoJson topic02){
		Device device = new Device(gateway,typeDevice,numberDevice,groupList);
		device.addTopic(topic01);
		device.addTopic(topic02);
		return device;
	}	
	
}
