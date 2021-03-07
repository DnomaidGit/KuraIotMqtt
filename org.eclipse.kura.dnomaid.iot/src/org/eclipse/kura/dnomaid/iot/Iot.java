package org.eclipse.kura.dnomaid.iot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.Mqtt;
import org.eclipse.kura.dnomaid.iot.mqtt.device.Devices;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iot implements ConfigurableComponent{
	private Mqtt mqtt;
	private Thread thread;
	private List<Scheduled> ls = new ArrayList<Scheduled>();
	
	private static final Logger S_LOGGER = LoggerFactory.getLogger(Iot.class);
    private static final String APP_ID = "org.eclipse.kura.dnomaid.iot";
    private List <String> relays = new  ArrayList<String>();
    private List <String> messageRelay = new  ArrayList<String>();    
    
    private final ScheduledExecutorService worker;
    private Map<String, Object> properties;
    
    public Iot() {
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	this.mqtt = new Mqtt();
    	for (int i = 0; i < Devices.getInst().getRelay().size(); i++) {
    		messageRelay.add("OFF");
    		relays.add(Devices.getInst().getRelay().get(i).getTopics().get(1).getName());
    	}
    }
        
    protected void activate(ComponentContext componentContext,Map<String, Object> properties) {
    	System.out.println("##Active component");    	
    	S_LOGGER.info("##Active component"); 
    	ls.add(new Scheduled(1));
    	ls.add(new Scheduled(2));
    	ls.add(new Scheduled(3));
    	ls.add(new Scheduled(4));
    	ls.add(new Scheduled(5));
    	ls.add(new Scheduled(6));
    	ls.add(new Scheduled(7));
    	ls.add(new Scheduled(8));
    	ls.add(new Scheduled(9));
    	updated(properties);
        mqtt.connection();
        System.out.println("##Bundle " + APP_ID + " has started!");
        S_LOGGER.info("##Bundle " + APP_ID + " has started!");
        thread =  new Thread(new Runnable() {
        	@Override
        	public void run() {
        		while(true) {
        			try {
        				Thread.sleep(1000);
        				scheduledRelay();
					} catch (Exception e) {
				        System.out.println("##Bundle error:" + APP_ID + " ->" + e);
				        S_LOGGER.info("##Bundle error:" + APP_ID + " ->" + e);
						e.printStackTrace();
					}        			
        		}
        	}
        });
        thread.start();        
    }
    
    protected void deactivate(ComponentContext componentContext) {
    	System.out.println("##Desactive component");    	
    	S_LOGGER.info("##Desactive component");    	
    	mqtt.disconnection();
    	System.out.println("Bundle " + APP_ID + " has stopped!");  
        S_LOGGER.info("Bundle " + APP_ID + " has stopped!");
        this.worker.shutdown();
    }
    
    public void updated(Map<String, Object> properties) {
        S_LOGGER.info("Updated properties...");
        this.properties = properties;
        // store the properties received
        if (properties != null) {
        	for (int j = 0; j < ls.size(); j++) {
        	if (properties.get(ls.get(j).getPropertyCmnd()) != null) {
        		ls.get(j).setValueCmnd((String) properties.get(ls.get(j).getPropertyCmnd()));
        		if (properties.get(ls.get(j).getPropertyRelay()) != null) {
	        		ls.get(j).setValueRelay((String) properties.get(ls.get(j).getPropertyRelay()));
	                S_LOGGER.info("##"+ls.get(j).getValueCmnd()+": "+ls.get(j).getValueRelay());
	        	}
	        	if (properties.get(ls.get(j).getPropertyHour()) != null) {
	        		ls.get(j).setValueHour((String) properties.get(ls.get(j).getPropertyHour()));
	                S_LOGGER.info("##"+ls.get(j).getValueCmnd()+" hour: "+ls.get(j).getValueHour());
	        	}
	        	if (properties.get(ls.get(j).getPropertyMinute()) != null) {
	        		ls.get(j).setValueMinute((String) properties.get(ls.get(j).getPropertyMinute()));
	                S_LOGGER.info("##"+ls.get(j).getValueCmnd()+" minute: "+ls.get(j).getValueMinute());
	        	}
        	}
        	}
        	
        	
        S_LOGGER.info("...Updated properties done.");
        }
    }
    	
	public void scheduledRelay() {
		String Hour ="";
		String Minute ="";
		String Second ="";
		try {
				LocalDateTime now = LocalDateTime.now();
				Hour = String.valueOf(now.getHour());
				Minute = String.valueOf(now.getMinute());
				Second = String.valueOf(now.getSecond());
			} catch (Exception e) {
		        System.out.println("##Bundle error:" + APP_ID + " ->" + e);
		        S_LOGGER.info("##Bundle error:" + APP_ID + " ->" + e);
				e.printStackTrace();
			}        			
		for (int i = 0; i < messageRelay.size(); i++) {messageRelay.set(i, "empty");}		

		for(int j = 0; j < ls.size(); j++) {
			if (!ls.get(j).getValueRelay().equals("none")) {
				if(ls.get(j).getValueHour().equals(Hour)&ls.get(j).getValueMinute().equals(Minute)&"0".equals(Second)) {
					S_LOGGER.info("## "+ls.toString()+": "+ls.get(j).getValueCmnd()+" "+ls.get(j).getValueRelay());
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
			if(!messageRelay.get(i).equals("empty"))mqtt.publish(relays.get(i), messageRelay.get(i));		
		}		
	}

}