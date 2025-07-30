package ir.nrdc.camera;


import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

import ir.nrdc.camera.com.github.dhaval2404.imagepicker.CameraActivity;
import ir.nrdc.camera.com.github.dhaval2404.imagepicker.OnCallBackListener;

/**
 * Abstract Provider class
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2019
 */
public abstract class BaseProvider extends ContextWrapper {

    protected final CameraActivity activity;
    protected final OnCallBackListener listener;

    public BaseProvider(CameraActivity activity,OnCallBackListener listener) {
        super(activity);
        this.activity = activity;
        this.listener = listener;
    }

    public File getFileDir(String path) {
        if (path != null) {
            return new File(path);
        } else {
            File externalDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
            return (externalDir != null) ? externalDir : activity.getFilesDir();
        }
    }

    /**
     * Cancel operation and set error message
     *
     * @param error Error message
     */
    protected void setError(String error) {
        onFailure();
        activity.showToast(error);
    }

    /**
     * Cancel operation and set error message
     *
     * @param errorRes Error message resource ID
     */
    protected void setError(int errorRes) {
        setError(getString(errorRes));
    }

    /**
     * Call this method when task is canceled in between the operation.
     * E.g. user hit back-press
     */
    protected void setResultCancel() {
        onFailure();
        activity.showToast("resultCancel");
    }

    /**
     * This method will be called on error; it can be used for cleanup tasks.
     */
    protected void onFailure() {
        // Default implementation does nothing; subclasses can override
    }

    /**
     * Save all appropriate provider state.
     */
    public void onSaveInstanceState(Bundle outState) {
        // Default implementation does nothing; subclasses can override
    }

    /**
     * Restores the saved state for all Providers.
     *
     * @param savedInstanceState the Bundle returned by {@link #onSaveInstanceState()}
     * @see #onSaveInstanceState()
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Default implementation does nothing; subclasses can override
    }
}
