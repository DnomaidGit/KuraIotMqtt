package org.eclipse.kura.dnomaid.iot;


import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.Mqtt;
import org.eclipse.kura.dnomaid.iot.mqtt.global.ConnectionConstants;
import org.eclipse.kura.dnomaid.iot.mqtt.global.Status;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMqtt implements ConfigurableComponent{
	private Mqtt mqtt;
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(ClientMqtt.class);
    private static final String APP_ID = "org.eclipse.kura.dnomaid.iot.ClientMqtt";
    private static final String ALIAS_APP_ID = "ClientMqtt"; //Client
    
    private final ScheduledExecutorService worker;
    private static boolean ENABLE;
    
    public ClientMqtt() {
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	this.mqtt = new Mqtt();
    	ENABLE = false;
    }
        
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
	private void logger (String message) {
		// var/log/kura-console.log
		System.out.println("::"+ALIAS_APP_ID+"::"+message);
		// var/log/kura.log
		S_LOGGER.info("::"+ALIAS_APP_ID+"::"+message);		
	}    	
}