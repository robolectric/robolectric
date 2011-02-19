package com.xtremelabs.robolectric.shadows;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.widget.Button;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(WithTestDefaultsRunner.class)
public class AlertDialogTest {

    /** Takes advantage of ShadowWrangler's ability to redirect to a shadow method that takes a CharSequence
     * when an int (resource id) method is actually called.
     * @see com.xtremelabs.robolectric.bytecode.ShadowWranglerWithDefaultsTest
     */
    @Test
    public void testBuilderWithPositiveButtonViaResourceId() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(null));
        DialogInterface.OnClickListener mockListener = mock(DialogInterface.OnClickListener.class);
        builder.setPositiveButton(R.string.hello, mockListener);

        AlertDialog alert = builder.create();
        Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        assertThat(button.getText().toString(), equalTo("Hello"));
        button.performClick();
        verify(mockListener).onClick(alert, AlertDialog.BUTTON_POSITIVE);
    }

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
