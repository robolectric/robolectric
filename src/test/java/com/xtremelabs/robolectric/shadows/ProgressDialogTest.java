package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ProgressDialogTest {

    private ProgressDialog dialog;
    private ShadowProgressDialog shadow;

    @Before
    public void setUp() {
        dialog = new ProgressDialog(null);
        shadow = Robolectric.shadowOf(dialog);
    }

    @Test
    public void shouldExtendAlertDialog() {
        assertThat(shadow, instanceOf(ShadowAlertDialog.class));
    }

    @Test
    public void shouldSetMessage() {
        CharSequence message = "This is only a test";

        assertThat(shadow.getMessage(), nullValue());
        dialog.setMessage(message);
        assertThat((CharSequence) shadow.getMessage(), equalTo(message));
    }

    @Test
    public void shouldSetIndeterminate() {
        assertThat(dialog.isIndeterminate(), equalTo(false));

        dialog.setIndeterminate(true);
        assertThat(dialog.isIndeterminate(), equalTo(true));

        dialog.setIndeterminate(false);
        assertThat(dialog.isIndeterminate(), equalTo(false));
    }

    @Test
    public void show_shouldCreateAProgressDialog() {
        Context context = new Activity();
        String title = "Title";
        String message = "Message";
        boolean indeterminate = true;
        boolean cancellable = true;

        TestOnCancelListener cancelListener = new TestOnCancelListener();
        ProgressDialog progressDialog = ProgressDialog.show(context, title, message, indeterminate, cancellable, cancelListener);

        assertProgressDialogWith(context, title, message, indeterminate, cancellable, progressDialog);

        progressDialog.cancel();
        assertThat(cancelListener.onCancelDialogInterface, is((DialogInterface) progressDialog));
    }

    @Test
    public void showWithoutCancellableAndCancellableListener_shouldCreateAProgressDialog() {
        Context context = new Activity();
        String title = "Title";
        String message = "Message";
        boolean indeterminate = true;
        boolean notCancellable = false;

        ProgressDialog progressDialog = ProgressDialog.show(context, title, message, indeterminate);

        assertProgressDialogWith(context, title, message, indeterminate, notCancellable, progressDialog);
    }

    @Test
    public void showWithoutIndeterminateAndCancellableAndCancellableListener_shouldCreateAProgressDialog() {
        Context context = new Activity();
        String title = "Title";
        String message = "Message";
        boolean determinate = false;
        boolean nonCancellable = false;

        ProgressDialog progressDialog = ProgressDialog.show(context, title, message);

        assertProgressDialogWith(context, title, message, determinate, nonCancellable, progressDialog);
    }

    private static class TestOnCancelListener implements DialogInterface.OnCancelListener {
        public DialogInterface onCancelDialogInterface;

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            onCancelDialogInterface = dialogInterface;
        }
    }

    private void assertProgressDialogWith(Context context, String title, String message, boolean indeterminate, boolean cancellable, ProgressDialog progressDialog) {
        ShadowProgressDialog shadowProgressDialog = shadowOf(progressDialog);
        assertThat(shadowProgressDialog.getContext(), is(context));
        assertThat(shadowProgressDialog.getTitle(), is(title));
        assertThat(shadowProgressDialog.getMessage(), equalTo(message));
        assertThat(shadowProgressDialog.isIndeterminate(), is(indeterminate));
        assertThat(shadowProgressDialog.isCancelable(), is(cancellable));
    }
}
