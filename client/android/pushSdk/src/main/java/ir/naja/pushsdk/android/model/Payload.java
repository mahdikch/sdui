package ir.naja.pushsdk.android.model;

import com.google.gson.Gson;

public class Payload {
    private String title;
    private Object body;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Payload() {
    }

    public Payload(String title, Object body) {
        this.title = title;
        this.body = body;
    }
}
