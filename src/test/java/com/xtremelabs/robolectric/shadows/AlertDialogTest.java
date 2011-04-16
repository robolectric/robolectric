package com.xtremelabs.robolectric.shadows;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AlertDialogTest {

    @Test
    public void testBuilder() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        builder.setTitle("title").setMessage("message");
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing(), equalTo(true));

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getTitle(), equalTo("title"));
        assertThat(shadowAlertDialog.getMessage(), equalTo("message"));
        assertThat(ShadowAlertDialog.getLatestAlertDialog(), sameInstance(shadowAlertDialog));
    }

    @Test
    public void testSetMessageAfterCreation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        builder.setTitle("title").setMessage("message");
        AlertDialog alert = builder.create();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage(), equalTo("message"));

        shadowAlertDialog.setMessage("new message");
        assertThat(shadowAlertDialog.getMessage(), equalTo("new message"));

        shadowAlertDialog.setMessage(null);
        assertThat(shadowAlertDialog.getMessage(), nullValue());
    }

    @Test
    public void testBuilderWithItemArrayViaResourceId() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

        builder.setTitle("title");
        builder.setItems(R.array.alertDialogTestItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {

                } else if (item == 1) {

                }
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing(), equalTo(true));

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getTitle(), equalTo("title"));
        assertThat(shadowAlertDialog.getItems().length, equalTo(2));
        assertEquals(shadowAlertDialog.getItems()[0], "Aloha");
        assertThat(ShadowAlertDialog.getLatestAlertDialog(), sameInstance(shadowAlertDialog));
    }
}
