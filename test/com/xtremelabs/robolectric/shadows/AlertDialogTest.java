package com.xtremelabs.robolectric.shadows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AlertDialogTest {
    @Before
    public void setUp() throws Exception {
        Robolectric.bindShadowClass(Dialog.class, ShadowDialog.class);
        Robolectric.bindShadowClass(AlertDialog.class, ShadowAlertDialog.class);
        Robolectric.bindShadowClass(AlertDialog.Builder.class, ShadowAlertDialog.ShadowBuilder.class);
    }

    @Test
    public void testBuilder() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        builder.setTitle("title")
                .setMessage("message");
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing(), equalTo(true));

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.title, equalTo("title"));
        assertThat(shadowAlertDialog.message, equalTo("message"));
        assertThat(ShadowAlertDialog.latestAlertDialog, sameInstance(shadowAlertDialog));
    }
}
