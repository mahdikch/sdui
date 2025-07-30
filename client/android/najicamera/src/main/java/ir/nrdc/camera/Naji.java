package ir.nrdc.camera;

import android.app.Activity;
import android.content.Intent;

import ir.nrdc.camera.com.github.dhaval2404.imagepicker.CameraActivity;
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener;

public class Naji  {
    public Naji() {
    }

    public void openCamera(Activity activity, OnCallBackListener listener, String type, String checkOcr,String iranian) {
        CameraActivity.setListener(listener);
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra("type",type);
        intent.putExtra("iranian",iranian);
        intent.putExtra("checkOcr",checkOcr);
        activity.startActivity(intent);
    }


}
