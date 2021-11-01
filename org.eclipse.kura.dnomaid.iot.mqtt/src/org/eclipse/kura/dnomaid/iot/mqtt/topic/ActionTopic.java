package org.eclipse.kura.dnomaid.iot.mqtt.topic;


public interface ActionTopic {
	String getValueTopic(TypeTopic typeTopic);
	enum TypeTopic{Battery, Humidity, Temperature, Power}
}
