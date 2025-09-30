package ir.naja.pushsdk.android;

import static ir.naja.pushsdk.android.Constant.APP_NAME;
import static ir.naja.pushsdk.android.Constant.Lat;
import static ir.naja.pushsdk.android.Constant.Lot;
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
    private static String serverUri = "tcp://" + "172.31.68.165" + ":" + "1885"; // local
//    private static String serverUri = "tcp://" + "10.33.106.68" + ":" + "1885";

    public void setListener(NajiPushCallBack listenerr) {
        this.listener = listenerr;
    }

    public static MqttAndroidClient getInstance(Activity activity) throws NullActivityException {
        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(activity, serverUri, getClientId(activity), new Intent());
        }
        return mqttAndroidClient;
    }

    public void setServerUri(String mqtt, String port) {
        NajiPush.serverUri = "tcp://" + mqtt + ":" + port;
    }

    public ResponseActive ActiveNajiFication(Activity activity, Intent mainIntentClass, String apiKeyServer, String username, String lat, String lot, String usernamePortal, String passwordPortal) {
//        String tp = "message/" + username + "/PHONE";

        try {

            HashMap<String, String> request = new HashMap<>();
            request.put("client_id", "pushnotif");
            request.put("client_secret", "pushnotif-changeitinportal");
            request.put("grant_type", "password");
            request.put("username", usernamePortal);
            request.put("password", passwordPortal);
//            request.put("username", "policeman");
//            request.put("password", "policeman123");
            try {
                String s = new ApiCall().makeApiPostRequestFromData(request);
                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
                }.getType());
                request = new HashMap<>();
                request.put("apiKeyServer", apiKeyServer);
                request.put("deviceType", "PHONE");
                request.put("username", username);
                request.put("latitude", lat);
                request.put("longitude", lot);
                String sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
                if (sessionId == null) {
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
                    SharedPrefs.save(activity, SharedPrefs.session1, o.getSessionId());
                    SharedPrefs.save(activity, SharedPrefs.topic1, o.getTopic());
                    connectNajiPush(activity, o.getTopic(), lat, lot, mainIntentClass, username, o.getSessionId());
                    return o;
                }

            } catch (IOException e) {

                try {
                    if (mqttAndroidClient != null)
                        mqttAndroidClient.disconnect();
                } catch (Exception ee) {
                }
            }
        } catch (Exception e) {
            try {
                if (mqttAndroidClient != null)
                    mqttAndroidClient.disconnect();
            } catch (Exception eee) {
            }

        }
        return new ResponseActive();
    }

    public ResponseActive ActiveNajiFicationTwoApp(Activity activity, Intent mainIntentClass, String apiKeyServer1, String apiKeyServer2, String username, String lat, String lot, String usernamePortal, String passwordPortal) {
//        String tp = "message/" + username + "/PHONE";

        try {

            HashMap<String, String> request = new HashMap<>();
            request.put("client_id", "pushnotif");
            request.put("client_secret", "pushnotif-changeitinportal");
            request.put("grant_type", "password");
            request.put("username", usernamePortal);
            request.put("password", passwordPortal);
//            request.put("username", "policeman");
//            request.put("password", "policeman123");
            try {
                String s = new ApiCall().makeApiPostRequestFromData(request);
                HashMap<String, String> response = new Gson().fromJson(s, new TypeToken<HashMap<String, String>>() {
                }.getType());
                request = new HashMap<>();
                request.put("apiKeyServer", apiKeyServer1);
                request.put("deviceType", "PHONE");
                request.put("username", username);
                request.put("latitude", lat);
                request.put("longitude", lot);
                String sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));
                if (sessionId == null) {
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
                    SharedPrefs.save(activity, SharedPrefs.session1, o.getSessionId());
                    SharedPrefs.save(activity, SharedPrefs.topic1, o.getTopic());
                    request = new HashMap<>();
                    request.put("apiKeyServer", apiKeyServer2);
                    request.put("deviceType", "PHONE");
                    request.put("username", "PH_NOTIF/"+username);
                    request.put("latitude", lat);
                    request.put("longitude", lot);
                    sessionId = new ApiCall().makeApiPostRequestWithObjHeader(request, "Authorization", "bearer " + (response != null ? response.get("access_token") : ""));


                    ResponseActive o2 = new Gson().fromJson(sessionId, ResponseActive.class);
                    SharedPrefs.save(activity, SharedPrefs.session2, o2.getSessionId());
                    SharedPrefs.save(activity, SharedPrefs.topic2, o2.getTopic());

                    connectNajiPush(activity, o.getTopic(), lat, lot, mainIntentClass, username, o.getSessionId(), o2.getTopic());
                    return o;
                }

            } catch (IOException e) {

                try {
                    if (mqttAndroidClient != null)
                        mqttAndroidClient.disconnect();
                } catch (Exception ee) {
                }
            }
        } catch (Exception e) {
            try {
                if (mqttAndroidClient != null)
                    mqttAndroidClient.disconnect();
            } catch (Exception eee) {
            }

        }
        return new ResponseActive();
    }

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
        this.myActivity = activity;
        this.mainIntentClass = mainIntentClass;
        Lat = lat;
        Lot = lot;
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(60);
        mqttConnectOptions.setKeepAliveInterval(600);
        mqttConnectOptions.setCleanSession(false);
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
                    } else {
                        listener.connected("Connected to: " + serverURI);
                        addToHistory("Connected to: " + serverURI);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    listener.connectionLost("The Connection was lost." + cause.getMessage());
                    addToHistory("The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Message message1 = null;
                    try {
                        try {
                            AES.setKey(SharedPrefs.getString(activity, SharedPrefs.session1));
                            String decrypt = AES.decrypt(new String(message.getPayload()));
                            message1 = new Gson().fromJson(decrypt, Message.class);
                        } catch (Exception e) {
                            message1 = new Gson().fromJson(new String(message.getPayload()), Message.class);
                        }
                    } catch (Exception e) {
                        message1 = new Message();
                        message1.setSenderTitle(topic);
                        Payload payload = new Payload();
                        payload.setBody(new String(message.getPayload()));
                        payload.setTitle(new String(message.getPayload()));
                        message1.setPayload(new Gson().toJson(payload));
                    }

//                    LinkedHashMap linkedHashMap = new Gson().fromJson(new String(message.getPayload()), LinkedHashMap.class);
//                    Object payload = linkedHashMap.get("payload");
//                    payload.toString().replaceAll("'","\"")
                    String spayload = message1.getPayload();
                    Payload payload = null;
                    try {

                        payload = new Gson().fromJson(spayload, Payload.class);
                    } catch (Exception e) {
                        String substring = message1.getPayload().substring(17, message1.getPayload().length() - 1);
                        payload.setTitle(message1.getSenderTitle());
                        payload.setBody(substring);
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
//                    Notify.notifcation(myActivity, payload.getTitle(), message1.getSenderTitle(), new Intent(), R.string.notifyTitle, mainIntent);
                    Notify.notifcation(myActivity, payload.getTitle(), "پلیس همراه +", new Intent(), R.string.notifyTitle, mainIntent);
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

        } catch (MqttException ex) {
            ex.printStackTrace();
            listener.connectFailed("Exception connecting: " + serverUri);
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
                    listener.connectionLost("The Connection was lost." + cause.getMessage());
                    addToHistory("The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Message message1 = null;
                    try {
                        if (topic.contains("PH_NOTIF")) {
                            try {
                                AES.setKey(SharedPrefs.getString(activity, SharedPrefs.session2));
                                String decrypt = AES.decrypt(new String(message.getPayload()));
                                message1 = new Gson().fromJson(decrypt, Message.class);
                            } catch (Exception e) {
                                message1 = new Gson().fromJson(new String(message.getPayload()), Message.class);
                            }
                        } else if (topic.contains("ROKHDAD_APP_MISSION")) {
                            try {
                                AES.setKey(SharedPrefs.getString(activity, SharedPrefs.session1));
                                String decrypt = AES.decrypt(new String(message.getPayload()));
                                message1 = new Gson().fromJson(decrypt, Message.class);
                            } catch (Exception e) {
                                message1 = new Gson().fromJson(new String(message.getPayload()), Message.class);
                            }
                        }
                    } catch (Exception e) {
                        message1 = new Message();
                        message1.setSenderTitle(topic);
                        Payload payload = new Payload();
                        payload.setBody(new String(message.getPayload()));
                        payload.setTitle(new String(message.getPayload()));
                        message1.setPayload(new Gson().toJson(payload));
                    }

//                    LinkedHashMap linkedHashMap = new Gson().fromJson(new String(message.getPayload()), LinkedHashMap.class);
//                    Object payload = linkedHashMap.get("payload");
//                    payload.toString().replaceAll("'","\"")
                    String spayload = message1.getPayload();
                    Payload payload = null;
                    try {

                        payload = new Gson().fromJson(spayload, Payload.class);
                    } catch (Exception e) {
                        String substring = message1.getPayload().substring(17, message1.getPayload().length() - 1);
                        payload.setTitle(message1.getSenderTitle());
                        payload.setBody(substring);
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
//                    Notify.notifcation(myActivity, payload.getTitle(), message1.getSenderTitle(), new Intent(), R.string.notifyTitle, mainIntent);
                    Notify.notifcation(myActivity, payload.getTitle(), "پلیس همراه +", new Intent(), R.string.notifyTitle, mainIntent);
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
            } catch (Exception e) {
                String substring = message.getPayload().substring(17, message.getPayload().length() - 1);
                payload.setTitle(message.getSenderTitle());
                payload.setBody(substring);
            }
            Intent mainIntent = new Intent(myActivity, mainIntentClass.getClass());
            Notify.notifcation(myActivity, payload.getTitle(), message.getSenderTitle(), new Intent(), R.string.notifyTitle, mainIntent);
            addToHistory("Incoming message: " + new String(message.getPayload()));
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
        System.out.println("LOG: " + mainText);
//        mAdapter.add(mainText);
        if (myActivity != null)
            Snackbar.make(myActivity.findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }


}
