package org.eclipse.kura.dnomaid.iot.mqtt;

import java.util.Map;
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
    private static final String APP_ID = "org.eclipse.kura.dnomaid.iot.ClientMqtt";
    private static final String ALIAS_APP_ID = "ClientMqtt"; 
    
    private final ScheduledExecutorService worker;
    private static boolean ENABLE;
    private static int MAXNUMBDEV=6;
    
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
    	logger("##Active component " + APP_ID); 
    	updated(properties);    	
        logger("##Bundle " + APP_ID + " has started!");
    }
    protected void deactivate(ComponentContext componentContext) {
    	logger("##Desactive component " + APP_ID);    	
    	mqtt.disconnection();
        logger("Bundle " + APP_ID + " has stopped!");
        this.worker.shutdown();
    }    
    public void updated(Map<String, Object> properties) {
        logger("Updated properties..." + APP_ID);
        // store the properties received
        if (properties != null) {
        	if(properties.get("Enable") != null){
        		ENABLE = (Boolean)properties.get("Enable");
        		logger("## "+ APP_ID +" Enable:::::>> "+ ENABLE);
        	}else {
        		logger("## "+ APP_ID +" Enable:::::>> "+ "null");        		
        	}   
        	if(properties.get("Server") != null){
        		ConnectionConstants.getInst().setServer((String)properties.get("Server"));
        		logger("## "+ APP_ID +" Sever:::::>> "+ ConnectionConstants.getInst().getServer());
        	}else {
        		logger("## "+ APP_ID +" Sever:::::>> "+ "null");        		
        	}        	
        	if(properties.get("Port") != null){
        		ConnectionConstants.getInst().setPort((int)properties.get("Port"));
        		logger("## "+ APP_ID +" Port:::::>> "+ ConnectionConstants.getInst().getPort());
        	}else {
        		logger("## "+ APP_ID +" Port:::::>> "+ "null");        		
        	}
        	if(properties.get("ClientId") != null){
        		ConnectionConstants.getInst().setClientId((String)properties.get("ClientId"));
        		logger("## "+ APP_ID +" ClientId:::::>> "+ ConnectionConstants.getInst().getClientId());
        	}else {
        		logger("## "+ APP_ID +" ClientId:::::>> "+ "null");        		
        	}
        	if(properties.get("CleanSession") != null){
        		ConnectionConstants.getInst().setCleanSession((Boolean)properties.get("CleanSession"));
        		logger("## "+ APP_ID +" CleanSession:::::>> "+ ConnectionConstants.getInst().isCleanSession());
        	}else {
        		logger("## "+ APP_ID +" CleanSession:::::>> "+ "null");        		
        	}
        	if(properties.get("Username") != null){
        		ConnectionConstants.getInst().setUsername((String)properties.get("Username"));
        		logger("## "+ APP_ID +" Username:::::>> "+ ConnectionConstants.getInst().getUsername());
        	}else {
        		logger("## "+ APP_ID +" Username:::::>> "+ "null");        		
        	}
        	if(properties.get("Password") != null){
        		ConnectionConstants.getInst().setPassword((String)properties.get("Password"));
        		logger("## "+ APP_ID +" Password:::::>> "+ ConnectionConstants.getInst().getPassword());
        	}else {
        		logger("## "+ APP_ID +" Password:::::>> "+ "null");        		
        	}
        	/*
        	Devices.getInst().deleteDevices();	
        	for (int i = 1; i <= MAXNUMBDEV; i++) {
        		String type = (String)properties.get(i+"TypeRelay");
        		String number = (String)properties.get(i+"NumberTypeDevice");
        		String alias = "relay0"+i;
        		addDevice(type, number, alias);
        		if(properties.get(i+"TypeRelay") != null){
            		logger("## "+ APP_ID +" "+i+"TypeRelay:::::>> "+ properties.get(i+"TypeRelay"));
            	}else {
            		logger("## "+ APP_ID +" "+i+"TypeRelay:::::>> "+ "null");        		
            	}        
            	if(properties.get(i+"NumberTypeDevice") != null){
            		logger("## "+ APP_ID +" "+i+"NumberTypeDevice:::::>> "+ properties.get(i+"NumberTypeDevice"));
            	}else {
            		logger("## "+ APP_ID +" "+i+"NumberTypeDevice:::::>> "+ "null");        		
            	}        		
			}
			*/
        }
        if (ENABLE) {
        	if(!Status.getInst().isConnectedOrConnecting()) {
        		mqtt.connection();
        	}
    	}else {
    		if(Status.getInst().isConnected()) {
    			mqtt.disconnection();    			    			
    		}   		
    	}
        logger("...Updated properties done."+ APP_ID);
    }

    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private void addDevice(String typeDevice, String numberDevice, String aliasDevice) {
    	if(typeDevice!=null & numberDevice!=null)
    	Devices.getInst().newDevice(TypeDevice.valueOf(typeDevice), numberDevice, aliasDevice);
    }    
    private void deleteDevice(String typeDevice, String numberDevice) {
    	Devices.getInst().deleteDevice(typeDevice, numberDevice);;
    }    
	private void logger (String message) {
		// var/log/kura-console.log
		System.out.println("::"+ALIAS_APP_ID+"::"+message);
		// var/log/kura.log
		S_LOGGER.info("::"+ALIAS_APP_ID+"::"+message);		
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
		for (int i = 0; i < Devices.getInst().getDevicesConfig().size(); i++) {
			String nameDevice = "?";
			//logger("Relay send: " + relay + "Device 1 config alias: " + Devices.getInst().getDevicesConfig().get(1).getAliasDevice());
			if(Devices.getInst().getDevicesConfig().get(i).getAliasDevice().equals(relay)) {
				nameDevice = Devices.getInst().getDevicesConfig().get(i).toString();
			}
			String topicDevice = "?";
			//logger("Name device1: " + nameDevice + "Topic device 1: " + Devices.getInst().getRelays().get(1).getTopics().toString());
			if(Devices.getInst().getRelays().get(i).getNameDevice().equals(nameDevice)) {
				topicDevice = Devices.getInst().getRelays().get(i).getTopics().get(2).getName();
			}
			if(!topicDevice.equals("?")) {
				S_LOGGER.info("{} -> Publish topic: {} - message: {}",ALIAS_APP_ID,topicDevice,message);
				mqtt.publish(topicDevice, message);				
			}						
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