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
	private List<ScheduleRelay> ls = new ArrayList<ScheduleRelay>();
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Scheduled.class);
    private static final String APP_ID = "org.eclipse.kura.dnomaid.iot.Scheduled";
    private static final String ALIAS_APP_ID = "Scheduled";
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
    	ls.add(new ScheduleRelay(1));
    	ls.add(new ScheduleRelay(2));
    	ls.add(new ScheduleRelay(3));
    	ls.add(new ScheduleRelay(4));
    	ls.add(new ScheduleRelay(5));
    	ls.add(new ScheduleRelay(6));
    	ls.add(new ScheduleRelay(7));
    	ls.add(new ScheduleRelay(8));
    	ls.add(new ScheduleRelay(9));
    	updated(properties);
        logger("##Bundle " + APP_ID + " has started!");
        thread =  new Thread(new Runnable() {
        	@Override
        	public void run() {
        		while(true) {
        			try {
        				Thread.sleep(1000);
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
				        logger("##Bundle error:" + APP_ID + " ->" + e);
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
        if (properties != null) {
        	for (int j = 0; j < ls.size(); j++) {
	        	if (properties.get(ls.get(j).getPropertyCmnd()) != null) {
	        		ls.get(j).setValueCmnd((String) properties.get(ls.get(j).getPropertyCmnd()));
	        		if (properties.get(ls.get(j).getPropertyRelay()) != null) {
		        		ls.get(j).setValueRelay((String) properties.get(ls.get(j).getPropertyRelay()));
		                logger("##"+ls.get(j).getValueCmnd()+": "+ls.get(j).getValueRelay());
		        	}
		        	if (properties.get(ls.get(j).getPropertyHour()) != null) {
		        		ls.get(j).setValueHour((String) properties.get(ls.get(j).getPropertyHour()));
		                logger("##"+ls.get(j).getValueCmnd()+" hour: "+ls.get(j).getValueHour());
		        	}
		        	if (properties.get(ls.get(j).getPropertyMinute()) != null) {
		        		ls.get(j).setValueMinute((String) properties.get(ls.get(j).getPropertyMinute()));
		                logger("##"+ls.get(j).getValueCmnd()+" minute: "+ls.get(j).getValueMinute());
		        	}
	        	}
        	}        	        	
        logger("...Updated properties done.");
        }
    }
    
    // ----------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------
    private void initRelays() {
    	try {
			for (int i = 0; i < refIntClientMqtt.numberRelay(); i++) {
				messageRelay.add("OFF");
				relays.add(refIntClientMqtt.nameRelay(i));
			}
		} catch (MessageException e) {
			logger("##Bundle error:" + APP_ID + " ->" + e);
		}
        MINITRELAYS=true;	
    }
    private void updateRelays() {
    	relays.clear();
    	try {
			for (int i = 0; i < refIntClientMqtt.numberRelay(); i++) {
				relays.add(refIntClientMqtt.nameRelay(i));
			}
		} catch (MessageException e) {
			logger("##Bundle error:" + APP_ID + " ->" + e);
		}
    }
    private void resetRelays() {
		messageRelay.clear();
		relays.clear();	
        MINITRELAYS=false;	
    }
	private void scheduledRelayPublish() {
		String Hour ="";
		String Minute ="";
		String Second ="";
		try {
				LocalDateTime now = LocalDateTime.now();
				Hour = String.valueOf(now.getHour());
				Minute = String.valueOf(now.getMinute());
				Second = String.valueOf(now.getSecond());
				if(Second.equals("0"))logger("##scheduledRelay LocalDateTime: " + Hour + " : " + Minute);				
			} catch (Exception e) {
		        logger("##Bundle error:" + APP_ID + " ->" + e);
				e.printStackTrace();
			}	
		for (int i = 0; i < messageRelay.size(); i++) {messageRelay.set(i, "empty");}		

		for(int j = 0; j < ls.size(); j++) {
			if (!ls.get(j).getValueRelay().equals("none")) {
				if(ls.get(j).getValueHour().equals(Hour)&ls.get(j).getValueMinute().equals(Minute)&"0".equals(Second)) {
					logger("## "+ls.toString()+": "+ls.get(j).getValueCmnd()+" "+ls.get(j).getValueRelay());
					for (int i = 0; i < messageRelay.size(); i++) {
						int numRelay=i+1;
						if (ls.get(j).getValueRelay().equals("relay0"+numRelay)) messageRelay.set(i,ls.get(j).getValueCmnd());
					}
					if (ls.get(j).getValueRelay().equals("all")) {
						for (int i = 0; i < messageRelay.size(); i++) {
							messageRelay.set(i, ls.get(j).getValueCmnd());
						}	
					}
				}
			}
		}
		for (int i = 0; i < messageRelay.size(); i++) {			
			if(!messageRelay.get(i).equals("empty")) {
				updateRelays();
				break;
			}
		}
		for (int i = 0; i < messageRelay.size(); i++) {			
			if(!messageRelay.get(i).equals("empty"))
				try {
					refIntClientMqtt.publish(relays.get(i), messageRelay.get(i));
				} catch (MessageException e) {
					logger("##Bundle error:" + APP_ID + " ->" + e);
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