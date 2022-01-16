package org.eclipse.kura.dnomaid.iot.mqtt;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.crypto.CryptoService;
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
    
    private static boolean ENABLE;
    
    private CryptoService refCryptoService;
    
    public ClientMqttService() {
    	super();
    	this.mqtt = new Mqtt();
    	ENABLE = false;
    }
    
    // ----------------------------------------------------------------
    // Reference
    // ----------------------------------------------------------------  
    public void setCryptoService(CryptoService cryptoService) {
        this.refCryptoService = cryptoService;
    }

    public void unsetCryptoService(CryptoService cryptoService) {
        this.refCryptoService = null;
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
    	if(mqtt!=null)mqtt.disconnection();     	       
        S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }    
    protected void updated(Map<String, Object> properties) {
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
    private void storeProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            switch (key) {
			case "enable":
				ENABLE = (Boolean)properties.get(key);
				break;
			case "server":
				ConnectionConstants.getInst().setServer((String)properties.get(key));
				break;
			case "port":
				ConnectionConstants.getInst().setPort((int)properties.get(key));
				break;
			case "clientId":
				ConnectionConstants.getInst().setClientId((String)properties.get(key));
				break;
			case "cleanSession":
				ConnectionConstants.getInst().setCleanSession((Boolean)properties.get(key));
				break;
			case "username":
				ConnectionConstants.getInst().setUsername((String)properties.get(key));
				break;
			case "password":
				/*
				try {
					ConnectionConstants.getInst().setPassword(decryptPassword(((String) properties.get("password")).toCharArray()));
					String propertiesKey = decryptPassword(((String) properties.get("password")).toCharArray());
					S_LOGGER.error("gETPassword: {}",ALIAS_APP_ID,ConnectionConstants.getInst().getPassword());
				} catch (KuraException e) {
					S_LOGGER.error("Error: " + e.getLocalizedMessage());
				}
				Password propertiesKey = (Password) properties.get("password");
				S_LOGGER.info("propertiesKey: {}",ALIAS_APP_ID,propertiesKey.toString());
				char[] arrayPassword = propertiesKey.toString().toCharArray();
				S_LOGGER.info("arrayPassword: {}",ALIAS_APP_ID,arrayPassword);
				String password = getPassword(arrayPassword);
				S_LOGGER.info("Password: {}",ALIAS_APP_ID,password);
 
 */
//				ConnectionConstants.getInst().setPassword((String)properties.get(key));
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
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
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
			if(!publishMessage)S_LOGGER.error("{} -> Error, {} not find alias!",ALIAS_APP_ID,alias);
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
    private String decryptPassword(char[] encryptedPassword) throws KuraException {
        final char[] decodedPasswordChars = this.refCryptoService.decryptAes(encryptedPassword);
        return new String(decodedPasswordChars);
    }
    private String getPassword(char [] passwordProperties) {
    	Password password = null;
		try {
			password = new Password(this.refCryptoService.decryptAes(passwordProperties));
			S_LOGGER.error("gETPassword: {}",ALIAS_APP_ID,password.toString());
		} catch (KuraException e) {
			S_LOGGER.error("Error: " + e.getLocalizedMessage());
		}
    	return password.toString();
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