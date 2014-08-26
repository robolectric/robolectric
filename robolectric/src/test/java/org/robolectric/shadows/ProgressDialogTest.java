package org.robolectric.shadows;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.concurrent.Callable;

import static junit.framework.Assert.assertNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ProgressDialogTest {

  private ProgressDialog dialog;
  private ShadowProgressDialog shadow;

  @Before
  public void setUp() {
    dialog = new ProgressDialog(Robolectric.application);
    shadow = Robolectric.shadowOf(dialog);
  }

  @Test
  public void shouldExtendAlertDialog() {
    assertThat(shadow).isInstanceOf(ShadowAlertDialog.class);
  }

  @Test
  public void shouldPutTheMessageIntoTheView() {
    String message = "This is only a test";
    shadow.callOnCreate(null);

    View dialogView = shadow.getView();
    assertThat(shadowOf(dialogView).innerText()).doesNotContain(message);
    dialog.setMessage(message);
    assertThat(shadowOf(shadow.getView()).innerText()).contains(message);
  }

  @Test
  public void shouldSetIndeterminate() {
    assertThat(dialog.isIndeterminate()).isFalse();

    dialog.setIndeterminate(true);
    assertThat(dialog.isIndeterminate()).isTrue();

    dialog.setIndeterminate(false);
    assertThat(dialog.isIndeterminate()).isFalse();
  }

  @Test
  public void shouldSetMax() {
    assertThat(dialog.getMax()).isEqualTo(0);

    dialog.setMax(41);
    assertThat(dialog.getMax()).isEqualTo(41);
  }

  @Test
  public void shouldSetProgress() {
    assertThat(dialog.getProgress()).isEqualTo(0);

    dialog.setProgress(42);
    assertThat(dialog.getProgress()).isEqualTo(42);
  }

  @Ignore("ProgressDialog is kinda busted") @Test // todo 2.0-cleanup
  public void show_shouldCreateAProgressDialog() {
    Context context = new Activity();
    TestOnCancelListener cancelListener = new TestOnCancelListener();
    ProgressDialog progressDialog = ProgressDialog.show(context, "Title", "Message", true, true, cancelListener);
    ShadowProgressDialog shadowProgressDialog = shadowOf(progressDialog);
    assertThat(progressDialog.getContext()).isSameAs(context);
    assertThat(shadowProgressDialog.getMessage()).isEqualTo("Message");
    assertTrue(progressDialog.isIndeterminate());
    assertTrue(shadowProgressDialog.isCancelable());

    progressDialog.cancel();
    assertThat(cancelListener.onCancelDialogInterface).isSameAs((DialogInterface) progressDialog);
  }

  @Ignore("ProgressDialog is kinda busted") @Test // todo  2.0-cleanup
  public void show_setsLatestAlertDialogAndLatestDialog_3args() throws Exception {
    assertLatestDialogsSet("Title", "Message", false, false, null, new Callable<ProgressDialog>() {
      @Override
      public ProgressDialog call() throws Exception {
        return ProgressDialog.show(Robolectric.application, "Title", "Message");
      }
    }
    );
  }

  @Ignore("ProgressDialog is kinda busted") @Test // todo  2.0-cleanup
  public void show_setsLatestAlertDialogAndLatestDialog_4args() throws Exception {
    assertLatestDialogsSet("Title", "Message", true, false, null, new Callable<ProgressDialog>() {
      @Override
      public ProgressDialog call() throws Exception {
        return ProgressDialog.show(Robolectric.application, "Title", "Message", true);
      }
    });
  }

  @Ignore("ProgressDialog is kinda busted") @Test // todo  2.0-cleanup
  public void show_setsLatestAlertDialogAndLatestDialog_5args() throws Exception {
    assertLatestDialogsSet("Title", "Message", true, true, null, new Callable<ProgressDialog>() {
      @Override
      public ProgressDialog call() throws Exception {
        return ProgressDialog.show(Robolectric.application, "Title", "Message", true, true);
      }
    });
  }

  @Ignore("ProgressDialog is kinda busted") @Test // todo  2.0-cleanup
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
    assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialog);
    assertThat(ShadowAlertDialog.getLatestAlertDialog()).isEqualTo(dialog);

    assertThat(dialog.isIndeterminate()).as("isIndeterminate").isEqualTo(expectedIndeterminate);
    assertThat(shadowOf(dialog).getMessage()).as("message").isEqualTo(expectedMessage);
    assertThat(shadowOf(dialog).getTitle()).as("title").isEqualTo(expectedTitle);
    assertThat(shadowOf(dialog).isCancelable()).as("isCancelable").isEqualTo(expectedCancelable);
    assertThat(shadowOf(dialog).getOnCancelListener()).isEqualTo(expectedCancelListener);
  }

  private static class TestOnCancelListener implements DialogInterface.OnCancelListener {

    public DialogInterface onCancelDialogInterface;

    @Override
    public void onCancel(DialogInterface dialogInterface) {
      onCancelDialogInterface = dialogInterface;
    }
  }
}
