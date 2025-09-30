package ir.naja.pushsdk.android.model;

public class ReceiverClient {
    private String clientID;
    private TypeClient type;

    public String getClientID() {
        return clientID;
    }

    public ReceiverClient(String clientID, TypeClient type) {
        this.clientID = clientID;
        this.type = type;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public TypeClient getType() {
        return type;
    }

    public void setType(TypeClient type) {
        this.type = type;
    }
}
