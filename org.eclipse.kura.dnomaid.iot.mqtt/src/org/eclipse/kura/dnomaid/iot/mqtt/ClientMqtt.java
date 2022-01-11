package org.eclipse.kura.dnomaid.iot.mqtt;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntClientMqtt;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntMqttDevice;
import org.eclipse.kura.dnomaid.iot.mqtt.device.Devices;
import org.eclipse.kura.dnomaid.iot.mqtt.global.ConnectionConstants;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Constants.TypeDevice;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Status;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMqtt implements ConfigurableComponent, IntClientMqtt, IntMqttDevice{
	private Mqtt mqtt;
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(ClientMqtt.class);
    private static final String ALIAS_APP_ID = "ClientMqtt"; 
    
    private final ScheduledExecutorService worker;
    private static boolean ENABLE;
    
    public ClientMqtt() {
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	this.mqtt = new Mqtt();
    	ENABLE = false;
    }
    
    // ----------------------------------------------------------------
    // Activation APIs
    // ----------------------------------------------------------------        
    protected void activate(ComponentContext componentContext,Map<String, Object> properties) {
    	S_LOGGER.info("Activating {} ...", ALIAS_APP_ID);        
    	updated(properties);    	
    	S_LOGGER.info("Activating {} ... Done.", ALIAS_APP_ID);
    }
    protected void deactivate(ComponentContext componentContext) {
    	S_LOGGER.info("Deactivating {} ...", ALIAS_APP_ID);    	
    	mqtt.disconnection();        
        this.worker.shutdown();
        S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }    
    public void updated(Map<String, Object> properties) {
    	S_LOGGER.info("Updated "+ ALIAS_APP_ID +"...");
        storeProperties("Update", properties);
        if (ENABLE) {
        	if(!Status.getInst().isConnectedOrConnecting()) {
        		mqtt.connection();
        	}
    	}else {
    		if(Status.getInst().isConnected()) {
    			mqtt.disconnection();    			    			
    		}   		
    	}
        S_LOGGER.info("Updated "+ ALIAS_APP_ID +"... Done.");
    }

    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private static void storeProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
            if(properties.get(key).equals("Enable"))
        		ENABLE = (Boolean)properties.get("Enable");
        	if(properties.get(key).equals("Server"))
        		ConnectionConstants.getInst().setServer((String)properties.get("Server"));
        	if(properties.get(key).equals("Port"))
        		ConnectionConstants.getInst().setPort((int)properties.get("Port"));
        	if(properties.get(key).equals("ClientId"))
        		ConnectionConstants.getInst().setClientId((String)properties.get("ClientId"));
        	if(properties.get(key).equals("CleanSession"))
        		ConnectionConstants.getInst().setCleanSession((Boolean)properties.get("CleanSession"));
        	if(properties.get(key).equals("Username"))
        		ConnectionConstants.getInst().setUsername((String)properties.get("Username"));
        	if(properties.get(key).equals("Password"))
        		ConnectionConstants.getInst().setPassword((String)properties.get("Password"));            
        }
    }
    private void addDevice(String typeDevice, String numberDevice, String aliasDevice) {
    	String NameDevice = typeDevice + "_" + numberDevice;
    	Boolean existDevice = false;
    	for (int i = 0; i < Devices.getInst().getRelays().size(); i++) {
    		if (Devices.getInst().getDevicesConfig().get(i).toString().equals(NameDevice)) {
    			Devices.getInst().getDevicesConfig().get(i).setAliasDevice(aliasDevice);
    			S_LOGGER.info("{} -> Device exist:> type:{} - number: {} - alias: {}",ALIAS_APP_ID,typeDevice,numberDevice);
    			existDevice = true;
    			break;
    		}    		
		}
    	if(!existDevice) {    	
    		Devices.getInst().newDevice(TypeDevice.valueOf(typeDevice), numberDevice, aliasDevice);
    		S_LOGGER.info("{} -> Number device relays: {} - alias: {}",ALIAS_APP_ID,Devices.getInst().getRelays().size());
    	}
    	
    }    
    private void deleteDevice(String typeDevice, String numberDevice) {
    	Devices.getInst().deleteDevice(typeDevice, numberDevice);;
    } 

    // ----------------------------------------------------------------
    // Implementation
    // ----------------------------------------------------------------
	@Override
	public boolean isConnected() throws MessageException {
		return Status.getInst().isConnected();
	}
	@Override
	public int numberRelay() throws MessageException {
		return Devices.getInst().getRelays().size();
	}
	@Override
	public String nameRelay(int number) throws MessageException {
		return Devices.getInst().getRelays().get(number).getTopics().get(1).getName();
	}
	@Override
	public void publish(String relay, String message) throws MessageException {
		S_LOGGER.info("{} -> Publish alias: {} - message: {}",ALIAS_APP_ID,relay,message);
		Boolean publishMessage = false;
		if(Status.getInst().isConnected()) {
			for (int i = 0; i < Devices.getInst().getDevicesConfig().size(); i++) {
				String nameDevice = "?";
				if(Devices.getInst().getDevicesConfig().get(i).getAliasDevice().equals(relay)) {
					nameDevice = Devices.getInst().getDevicesConfig().get(i).toString();
				}
				String topicDevice = "?";
				if(Devices.getInst().getRelays().get(i).getNameDevice().equals(nameDevice)) {
					topicDevice = Devices.getInst().getRelays().get(i).getTopics().get(1).getName();
				}
				if(!topicDevice.equals("?")) {
					mqtt.publish(topicDevice, message);				
					S_LOGGER.info("{} -> Publish topic: {} - message: {}",ALIAS_APP_ID,topicDevice,message);
					publishMessage = true;
				}						
			}
			if(!publishMessage)S_LOGGER.info("{} -> Not find alias!",ALIAS_APP_ID);
			if(!publishMessage)S_LOGGER.error("{} -> Error, not find alias!",ALIAS_APP_ID);
		}else {
			S_LOGGER.info("{} -> Client mqtt is not connected!",ALIAS_APP_ID);	
		}
	}

	@Override
	public void addMqttDevice(String typeDevice, String numberDevice, String aliasDevice) throws MessageMqttDeviceException {
		S_LOGGER.info("{} -> add Mqtt Device:> type:{} - number: {} - alias: {}",ALIAS_APP_ID,typeDevice,numberDevice);
		addDevice(typeDevice, numberDevice, aliasDevice);		
	}

	@Override
	public void deleteMqttDevice(String typeRelay, String NumberTypeDevice) throws MessageMqttDeviceException {
		S_LOGGER.info("{} -> delete Mqtt Device:> type:{} - number: {}",ALIAS_APP_ID,typeRelay,NumberTypeDevice);
		deleteDevice(typeRelay,NumberTypeDevice);		
	} 
	
}