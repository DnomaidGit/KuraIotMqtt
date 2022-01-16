package org.eclipse.kura.dnomaid.iot.mqtt.schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntClientMqtt;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntClientMqtt.MessageException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledService implements ConfigurableComponent{
	private static final Logger S_LOGGER = LoggerFactory.getLogger(ScheduledService.class);
    private static final String ALIAS_APP_ID = "Scheduled";
    private static final Integer NUMBER_SCHEDULE = 6;
    private static final Integer NUMBER_RELAY = 6;
    private static final String ALIAS_RELAY = "relay0";
    private static String NAME_COMPONENT = "NameComponent?";
    
    private List<ScheduleSetting> scheduleSetting = new ArrayList<ScheduleSetting>();
    private List <String> messageRelay = new  ArrayList<String>();     
    
    private ScheduledExecutorService worker;
    private ScheduledFuture<?> handle;
    
    private IntClientMqtt refIntClientMqtt;
    
    public ScheduledService() {
    	super();
    	initMessageRelay();
    }
    
	// ----------------------------------------------------------------
    // Reference
    // ----------------------------------------------------------------   
	protected void setIntClientMqtt(IntClientMqtt intClientMqtt) {
		refIntClientMqtt = intClientMqtt;
	}
	protected void unsetIntClientMqtt(IntClientMqtt intClientMqtt) {
		refIntClientMqtt = null;
	}	    
	
    // ----------------------------------------------------------------
    // Activation APIs
    // ----------------------------------------------------------------         
    protected void activate(ComponentContext componentContext,Map<String, Object> properties) {
    	S_LOGGER.info("Activating {} ...", ALIAS_APP_ID);         	
        S_LOGGER.info("Activating {} ... Done.", ALIAS_APP_ID);
    }
    protected void deactivate(ComponentContext componentContext) {  	
    	S_LOGGER.info("Deactivating {} ...", ALIAS_APP_ID);   	        
    	stopService();
        S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }
    protected void updated(Map<String, Object> properties) {
    	S_LOGGER.info("Updated {} ...", ALIAS_APP_ID);
    	dumpProperties("Update", properties);
    	initScheduleSetting(properties);
    	startService();
    	S_LOGGER.info("Updated {} ... Done.", ALIAS_APP_ID);
    }
    
    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private static void dumpProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
            if (key.equals("kura.service.pid"))
            	NAME_COMPONENT = (String)properties.get(key);
        }
    }
    private void initMessageRelay() { 
    	messageRelay.clear();
		for (int i = 0; i < NUMBER_RELAY; i++) {			
			messageRelay.add("empty");			
		}    	
    }
    private void initScheduleSetting(Map<String, Object> properties) {
    	scheduleSetting.clear();
    	for (int i = 1; i <= NUMBER_SCHEDULE; i++) {
    		scheduleSetting.add(new ScheduleSetting(properties, i));
		}    	
    }    
    private void startService() {
    	if (this.worker != null) {
    		S_LOGGER.info("Already running", ALIAS_APP_ID);
            return;
        }
        if (this.handle != null) {
            this.handle.cancel(true);
        }
        String componentName = NAME_COMPONENT;
    	this.worker = Executors.newSingleThreadScheduledExecutor();
        int pubrate = 1000;
        this.handle = this.worker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	try {    				        				
					scheduledRelayPublish(componentName);
				} catch (Exception e) {
					S_LOGGER.error("{} -> Error runnable: {}",ALIAS_APP_ID,e.getCause());
				}        			
            }
        }, 0, pubrate, TimeUnit.MILLISECONDS);
    }
    private void stopService() {
        if (this.handle != null) {
        this.handle.cancel(false);
        this.handle = null;
    }
    if (this.worker != null) {
        this.worker.shutdown();
        this.worker = null;
    }
}
	private void scheduledRelayPublish(String componentName) {
		String Hour = "0",Minute = "0",Second ="0";
		//Time
		try {
			LocalDateTime now = LocalDateTime.now();
			Hour = String.valueOf(now.getHour());
			Minute = String.valueOf(now.getMinute());
			Second = String.valueOf(now.getSecond());
			if(Second.equals("0")) {				
				S_LOGGER.info("{} -> Running component {} Time {} : {}",ALIAS_APP_ID,componentName,Hour,Minute);        		
			}							
		} catch (Exception e) {
			S_LOGGER.error("{} -> Error time: {}",ALIAS_APP_ID,e.getCause());				
		}
		//Initialize list
		initMessageRelay();
		//Scheduled
		for(int j = 0; j < scheduleSetting.size(); j++) {
			if (!scheduleSetting.get(j).getRelay().equals("none")) {
				// Check scheduled time
				if(scheduleSetting.get(j).getHour().equals(Hour)&scheduleSetting.get(j).getMinute().equals(Minute)&"0".equals(Second)) {
					S_LOGGER.info("{} -> Number Schedule: {} - Cmnd: {} - Relay: {}"
							      ,ALIAS_APP_ID,j+1,scheduleSetting.get(j).getCmnd(),scheduleSetting.get(j).getRelay());
					//Search relay name
					for (int i = 0; i < messageRelay.size(); i++) {
						int numRelay=i+1;
						if (scheduleSetting.get(j).getRelay().equals(ALIAS_RELAY+numRelay)) messageRelay.set(i,scheduleSetting.get(j).getCmnd());
					}
					if (scheduleSetting.get(j).getRelay().equals("all")) {
						for (int i = 0; i < messageRelay.size(); i++) {
							messageRelay.set(i, scheduleSetting.get(j).getCmnd());
						}	
					}
				}
			}
		}		
		//Publish message		
		for (int i = 0; i < messageRelay.size(); i++) {			
			if(!messageRelay.get(i).equals("empty")) {
				try {
					Integer numberRelay = i + 1; 
					String alias = ALIAS_RELAY + numberRelay;
					refIntClientMqtt.publishRelay(alias, messageRelay.get(i));
					S_LOGGER.info("{} -> Publish alias: {} - message: {}",ALIAS_APP_ID,alias,messageRelay.get(i));
				} catch (MessageException e) {
					S_LOGGER.error("{} -> Error publish: {}",ALIAS_APP_ID,e.getCause());
				}
			}
		}		
	}
		
}