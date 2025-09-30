package ir.naja.pushsdk.android.model;

public class Message {
    private String id;
    private String payload;
    private String topic;
    private String senderId;
    private String senderTitle;
    private String sentTimestamp;
    private String level;

    public Message() {
    }

    public Message(MessageUnread message) {
        this.id = message.getMessageId();
        this.payload = message.getMessage();
        this.topic = message.getTopic();
        this.senderId = message.getSenderId();
        this.senderTitle = message.getSenderTitle();
        this.sentTimestamp = message.getSentTimestamp();
        this.level = message.getLevel();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderTitle() {
        return senderTitle;
    }

    public void setSenderTitle(String senderTitle) {
        this.senderTitle = senderTitle;
    }

    public String getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
