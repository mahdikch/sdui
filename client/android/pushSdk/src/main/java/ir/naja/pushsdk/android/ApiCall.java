package ir.naja.pushsdk.android;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.naja.pushsdk.android.model.ActiveSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Handshake;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ApiCall {
    Object result = null;
    public static final MediaType mediaType = MediaType.get("application/json;charset=utf-8");

    private OkHttpClient client() {
        return PushSsl.client(null);
    }

    public Object makeApiGetRequest(String data) {

        Request request = new Request.Builder().url(Constant.server_Url + data).get().build();
        client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println("e.getMessage() = " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        result = response.body().string();
                } else {
                    System.out.println("ApiError = " + call);
                }
            }
        });
        return result;
    }

    public Object makeApiPostRequest(String data) {
        RequestBody requestBody = RequestBody.create(data, mediaType);
        Request request = new Request.Builder().url(Constant.server_Url + data).post(requestBody).build();
        client().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println("e.getMessage() = " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        result = response.body().string();
                } else {
                    System.out.println("ApiError = " + call);
                }
            }
        });
        return result;
    }
    public  String makeApiPostRequestWithObjHeader( Object object, String headerName, String token) throws IOException {
        return postEnableDevice(object, headerName, token, true);
    }

    private String postEnableDevice(Object object, String headerName, String token, boolean recoverAuthFailure) throws IOException {
        OkHttpClient client = client();
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(object);
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaType);
        Request request = new Request.Builder()
                .url(Constant.server_Url)
                .post(requestBody)
                .addHeader(headerName, token)
                .addHeader("Content-Type", "application/json")
                .build();

        PushLog.i("HTTP enable-device POST " + Constant.server_Url
                + " token=" + PushLog.preview(token));
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : null;
            int code = response.code();
            PushLog.i("HTTP enable-device code=" + code
                    + " ok=" + response.isSuccessful()
                    + " body=" + PushLog.preview(body));
            if ((code == 401 || code == 403) && recoverAuthFailure) {
                PushLog.w("HTTP enable-device auth failure code=" + code + ", clearing sessions");
                if (clearActiveSessions(object, headerName, token)) {
                    PushLog.i("Sessions cleared, retrying enable-device");
                    return postEnableDevice(object, headerName, token, false);
                }
                PushLog.w("Session clear failed, enable-device body=" + (body == null ? "null" : body));
                return body;
            }
            if (!response.isSuccessful()) {
                PushLog.w("HTTP enable-device body=" + (body == null ? "null" : body));
            }
            return body;
        } catch (IOException e) {
            PushLog.e("HTTP enable-device failed url=" + Constant.server_Url
                    + " err=" + PushLog.errorSummary(e), e);
            throw e;
        }
    }

    private boolean clearActiveSessions(Object requestObject, String headerName, String token) throws IOException {
        String username = stringField(requestObject, "username");
        String apiKeyServer = stringField(requestObject, "apiKeyServer");
        if (username == null || username.isEmpty() || apiKeyServer == null || apiKeyServer.isEmpty()) {
            PushLog.w("Session clear skipped: username or apiKeyServer missing");
            return false;
        }

        HashMap<String, String> query = new HashMap<>();
        query.put("username", username);
        query.put("apiKeyServer", apiKeyServer);
        String sessionsUrl = sessionApiUrl("get-active-sessions");
        PushLog.i("HTTP get-active-sessions POST " + sessionsUrl);
        HttpResult sessionsResult = postJson(sessionsUrl, query, headerName, token);
        PushLog.i("HTTP get-active-sessions code=" + sessionsResult.code
                + " body=" + PushLog.preview(sessionsResult.body));
        if (sessionsResult.code != 200 || sessionsResult.body == null) {
            PushLog.w("HTTP get-active-sessions failed code=" + sessionsResult.code
                    + " body=" + (sessionsResult.body == null ? "null" : sessionsResult.body));
            return false;
        }

        List<ActiveSession> sessions;
        try {
            Type listType = new TypeToken<List<ActiveSession>>() {
            }.getType();
            sessions = new Gson().fromJson(sessionsResult.body, listType);
        } catch (RuntimeException e) {
            PushLog.e("HTTP get-active-sessions parse failed " + PushLog.errorSummary(e), e);
            return false;
        }
        if (sessions == null) {
            sessions = Collections.emptyList();
        }
        PushLog.i("Active sessions count=" + sessions.size());

        String logoutUrl = sessionApiUrl("logout");
        boolean allLoggedOut = true;
        for (ActiveSession session : sessions) {
            if (session == null || session.getCreationTime() == null || session.getCreationTime().isEmpty()) {
                PushLog.w("HTTP logout skipped: session has no creationTime");
                allLoggedOut = false;
                continue;
            }
            String deviceType = session.getDeviceType() == null || session.getDeviceType().isEmpty()
                    ? "PHONE"
                    : session.getDeviceType();
            HashMap<String, String> logout = new HashMap<>();
            logout.put("username", username);
            logout.put("apiKeyServer", apiKeyServer);
            logout.put("creationTime", session.getCreationTime());
            logout.put("deviceType", deviceType);
            PushLog.i("HTTP logout POST " + logoutUrl
                    + " creationTime=" + session.getCreationTime()
                    + " deviceType=" + deviceType);
            HttpResult logoutResult = postJson(logoutUrl, logout, headerName, token);
            PushLog.i("HTTP logout code=" + logoutResult.code
                    + " body=" + PushLog.preview(logoutResult.body));
            if (logoutResult.code != 200) {
                PushLog.w("HTTP logout failed code=" + logoutResult.code
                        + " body=" + (logoutResult.body == null ? "null" : logoutResult.body));
                allLoggedOut = false;
            }
        }
        return allLoggedOut;
    }

    private HttpResult postJson(String url, Object object, String headerName, String token) throws IOException {
        String jsonPayload = new Gson().toJson(object);
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaType);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(headerName, token)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client().newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : null;
            return new HttpResult(response.code(), body);
        }
    }

    private static String sessionApiUrl(String action) {
        String enableUrl = Constant.server_Url;
        String marker = "/session-api/";
        int index = enableUrl.indexOf(marker);
        if (index >= 0) {
            return enableUrl.substring(0, index + marker.length()) + action;
        }
        return enableUrl;
    }

    private static String stringField(Object requestObject, String key) {
        if (!(requestObject instanceof Map)) {
            return null;
        }
        Object value = ((Map<?, ?>) requestObject).get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static final class HttpResult {
        final int code;
        final String body;

        HttpResult(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }

    public  String makeApiPostRequestWithObjHeaderSend( Object object, String headerName, String token) throws IOException {
        // Initialize OkHttpClient
        OkHttpClient client = client();
        // Convert object to JSON using Gson
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(object);

        // Create the request body
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaType);

        // Create the request with headers
        Request request = new Request.Builder()
                .url(Constant.send_Url)
                .post(requestBody)
                .addHeader(headerName, token)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
//                throw new IOException("Failed : HTTP error code : " + response.code());
            }

            // Return the response body as a string
            return response.body() != null ? response.body().string() : null;
        }
    }

    public String makeApiPostRequestWithObjHeaderSync(Object object, String headerName, String token) throws IOException {
        // Initialize OkHttpClient
        OkHttpClient client = client();
        // Convert object to JSON using Gson
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(object);

        // Create the request body
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaType);

        // Create the request with headers
        Request request = new Request.Builder()
                .url(Constant.unread_Url)
                .post(requestBody)
                .addHeader(headerName, token)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
//                throw new IOException("Failed : HTTP error code : " + response.code());
            }

            // Return the response body as a string
            return response.body() != null ? response.body().string() : null;
        }
    }

    public String makeApiPostRequestFromData(HashMap<String, String> object) {
        // Initialize OkHttpClient
        OkHttpClient client = client();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        // Build the form body
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : object.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        FormBody formBody = formBodyBuilder.build();
        StringBuilder queryString = new StringBuilder();
        for (HashMap.Entry<String, String> entry : object.entrySet()) {

            if (queryString.length() > 0) {
                queryString.append("&"); // Add & between key-value pairs
            }
            queryString.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }
        RequestBody requestBody = RequestBody.create(mediaType, queryString.toString());

        Request request = new Request.Builder()
                .url(Constant.portal_Url) // Replace with your endpoint
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        // Create the request
//        Request request = new Request.Builder()
//                .url(Constant.portal_Url)
//                .post(requestBody)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();

        // Execute the request
        PushLog.i("HTTP portal POST " + Constant.portal_Url);
        try (Response response = client.newCall(request).execute()) {
            Handshake handshake = response.handshake();
            if (handshake != null) {
                PushLog.i("HTTP portal TLS tls=" + handshake.tlsVersion()
                        + " cipher=" + handshake.cipherSuite()
                        + " peerCerts=" + handshake.peerCertificates().size());
            } else {
                PushLog.w("HTTP portal TLS handshake=null (unexpected for https)");
            }
            String body = response.body() != null ? response.body().string() : null;
            PushLog.i("HTTP portal code=" + response.code()
                    + " ok=" + response.isSuccessful()
                    + " body=" + PushLog.preview(body));
            if (!response.isSuccessful()) {
                PushLog.w("HTTP portal body=" + (body == null ? "null" : body));
            }
            return body;
        } catch (IOException e) {
            PushLog.e("HTTP portal failed url=" + Constant.portal_Url
                    + " clientSslConfigured=" + (client.sslSocketFactory() != null)
                    + " err=" + PushLog.errorSummary(e), e);
            Throwable c = e.getCause();
            int depth = 0;
            while (c != null && depth < 6) {
                PushLog.e("HTTP portal cause[" + depth + "] "
                        + c.getClass().getName() + ": " + c.getMessage());
                c = c.getCause();
                depth++;
            }
            return "";
        }
    }
}
