package org.eclipse.kura.dnomaid.iot.mqtt.device;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;

public class DeviceSetting {
	
	// Default value
	private static final Boolean DEFAULT_ENABLE_PID = false;
    private static final String DEFAULT_TYPE = "unknown";
    private static final String DEFAULT_NUMBER = "0";
    private static final String DEFAULT_ALIAS = "relay00";

    // Property Names
    private static final String ENABLE = "enable";
    private static final String TYPE = "typeDevice";
    private static final String NUMBER = "numberDevice";
    private static final String ALIAS = "aliasDevice";
    
    private final Map<String, Object> properties;

    DeviceSetting(final Map<String, Object> properties) {
        requireNonNull(properties);
        this.properties = properties;
    }

    Boolean isEnablePid() {
        Boolean enablePid = DEFAULT_ENABLE_PID;
        Object setupEnablePid = this.properties.get(ENABLE);
        if (nonNull(setupEnablePid) && setupEnablePid instanceof Boolean) {
      	enablePid = (Boolean) setupEnablePid;
      	}
        return enablePid;
    }

    String getTypeDevice() {
        String name = DEFAULT_TYPE;
        Object propertie = this.properties.get(TYPE);
        if (nonNull(propertie) && propertie instanceof String) {
        	name = (String) propertie;
        }
        return name;
    }

    String getNumberDevice() {
        String name = DEFAULT_NUMBER;
        Object propertie = this.properties.get(NUMBER);
        if (nonNull(propertie) && propertie instanceof String) {
        	name = (String) propertie;
        }
        return name;
    }
    
    String getAliasDevice() {
        String name = DEFAULT_ALIAS;
        Object propertie = this.properties.get(ALIAS);
        if (nonNull(propertie) && propertie instanceof String) {
        	name = (String) propertie;
        }
        return name;
    }

}
