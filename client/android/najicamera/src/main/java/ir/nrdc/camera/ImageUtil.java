package ir.nrdc.camera;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    public static File compressImage(
            File imageFile,
            float reqWidth,
            float reqHeight,
            Bitmap.CompressFormat compressFormat,
            String destinationPath
    ) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            // Write the compressed bitmap at the destination specified by destinationPath
            Bitmap bitmap = decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight);
            if (bitmap != null) {
                bitmap.compress(compressFormat, 100, fileOutputStream);
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        return new File(destinationPath);
    }

    private static Bitmap decodeSampledBitmapFromFile(
            File imageFile,
            float reqWidth,
            float reqHeight
    ) {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = (float) actualWidth / actualHeight;
        float maxRatio = reqWidth / reqHeight;

        if (actualHeight > reqHeight || actualWidth > reqWidth) {
            // If Height is greater
            if (imgRatio < maxRatio) {
                imgRatio = reqHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) reqHeight;
            } // If Width is greater
            else if (imgRatio > maxRatio) {
                imgRatio = reqWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) reqWidth;
            } else {
                actualHeight = (int) reqHeight;
                actualWidth = (int) reqWidth;
            }
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;

        if (bmp != null && canUseForInBitmap(bmp, options)) {
            // inBitmap only works with mutable bitmaps, so force the decoder to
            // return mutable bitmaps.
            options.inMutable = true;
            options.inBitmap = bmp;
        }
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        Bitmap scaledBitmap = null;
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        if (scaledBitmap != null) {
            float ratioX = (float) actualWidth / options.outWidth;
            float ratioY = (float) actualHeight / options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                    middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            bmp.recycle();

            Matrix matrix = new Matrix();
            scaledBitmap = Bitmap.createBitmap(
                    scaledBitmap, 0, 0, scaledBitmap.getWidth(),
                    scaledBitmap.getHeight(), matrix, true
            );
        }

        return scaledBitmap;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options,
            int reqWidth,
            int reqHeight
    ) {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static boolean canUseForInBitmap(
            Bitmap candidate,
            BitmapFactory.Options targetOptions
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        } else {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth &&
                    candidate.getHeight() == targetOptions.outHeight &&
                    targetOptions.inSampleSize == 1;
        }
    }

    private static int getBytesPerPixel(Bitmap.Config config) {
        switch (config) {
            case ARGB_8888:
                return 4;
            case RGB_565:
            case ARGB_4444:
                return 2;
            case ALPHA_8:
                return 1;
            default:
                return 1;
        }
    }
}
