package ir.naja.pushsdk.android;

public class Constant {
//        public static String MQTT_URL = "172.19.83.16";//wifi
//    public static String MQTT_URL = "svr-app.bazresi.naja.net";//wifi
//    public static String MQTT_URL = "5.34.205.217";//wifi
//    public static String MQTT_URL = "192.168.189.6";//wifi
//    public static String MQTT_URL = "192.168.0.90";//wifi
    //main
//    public static String MQTT_URL = "push.pishgiri.naja.net";//wifi
//
//        public static String server_Url ="https://push.pishgiri.naja.net/pushnotif/session-api/enable-device";
//    public static String MQTT_PORT = "1883";
//    public static String send_Url = "https://push.pishgiri.naja.net/pushnotif/sending-notifications/send-push";
//    public static String portal_Url = "https://push.pishgiri.naja.net/portal/oauth/token";
//    public static String unread_Url = "https://push.pishgiri.naja.net/pushnotif/sending-notifications/sync-unread";
//    public static String API_KEY = "788de465-a105-4d9d-aca7-292e8a78b409";// local token
//    public static String USERNAME_PORTAL = "policehamrah";// ghasht
//    public static String PASSWORD_PORTAL = "123456";// ghasht
//    public static final boolean MQTT_USE_SSL = false;





    //test
       public static String MQTT_URL = "172.31.68.165";//wifi
        public static String USERNAME_PORTAL = "policehamrah";// ghasht
        public static String PASSWORD_PORTAL = "123456";// ghasht
        public static String server_Url ="https://172.31.68.165/pushnotif/session-api/enable-device";
        public static String send_Url = "https://172.31.68.165/pushnotif/sending-notifications/send-push"; // local
        public static String portal_Url = "https://172.31.68.165/portal/oauth/token"; // local
        public static String unread_Url = "https://172.31.68.165/pushnotif/sending-notifications/sync-unread";
        public static String API_KEY = "ab864b5e-fe84-4a0a-85ba-58ad1af12d4e";// local token*/

//        public static String MQTT_URL = "10.33.202.163";//wifi
//    public static String MQTT_URL = "10.33.165.130";//wifi
//    public static String MQTT_PORT = "1885";//wifi
//    public static String MQTT_PORT = "1883";
    public static String MQTT_PORT = "8989";
    /** When true, broker URI is ssl:// and MqttSslHelper (assets/ca.crt) is required — same TLS path as App-background0508. */
    public static final boolean MQTT_USE_SSL = true;
    public static String APP_NAME = "PHPLUS_APP/";// ghasht


// public static String APP_NAME = "ROKHDAD_APP_MISSION/";// ghasht
    public static String APP_NAME_SEND = "PHPLUS_LOCATION";// ghasht
//  public static String USERNAME = "test_mrz";//ghasht
    public static String USERNAME = "";//ghasht


    //    public static String USERNAME_PORTAL = "policeman";//policeman
//    public static String USERNAME = "test_mrz";//policeman
//    public static String PASSWORD_PORTAL = "policeman123";// policeman
//    public static String APP_NAME = "PM_EVENT/";// policeman
    public static String Lat = "0";//wifi
    public static String Lot = "0";//wifi

//    public static String API_KEY = "e50c0578-2241-4e6f-93da-e4d839c8b8f7"; // main token

    public static String API_KEY2 = "e573552c-b409-4f25-99b5-27da8f2a29e6";// local token
//    public static String API_KEY = "e5e8edea-06fb-4aea-bb65-572d4b8806b0";// police man local token
//    public static String send_Url = "http://172.31.68.165/pushnotif/sending-notifications/send-push"; // local
//    public static String server_Url = "http://172.31.68.165/pushnotif/session-api/enable-device"; // local
//    public static String unread_Url ="http://172.31.68.165/pushnotif/sending-notifications/sync-unread";
//        public static String send_Url ="http://172.31.68.165/pushnotif/sending-notifications/send-push";
//        public static String send_Url ="http://172.31.68.165/pushnotif/sending-notifications/send-push";
//        public static String send_Url ="http://172.31.68.165/pushnotif/sending-notifications/send-push";

//    public static String server_Url ="http://172.31.68.165/pushnotif/session-api/enable-device";
//    public static String server_Url ="http://push.pishgiri.naja.net/pushnotif/session-api/enable-device";
//    public static String portal_Url ="http://192.168.0.120/portal/oauth/token";

//    public static String portal_Url ="http://172.31.68.165/portal/oauth/token";

    //
//    public static String unread_Url ="http://172.31.68.165/pushnotif/sending-notifications/sync-unread";
    public static String sessionId = "";
}
