package ir.naja.pushsdk.android.model;

import java.util.List;

public class SendMessage {
    private String destApp;
    private Payload message;
    private ReceiverClient client;

    public String getDestApp() {
        return destApp;
    }

    public void setDestApp(String destApp) {
        this.destApp = destApp;
    }

    public Payload getMessage() {
        return message;
    }

    public void setMessage(Payload message) {
        this.message = message;
    }

    public ReceiverClient getClient() {
        return client;
    }

    public void setClient(ReceiverClient client) {
        this.client = client;
    }
}
