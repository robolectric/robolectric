package com.xtremelabs.robolectric.shadows;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.DogfoodRobolectricTestRunner.shadowOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class AlertDialogTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(Dialog.class, ShadowDialog.class);
        DogfoodRobolectricTestRunner.addProxy(AlertDialog.class, ShadowAlertDialog.class);
        DogfoodRobolectricTestRunner.addProxy(AlertDialog.Builder.class, ShadowAlertDialog.ShadowBuilder.class);
    }

    @Test
    public void testBuilder() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        builder.setTitle("title")
                .setMessage("message");
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing(), equalTo(true));

        ShadowAlertDialog shadowAlertDialog = (ShadowAlertDialog) shadowOf(alert);
        assertThat(shadowAlertDialog.title, equalTo("title"));
        assertThat(shadowAlertDialog.message, equalTo("message"));
        assertThat(ShadowAlertDialog.latestAlertDialog, sameInstance(shadowAlertDialog));
    }
}
