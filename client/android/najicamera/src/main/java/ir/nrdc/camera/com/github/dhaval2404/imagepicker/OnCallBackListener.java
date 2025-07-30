package ir.nrdc.camera.com.github.dhaval2404.imagepicker;

import java.io.Serializable;

public interface OnCallBackListener extends Serializable {
    void getImageMainAndCropped(String bitmapMain, String imageUriCrop,String type,String checkOcr,String iranian);

    void getImageMainCompress(String bitmapMainCompress,String type);

    void getImageCroppedCompress(String imageUriCropCompress,String type);

}
