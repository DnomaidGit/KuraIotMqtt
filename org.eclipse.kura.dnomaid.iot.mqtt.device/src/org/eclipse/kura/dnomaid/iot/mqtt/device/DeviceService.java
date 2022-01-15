package org.eclipse.kura.dnomaid.iot.mqtt.device;

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
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntMqttDevice;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntMqttDevice.MessageMqttDeviceException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceService implements ConfigurableComponent {
    private static final Logger S_LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private static final String ALIAS_APP_ID = "DeviceDnomaid";
    private static String NAME_COMPONENT = "NameComponent?";
    
    private Boolean updatedOk;
    private Boolean addDevice;
    
    private DeviceSetting deviceSetting;
    
    private ScheduledExecutorService worker;
    private ScheduledFuture<?> handle;
    
    private IntMqttDevice  refIntMqttDevice;
    private IntClientMqtt refIntClientMqtt;
    
    public DeviceService(){
    	super();
    	updatedOk = false;
    	addDevice = false;
    }
		
	// ----------------------------------------------------------------
    // Reference
    // ----------------------------------------------------------------   
    protected void setIntMqttDevice(IntMqttDevice intMqttDevice) {
		refIntMqttDevice = intMqttDevice;
	}
	protected void unsetIntMqttDevice(IntMqttDevice intMqttDevice) {
		refIntMqttDevice = null;
	}
	protected void setIntClientMqtt(IntClientMqtt intClientMqtt) {
		refIntClientMqtt = intClientMqtt;
	}
	protected void unsetIntClientMqtt(IntClientMqtt intClientMqtt) {
		refIntClientMqtt = null;
	}	
    // ----------------------------------------------------------------
    // Activation APIs
    // ----------------------------------------------------------------
    protected void activate(ComponentContext componentContext) {
    	S_LOGGER.info("Activating {} ...", ALIAS_APP_ID);    	
        S_LOGGER.info("Activating {} ... Done.", ALIAS_APP_ID);
    }
    protected void deactivate(ComponentContext componentContext) {
    	S_LOGGER.info("Deactivating {} ...", ALIAS_APP_ID); 
    	delete(deviceSetting.getTypeDevice(), deviceSetting.getNumberDevice());
    	stopService();
    	S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }
    protected void updated(Map<String, Object> properties) {
    	S_LOGGER.info("Updated {} ...", ALIAS_APP_ID);
    	dumpProperties("Update", properties);
        this.deviceSetting = new DeviceSetting(properties); 
        updatedOk = false;
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
    private void startService() {     	
        if(deviceSetting.isEnablePid()) {
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
	            		if(!updatedOk) {
	    				add(deviceSetting.getTypeDevice(), deviceSetting.getNumberDevice(), deviceSetting.getAliasDevice());
	            		S_LOGGER.info("{} -> Running component {}",ALIAS_APP_ID, componentName);
	            		}
					} catch (Exception e) {
						S_LOGGER.error("{} -> component {} Error runnable: {}",ALIAS_APP_ID,componentName,e.getCause());
					}        			
	            }
	        }, 0, pubrate, TimeUnit.MILLISECONDS);
        }else {
        	if(!updatedOk) {
        	delete(deviceSetting.getTypeDevice(), deviceSetting.getNumberDevice());
        	stopService();
        	}
        }
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
    private void add (String type, String number, String alias) {
    	try {					
			this.refIntMqttDevice.addMqttDevice(type, number, alias );
			S_LOGGER.info("{} -> Added mqtt device: {} - {} - {}",ALIAS_APP_ID,type,number,alias);
			S_LOGGER.info("{} -> Number devices: {}",ALIAS_APP_ID,refIntClientMqtt.numberRelay());					
			updatedOk = true;
			addDevice = true;
		} catch (MessageMqttDeviceException e) {    			
			S_LOGGER.error("{} -> Error added mqtt device: {}",ALIAS_APP_ID,e.getCause()); 
		} catch (MessageException e) {
			S_LOGGER.error("{} -> Error added mqtt device: {}",ALIAS_APP_ID,e.getCause());
		}	    	
    }
    private void delete(String type, String number) {
    	if(addDevice) {        	
    		try {
    			this.refIntMqttDevice.deleteMqttDevice(type, number);
    			S_LOGGER.info("{} -> Deleted mqtt device: {} - {} - {}",ALIAS_APP_ID,type,number);
    			S_LOGGER.info("{} -> Number devices: {}",ALIAS_APP_ID,refIntClientMqtt.numberRelay());
    			updatedOk = true;
    			addDevice = false;    			
    		} catch (MessageMqttDeviceException e) {
    			S_LOGGER.error("{} -> Error deleted mqtt device: {}",ALIAS_APP_ID,e.getCause());
    		} catch (MessageException e) {
    			S_LOGGER.error("{} -> Error deleted mqtt device: {}",ALIAS_APP_ID,e.getCause());
			}	        	
    	}
    }
}
