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

public class Scheduled implements ConfigurableComponent{
	private List<ScheduleSetting> scheduleSetting = new ArrayList<ScheduleSetting>();
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Scheduled.class);
    private static final String ALIAS_APP_ID = "Scheduled";
    private static final Integer NUMBER_SCHEDULE = 9;
    private static final Integer NUMBER_RELAY = 6;
    private static final String ALIAS_RELAY = "relay0";

    private List <String> messageRelay = new  ArrayList<String>();     
    
    private ScheduledExecutorService worker;
    private ScheduledFuture<?> handle;
    
    private IntClientMqtt refIntClientMqtt;
    
    public Scheduled() {
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	init();
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
    	updated(properties);                
        S_LOGGER.info("Activating {} ... Done.", ALIAS_APP_ID);
    }
    protected void deactivate(ComponentContext componentContext) {  	
    	S_LOGGER.info("Deactivating {} ...", ALIAS_APP_ID);   	        
        this.worker.shutdown();
        S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }
    private void updated(Map<String, Object> properties) {
    	S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	dumpProperties("Update", properties);
    	scheduleSetting.clear();
    	for (int i = 1; i <= NUMBER_SCHEDULE; i++) {
    		scheduleSetting.add(new ScheduleSetting(properties, i));
		}
    	start();
    	S_LOGGER.info("Updated "+ ALIAS_APP_ID +"... Done.");
    }
    
    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private static void dumpProperties(final String action, final Map<String, Object> properties) {
        final Set<String> keys = new TreeSet<>(properties.keySet());
        for (final String key : keys) {
            S_LOGGER.info("{} - {}: {}", action, key, properties.get(key));
        }
    }
    
    private void init() {
		for (int i = 0; i < NUMBER_RELAY; i++) {
			messageRelay.add("OFF");
		}		
    }
    private void start() {
        if (this.handle != null) {
            this.handle.cancel(true);
        }
        int pubrate = 1000;
        this.handle = this.worker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	try {    				        				
    				S_LOGGER.info("{} -> Number device: {}",ALIAS_APP_ID,refIntClientMqtt.numberRelay());
					scheduledRelayPublish();
				} catch (Exception e) {
					S_LOGGER.error("{} -> Error runnable: {}",ALIAS_APP_ID,e.getCause());
				}        			
            }
        }, 0, pubrate, TimeUnit.MILLISECONDS);
    }
    
	private void scheduledRelayPublish() {
		String Hour = "0",Minute = "0",Second ="0";
		S_LOGGER.info("{} -> scheduledRelayPublish",ALIAS_APP_ID);
		try {
			if(refIntClientMqtt.isConnected()) {	        					
				S_LOGGER.info("{} -> isConnected",ALIAS_APP_ID);
			}
		} catch (MessageException e) {
			S_LOGGER.error("{} -> Error runnable: {}",ALIAS_APP_ID,e.getCause());
		} 
		//Time
		try {
			LocalDateTime now = LocalDateTime.now();
			Hour = String.valueOf(now.getHour());
			Minute = String.valueOf(now.getMinute());
			Second = String.valueOf(now.getSecond());
			if(Second.equals("0")) {
				S_LOGGER.info("{} -> Time {} : {}",ALIAS_APP_ID,Hour,Minute);
			}							
		} catch (Exception e) {
			S_LOGGER.error("{} -> Error time: {}",ALIAS_APP_ID,e.getCause());				
		}
		//Initialize list
		for (int i = 0; i < messageRelay.size(); i++) {messageRelay.set(i, "empty");}		
        //Scheduled
		for(int j = 0; j < scheduleSetting.size(); j++) {
			if (!scheduleSetting.get(j).getRelay().equals("none")) {
				if(scheduleSetting.get(j).getHour().equals(Hour)&scheduleSetting.get(j).getMinute().equals(Minute)&"0".equals(Second)) {
					S_LOGGER.info("{} -> Schedule Setting: {} - Cmnd: {} - Relay: {}"
							      ,ALIAS_APP_ID,scheduleSetting.toString(),scheduleSetting.get(j).getCmnd(),scheduleSetting.get(j).getRelay());
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
			if(!messageRelay.get(i).equals("empty"))
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