package org.robolectric.shadows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AlertDialogTest {

    @Test
    public void testBuilder() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        builder.setTitle("title").setMessage("message");
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing()).isTrue();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertEquals("title", shadowAlertDialog.getTitle());
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("message");
        assertThat(shadowAlertDialog.isCancelable()).isTrue();
        assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog())).isSameAs(shadowAlertDialog);
        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(alert);
    }

    @Test
    public void nullTitleAndMessageAreOkay() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application) //
                .setTitle(null) //
                .setMessage(null);
        ShadowAlertDialog shadowAlertDialog = shadowOf(builder.create());
        assertThat(shadowAlertDialog.getTitle().toString()).isEqualTo("");
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("");
    }
    
    @Test
    public void getLatestAlertDialog_shouldReturnARealAlertDialog() throws Exception {
        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isNull();

        AlertDialog dialog = new AlertDialog.Builder(application).show();
        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(dialog);
    }

    @Test
    public void shouldOnlyCreateRequestedButtons() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        assertThat(shadowOf(dialog).getButton(AlertDialog.BUTTON_POSITIVE)).isNotNull();
        assertThat(shadowOf(dialog).getButton(AlertDialog.BUTTON_NEGATIVE)).isNull();
    }

    @Test
    public void shouldAllowNullButtonListeners() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        Robolectric.clickOn(dialog.getButton(AlertDialog.BUTTON_POSITIVE));
    }

    @Test
    public void testSetMessageAfterCreation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        builder.setTitle("title").setMessage("message");
        AlertDialog alert = builder.create();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("message");

        shadowAlertDialog.setMessage("new message");
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("new message");

        shadowAlertDialog.setMessage(null);
        assertThat(shadowAlertDialog.getMessage()).isNull();
    }

    @Test
    public void shouldSetMessageFromResourceId() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new Activity());
        builder.setTitle("title").setMessage(R.string.hello);

        AlertDialog alert = builder.create();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage()).isEqualTo("Hello");
    }

    @Test
    public void shouldSetView() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        EditText view = new EditText(application);
        builder.setView(view);

        AlertDialog alert = builder.create();
        assertThat(shadowOf(alert).getView()).isEqualTo(view);
    }

    @Test
    public void shouldSetCustomTitleView() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        View view = new View(application);
        assertThat(builder.setCustomTitle(view)).isSameAs(builder);

        AlertDialog alert = builder.create();
        assertThat(shadowOf(alert).getCustomTitleView()).isEqualTo(view);
    }
    
    @Test
    public void shouldSetThePositiveButtonAfterCreation() throws Exception {
        final AlertDialog alertDialog = new AlertDialog.Builder(application)
            .setPositiveButton("Positive", null).create();
        
        TestDialogOnClickListener listener = new TestDialogOnClickListener();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "More Positive", listener);
        
        final Button positiveButton = shadowOf(alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.performClick();

        assertThat(positiveButton.getText().toString()).isEqualTo("More Positive");
        assertThat(listener.clickedItem).isEqualTo(AlertDialog.BUTTON_POSITIVE);
    }
    
    @Test
    public void shouldSetTheNegativeButtonAfterCreation() throws Exception {
        final AlertDialog alertDialog = new AlertDialog.Builder(application)
            .setNegativeButton("Negative", null).create();
        
        TestDialogOnClickListener listener = new TestDialogOnClickListener();
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "More Negative", listener);
        
        final Button negativeButton = shadowOf(alertDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.performClick();

        assertThat(negativeButton.getText().toString()).isEqualTo("More Negative");
        assertThat(listener.clickedItem).isEqualTo(AlertDialog.BUTTON_NEGATIVE);
    }
    
    @Test
    public void shouldSetTheNeutralButtonAfterCreation() throws Exception {
        final AlertDialog alertDialog = new AlertDialog.Builder(application)
            .setNegativeButton("Neutral", null).create();
        
        TestDialogOnClickListener listener = new TestDialogOnClickListener();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Still Neutral", listener);
        
        final Button neutralButton = shadowOf(alertDialog).getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.performClick();

        assertThat(neutralButton.getText().toString()).isEqualTo("Still Neutral");
        assertThat(listener.clickedItem).isEqualTo(AlertDialog.BUTTON_NEUTRAL);
    }

    @Test
    public void clickingPositiveButtonDismissesDialog() throws Exception {
        AlertDialog alertDialog = new AlertDialog.Builder(application)
                .setPositiveButton("Positive", null).create();
        alertDialog.show();

        assertTrue(alertDialog.isShowing());
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertFalse(alertDialog.isShowing());
    }

    @Test
    public void clickingNeutralButtonDismissesDialog() throws Exception {
        AlertDialog alertDialog = new AlertDialog.Builder(application)
                .setNeutralButton("Neutral", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        alertDialog.show();

        assertTrue(alertDialog.isShowing());
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();
        assertFalse(alertDialog.isShowing());
    }

    @Test
    public void clickingNegativeButtonDismissesDialog() throws Exception {
        AlertDialog alertDialog = new AlertDialog.Builder(application)
                .setNegativeButton("Negative", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        alertDialog.show();

        assertTrue(alertDialog.isShowing());
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        assertFalse(alertDialog.isShowing());
    }

    @Test
    public void testBuilderWithItemArrayViaResourceId() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

        builder.setTitle("title");
        builder.setItems(R.array.alertDialogTestItems, new TestDialogOnClickListener());
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing()).isTrue();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getTitle().toString()).isEqualTo("title");
        assertThat(shadowAlertDialog.getItems().length).isEqualTo(2);
        assertEquals(shadowAlertDialog.getItems()[0], "Aloha");
        assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog())).isSameAs(shadowAlertDialog);
        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(alert);
    }


//    @Test
//    public void testBuilderWithItemArrayCanPerformClickOnItem() throws Exception {
//        TestDialogOnClickListener listener = new TestDialogOnClickListener();
//        AlertDialog alert = new AlertDialog.Builder(new ContextWrapper(Robolectric.application))
//                .setItems(R.array.alertDialogTestItems, listener)
//                .create();
//
//        alert.show();
//        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
//        shadowAlertDialog.clickOnItem(1);
//
//
//    }

    @Test
    public void testBuilderWithAdapter() throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        list.add(99);
        list.add(88);
        list.add(77);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(Robolectric.application, R.layout.main, R.id.title, list);

        AlertDialog.Builder builder = new AlertDialog.Builder(application);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        assertTrue(alert.isShowing());
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertEquals(shadowAlertDialog.getAdapter().getCount(), 3);
        assertEquals(shadowAlertDialog.getAdapter().getItem(0), 99);
    }

    @Test
    public void show_setsLatestAlertDialogAndLatestDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(Robolectric.application).create();
        assertNull(ShadowDialog.getLatestDialog());
        assertNull(ShadowAlertDialog.getLatestAlertDialog());

        alertDialog.show();

        assertEquals(alertDialog, ShadowDialog.getLatestDialog());
        assertEquals(alertDialog, ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void shouldReturnTheIndexOfTheCheckedItemInASingleChoiceDialog() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

        builder.setSingleChoiceItems(new String[]{"foo", "bar"}, 1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        assertThat(alert.isShowing()).isTrue();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertEquals(shadowAlertDialog.getCheckedItemIndex(), 1);
        assertEquals(shadowAlertDialog.getItems()[0], "foo");
        assertThat(shadowAlertDialog.getItems().length).isEqualTo(2);
        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(alert);
    }

    @Test
    public void shouldCallTheClickListenerOfTheCheckedItemInASingleChoiceDialog() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

        TestDialogOnClickListener listener = new TestDialogOnClickListener();
        builder.setSingleChoiceItems(new String[]{"foo", "bar"}, 1, listener);

        AlertDialog alert = builder.create();
        alert.show();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        shadowAlertDialog.clickOnItem(0);
        assertThat(listener.clickedItem).isEqualTo(0);
        assertThat(shadowAlertDialog.getCheckedItemIndex()).isEqualTo(0);

        shadowAlertDialog.clickOnItem(1);
        assertThat(listener.clickedItem).isEqualTo(1);
        assertThat(shadowAlertDialog.getCheckedItemIndex()).isEqualTo(1);

    }

    @Test
    public void shouldCallTheClickListenerOfTheCheckedAdapterInASingleChoiceDialog() throws Exception {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

        TestDialogOnClickListener listener = new TestDialogOnClickListener();
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(Robolectric.application, R.layout.main, R.id.title, list);
        builder.setSingleChoiceItems(arrayAdapter, 1, listener);

        AlertDialog alert = builder.create();
        alert.show();

        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        shadowAlertDialog.clickOnItem(0);
        assertThat(listener.clickedItem).isEqualTo(0);
        assertThat(shadowAlertDialog.getCheckedItemIndex()).isEqualTo(0);

        shadowAlertDialog.clickOnItem(1);
        assertThat(listener.clickedItem).isEqualTo(1);
        assertThat(shadowAlertDialog.getCheckedItemIndex()).isEqualTo(1);

    }

    @Test
    public void shouldFindViewsByIdIfAViewIsSet() throws Exception {
        AlertDialog dialog = new AlertDialog.Builder(application).create();

        assertThat(dialog.findViewById(99)).isNull();

        View view = new View(application);
        view.setId(99);
        dialog.setView(view);
        assertThat(dialog.findViewById(99)).isSameAs(view);

        assertThat(dialog.findViewById(66)).isNull();
    }

    @Test
    public void shouldDelegateToDialogFindViewByIdIfViewIsNull() {
        AlertDialog dialog = new AlertDialog(Robolectric.application) {
        };

        assertThat(dialog.findViewById(99)).isNull();

        dialog.setContentView(R.layout.main);
        assertNotNull(dialog.findViewById(R.id.title));
    }
    
    @Test
    public void shouldReturnACustomFrameLayout() {
        AlertDialog dialog = new AlertDialog.Builder(Robolectric.application).create();

        assertThat(dialog.findViewById(android.R.id.custom)).isNotNull();
        assertThat(dialog.findViewById(android.R.id.custom)).isInstanceOf(FrameLayout.class);
        assertThat(dialog.findViewById(android.R.id.custom)).isSameAs(dialog.findViewById(android.R.id.custom));
    
    }


    private static class TestDialogOnClickListener implements DialogInterface.OnClickListener {
        private DialogInterface dialog;
        private int clickedItem;

        public void onClick(DialogInterface dialog, int item) {
            this.dialog = dialog;
            this.clickedItem = item;
        }
    }
}
