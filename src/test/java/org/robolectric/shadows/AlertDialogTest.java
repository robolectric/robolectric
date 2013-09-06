package org.robolectric.shadows;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AlertDialogTest {

  @Test @Config(emulateSdk = 16)
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
    assertThat(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getVisibility()).isEqualTo(View.GONE);
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

    alert.setMessage("new message");
    assertThat(shadowAlertDialog.getMessage()).isEqualTo("new message");

    alert.setMessage(null);
    assertThat(shadowAlertDialog.getMessage()).isEqualTo("");
  }

  @Test
  public void shouldSetMessageFromResourceId() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(application);
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

  @Test @Ignore("this seems to no longer be true...")
  public void shouldSetThePositiveButtonAfterCreation() throws Exception {
    final AlertDialog alertDialog = new AlertDialog.Builder(application)
      .setPositiveButton("Positive", null).create();
    shadowOf(alertDialog).callOnCreate(null);

    TestDialogOnClickListener listener = new TestDialogOnClickListener();
    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "More Positive", listener);

    final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    positiveButton.performClick();

    assertThat(positiveButton.getText().toString()).isEqualTo("More Positive");
    listener.assertEventsSoFar("clicked on " + AlertDialog.BUTTON_POSITIVE);
  }

  @Test @Ignore("this seems to no longer be true...")
  public void shouldSetTheNegativeButtonAfterCreation() throws Exception {
    final AlertDialog alertDialog = new AlertDialog.Builder(application)
      .setNegativeButton("Negative", null).create();

    TestDialogOnClickListener listener = new TestDialogOnClickListener();
    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "More Negative", listener);

    final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
    negativeButton.performClick();

    assertThat(negativeButton.getText().toString()).isEqualTo("More Negative");
    listener.assertEventsSoFar("clicked on " + AlertDialog.BUTTON_NEGATIVE);
  }

  @Test @Ignore("this seems to no longer be true...")
  public void shouldSetTheNeutralButtonAfterCreation() throws Exception {
    final AlertDialog alertDialog = new AlertDialog.Builder(application)
      .setNegativeButton("Neutral", null).create();

    TestDialogOnClickListener listener = new TestDialogOnClickListener();
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Still Neutral", listener);

    final Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
    neutralButton.performClick();

    assertThat(neutralButton.getText().toString()).isEqualTo("Still Neutral");
    listener.assertEventsSoFar("clicked on " + AlertDialog.BUTTON_NEUTRAL);
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

//  @Test
//  public void testBuilderWithItemArrayCanPerformClickOnItem() throws Exception {
//    TestDialogOnClickListener listener = new TestDialogOnClickListener();
//    AlertDialog alert = new AlertDialog.Builder(new ContextWrapper(Robolectric.application))
//        .setItems(R.array.alertDialogTestItems, listener)
//        .create();
//
//    alert.show();
//    ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
//    shadowAlertDialog.clickOnItem(1);
//
//
//  }

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

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void shouldReturnTheIndexOfTheCheckedItemInASingleChoiceDialog() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

    builder.setSingleChoiceItems(new String[]{"foo", "bar"}, 1, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
      }
    });
    ItemListener itemListener = new ItemListener();
    builder.setOnItemSelectedListener(itemListener);
    AlertDialog alert = builder.create();
    alert.show();

    assertThat(alert.isShowing()).isTrue();

    shadowOf(alert).clickOnItem(0);
    itemListener.assertEventsSoFar("selected foo");
    assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(alert);
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
  @Test
  public void shouldCallTheClickListenerOfTheCheckedItemInASingleChoiceDialog() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(Robolectric.application));

    TestDialogOnClickListener listener = new TestDialogOnClickListener();
    builder.setSingleChoiceItems(new String[]{"foo", "bar"}, 1, listener);

    ItemListener itemListener = new ItemListener();
    builder.setOnItemSelectedListener(itemListener);
    AlertDialog alert = builder.create();
    alert.show();

    ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
    shadowAlertDialog.clickOnItem(0);
    listener.assertEventsSoFar("clicked on 0");
    itemListener.assertEventsSoFar("selected foo");

    shadowAlertDialog.clickOnItem(1);
    listener.assertEventsSoFar("clicked on 1");
    itemListener.assertEventsSoFar("selected bar");

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
    listener.assertEventsSoFar("clicked on 0");

    shadowAlertDialog.clickOnItem(1);
    listener.assertEventsSoFar("clicked on 1");

  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
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

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void shouldReturnACustomFrameLayout() {
    AlertDialog dialog = new AlertDialog.Builder(Robolectric.application).create();

    assertThat(dialog.findViewById(android.R.id.custom)).isNotNull();
    assertThat(dialog.findViewById(android.R.id.custom)).isInstanceOf(FrameLayout.class);
    assertThat(dialog.findViewById(android.R.id.custom)).isSameAs(dialog.findViewById(android.R.id.custom));

  }

  @Test
  public void shouldNotExplodeWhenNestingAlerts() throws Exception {
    final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    final AlertDialog nestedDialog = new AlertDialog.Builder(activity)
        .setTitle("Dialog 2")
        .setMessage("Another dialog")
        .setPositiveButton("OK", null)
        .create();

    final AlertDialog dialog = new AlertDialog.Builder(activity)
        .setTitle("Dialog 1")
        .setMessage("A dialog")
        .setPositiveButton("Button 1", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            nestedDialog.show();
          }
        }).create();

    dialog.show();
    assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialog);

    dialog.getButton(Dialog.BUTTON_POSITIVE).performClick();
    assertThat(ShadowDialog.getLatestDialog()).isEqualTo(nestedDialog);
  }


  private static class TestDialogOnClickListener extends Transcript implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int item) {
      add("clicked on " + item);
    }
  }

  private static class ItemListener extends Transcript implements AdapterView.OnItemSelectedListener {
    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      add("selected " + Robolectric.innerText(view));
    }

    @Override public void onNothingSelected(AdapterView<?> parent) {
      add("selected none");
    }
  }

}
