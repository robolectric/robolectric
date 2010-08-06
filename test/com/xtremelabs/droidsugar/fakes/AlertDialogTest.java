package com.xtremelabs.droidsugar.fakes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner.proxyFor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class AlertDialogTest {
    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Dialog.class, FakeDialog.class);
        DroidSugarAndroidTestRunner.addProxy(AlertDialog.class, FakeAlertDialog.class);
        DroidSugarAndroidTestRunner.addProxy(AlertDialog.Builder.class, FakeAlertDialog.FakeBuilder.class);
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
