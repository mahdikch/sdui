package ir.naja.pushsdk.android;

import static ir.naja.pushsdk.android.Constant.APP_NAME;
import static ir.naja.pushsdk.android.Constant.Lat;
import static ir.naja.pushsdk.android.Constant.Lot;
import static ir.naja.pushsdk.android.Constant.MQTT_PORT;
import static ir.naja.pushsdk.android.Constant.MQTT_URL;
import static ir.naja.pushsdk.android.Constant.PASSWORD_PORTAL;
import static ir.naja.pushsdk.android.Constant.USERNAME_PORTAL;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//import br.com.safety.locationlistenerhelper.core.LocationTracker;
import ir.naja.pushsdk.R;
import ir.naja.pushsdk.android.Exception.NullActivityException;
import ir.naja.pushsdk.android.model.Message;
import ir.naja.pushsdk.android.model.MessageStatus;
import ir.naja.pushsdk.android.model.MessageUnread;
import ir.naja.pushsdk.android.model.Payload;
import ir.naja.pushsdk.android.model.ReceiverClient;
import ir.naja.pushsdk.android.model.ResponseActive;
import ir.naja.pushsdk.android.model.SendMessage;
import ir.naja.pushsdk.android.model.SendMessages;
import ir.naja.pushsdk.android.model.SetStatusRequest;
import ir.naja.pushsdk.android.model.TypeClient;
import ir.naja.pushsdk.android.model.UnreadMessageReq;
import ir.naja.pushsdk.android.service.MqttAndroidClient;
import ir.naja.pushsdk.android.service.MqttService;
import ir.naja.pushsdk.android.service.Notify;

public class NajiPush {
    private static int REQUEST_RESULT = 100;
    //    private LocationTracker locationTracker;
    private static MqttAndroidClient mqttAndroidClient;

    private static String clientId = null;
    private Activity myActivity;
    private Intent mainIntentClass;
    private int qos = 2;
    private NajiPushCallBack listener;
    private static String serverUri = buildMqttUri();

    public void setListener(NajiPushCallBack listenerr) {
        this.listener = listenerr;
    }

    private static String buildMqttUri() {
        String scheme = Constant.MQTT_USE_SSL ? "ssl://" : "tcp://";
        return scheme + MQTT_URL + ":" + MQTT_PORT;
    }

    public static MqttAndroidClient getInstance(Activity activity) throws NullActivityException {
        String uri = buildMqttUri();
        if (mqttAndroidClient == null || !uri.equals(serverUri)) {
            serverUri = uri;
            mqttAndroidClient = new MqttAndroidClient(activity, serverUri, getClientId(activity), new Intent());
            PushLog.i("MQTT client created uri=" + serverUri + " clientId=" + clientId);
        }
        return mqttAndroidClient;
    }

    public void setServerUri(String mqtt, String port) {
        String scheme = Constant.MQTT_USE_SSL ? "ssl://" : "tcp://";
        NajiPush.serverUri = scheme + mqtt + ":" + port;
        mqttAndroidClient = null;
    }

    public ResponseActive ActiveNajiFication(Activity activity, Intent mainIntentClass, String apiKeyServer, String username, String lat, String lot, String usernamePortal, String passwordPortal) {
//        String tp = "message/" + username + "/PHONE";

        try {

            PushSsl.init(activity);
            PushLog.i("ActiveNajiFication start user=" + username
                    + " portalUser=" + usernamePortal
                    + " portal=" + Constant.portal_Url
                    + " enable=" + Constant.server_Url
                    + " mqtt=" + serverUri
                    + " thread=" + Thread.currentThread().getName());
            HashMap<String, String> request = new HashMap<>();
            request.put("client_id", "pushnotif");
            request.put("client_secret", "pushnotif-changeitinportal");
            request.put("grant_type", "password");
            request.put("username", usernamePortal);
            request.put("password", passwordPortal);
//            request.put("username", "policeman");
//            request.put("password", "policeman123");
            try {
                PushSsl.init(activity);
                PushLog.i("Requesting portal token");
                String s = new ApiCall().makeApiPostRequestFromData(request);
                PushLog.i("Portal token response " + PushLog.preview(s));
                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
                }.getType());
                if (response == null || response.get("access_token") == null) {
                    PushLog.e("Portal token missing. body=" + s);
                    return new ResponseActive();
                }
                PushLog.i("Portal token ok " + PushLog.preview(response.get("access_token")));
                request = new HashMap<>();
                request.put("apiKeyServer", apiKeyServer);
                request.put("deviceType", "PHONE");
                request.put("username", "PHPLUS_APP/" +username);
                request.put("latitude", lat);
                request.put("longitude", lot);
                String sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + response.get("access_token"));
                PushLog.i("Enable-device response " + PushLog.preview(sessionId));
                if (sessionId == null) {
                    PushLog.e("Enable-device returned null");
                    try {
                        if (mqttAndroidClient != null)
                            mqttAndroidClient.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        if (mqttAndroidClient != null)
                            mqttAndroidClient.disconnect();
                    } catch (Exception ee) {
                    }
                    ResponseActive o = new Gson().fromJson(sessionId, ResponseActive.class);
                    PushLog.i("Enable-device parsed topic=" + o.getTopic()
                            + " session=" + PushLog.preview(o.getSessionId()));
                    SharedPrefs.save(activity, SharedPrefs.session1, o.getSessionId());
                    SharedPrefs.save(activity, SharedPrefs.topic1, o.getTopic());
                    connectNajiPush(activity, o.getTopic(), lat, lot, mainIntentClass, username, o.getSessionId());
                    return o;
                }

            } catch (IOException e) {
                PushLog.e("ActiveNajiFication IO error " + PushLog.errorSummary(e), e);

                try {
                    if (mqttAndroidClient != null)
                        mqttAndroidClient.disconnect();
                } catch (Exception ee) {
                }
            }
        } catch (Exception e) {
            PushLog.e("ActiveNajiFication failed " + PushLog.errorSummary(e), e);
            try {
                if (mqttAndroidClient != null)
                    mqttAndroidClient.disconnect();
            } catch (Exception eee) {
            }

        }
        return new ResponseActive();
    }

//    public ResponseActive ActiveNajiFicationTwoApp(Activity activity, Intent mainIntentClass, String apiKeyServer1, String apiKeyServer2, String username, String lat, String lot, String usernamePortal, String passwordPortal) {
////        String tp = "message/" + username + "/PHONE";
//
//        try {
//
//            HashMap<String, String> request = new HashMap<>();
//            request.put("client_id", "pushnotif");
//            request.put("client_secret", "pushnotif-changeitinportal");
//            request.put("grant_type", "password");
//            request.put("username", usernamePortal);
//            request.put("password", passwordPortal);
////            request.put("username", "policeman");
////            request.put("password", "policeman123");
//            try {
//                String s = new ApiCall().makeApiPostRequestFromData(request);
//                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
//                }.getType());
//                request = new HashMap<>();
//                request.put("apiKeyServer", apiKeyServer1);
//                request.put("deviceType", "PHONE");
//                request.put("username", username);
//                request.put("latitude", lat);
//                request.put("longitude", lot);
//                String sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
//                if (sessionId == null) {
//                    try {
//                        if (mqttAndroidClient != null)
//                            mqttAndroidClient.disconnect();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                } else {
//                    try {
//                        if (mqttAndroidClient != null)
//                            mqttAndroidClient.disconnect();
//                    } catch (Exception ee) {
//                    }
//                    ResponseActive o = new Gson().fromJson(sessionId, ResponseActive.class);
//                    SharedPrefs.save(activity, SharedPrefs.session1, o.getSessionId());
//                    SharedPrefs.save(activity, SharedPrefs.topic1, o.getTopic());
//                    request = new HashMap<>();
//                    request.put("apiKeyServer", apiKeyServer2);
//                    request.put("deviceType", "PHONE");
//                    request.put("username", "PH_NOTIF/" + username);
//                    request.put("latitude", lat);
//                    request.put("longitude", lot);
//                    sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
//
//
//                    ResponseActive o2 = new Gson().fromJson(sessionId, ResponseActive.class);
//                    SharedPrefs.save(activity, SharedPrefs.session2, o2.getSessionId());
//                    SharedPrefs.save(activity, SharedPrefs.topic2, o2.getTopic());
//
//                    connectNajiPush(activity, o.getTopic(), lat, lot, mainIntentClass, username, o.getSessionId(), o2.getTopic());
//                    return o;
//                }
//
//            } catch (IOException e) {
//
//                try {
//                    if (mqttAndroidClient != null)
//                        mqttAndroidClient.disconnect();
//                } catch (Exception ee) {
//                }
//            }
//        } catch (Exception e) {
//            try {
//                if (mqttAndroidClient != null)
//                    mqttAndroidClient.disconnect();
//            } catch (Exception eee) {
//            }
//
//        }
//        return new ResponseActive();
//    }

    public String sendMessage(String usernamePortal, String passwordPortal, SendMessage sendMessage) {

        try {

            HashMap<String, String> request = new HashMap<>();
            request.put("client_id", "pushnotif");
            request.put("client_secret", "pushnotif-changeitinportal");
            request.put("grant_type", "password");
            request.put("username", usernamePortal);
            request.put("password", passwordPortal);
            try {
                String s = new ApiCall().makeApiPostRequestFromData(request);
                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
                }.getType());
                SendMessages sendMessages = new SendMessages(sendMessage);
                String isSend = new ApiCall().makeApiPostRequestWithObjHeaderSend(sendMessages, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
                return isSend;

            } catch (IOException e) {
                return "-1";
            }
        } catch (Exception e) {
            return "-1";
        }

    }

    public List<Message> unreadMessage(UnreadMessageReq sendMessage) {
        List<Message> messages = new ArrayList<>();
        try {

            HashMap<String, String> request = new HashMap<>();
            request.put("client_id", "pushnotif");
            request.put("client_secret", "pushnotif-changeitinportal");
            request.put("grant_type", "password");
            request.put("username", USERNAME_PORTAL);
            request.put("password", PASSWORD_PORTAL);
            try {
                String s = new ApiCall().makeApiPostRequestFromData(request);
                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
                }.getType());
                String unreadMessage = new ApiCall().makeApiPostRequestWithObjHeaderSync(sendMessage, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
                List<MessageUnread> unreadMessage1 = new Gson().fromJson(unreadMessage, new TypeToken<List<MessageUnread>>() {
                }.getType());
                for (MessageUnread messageUnread : unreadMessage1) {
                    Message message = new Message(messageUnread);
                    messages.add(message);
                }
                return messages;
            } catch (IOException e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    public void connectNajiPush(Activity activity, String topic, String lat, String lot, Intent mainIntentClass, String username, String sessionId) throws NullActivityException {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            PushLog.i("MQTT connect posted to main thread");
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    connectNajiPush(activity, topic, lat, lot, mainIntentClass, username, sessionId);
                } catch (Exception e) {
                    PushLog.e("MQTT connect on main failed " + PushLog.errorSummary(e), e);
                }
            });
            return;
        }
        this.myActivity = activity;
        this.mainIntentClass = mainIntentClass;
        Lat = lat;
        Lot = lot;
        if (lat.isEmpty() && lot.isEmpty())
        {
            Lat="31.00";
                Lot="51.00";

        }
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(120);
        mqttConnectOptions.setKeepAliveInterval(600);
        mqttConnectOptions.setCleanSession(false);
        PushSsl.init(activity);
        boolean useSsl = serverUri.startsWith("ssl://") || serverUri.startsWith("tls://");
        PushLog.i("MQTT connect start uri=" + serverUri
                + " topic=" + topic
                + " user=" + username
                + " sslUri=" + useSsl
                + " factory=" + (PushSsl.mqttSocketFactory(activity) != null));
        if (useSsl) {
            javax.net.ssl.SSLSocketFactory socketFactory = PushSsl.mqttSocketFactory(activity);
            if (socketFactory != null) {
                mqttConnectOptions.setSocketFactory(socketFactory);
                PushLog.i("MQTT SSL socket factory applied");
            } else {
                PushLog.e("MQTT SSL requested but socket factory is null");
            }
        } else {
            PushLog.i("MQTT connecting over TCP (no TLS)");
        }
//        mqttConnectOptions.setWill(topic, "client Disconnect".getBytes(), 1, false);

        Log.d("naji push", "app username" + APP_NAME + username);
        if (username.split("/").length > 1)
            mqttConnectOptions.setUserName(username);
        else // police man
            mqttConnectOptions.setUserName(APP_NAME + username); // police man
//        mqttConnectOptions.setUserName(username); // main and local gasht
        mqttConnectOptions.setPassword(sessionId.toCharArray());
//        mqttConnectOptions.setPassword("testuser1".toCharArray());
//        mqttConnectOptions.setUserName("testuser1");
        mqttAndroidClient = getInstance(activity);
        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    PushLog.i("MQTT connect onSuccess uri=" + serverUri + " topic=" + topic);
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
//                    if (activity == null)
                    ContextCompat.startForegroundService(activity, new Intent(activity, MqttService.class));
                    /*else
                        activity.startService(new Intent(activity, MqttService.class));*/
                    if (!topic.equals("")) {
                        subscribeToTopic(topic);
                        subscribeToTopic("remote/" + topic);
                    }
//                    locationTracker=new LocationTracker("my.action")
//                            .setInterval(50000)
//                            .setGps(true)
//                            .setNetWork(false)
//
//                            // IF YOU WANT JUST CURRENT LOCATION
//                            // .currentLocation(new CurrentLocationReceiver(new CurrentLocationListener() {
//                            //
//                            //            @Override
//                            //            public void onCurrentLocation(Location location) {
//                            //               Log.d("callback", ":onCurrentLocation" + location.getLongitude());
//                            //               locationTracker.stopLocationService(getBaseContext());
//                            //            }
//                            //
//                            //            @Override
//                            //            public void onPermissionDiened() {
//                            //                Log.d("callback", ":onPermissionDiened");
//                            //                locationTracker.stopLocationService(getBaseContext());
//                            //            }
//                            // }))
//
//                            .start(myActivity,null);

                    // IF YOU WANT RUN IN SERVICE
                    // .start(this);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    PushLog.e("MQTT connect onFailure uri=" + serverUri
                            + " err=" + PushLog.errorSummary(exception), exception);
                    listener.connectFailed("Failed to connect to: " + serverUri);
                    addToHistory("Failed to connect to: " + serverUri);
                }
            });

            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    PushLog.i("MQTT connectComplete reconnect=" + reconnect + " uri=" + serverURI);

                    if (reconnect) {
                        listener.reconnected("Reconnected to : " + serverURI);
                        addToHistory("Reconnected to : " + serverURI);
                        // Because Clean Session is true, we need to re-subscribe
                        subscribeToTopic(topic);
                        subscribeToTopic("remote/" + topic);
                    } else {
                        listener.connected("Connected to: " + serverURI);
                        addToHistory("Connected to: " + serverURI);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    PushLog.e("MQTT connectionLost " + PushLog.errorSummary(cause), cause);
                    if (listener != null) {
                        String message = "The Connection was lost.";
                        if (cause != null && cause.getMessage() != null) {
                            message += " " + cause.getMessage();
                        }
                        listener.connectionLost(message);
                    }
                    addToHistory("The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    PushLog.i("MQTT messageArrived topic=" + topic
                            + " bytes=" + (message == null || message.getPayload() == null
                            ? 0 : message.getPayload().length));
                    Message message1 = decodeIncomingMessage(
                            message, SharedPrefs.getString(activity, SharedPrefs.session1));
                    if (message1 == null) {
                        // Not marked DELIVERED, so the server resends it once the right key is set.
                        PushLog.w("MQTT message dropped: decrypt failed topic=" + topic);
                        return;
                    }

                    String spayload = message1.getPayload();
                    Payload payload = null;
                    try {
                        payload = new Gson().fromJson(spayload, Payload.class);
                    } catch (Exception ignored) {
                    }
                    listener.messageArrived(topic, message1);
                    SetStatusRequest setStatusRequest = new SetStatusRequest();
                    setStatusRequest.setMessageId(message1.getId());
                    setStatusRequest.setStatus(MessageStatus.DELIVERED);
                    setStatusRequest.setRequestId(UUID.randomUUID().toString());
                    setStatusRequest.setLang(Lot);
                    setStatusRequest.setLat(Lat);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setStatusRequest.setTimestamp(LocalDateTime.now().toString());
                    }
                    setStatusRequest.setIpAddress(Util.getIPAddress(true));
                    try {
                        setStatusRequest.setClient(new ReceiverClient(topic.split("/")[1], TypeClient.PHONE));
                        publishMessage("status/" + topic.split("/")[1], new Gson().toJson(setStatusRequest));
                    } catch (Exception e) {

                    }

                    Intent mainIntent = new Intent(myActivity, mainIntentClass.getClass());
                    // Skip notification for location messages
                    if (payload != null && (topic == null || !topic.contains("PHPLUS_LOCATION"))) {
                        Notify.notifcation(myActivity, payload.getTitle(), "پلیس همراه +", new Intent(), R.string.notifyTitle, mainIntent);
                    }
                    addToHistory("Incoming message: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        listener.deliveryComplete("deliveryComplete : " + token.getMessage());
                        addToHistory("deliveryComplete : " + token.getMessage());
                    } catch (MqttException e) {
                        e.printStackTrace();
                        listener.deliveryNotComplete("deliveryNotCompleted : " + token.isComplete());
                        addToHistory("deliveryNotCompleted : " + token.isComplete());

                    }
                }
            });

        } catch (Exception ex) {
            PushLog.e("MQTT connect exception uri=" + serverUri
                    + " err=" + PushLog.errorSummary(ex), ex);
            if (listener != null) {
                listener.connectFailed("Exception connecting: " + serverUri);
            }
        }

    }

    public void connectNajiPush(Activity activity, String topic, String lat, String lot, Intent mainIntentClass, String username, String sessionId, String topic2) throws NullActivityException {
        this.myActivity = activity;
        this.mainIntentClass = mainIntentClass;
        Lat = lat;
        Lot = lot;
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(60);
        mqttConnectOptions.setKeepAliveInterval(600);
        mqttConnectOptions.setCleanSession(false);
        PushSsl.init(activity);
        if (serverUri.startsWith("ssl://") || serverUri.startsWith("tls://")) {
            javax.net.ssl.SSLSocketFactory socketFactory = PushSsl.mqttSocketFactory(activity);
            if (socketFactory != null) {
                mqttConnectOptions.setSocketFactory(socketFactory);
            }
        }
//        mqttConnectOptions.setWill(topic, "client Disconnect".getBytes(), 1, false);

        Log.d("naji push", "app username" + APP_NAME + username);
        if (username.split("/").length > 1)
            mqttConnectOptions.setUserName(username);
        else // police man
            mqttConnectOptions.setUserName(APP_NAME + username); // police man
//        mqttConnectOptions.setUserName(username); // main and local gasht
        mqttConnectOptions.setPassword(sessionId.toCharArray());
//        mqttConnectOptions.setPassword("testuser1".toCharArray());
//        mqttConnectOptions.setUserName("testuser1");
        mqttAndroidClient = getInstance(activity);
        try {
            //addToHistory("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
//                    if (activity == null)
                    ContextCompat.startForegroundService(activity, new Intent(activity, MqttService.class));
                    /*else
                        activity.startService(new Intent(activity, MqttService.class));*/
                    if (!topic.equals("")) {
                        subscribeToTopic(topic);
                        subscribeToTopic("remote/" + topic);
                        subscribeToTopic(topic2);
                        subscribeToTopic("remote/" + topic2);
                    }
//                    locationTracker=new LocationTracker("my.action")
//                            .setInterval(50000)
//                            .setGps(true)
//                            .setNetWork(false)
//
//                            // IF YOU WANT JUST CURRENT LOCATION
//                            // .currentLocation(new CurrentLocationReceiver(new CurrentLocationListener() {
//                            //
//                            //            @Override
//                            //            public void onCurrentLocation(Location location) {
//                            //               Log.d("callback", ":onCurrentLocation" + location.getLongitude());
//                            //               locationTracker.stopLocationService(getBaseContext());
//                            //            }
//                            //
//                            //            @Override
//                            //            public void onPermissionDiened() {
//                            //                Log.d("callback", ":onPermissionDiened");
//                            //                locationTracker.stopLocationService(getBaseContext());
//                            //            }
//                            // }))
//
//                            .start(myActivity,null);

                    // IF YOU WANT RUN IN SERVICE
                    // .start(this);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    listener.connectFailed("Failed to connect to: " + serverUri);
                    addToHistory("Failed to connect to: " + serverUri);
                }
            });

            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    if (reconnect) {
                        listener.reconnected("Reconnected to : " + serverURI);
                        addToHistory("Reconnected to : " + serverURI);
                        // Because Clean Session is true, we need to re-subscribe
                        subscribeToTopic(topic);
                        subscribeToTopic("remote/" + topic);
                        subscribeToTopic(topic2);
                        subscribeToTopic("remote/" + topic2);
                    } else {
                        listener.connected("Connected to: " + serverURI);
                        addToHistory("Connected to: " + serverURI);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    String message = "The Connection was lost.";
                    if (cause != null && cause.getMessage() != null) {
                        message += " " + cause.getMessage();
                    }
                    listener.connectionLost(message);
                    addToHistory("The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    PushLog.i("MQTT messageArrived topic=" + topic
                            + " bytes=" + (message == null || message.getPayload() == null
                            ? 0 : message.getPayload().length));
                    String sessionKey = null;
                    if (topic != null && topic.contains("PH_NOTIF")) {
                        sessionKey = SharedPrefs.getString(activity, SharedPrefs.session2);
                    } else if (topic != null && topic.contains("ROKHDAD_APP_MISSION")) {
                        sessionKey = SharedPrefs.getString(activity, SharedPrefs.session1);
                    }
                    Message message1 = sessionKey == null ? null : decodeIncomingMessage(message, sessionKey);
                    if (message1 == null) {
                        PushLog.w("MQTT message dropped: decrypt failed topic=" + topic);
                        return;
                    }

                    String spayload = message1.getPayload();
                    Payload payload = null;
                    try {
                        payload = new Gson().fromJson(spayload, Payload.class);
                    } catch (Exception ignored) {
                    }
                    listener.messageArrived(topic, message1);
                    SetStatusRequest setStatusRequest = new SetStatusRequest();
                    setStatusRequest.setMessageId(message1.getId());
                    setStatusRequest.setStatus(MessageStatus.DELIVERED);
                    setStatusRequest.setRequestId(UUID.randomUUID().toString());
                    setStatusRequest.setLang(Lot);
                    setStatusRequest.setLat(Lat);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setStatusRequest.setTimestamp(LocalDateTime.now().toString());
                    }
                    setStatusRequest.setIpAddress(Util.getIPAddress(true));
                    try {
                        setStatusRequest.setClient(new ReceiverClient(topic.split("/")[1], TypeClient.PHONE));
                        publishMessage("status/" + topic.split("/")[1], new Gson().toJson(setStatusRequest));
                    } catch (Exception e) {

                    }

                    Intent mainIntent = new Intent(myActivity, mainIntentClass.getClass());
                    // Skip notification for location messages
                    if (payload != null && (topic == null || !topic.contains("PHPLUS_LOCATION"))) {
                        Notify.notifcation(myActivity, payload.getTitle(), "پلیس همراه +", new Intent(), R.string.notifyTitle, mainIntent);
                    }
                    addToHistory("Incoming message: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        listener.deliveryComplete("deliveryComplete : " + token.getMessage());
//                        Log.d("deliveryComplete:",""+token.getMessage());
                        addToHistory("deliveryComplete : " + token.getMessage());
                    } catch (MqttException e) {
                        e.printStackTrace();
                        listener.deliveryNotComplete("deliveryNotCompleted : " + token.isComplete());
                        addToHistory("deliveryNotCompleted : " + token.isComplete());

                    }
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
            listener.connectFailed("Exception connecting: " + serverUri);
        }

    }

    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    PushLog.i("MQTT subscribed topic=" + topic);
                    listener.Subscribed("Subscribed!");
                    try {
                        HandlerThread handlerThread = new HandlerThread("NetworkThread");
                        handlerThread.start();
                        Handler handler = new Handler(handlerThread.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                syncUnreadMessage(topic);
                            }
                        });
                    } catch (Exception e) {

                    }

                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    PushLog.e("MQTT subscribe failed topic=" + topic
                            + " err=" + PushLog.errorSummary(exception), exception);
                    listener.SubscribeFailed("Failed to subscribe");
                    addToHistory("Failed to subscribe");
                    // TODO: 11/17/2024 timescahul
                    // subscribeToTopic(topic);
                }
            });

            // THIS DOES NOT WORK!
/*
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });
*/

        } catch (MqttException ex) {
            listener.SubscribeFailed("Exception whilst subscribing");
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void syncUnreadMessage(String topic) {
        String[] split = topic.split("/");
        String s = split[2] + "/" + split[3];
        List<Message> messages = unreadMessage(new UnreadMessageReq(Constant.API_KEY, s));
        for (Message message : messages) {
            if (message == null || !isJsonObject(message.getPayload())) {
                PushLog.w("Unread message dropped: payload is not readable topic=" + s);
                continue;
            }
            listener.messageArrived(s, message);
            SetStatusRequest setStatusRequest = new SetStatusRequest();
            setStatusRequest.setMessageId(message.getId());
            setStatusRequest.setStatus(MessageStatus.DELIVERED);
            setStatusRequest.setRequestId(UUID.randomUUID().toString());
            setStatusRequest.setLang(Lot);
            setStatusRequest.setLat(Lat);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setStatusRequest.setTimestamp(LocalDateTime.now().toString());
            }
            setStatusRequest.setIpAddress(Util.getIPAddress(true));
            try {
                setStatusRequest.setClient(new ReceiverClient(message.getTopic().split("/")[1], TypeClient.PHONE));
                publishMessage("status/" + message.getTopic().split("/")[1], new Gson().toJson(setStatusRequest));
            } catch (Exception e) {

            }
            String spayload = message.getPayload();
            Payload payload = null;
            try {
                payload = new Gson().fromJson(spayload, Payload.class);
            } catch (Exception ignored) {
            }
            Intent mainIntent = new Intent(myActivity, mainIntentClass.getClass());
            // Skip notification for location messages
            String messageTopic = message.getTopic();
            if (payload != null && (messageTopic == null || !messageTopic.contains("PHPLUS_LOCATION"))) {
                Notify.notifcation(myActivity, payload.getTitle(), message.getSenderTitle(), new Intent(), R.string.notifyTitle, mainIntent);
            }
            addToHistory("Incoming message: " + new String(message.getPayload()));
        }
    }

    /**
     * Returns null when the payload cannot be decrypted with the current session key,
     * so ciphertext is never delivered as a notification.
     */
    private static Message decodeIncomingMessage(MqttMessage mqttMessage, String sessionKey) {
        if (mqttMessage == null || mqttMessage.getPayload() == null) return null;
        String raw = new String(mqttMessage.getPayload());
        if (sessionKey != null && !sessionKey.isEmpty()) {
            try {
                AES.setKey(sessionKey);
                Message decrypted = new Gson().fromJson(AES.decrypt(raw), Message.class);
                if (decrypted != null && isJsonObject(decrypted.getPayload())) {
                    return decrypted;
                }
            } catch (Exception ignored) {
            }
        }
        if (!isJsonObject(raw)) return null;
        try {
            Message plain = new Gson().fromJson(raw, Message.class);
            if (plain != null && isJsonObject(plain.getPayload())) {
                return plain;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isJsonObject(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (!trimmed.startsWith("{")) return false;
        try {
            return new com.google.gson.JsonParser().parse(trimmed).isJsonObject();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        if (mqttAndroidClient != null) {
            return mqttAndroidClient.isConnected();
        } else return false;
    }

    public void publishMessage(String topic, String message) {

        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttAndroidClient.publish(topic, mqttMessage);
            addToHistory("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                String s = mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.";
                listener.bufferedMessageCount(s);
                addToHistory(s);
            }
        } catch (MqttException e) {
            listener.errorPublishing("Error Publishing: " + e.getMessage());
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void seenMessage(String msgId, String topic) {

        try {
            SetStatusRequest setStatusRequest = new SetStatusRequest();
            setStatusRequest.setMessageId(msgId);
            setStatusRequest.setStatus(MessageStatus.SEEN);
            setStatusRequest.setRequestId(UUID.randomUUID().toString());
            setStatusRequest.setLang(Lot);
            setStatusRequest.setLat(Lat);
            setStatusRequest.setIpAddress(Util.getIPAddress(true));
            setStatusRequest.setClient(new ReceiverClient(topic, TypeClient.PHONE));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setStatusRequest.setTimestamp(LocalDateTime.now().toString());
            }
            String message = new Gson().toJson(setStatusRequest);
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttAndroidClient.publish("status/" + topic, mqttMessage);
            addToHistory("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                String s = mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.";
                listener.bufferedMessageCount(s);
                addToHistory(s);
            }
        } catch (MqttException e) {
            listener.errorPublishing("Error Publishing: " + e.getMessage());
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getClientId(Activity activity) throws NullActivityException {
        if (clientId == null) {
            return clientId = getImei(activity) + System.currentTimeMillis();
        } else return clientId;
    }

    private static String getImei(Activity activity) throws NullActivityException {
        if (activity == null) {
            throw new NullActivityException("ورودی Activity ضروری است.");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "دسترسی PHONE_STATE نیاز می باشد", Toast.LENGTH_LONG);
                });
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_RESULT);
            } else {
                try {
                    return ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                } catch (Exception e) {
                    return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }
        } else {
            try {
                return ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            } catch (Exception e) {
                return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        return "UNKNOWN";
    }

    private void addToHistory(String mainText) {
        PushLog.i(mainText);
    }

    public void disconnect() {
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.disconnect();
            }
        } catch (MqttException e) {
            PushLog.e("MQTT disconnect failed " + PushLog.errorSummary(e), e);
        } finally {
            mqttAndroidClient = null;
        }
    }

}
