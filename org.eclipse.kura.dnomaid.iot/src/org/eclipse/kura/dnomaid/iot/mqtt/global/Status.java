package org.eclipse.kura.dnomaid.iot.mqtt.global;

public class Status {
    private ConnectionStatus status = ConnectionStatus.NONE;
    private TopicStatus topicStatus = TopicStatus.NONE;
    public static final String SPACE = " ";
    public static final String EMPTY = new String();
    public enum ConnectionStatus {CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, ERROR, NONE}
    public enum TopicStatus {SUBSCRIBED, UNSUBSCRIBED, PUBLISHED, ERROR, NONE}

    private static Status instance = null;
    private Status(){ }
    public  static synchronized Status getInst() {
        if (instance==null) {
            instance=new Status();
        }
        return instance;
    }
    public void addStatusChange(String status) {Notify.printf(status); }
    public void changeConnectionStatus(ConnectionStatus connectionStatus) { 
    	status = connectionStatus;
    	addStatusChange("--Status--: "+ status);
    }
    public String getConnectionStatus() { return status.toString(); }
    public void changeTopicStatus(TopicStatus topicStatus) { 
    	this.topicStatus = topicStatus; 
    	addStatusChange("--Status--: "+ this.topicStatus);    	
    }
    public String getTopicStatus() { return topicStatus.toString(); }
    public boolean isNoneTopicStatus() {
        return topicStatus == TopicStatus.NONE;
    }
    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }
    public boolean isSubscribed() { return (topicStatus==TopicStatus.SUBSCRIBED||topicStatus==TopicStatus.PUBLISHED);}
    public boolean isConnectedOrConnecting() {
        return (status == ConnectionStatus.CONNECTED) || (status == ConnectionStatus.CONNECTING);
    }
    public boolean noError() { return status != ConnectionStatus.ERROR; }

}