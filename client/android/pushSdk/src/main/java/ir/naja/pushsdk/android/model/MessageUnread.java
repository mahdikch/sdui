package ir.naja.pushsdk.android.model;

public class MessageUnread {
    private String messageId;
    private String message;
    private String messageType;
    private String topic;
    private String senderId;
    private String senderTitle;
    private String sentTimestamp;
    private String level;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
