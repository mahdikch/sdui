package ir.naja.pushsdk.android;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ApiCall {
    Object result = null;
    public static final MediaType mediaType = MediaType.get("application/json;charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public Object makeApiGetRequest(String data) {

        Request request = new Request.Builder().url(Constant.server_Url + data).get().build();
        client.newCall(request).enqueue(new Callback() {
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
        client.newCall(request).enqueue(new Callback() {
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
        // Initialize OkHttpClient
        OkHttpClient client = new OkHttpClient();
        // Convert object to JSON using Gson
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(object);

        // Create the request body
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaType);

        // Create the request with headers
        Request request = new Request.Builder()
                .url(Constant.server_Url)
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

    public  String makeApiPostRequestWithObjHeaderSend( Object object, String headerName, String token) throws IOException {
        // Initialize OkHttpClient
        OkHttpClient client = new OkHttpClient();
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
        OkHttpClient client = new OkHttpClient();
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
        OkHttpClient client = new OkHttpClient();
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
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
//                throw new IOException("Failed : HTTP error code : " + response.code());
            }

            // Return the response body as a string
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
//            throw new RuntimeException(e);
            return "";
        }
    }
}
