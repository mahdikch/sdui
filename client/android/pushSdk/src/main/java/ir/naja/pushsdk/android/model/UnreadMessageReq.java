package ir.naja.pushsdk.android.model;

public class UnreadMessageReq {
    private String apiKey;
    private String topic;

    public String getApiKey() {
        return apiKey;
    }

    public UnreadMessageReq(String apiKey, String topic) {
        this.apiKey = apiKey;
        this.topic = topic;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
