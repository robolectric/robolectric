package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.ProgressDialog;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        TestOnCancelListener cancelListener = new TestOnCancelListener();
        ProgressDialog progressDialog = ProgressDialog.show(context, "Title", "Message", true, true, cancelListener);
        ShadowProgressDialog shadowProgressDialog = shadowOf(progressDialog);
        assertThat(shadowProgressDialog.getContext(), is(context));
        assertThat(shadowProgressDialog.getMessage(), equalTo("Message"));
        assertTrue(shadowProgressDialog.isIndeterminate());
        assertTrue(shadowProgressDialog.isCancelable());

        progressDialog.cancel();
        assertThat(cancelListener.onCancelDialogInterface, is((DialogInterface) progressDialog));
    }

    @Test
    public void showWithoutCancellableAndCancellableListener_shouldCreateAProgressDialog() {
        Context context = new Activity();
        ProgressDialog progressDialog = ProgressDialog.show(context, "Title", "Message", true);
        ShadowProgressDialog shadowProgressDialog = shadowOf(progressDialog);
        assertThat(shadowProgressDialog.getContext(), is(context));
        assertThat(shadowProgressDialog.getMessage(), equalTo("Message"));
        assertTrue(shadowProgressDialog.isIndeterminate());
        assertFalse(shadowProgressDialog.isCancelable());
    }

    @Test
    public void showWithoutIndeterminateAndCancellableAndCancellableListener_shouldCreateAProgressDialog() {
        Context context = new Activity();
        ProgressDialog progressDialog = ProgressDialog.show(context, "Title", "Message");
        ShadowProgressDialog shadowProgressDialog = shadowOf(progressDialog);
        assertThat(shadowProgressDialog.getContext(), is(context));
        assertThat(shadowProgressDialog.getMessage(), equalTo("Message"));
        assertFalse(shadowProgressDialog.isIndeterminate());
        assertFalse(shadowProgressDialog.isCancelable());
    }

    private static class TestOnCancelListener implements DialogInterface.OnCancelListener {
        public DialogInterface onCancelDialogInterface;

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            onCancelDialogInterface = dialogInterface;

        }
    }
}
