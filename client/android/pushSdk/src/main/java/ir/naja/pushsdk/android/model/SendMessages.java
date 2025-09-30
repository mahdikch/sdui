package ir.naja.pushsdk.android.model;

import java.util.ArrayList;
import java.util.List;

public class SendMessages {
    private String destApp;
    private Payload message;
    private List<ReceiverClient> clients;

    public SendMessages(SendMessage sendMessage) {
        this.destApp = sendMessage.getDestApp();
        this.message = sendMessage.getMessage();
        this.clients = new ArrayList<>();
        this.clients.add(sendMessage.getClient());
    }

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

    public List<ReceiverClient> getClients() {
        return clients;
    }

    public void setClients(List<ReceiverClient> clients) {
        this.clients = clients;
    }
}
