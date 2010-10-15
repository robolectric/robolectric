package com.xtremelabs.robolectric.fakes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.RobolectricAndroidTestRunner.proxyFor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricAndroidTestRunner.class)
public class AlertDialogTest {
    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addProxy(Dialog.class, FakeDialog.class);
        RobolectricAndroidTestRunner.addProxy(AlertDialog.class, FakeAlertDialog.class);
        RobolectricAndroidTestRunner.addProxy(AlertDialog.Builder.class, FakeAlertDialog.FakeBuilder.class);
    }

    @Test
    public void testBuilder() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        builder.setTitle("title")
                .setMessage("message");
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing(), equalTo(true));

        FakeAlertDialog fakeAlertDialog = (FakeAlertDialog) proxyFor(alert);
        assertThat(fakeAlertDialog.title, equalTo("title"));
        assertThat(fakeAlertDialog.message, equalTo("message"));
        assertThat(FakeAlertDialog.latestAlertDialog, sameInstance(fakeAlertDialog));
    }
}
