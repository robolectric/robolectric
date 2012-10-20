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

import java.util.concurrent.Callable;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        assertThat(shadow.getMessage(), equalTo(message));
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
    public void shouldSetMax() {
        assertThat(dialog.getMax(), equalTo(0));
        assertThat(shadow.getMax(), equalTo(0));

        dialog.setMax(41);
        assertThat(dialog.getMax(), equalTo(41));
        assertThat(shadow.getMax(), equalTo(41));
    }

    @Test
    public void shouldSetProgress() {
        assertThat(dialog.getProgress(), equalTo(0));
        assertThat(shadow.getProgress(), equalTo(0));

        dialog.setProgress(42);
        assertThat(dialog.getProgress(), equalTo(42));
        assertThat(shadow.getProgress(), equalTo(42));
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
    public void show_setsLatestAlertDialogAndLatestDialog_3args() throws Exception {
        assertLatestDialogsSet("Title", "Message", false, false, null, new Callable<ProgressDialog>() {
            @Override
            public ProgressDialog call() throws Exception {
                return ProgressDialog.show(Robolectric.application, "Title", "Message");
            }
        }
        );
    }

    @Test
    public void show_setsLatestAlertDialogAndLatestDialog_4args() throws Exception {
        assertLatestDialogsSet("Title", "Message", true, false, null, new Callable<ProgressDialog>() {
            @Override
            public ProgressDialog call() throws Exception {
                return ProgressDialog.show(Robolectric.application, "Title", "Message", true);
            }
        });
    }

    @Test
    public void show_setsLatestAlertDialogAndLatestDialog_5args() throws Exception {
        assertLatestDialogsSet("Title", "Message", true, true, null, new Callable<ProgressDialog>() {
            @Override
            public ProgressDialog call() throws Exception {
                return ProgressDialog.show(Robolectric.application, "Title", "Message", true, true);
            }
        });
    }

    @Test
    public void show_setsLatestAlertDialogAndLatestDialog_6args() throws Exception {
        final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        };

        assertLatestDialogsSet("Title", "Message", true, true, cancelListener, new Callable<ProgressDialog>() {
            @Override
            public ProgressDialog call() throws Exception {
                return ProgressDialog.show(Robolectric.application, "Title", "Message", true, true, cancelListener);
            }
        });
    }

    private void assertLatestDialogsSet(CharSequence expectedTitle, CharSequence expectedMessage, boolean expectedIndeterminate,
            boolean expectedCancelable, DialogInterface.OnCancelListener expectedCancelListener,
            Callable<ProgressDialog> callable) throws Exception {
        assertNull(ShadowDialog.getLatestDialog());
        assertNull(ShadowAlertDialog.getLatestAlertDialog());

        dialog = callable.call();

        assertNotNull(dialog);
        assertEquals(dialog, ShadowDialog.getLatestDialog());
        assertEquals(dialog, ShadowAlertDialog.getLatestAlertDialog());

        assertEquals(expectedIndeterminate, dialog.isIndeterminate());
        assertEquals(expectedMessage, shadowOf(dialog).getMessage());
        assertEquals(expectedTitle, shadowOf(dialog).getTitle());
        assertEquals(expectedCancelable, shadowOf(dialog).isCancelable());
        assertEquals(expectedCancelListener, shadowOf(dialog).getOnCancelListener());
    }

    private static class TestOnCancelListener implements DialogInterface.OnCancelListener {

        public DialogInterface onCancelDialogInterface;

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            onCancelDialogInterface = dialogInterface;
        }
    }
}
