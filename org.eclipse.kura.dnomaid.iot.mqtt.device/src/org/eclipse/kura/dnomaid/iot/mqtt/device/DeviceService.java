package org.eclipse.kura.dnomaid.iot.mqtt.device;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntMqttDevice;
import org.eclipse.kura.dnomaid.iot.mqtt.api.IntMqttDevice.MessageMqttDeviceException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceService implements ConfigurableComponent {
    private static final Logger S_LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private static final String ALIAS_APP_ID = "DeviceDnomaid";
    
    private Boolean updatedOk;
    
    private DeviceSetting deviceSetting;
    private IntMqttDevice  refIntMqttDevice;
    
    private ScheduledExecutorService worker;
    
    public DeviceService(){
    	super();
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	updatedOk = false;
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
    // ----------------------------------------------------------------
    // Activation APIs
    // ----------------------------------------------------------------
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
    	S_LOGGER.info("Activating {} ...", ALIAS_APP_ID);
        S_LOGGER.info("Activating {} ... Done.", ALIAS_APP_ID);
    }
    protected void deactivate(ComponentContext componentContext) {
    	S_LOGGER.info("Deactivating {} ...", ALIAS_APP_ID);
    	try {
			this.refIntMqttDevice.deleteMqttDevice(deviceSetting.getTypeDevice(), deviceSetting.getNumberDevice());
		} catch (MessageMqttDeviceException e) {
			S_LOGGER.error("{} -> Error deactivate api: {}",ALIAS_APP_ID,e.getCause());
		}
    	this.worker.shutdown();
    	S_LOGGER.info("Deactivating {} ... Done.", ALIAS_APP_ID);
    }
    public void updated(Map<String, Object> properties) {
    	S_LOGGER.info("Updated "+ ALIAS_APP_ID +"...");
    	this.worker = Executors.newSingleThreadScheduledExecutor();
    	dumpProperties("Update", properties);
        this.deviceSetting = new DeviceSetting(properties);        
        updatedMqttDevice(deviceSetting.isEnablePid(), deviceSetting.getTypeDevice(), deviceSetting.getNumberDevice(), deviceSetting.getAliasDevice());
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
    private void updatedMqttDevice (Boolean enable, String type, String number, String alias) {    	
    	if(enable) {    		
    		try {
    			this.refIntMqttDevice.addMqttDevice(type, number, alias );
    			S_LOGGER.info("{} -> Added mqtt device: {} - {} - {}",ALIAS_APP_ID,type,number,alias);
    			updatedOk = true;
    		} catch (MessageMqttDeviceException e) {    			
    			S_LOGGER.error("{} -> Error added mqtt device: {}",ALIAS_APP_ID,e.getCause());
    		}     		     	
		}else {
        	if(updatedOk) {        	
        		try {
        			this.refIntMqttDevice.deleteMqttDevice(type, number);
        			S_LOGGER.info("{} -> Deleted mqtt device: {} - {} - {}",ALIAS_APP_ID,type,number);
        			updatedOk = false;
        		} catch (MessageMqttDeviceException e) {
        			S_LOGGER.error("{} -> Error deleted mqtt device: {}",ALIAS_APP_ID,e.getCause());
        		}
        	
        	}    			    			
		}    	
    }    
}
