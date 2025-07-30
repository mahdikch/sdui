package ir.nrdc.camera;


import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ExifDataCopier {

    private static final List<String> ATTRIBUTES = Arrays.asList(
            "FNumber",
            "ExposureTime",
            "ISOSpeedRatings",
            "GPSAltitude",
            "GPSAltitudeRef",
            "FocalLength",
            "GPSDateStamp",
            "WhiteBalance",
            "GPSProcessingMethod",
            "GPSTimeStamp",
            "DateTime",
            "Flash",
            "GPSLatitude",
            "GPSLatitudeRef",
            "GPSLongitude",
            "GPSLongitudeRef",
            "Make",
            "Model",
            "Orientation"
    );

    public static void copyExif(File filePathOri, File filePathDest) {
        try {
            ExifInterface oldExif = new ExifInterface(filePathOri.getAbsolutePath());
            ExifInterface newExif = new ExifInterface(filePathDest.getAbsolutePath());

            for (String attribute : ATTRIBUTES) {
                setIfNotNull(oldExif, newExif, attribute);
            }
            newExif.saveAttributes();
        } catch (IOException e) {
            Log.e("ExifDataCopier", "Error preserving Exif data on selected image: " + e);
        }
    }

    private static void setIfNotNull(ExifInterface oldExif, ExifInterface newExif, String property) {
        String value = oldExif.getAttribute(property);
        if (value != null) {
            newExif.setAttribute(property, value);
        }
    }
}
