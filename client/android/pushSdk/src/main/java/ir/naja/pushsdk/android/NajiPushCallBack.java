package ir.naja.pushsdk.android;

import ir.naja.pushsdk.android.model.Message;

public interface NajiPushCallBack {

    void reconnected(String serverUri);

    void connected(String serverUri);

    void connectFailed(String serverUri);

    void connectionLost(String serverUri);

    void messageArrived(String topic, Message message);

    void deliveryComplete(String s);

    void deliveryNotComplete(String s);

    void Subscribed(String s);

    void SubscribeFailed(String failed_to_subscribe);

    void errorPublishing(String s);

    void bufferedMessageCount(String s);
}
