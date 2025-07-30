package ir.nrdc.camera;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUriUtils {

    public static String getRealPath(Context context, Uri uri) {
        String path = getPathFromLocalUri(context, uri);
        if (path == null) {
            path = getPathFromRemoteUri(context, uri);
        }
        return path;
    }

    private static String getPathFromLocalUri(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    return split.length > 1 ? Environment.getExternalStorageDirectory() + "/" + split[1] : Environment.getExternalStorageDirectory() + "/";
                } else {
                    String path = "storage/" + docId.replace(":", "/");
                    File file = new File(path);
                    if (file.exists()) {
                        return path;
                    } else {
                        return "/storage/sdcard/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                return getDownloadDocument(context, uri);
            } else if (isMediaDocument(uri)) {
                return getMediaDocument(context, uri);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            return isGooglePhotosUri(uri) ? uri.getLastPathSegment() : getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String column = "_data";
        String[] projection = {column};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String getDownloadDocument(Context context, Uri uri) {
        String fileName = getFilePath(context, uri);
        if (fileName != null) {
            String path = Environment.getExternalStorageDirectory() + "/Download/" + fileName;
            if (new File(path).exists()) {
                return path;
            }
        }

        String id = DocumentsContract.getDocumentId(uri);
        if (id.contains(":")) {
            id = id.split(":")[1];
        }
        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
        return getDataColumn(context, contentUri, null, null);
    }

    private static String getMediaDocument(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String selection = "_id=?";
        String[] selectionArgs = {split[1]};

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    private static String getFilePath(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        }
        return null;
    }

    private static String getPathFromRemoteUri(Context context, Uri uri) {
        File file = null;
        boolean success = false;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file = FileUtil.getImageFile(context.getCacheDir(), FileUtil.getImageExtension(uri)))) {
            if (inputStream != null) {
                byte[] buffer = new byte[4 * 1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                success = true;
            }
        } catch (IOException ignored) {
        }
        return success ? file.getPath() : null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

