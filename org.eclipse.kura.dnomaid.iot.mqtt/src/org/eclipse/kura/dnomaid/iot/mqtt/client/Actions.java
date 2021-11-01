package org.eclipse.kura.dnomaid.iot.mqtt.client;

public interface Actions {
    void connection();
    void disconnection();
    void subscribe();
    void unsubscribe();
    void publish(String topic, String message);
}
