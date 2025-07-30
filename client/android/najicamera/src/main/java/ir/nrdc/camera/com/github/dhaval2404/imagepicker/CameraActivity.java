package ir.nrdc.camera.com.github.dhaval2404.imagepicker;

import static ir.nrdc.camera.Utility.dp2px;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.nrdc.camera.CompressionProvider;
import ir.nrdc.camera.R;
import ir.nrdc.camera.Utility;

public class CameraActivity extends AppCompatActivity  {
    private PreviewView previewView;

    private RelativeLayout cameraView;
    private ImageView imageView;
    private ImageCapture imageCapture;
    private File outputDirectory;
    private String type;
    private String iranian="true";
    private String checkOcr;
    private AppCompatImageView imgTakePhoto;
    private ExecutorService cameraExecutor;
    private AppCompatImageView appCompatImageView;
    private static OnCallBackListener onCallBackListener;


    public static void setListener(OnCallBackListener listener) {
        onCallBackListener = listener;
    }

    private static final String[] REQUESTED_PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_PERMISSION = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naji_camera);
//        OnCallBackListener testInterface = (OnCallBackListener) getIntent().getSerializableExtra("testInterface");
//        setListener(testInterface);
Intent intent=getIntent();
 type=intent.getExtras().getString("type");
 iranian=intent.getExtras().getString("iranian");
 checkOcr=intent.getExtras().getString("checkOcr");
//        LayoutInflater layoutInflater=LayoutInflater.from(this);
//        View root=layoutInflater.inflate(R.layout.activity_naji_camera,null);
        cameraView = findViewById(R.id.cameraView);
        imageView = findViewById(R.id.imageView);
        imgTakePhoto = findViewById(R.id.takePhoto);
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
*/

//        if (allPermissionGranted()) {
            startCamera();
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                ActivityCompat.requestPermissions(
//                        CameraActivity.this,
//                        REQUESTED_PERMISSION,
//                        REQUEST_CODE_PERMISSION
//                );
//            }
//        }

        setDraw();

        // Set the background size


        imgTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    takePhoto();
                }else {
                    takePhoto();
                }
            }
        });

        outputDirectory = getOutputDirectory();
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindCameraUseCases(cameraProvider);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setDraw() {

        appCompatImageView = new AppCompatImageView(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Calculate percentages
        float backgroundPercentageX = 0.90f; // 95% of screen width
        float backgroundPercentageY = 0.30f; // 30% of screen height
if (Objects.equals(type, "person")){
     backgroundPercentageX = 0.70f; // 95% of screen width
         backgroundPercentageY = 0.50f; // 30% of screen height
    }
if (Objects.equals(type, ""))
    imageView.setVisibility(View.GONE);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = (int) (screenWidth * backgroundPercentageX);
        params.height = (int) (screenHeight * backgroundPercentageY);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        appCompatImageView.setLayoutParams(params);
        appCompatImageView.setBackground(getResources().getDrawable(R.drawable.camera_background, this.getTheme()));

        cameraView.addView(appCompatImageView);

        /*previewView.post(new Runnable() {
            @Override
            public void run() {

                float backgroundPercentageX = 0.90f; // 95% of screen width
                float backgroundPercentageY = 0.30f; // 30% of screen height

                previewView.getDrawingCache(true);
                Bitmap bitmap = previewView.getDrawingCache();
                Canvas canvas = new Canvas(bitmap);

                Paint paint = new Paint();
                paint.setColor(Color.rgb(255, 153, 51));
                paint.setStrokeWidth(10F);
                paint.setStyle(Paint.Style.STROKE);

                canvas.drawRoundRect(
                        backgroundPercentageX,
                        backgroundPercentageY,
                        imageView.getWidth() + imageView.getX(),
                        imageView.getHeight() + imageView.getY(),
                        10f,
                        10f,
                        paint
                );

            }
        });*/

    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        //VideoCapture videoCapture = new VideoCapture.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getOutputDirectory() {
        File mediaDir = null;
        if (getExternalMediaDirs().length > 0) {
            mediaDir = new File(getExternalMediaDirs()[0], getResources().getString(R.string.app_name) + ".png");
            mediaDir.mkdirs();
        }

        return (mediaDir != null && mediaDir.exists()) ? mediaDir : getFilesDir();
    }

    @SuppressLint("LongLogTag")
    private void takePhoto() {
        Bitmap bitmap = previewView.getBitmap();
//        Canvas canvas = new Canvas(bitmap);


        Bitmap cropedBitmap = cropBitmap(bitmap, appCompatImageView.getX(), appCompatImageView.getY(), appCompatImageView.getWidth(), appCompatImageView.getHeight());
        /*Toast.makeText(
                this,
                " x : " + imageView.getX() + " y : " + imageView.getY() + " width : " + (imageView.getWidth() + imageView.getX()) + " height : " + (imageView.getHeight() + imageView.getY()),
                Toast.LENGTH_LONG
        ).show();*/


        Log.d("position of imageView : ", " x : " + appCompatImageView.getX() + " y : " + appCompatImageView.getY() + " width : " + (appCompatImageView.getWidth() + appCompatImageView.getX()) + " height : " + (appCompatImageView.getHeight() + appCompatImageView.getY()));

        saveBitmap(bitmap, "capture");

        Uri bitmapUri = saveBitmapAsUri(bitmap);

        Intent resultIntent = new Intent();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cropedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        int targetWidth = cropedBitmap.getWidth() / 4;
        int targetHeight = cropedBitmap.getHeight() / 4;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(cropedBitmap, targetWidth, targetHeight, true);

        Uri bitmapCropUri = saveBitmapAsUri(resizedBitmap);
        try {
            byte[] byteArray = stream.toByteArray();
            resultIntent.putExtra("bitmap", bitmapCropUri.toString());
            resultIntent.putExtra("imageUri", bitmapUri.toString());
            String bitmapCropUriBase64 = Utility.getBase64FromUri(this, bitmapCropUri);
            CompressionProvider compressionProvider = new CompressionProvider(CameraActivity.this, onCallBackListener, 450, 500, 500 * 1024l, null,type,iranian);
            compressionProvider.compress(bitmapUri, 1);
            String imageMain = Utility.getBase64FromUri(this, bitmapUri);
            compressionProvider = new CompressionProvider(CameraActivity.this, onCallBackListener, 450, 500, 500 * 1024l, null,type,iranian);
            compressionProvider.compress(bitmapCropUri, 2);
            onCallBackListener.getImageMainAndCropped(imageMain, bitmapCropUriBase64,type,checkOcr,iranian);
//            listener.getImageMainAndCropped(imageMain, bitmapCropUriBase64);
//            setResult(RESULT_OK, resultIntent);
            finish(); // Close CameraActivity
        } catch (Exception e) {
            Log.e("IMAGE", "takePhoto: " + e.toString());
        }

        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) return;

        File photoFile = new File(
                outputDirectory,
                new SimpleDateFormat("", Locale.US).format(System.currentTimeMillis())
        );

        ImageCapture.OutputFileOptions outputOption = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

    /*imageCapture.takePicture(
        outputOption,
        ContextCompat.getMainExecutor(this),
        new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                String msg = "Photo capture succeeded : " + savedUri;
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                Log.d(TAG, msg);
            }

            @Override
            public void onError(ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed " + exception.getMessage(), exception);
            }
        }
    );*/
    }

    public static Bitmap cropBitmap(Bitmap sourceBitmap, float x, float y, float width, float height) {
        if (sourceBitmap == null) {
            Log.e("ImageUtils", "Source bitmap is null");
            return null;
        }

        int bitmapWidth = sourceBitmap.getWidth();
        int bitmapHeight = sourceBitmap.getHeight();

        // Ensure the cropping rectangle is within the bounds of the original bitmap
        int cropX = Math.max((int) x, 0);
        int cropY = Math.max((int) y, 0);
        int cropWidth = Math.min((int) width, bitmapWidth - cropX);
        int cropHeight = Math.min((int) height, bitmapHeight - cropY);

        // Check if the width and height are valid
        if (cropWidth <= 0 || cropHeight <= 0) {
            Log.e("ImageUtils", "Invalid cropping dimensions");
            return null;
        }

        try {
            // Create the cropped bitmap
            return Bitmap.createBitmap(sourceBitmap, cropX, cropY, cropWidth, cropHeight);
        } catch (Exception e) {
            Log.e("ImageUtils", "Error cropping bitmap", e);
            return null;
        }
    }

    private String saveBitmap(Bitmap bmp, String plate) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File fileSignature = new File(Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name));
        if (!fileSignature.exists()) fileSignature.mkdir();
        return createImageAndPath(bytes, plate);
    }

    public Uri saveBitmapAsUri(Bitmap bitmap) {
        String fileName = "bitmap_" + System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    public Uri saveBitmapCropAsUri(Bitmap bitmap) {
        String fileName = "bitmap_" + System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image_crop/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }


    private String createImageAndPath(ByteArrayOutputStream bytes, String plate) {
        try {
            File f = new File(Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name) + File.separator + plate + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            return f.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean allPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (String permission : REQUESTED_PERMISSION) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public void showToast(String msg) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView txtMsg = layout.findViewById(R.id.txt);
        txtMsg.setText(msg);
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.BOTTOM, 0, dp2px(100));
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
//                Toast.makeText(currentActivity,message , Toast.LENGTH_LONG).show();
    }

    public void setCompressImagePlate(int resultOk, Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}