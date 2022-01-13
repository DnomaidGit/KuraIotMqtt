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

public class ClientMqttService implements ConfigurableComponent, IntClientMqtt, IntMqttDevice{
	private Mqtt mqtt;
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(ClientMqttService.class);
    private static final String ALIAS_APP_ID = "ClientMqtt"; 
    
    private final ScheduledExecutorService worker;
    private static boolean ENABLE;
    
    public ClientMqttService() {
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
    	S_LOGGER.info("Updated {} ...", ALIAS_APP_ID); 
    	storeProperties("Update", properties);
        if (ENABLE) {
        	S_LOGGER.info("Client connecting {} ...", ALIAS_APP_ID);
        	if(!Status.getInst().isConnectedOrConnecting()) {
        		mqtt.connection();
            	S_LOGGER.info("Client connected {} !", ALIAS_APP_ID);
        	}
    	}else {
        	S_LOGGER.info("Client disconnecting {} ...", ALIAS_APP_ID);
    		if(Status.getInst().isConnected()) {
    			mqtt.disconnection();    			    			
            	S_LOGGER.info("Client disconnected {} !", ALIAS_APP_ID);
    		}   		
    	}
        S_LOGGER.info("Updated {} ... Done.", ALIAS_APP_ID);
    }

    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private static void storeProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
            switch (key) {
			case "Enable":
				ENABLE = (Boolean)properties.get(key);
				break;
			case "Server":
				ConnectionConstants.getInst().setServer((String)properties.get(key));
				break;
			case "Port":
				ConnectionConstants.getInst().setPort((int)properties.get(key));
				break;
			case "ClientId":
				ConnectionConstants.getInst().setClientId((String)properties.get(key));
				break;
			case "CleanSession":
				ConnectionConstants.getInst().setCleanSession((Boolean)properties.get(key));
				break;
			case "Username":
				ConnectionConstants.getInst().setUsername((String)properties.get(key));
				break;
			case "Password":
				ConnectionConstants.getInst().setPassword((String)properties.get(key));
				break;
			case "component.id":
				break;
			case "component.name":
				break;
			case "kura.service.pid":
				break;
			case "service.pid":
				break;		
			default:
				S_LOGGER.warn("{} unknown properties: {} ", ALIAS_APP_ID, key);
				break;
			}                  
        }
    }
    private void publish(String alias, String message) {
    	Boolean publishMessage = false;
		if(Status.getInst().isConnected()) {
			for (int i = 0; i < Devices.getInst().getDevicesConfig().size(); i++) {
				String nameDevice = "?";
				if(Devices.getInst().getDevicesConfig().get(i).getAliasDevice().equals(alias)) {
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
			if(!publishMessage)S_LOGGER.error("{} -> Error, not find alias!",ALIAS_APP_ID);
		}else {
			S_LOGGER.info("{} -> Client mqtt is not connected!",ALIAS_APP_ID);	
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
	public void publishRelay(String alias, String message) throws MessageException {
		S_LOGGER.info("{} -> Publish alias: {} - message: {}",ALIAS_APP_ID,alias,message);
		publish(alias, message);
	}

	@Override
	public void addMqttDevice(String typeDevice, String numberDevice, String aliasDevice) throws MessageMqttDeviceException {
		S_LOGGER.info("{} -> add Mqtt Device:> type:{} - number: {} - alias: {}",ALIAS_APP_ID,typeDevice,numberDevice,aliasDevice);
		addDevice(typeDevice, numberDevice, aliasDevice);
		S_LOGGER.info("{} -> Number device: {}",ALIAS_APP_ID,Devices.getInst().getRelays().size());
	}

	@Override
	public void deleteMqttDevice(String typeRelay, String NumberTypeDevice) throws MessageMqttDeviceException {
		S_LOGGER.info("{} -> delete Mqtt Device:> type:{} - number: {}",ALIAS_APP_ID,typeRelay,NumberTypeDevice);
		deleteDevice(typeRelay,NumberTypeDevice);
		S_LOGGER.info("{} -> Number device: {}",ALIAS_APP_ID,Devices.getInst().getRelays().size());
	} 
	
}