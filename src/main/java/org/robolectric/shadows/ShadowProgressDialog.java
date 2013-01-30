package com.xtremelabs.robolectric.shadows;


import android.app.ProgressDialog;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {

    private boolean indeterminate;
    private int max;
    private int progress;

    @Implementation
    public static ProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    @Implementation
    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    @Implementation
    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    @Implementation
    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, ProgressDialog.OnCancelListener onCancelListener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(indeterminate);
        progressDialog.setCancelable(cancelable);
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.show();
        Robolectric.getShadowApplication().setLatestAlertDialog(shadowOf(progressDialog));
        return progressDialog;
    }

    @Implementation
    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }

    @Implementation
    public boolean isIndeterminate() {
        return indeterminate;
    }

    @Implementation
    public void setMax(int max) {
        this.max = max;
    }

    @Implementation
    public int getMax() {
        return max;
    }

    @Implementation
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Implementation
    public int getProgress() {
        return progress;
    }
}
