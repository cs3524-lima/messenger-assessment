package shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private String messageType;
    private String target;
    private String messageBody;
    private String user;
    private String timestamp;
    private String topic;

    public Message(String messageType, String target, String messageBody, String user) {
        this.messageType = messageType;
        this.target = target;
        this.messageBody = messageBody;
        this.user = user;
        this.timestamp = getCurrentTimestamp();
        this.topic = "";
    }

    public Message(String messageType, String target, String messageBody, String user, String topic) {
        this.messageType = messageType;
        this.target = target;
        this.messageBody = messageBody;
        this.user = user;
        this.timestamp = getCurrentTimestamp();
        this.topic = topic;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + user + ": " + messageBody;
    }
}