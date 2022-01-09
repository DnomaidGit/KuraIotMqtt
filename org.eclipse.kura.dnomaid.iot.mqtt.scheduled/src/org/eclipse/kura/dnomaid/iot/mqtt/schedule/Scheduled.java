package org.eclipse.kura.dnomaid.iot.mqtt.schedule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntClientMqtt;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntClientMqtt.MessageException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scheduled implements ConfigurableComponent{
	private Thread thread;
	private List<ScheduleSetting> scheduleSetting = new ArrayList<ScheduleSetting>();
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Scheduled.class);
    private static final String APP_ID = "org.eclipse.kura.dnomaid.iot.Scheduled";
    private static final String ALIAS_APP_ID = "Scheduled";
    private static final Integer NUMBERSCHEDULE = 9;
    private List <String> relays = new  ArrayList<String>();
    private List <String> messageRelay = new  ArrayList<String>(); 
    private static boolean MINITRELAYS;
    
    private final ScheduledExecutorService worker;
    
    private IntClientMqtt refIntClientMqtt;
    
    public Scheduled() {
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	MINITRELAYS = false;
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
    	logger("##Active component"); 	
    	updated(properties);
        logger("##Bundle " + APP_ID + " has started!");
        thread =  new Thread(new Runnable() {
        	@Override
        	public void run() {
        		while(true) {
        			try {
        				Thread.sleep(1000);        				
        				S_LOGGER.info("{} -> Number device: {}",refIntClientMqtt.numberRelay());
        				if(refIntClientMqtt.isConnected()) {	        					
	        					if(!MINITRELAYS) {
	        						initRelays();
	        					}else {
	        						scheduledRelayPublish();
	        					}
        				}else {
        					resetRelays();
        				}
        				
					} catch (Exception e) {
						S_LOGGER.error("{} -> Error runnable: {}",ALIAS_APP_ID,e.getCause());
						e.printStackTrace();
					}        			
        		}
        	}
        });
        thread.start();        
    }
    protected void deactivate(ComponentContext componentContext) {  	
    	logger("##Desactive component");    	
        logger("Bundle " + APP_ID + " has stopped!");
        this.worker.shutdown();
    }
    private void updated(Map<String, Object> properties) {
        logger("Updated properties...");
        // store the properties received
    	scheduleSetting.clear();
    	for (int i = 1; i <= NUMBERSCHEDULE; i++) {
    		scheduleSetting.add(new ScheduleSetting(properties, i));
		}
    }
    
    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private void initRelays() {
    	try {
//			for (int i = 0; i < refIntClientMqtt.numberRelay(); i++) {
			for (int i = 0; i < 2; i++) {
				messageRelay.add("OFF");
				relays.add(refIntClientMqtt.nameRelay(i));
			}
		} catch (MessageException e) {
			S_LOGGER.error("{} -> Error initialize relays: {}",ALIAS_APP_ID,e.getCause());
		}
        MINITRELAYS=true;	
    }
    private void updateRelays() {
    	relays.clear();
    	try {
//			for (int i = 0; i < refIntClientMqtt.numberRelay(); i++) {
			for (int i = 0; i < 2; i++) {
				relays.add(refIntClientMqtt.nameRelay(i));
			}
		} catch (MessageException e) {
			S_LOGGER.error("{} -> Error update relays: {}",ALIAS_APP_ID,e.getCause());
		}
    }
    private void resetRelays() {
		messageRelay.clear();
		relays.clear();	
        MINITRELAYS=false;	
    }
	private void scheduledRelayPublish() {
		String Hour = "--",Minute = "--",Second ="--";		
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
		logger("## message relay size: "+ messageRelay.size());
		logger("## relays size: "+ relays.size());
//		logger("## ls size: "+ ls.size());
		//Initialize list
		for (int i = 0; i < messageRelay.size(); i++) {messageRelay.set(i, "empty");}		
        //Scheduled
		for(int j = 0; j < scheduleSetting.size(); j++) {
			if (!scheduleSetting.get(j).getRelay().equals("none")) {
				if(scheduleSetting.get(j).getHour().equals(Hour)&scheduleSetting.get(j).getMinute().equals(Minute)&"0".equals(Second)) {
					logger("## "+scheduleSetting.toString()+": "+scheduleSetting.get(j).getCmnd()+" "+scheduleSetting.get(j).getRelay());
					for (int i = 0; i < messageRelay.size(); i++) {
						int numRelay=i+1;
						if (scheduleSetting.get(j).getRelay().equals("relay0"+numRelay)) messageRelay.set(i,scheduleSetting.get(j).getCmnd());
					}
					if (scheduleSetting.get(j).getRelay().equals("all")) {
						for (int i = 0; i < messageRelay.size(); i++) {
							messageRelay.set(i, scheduleSetting.get(j).getCmnd());
						}	
					}
				}
			}
		}
		//Update message
		/*
		for (int i = 0; i < messageRelay.size(); i++) {			
			if(!messageRelay.get(i).equals("empty")) {
				updateRelays();
				break;
			}
		}
		*/
		//Publish message
		
		for (int i = 0; i < messageRelay.size(); i++) {			
			if(!messageRelay.get(i).equals("empty"))
				try {
					Integer numberRelay = i + 1; 
					String alias = "relay0" + numberRelay;
					//refIntClientMqtt.publish(relays.get(i), messageRelay.get(i));
					refIntClientMqtt.publish(alias, messageRelay.get(i));
					S_LOGGER.info("{} -> Publish alias: {} - message: {}",ALIAS_APP_ID,alias,messageRelay.get(i));
				} catch (MessageException e) {
					S_LOGGER.error("{} -> Error publish: {}",ALIAS_APP_ID,e.getCause());
				}
		}
				
		
	}
	private void logger (String message) {
		//  var/log/kura-console.log
		System.out.println("::"+ALIAS_APP_ID+"::"+message);
		//  var/log/kura.log
		S_LOGGER.info("::"+ALIAS_APP_ID+"::"+message);		
	}
	
}