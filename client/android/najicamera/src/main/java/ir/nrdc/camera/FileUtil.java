package ir.nrdc.camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    /**
     * Get Image File
     *
     * Default it will take Camera folder as it's directory
     *
     * @param fileDir File Folder in which file needs to be created.
     * @param extension String Image file extension.
     * @return Return Empty file to store camera image.
     * @throws IOException if permission denied or failed to create new file.
     */
    public static File getImageFile(File fileDir, String extension) {
        try {
            // Create an image file name
            String ext = extension != null ? extension : ".jpg";
            String fileName = getFileName();
            String imageFileName = fileName + ext;

            // Create Directory If not exist
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            // Create File Object
            File file = new File(fileDir, imageFileName);

            // Create empty file
            file.createNewFile();

            return file;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String getFileName() {
        return "IMG_" + getTimestamp();
    }

    /**
     * Get Current Time in yyyyMMdd_HHmmssSSS format
     *
     * E.g. 20190130_103020000
     */
    private static String getTimestamp() {
        String timeFormat = "yyyyMMdd_HHmmssSSS";
        return new SimpleDateFormat(timeFormat, Locale.getDefault()).format(new Date());
    }

    /**
     * Get Free Space size
     * @param file directory object to check free space.
     */
    public static long getFreeSpace(File file) {
        StatFs stat = new StatFs(file.getPath());
        long availBlocks = stat.getAvailableBlocksLong();
        long blockSize = stat.getBlockSizeLong();
        return availBlocks * blockSize;
    }

    /**
     * Get Image Width & Height from Uri
     *
     * @param context Context to get Content Resolver
     * @param uri Uri to get Image Size
     * @return Pair where Index 0 has width and Index 1 has height
     */
    public static Pair<Integer, Integer> getImageResolution(Context context, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (FileInputStream stream = (FileInputStream) context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(options.outWidth, options.outHeight);
    }

    /**
     * Get Image Width & Height from File
     *
     * @param file File to get Image Size
     * @return Pair where Index 0 has width and Index 1 has height
     */
    public static Pair<Integer, Integer> getImageResolution(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return new Pair<>(options.outWidth, options.outHeight);
    }

    /**
     * Get Image File Size
     *
     * @param context Context to get Content Resolver
     * @param uri Uri to get Image Size
     * @return Image File Size
     */
    public static long getImageSize(Context context, Uri uri) {
        DocumentFile documentFile = getDocumentFile(context, uri);
        return documentFile != null ? documentFile.length() : 0;
    }

    /**
     * Create copy of Uri into application specific local path
     *
     * @param context Application Context
     * @param uri Source Uri
     * @return File return copy of Uri object
     */
    public static File getTempFile(Context context, Uri uri) {
        try {
            File destination = new File(context.getCacheDir(), "image_picker.png");

            try (FileInputStream src = new FileInputStream(context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor());
                 FileOutputStream dst = new FileOutputStream(destination)) {
                dst.getChannel().transferFrom(src.getChannel(), 0, src.getChannel().size());
            }

            return destination;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get DocumentFile from Uri
     *
     * @param context Application Context
     * @param uri Source Uri
     * @return DocumentFile return DocumentFile from Uri
     */
    public static DocumentFile getDocumentFile(Context context, Uri uri) {
        if (isFileUri(uri)) {
            String path = FileUriUtils.getRealPath(context, uri);
            if (path != null) {
                return DocumentFile.fromFile(new File(path));
            }
        }
        return DocumentFile.fromSingleUri(context, uri);
    }

    /**
     * Get Bitmap Compress Format
     *
     * @param extension Image File Extension
     * @return Bitmap CompressFormat
     */
    public static Bitmap.CompressFormat getCompressFormat(String extension) {
        if (extension != null && extension.toLowerCase().contains("png")) {
            return Bitmap.CompressFormat.PNG;
        } else if (extension != null && extension.toLowerCase().contains("webp")) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? Bitmap.CompressFormat.WEBP_LOSSLESS : Bitmap.CompressFormat.WEBP;
        } else {
            return Bitmap.CompressFormat.JPEG;
        }
    }

    /**
     * Get Image Extension i.e. .png, .jpg
     *
     * @return extension of image with dot, or default .jpg if it none.
     */
    public static String getImageExtension(File file) {
        return getImageExtension(Uri.fromFile(file));
    }

    /**
     * Get Image Extension i.e. .png, .jpg
     *
     * @return extension of image with dot, or default .jpg if it none.
     */
    public static String getImageExtension(Uri uriImage) {
        String extension = null;

        try {
            String imagePath = uriImage.getPath();
            if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
                extension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
            }
        } catch (Exception e) {
            extension = null;
        }

        if (extension == null || extension.isEmpty()) {
            // default extension for matches the previous behavior of the plugin
            extension = "jpg";
        }

        return "." + extension;
    }

    /**
     * Check if provided URI is backed by File
     *
     * @return Boolean, True if Uri is local file object else return false
     */
    private static boolean isFileUri(Uri uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    // Assuming Pair is a simple container class
    public static class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}

