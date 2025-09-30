package ir.naja.pushsdk.android.model;


import java.time.LocalDateTime;

public class SetStatusRequest {
    private String requestId;
    private String messageId;
    private MessageStatus status;
    private String ipAddress;
    private String timestamp;
    private String lat;
    private String lang;
    private ReceiverClient client;

    public SetStatusRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public ReceiverClient getClient() {
        return client;
    }

    public void setClient(ReceiverClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "SetStatusRequest{" +
                "requestId='" + requestId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", status=" + status +
                ", ipAddress='" + ipAddress + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", lat='" + lat + '\'' +
                ", lang='" + lang + '\'' +
                ", client=" + client +
                '}';
    }
}
