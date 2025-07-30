package ir.nrdc.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

//import com.github.dhaval2404.imagepicker.ImagePicker;

//import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ir.nrdc.camera.com.github.dhaval2404.imagepicker.CameraActivity;
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.*;
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener;


//import ir.nrdc.camera.com.github.dhaval2404.imagepicker.CameraActivity;
//import ir.nrdc.camera.com.github.dhaval2404.imagepicker.ImagePicker;
//import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener;

/**
 * Compress Selected/Captured Image
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2019
 */
public class CompressionProvider extends BaseProvider {

    private static final String TAG = CompressionProvider.class.getSimpleName();

    private final int mMaxWidth;
    private final int mMaxHeight;
    private final long mMaxFileSize;
    private String imageType;
    private String iranian;


    private final File mFileDir;

    public CompressionProvider(CameraActivity activity, OnCallBackListener listener) {
        super(activity,listener);

        Bundle bundle = activity.getIntent().getExtras() != null ? activity.getIntent().getExtras() : new Bundle();

        // Get Max Width/Height parameter from Intent
        mMaxWidth = bundle.getInt("extra.max_width", 0);
        mMaxHeight = bundle.getInt("extra.max_height", 0);

        // Get Maximum Allowed file size
        mMaxFileSize = bundle.getLong("extra.image_max_size", 0);

        // Get File Directory
        String fileDir = bundle.getString("extra.save_directory");
        mFileDir = getFileDir(fileDir);
    }

    public CompressionProvider(CameraActivity activity,OnCallBackListener listener, int mMaxWidth, int mMaxHeight, long mMaxFileSize, String mFileDir,String imageType,String iranian) {
        super(activity,listener);
        this.mMaxWidth = mMaxWidth;
        this.mMaxHeight = mMaxHeight;
        this.mMaxFileSize = mMaxFileSize;
        this.imageType = imageType;
        this.iranian = iranian;
        this.mFileDir = getFileDir(mFileDir);;
    }

    /**
     * Check if compression should be enabled or not
     *
     * @return Boolean. True if Compression should be enabled else false.
     */
    private boolean isCompressEnabled() {
        return mMaxFileSize > 0L;
    }

    /**
     * Check if compression is required
     * @param file File object to apply Compression
     */
    private boolean isCompressionRequired(File file) {
        boolean status = isCompressEnabled() && getSizeDiff(file) > 0L;
        if (!status && mMaxWidth > 0 && mMaxHeight > 0) {
            // Check image resolution
            FileUtil.Pair<Integer, Integer> resolution = FileUtil.getImageResolution(file);
            return resolution.first > mMaxWidth || resolution.second > mMaxHeight;
        }
        return status;
    }

    /**
     * Check if compression is required
     * @param uri Uri object to apply Compression
     */
    public boolean isCompressionRequired(Uri uri) {
        boolean status = isCompressEnabled() && getSizeDiff(uri) > 0L;
        if (!status && mMaxWidth > 0 && mMaxHeight > 0) {
            // Check image resolution
            FileUtil.Pair<Integer, Integer> resolution = FileUtil.getImageResolution(this, uri);
            return resolution.first > mMaxWidth || resolution.second > mMaxHeight;
        }
        return status;
    }

    private long getSizeDiff(File file) {
        return file.length() - mMaxFileSize;
    }

    private long getSizeDiff(Uri uri) {
        long length = FileUtil.getImageSize(this, uri);
        return length - mMaxFileSize;
    }

    /**
     * Compress given file if enabled.
     *
     * @param uri Uri to compress
     */
    public void compress(Uri uri,int type) {
        startCompressionWorker(uri,type);
    }

    /**
     * Start Compression in Background
     */
    @SuppressLint("StaticFieldLeak")
    private void startCompressionWorker(final Uri uri, int type) {
        new AsyncTask<Uri, Void, File>() {
            @Override
            protected File doInBackground(Uri... params) {
                // Perform operation in background
                File file = FileUtil.getTempFile(CompressionProvider.this, params[0]);
                try {
                    return file != null ? startCompression(file) : null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                if (file != null) {
                    // Post Result
                    handleResult(file,type);
                } else {
                    // Post Error
                    setError(R.string.error_failed_to_compress_image);
                }
            }
        }.execute(uri);
    }

    /**
     * Check if compression required, And Apply compression until file size reach below Max Size.
     */
    private File startCompression(File file) throws IOException {
        File newFile = null;
        int attempt = 0;
        int lastAttempt = 0;
        do {
            // Delete file if exist, fill will be exist in second loop.
            if (newFile != null) {
                newFile.delete();
            }

            newFile = applyCompression(file, attempt);
            if (newFile == null) {
                return attempt > 0 ? applyCompression(file, lastAttempt) : null;
            }
            lastAttempt = attempt;

            if (mMaxFileSize > 0) {
                long diff = getSizeDiff(newFile);
                attempt += diff > 1024 * 1024 ? 3 :
                        (diff > 500 * 1024 ? 2 : 1);
            } else {
                attempt++;
            }
        } while (isCompressionRequired(newFile));

        // Copy Exif Data
        ExifDataCopier.copyExif(file, newFile);

        return newFile;
    }

    /**
     * Compress the file
     */
    private File applyCompression(File file, int attempt) throws IOException {
        int[] resolution = resolutionList().get(attempt);
        int maxWidth = resolution[0];
        int maxHeight = resolution[1];

        if (mMaxWidth > 0 && mMaxHeight > 0) {
            if (maxWidth > mMaxWidth || maxHeight > mMaxHeight) {
                maxHeight = mMaxHeight;
                maxWidth = mMaxWidth;
            }
        }

        // Check file format
        Bitmap.CompressFormat format = file.getAbsolutePath().endsWith(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

        String extension = FileUtil.getImageExtension(file);
        File compressFile = FileUtil.getImageFile(mFileDir, extension);
        return compressFile != null ? ImageUtil.compressImage(file, maxWidth, maxHeight, format, compressFile.getAbsolutePath()) : null;
    }

    /**
     * Image Resolution will be reduce with below parameters.
     *
     */
    private List<int[]> resolutionList() {
        List<int[]> list = new ArrayList<>();
        list.add(new int[]{2448, 3264}); // 8.0 Megapixel
        list.add(new int[]{2008, 3032}); // 6.0 Megapixel
        list.add(new int[]{1944, 2580}); // 5.0 Megapixel
        list.add(new int[]{1680, 2240}); // 4.0 Megapixel
        list.add(new int[]{1536, 2048}); // 3.0 Megapixel
        list.add(new int[]{1200, 1600}); // 2.0 Megapixel
        list.add(new int[]{1024, 1392}); // 1.3 Megapixel
        list.add(new int[]{960, 1280}); // 1.0 Megapixel
        list.add(new int[]{768, 1024}); // 0.7 Megapixel
        list.add(new int[]{600, 800}); // 0.4 Megapixel
        list.add(new int[]{480, 640}); // 0.3 Megapixel
        list.add(new int[]{240, 320}); // 0.15 Megapixel
        list.add(new int[]{120, 160}); // 0.08 Megapixel
        list.add(new int[]{60, 80}); // 0.04 Megapixel
        list.add(new int[]{30, 40}); // 0.02 Megapixel
        return list;
    }

    /**
     * This method will be called when final result for this provider is enabled.
     */
    private void handleResult(File file, int type) {
//        activity.setCompressedImage(Uri.fromFile(file));
        Intent intent = new Intent();
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        intent.putExtra("extra.file_path", FileUriUtils.getRealPath(this, uri));
        if(type==1){
            try {
                String imageMain= Utility.getBase64FromUri(this, uri);
                listener.getImageMainCompress(imageMain,imageType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else if(type==2){
            String imageMain= null;
            try {
                imageMain = Utility.getBase64FromUri(this, uri);
                listener.getImageCroppedCompress(imageMain,imageType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        activity.setCompressImagePlate(Activity.RESULT_OK, intent);
    }
}
