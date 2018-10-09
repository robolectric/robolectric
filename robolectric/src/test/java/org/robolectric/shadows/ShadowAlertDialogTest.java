package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.CustomView;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowAlertDialogTest {

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
    assertThat(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldAllowNullButtonListeners() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(application);
    builder.setPositiveButton("OK", null);
    AlertDialog dialog = builder.create();
    dialog.show();
    ShadowView.clickOn(dialog.getButton(AlertDialog.BUTTON_POSITIVE));
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
  @Config(minSdk = LOLLIPOP)
  public void shouldSetView_withLayoutId() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(application);
    builder.setView(R.layout.custom_layout);

    AlertDialog alert = builder.create();
    View view = shadowOf(alert).getView();
    assertThat(view.getClass()).isEqualTo(CustomView.class);
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
    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(RuntimeEnvironment.application));

    builder.setTitle("title");
    builder.setItems(R.array.alertDialogTestItems, new TestDialogOnClickListener());
    AlertDialog alert = builder.create();
    alert.show();

    assertThat(alert.isShowing()).isTrue();

    ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
    assertThat(shadowAlertDialog.getTitle().toString()).isEqualTo("title");
    assertThat(shadowAlertDialog.getItems().length).isEqualTo(2);
    assertThat(shadowAlertDialog.getItems()[0]).isEqualTo("Aloha");
    assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog())).isSameAs(shadowAlertDialog);
    assertThat(ShadowAlertDialog.getLatestAlertDialog()).isSameAs(alert);
  }

  @Test
  public void testBuilderWithAdapter() throws Exception {
    List<Integer> list = new ArrayList<>();
    list.add(99);
    list.add(88);
    list.add(77);
    ArrayAdapter<Integer> adapter = new ArrayAdapter<>(RuntimeEnvironment.application, R.layout.main, R.id.title, list);

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
    assertThat(shadowAlertDialog.getAdapter().getCount()).isEqualTo(3);
    assertThat(shadowAlertDialog.getAdapter().getItem(0)).isEqualTo(99);
  }

  @Test
  public void show_setsLatestAlertDialogAndLatestDialog() {
    AlertDialog alertDialog = new AlertDialog.Builder(RuntimeEnvironment.application).create();
    assertNull(ShadowDialog.getLatestDialog());
    assertNull(ShadowAlertDialog.getLatestAlertDialog());

    alertDialog.show();

    assertEquals(alertDialog, ShadowDialog.getLatestDialog());
    assertEquals(alertDialog, ShadowAlertDialog.getLatestAlertDialog());
  }

  @Test
  public void shouldCallTheClickListenerOfTheCheckedAdapterInASingleChoiceDialog() throws Exception {
    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextWrapper(RuntimeEnvironment.application));

    TestDialogOnClickListener listener = new TestDialogOnClickListener();
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<>(RuntimeEnvironment.application, R.layout.main, R.id.title, list);
    builder.setSingleChoiceItems(arrayAdapter, 1, listener);

    AlertDialog alert = builder.create();
    alert.show();

    ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
    shadowAlertDialog.clickOnItem(0);
    assertThat(listener.transcript).containsExactly("clicked on 0");
    listener.transcript.clear();

    shadowAlertDialog.clickOnItem(1);
    assertThat(listener.transcript).containsExactly("clicked on 1");

  }

  @Test
  public void shouldDelegateToDialogFindViewByIdIfViewIsNull() {
    AlertDialog dialog = new AlertDialog(RuntimeEnvironment.application) {
    };

    assertThat((View) dialog.findViewById(99)).isNull();

    dialog.setContentView(R.layout.main);
    assertNotNull(dialog.findViewById(R.id.title));
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

  @Test
  public void alertControllerShouldStoreCorrectIconIdFromBuilder() {
    AlertDialog.Builder builder = new AlertDialog.Builder(application);
    builder.setIcon(R.drawable.an_image);

    AlertDialog alertDialog = builder.create();
    assertThat(shadowOf(alertDialog).getIconId()).isEqualTo(R.drawable.an_image);
  }

  private static class TestDialogOnClickListener implements DialogInterface.OnClickListener {
    private final ArrayList<String> transcript = new ArrayList<>();

    @Override
    public void onClick(DialogInterface dialog, int item) {
      transcript.add("clicked on " + item);
    }
  }
}
