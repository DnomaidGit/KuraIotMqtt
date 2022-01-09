package org.eclipse.kura.dnomaid.iot.mqtt.schedule;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;

public class ScheduleSetting {
	// Default value
	    private static final String DEFAULT_RELAY = "none";
	    private static final String DEFAULT_CMND = "off";
	    private static final String DEFAULT_HOUR = "00";
	    private static final String DEFAULT_MINUTE = "00";

	    // Property Names
	    private String relay = "ScheduledRelay";
	    private String cmnd = "ScheduledCmnd";
	    private String hour = "ScheduledHour";
	    private String minute = "ScheduledMinute";
	    
	    private final Map<String, Object> properties;

	    ScheduleSetting(final Map<String, Object> properties, Integer number) {
	        requireNonNull(properties);
	        this.properties = properties;
	        this.relay = number + this.relay;
	        this.cmnd = number + this.cmnd;
	        this.hour = number + this.hour;
	        this.minute = number + this.minute;
	    }

	    
	    String getRelay() {
	        String name = DEFAULT_RELAY;
	        Object propertie = this.properties.get(relay);
	        if (nonNull(propertie) && propertie instanceof String) {
	        	name = (String) propertie;
	        }
	        return name;
	    }

	    String getCmnd() {
	        String name = DEFAULT_CMND;
	        Object propertie = this.properties.get(cmnd);
	        if (nonNull(propertie) && propertie instanceof String) {
	        	name = (String) propertie;
	        }
	        return name;
	    }
	    
	    String getHour() {
	        String name = DEFAULT_HOUR;
	        Object propertie = this.properties.get(hour);
	        if (nonNull(propertie) && propertie instanceof String) {
	        	name = (String) propertie;
	        }
	        return name;
	    }
	    
	    String getMinute() {
	        String name = DEFAULT_MINUTE;
	        Object propertie = this.properties.get(minute);
	        if (nonNull(propertie) && propertie instanceof String) {
	        	name = (String) propertie;
	        }
	        return name;
	    }


		@Override
		public String toString() {
			return "ScheduleSetting: " + relay;
		}
	    
	    

}
