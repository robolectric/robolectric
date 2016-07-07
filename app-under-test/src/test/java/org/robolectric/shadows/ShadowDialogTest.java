package org.robolectric.shadows;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.Transcript;

import static junit.framework.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.TestUtil.assertInstanceOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowDialogTest {
  @Test
  public void shouldCallOnDismissListener() throws Exception {
    final Transcript transcript = new Transcript();

    final Dialog dialog = new Dialog(RuntimeEnvironment.application);
    dialog.show();
    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialogInListener) {
        assertThat((Dialog) dialogInListener).isSameAs(dialog);
        transcript.add("onDismiss called!");
      }
    });

    dialog.dismiss();

    transcript.assertEventsSoFar("onDismiss called!");
  }

  @Test
  public void setContentViewWithViewAllowsFindById() throws Exception {
    final int viewId = 1234;
    Context context = RuntimeEnvironment.application;
    final Dialog dialog = new Dialog(context);
    final View view = new View(context);
    view.setId(viewId);
    dialog.setContentView(view);

    assertSame(view, dialog.findViewById(viewId));
  }

  @Test
  public void shouldGetLayoutInflater() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    assertNotNull(dialog.getLayoutInflater());
  }

  @Test
  public void shouldCallOnStartFromShow() {
    TestDialog dialog = new TestDialog(RuntimeEnvironment.application);
    dialog.show();

    assertTrue(dialog.onStartCalled);
  }

  @Test
  public void shouldSetCancelable() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    ShadowDialog shadow = shadowOf(dialog);

    dialog.setCancelable(false);
    assertThat(shadow.isCancelable()).isFalse();
  }

  @Test
  public void shouldDismissTheRealDialogWhenCancelled() throws Exception {
    TestDialog dialog = new TestDialog(RuntimeEnvironment.application);
    dialog.cancel();
    assertThat(dialog.wasDismissed).isTrue();
  }

  @Test
  public void shouldDefaultCancelableToTrueAsTheSDKDoes() throws Exception {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    ShadowDialog shadow = shadowOf(dialog);

    assertThat(shadow.isCancelable()).isTrue();
  }

  @Test
  public void shouldOnlyCallOnCreateOnce() {
    final Transcript transcript = new Transcript();

    Dialog dialog = new Dialog(RuntimeEnvironment.application) {
      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transcript.add("onCreate called");
      }
    };

    dialog.show();
    transcript.assertEventsSoFar("onCreate called");

    dialog.dismiss();
    dialog.show();
    transcript.assertNoEventsSoFar();
  }

  @Test
  public void show_setsLatestDialog() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    assertNull(ShadowDialog.getLatestDialog());

    dialog.show();

    assertSame(dialog, ShadowDialog.getLatestDialog());
    assertNull(ShadowAlertDialog.getLatestAlertDialog());
  }

  @Test
  public void getLatestDialog_shouldReturnARealDialog() throws Exception {
    assertThat(ShadowDialog.getLatestDialog()).isNull();

    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    dialog.show();
    assertThat(ShadowDialog.getLatestDialog()).isSameAs(dialog);
  }

  @Test
  public void shouldKeepListOfOpenedDialogs() throws Exception {
    assertEquals(0, ShadowDialog.getShownDialogs().size());

    TestDialog dialog = new TestDialog(RuntimeEnvironment.application);
    dialog.show();

    assertEquals(1, ShadowDialog.getShownDialogs().size());
    assertEquals(dialog, ShadowDialog.getShownDialogs().get(0));

    TestDialog dialog2 = new TestDialog(RuntimeEnvironment.application);
    dialog2.show();

    assertEquals(2, ShadowDialog.getShownDialogs().size());
    assertEquals(dialog2, ShadowDialog.getShownDialogs().get(1));

    dialog.dismiss();

    assertEquals(2, ShadowDialog.getShownDialogs().size());

    ShadowDialog.reset();

    assertEquals(0, ShadowDialog.getShownDialogs().size());
  }

  @Test
  public void shouldPopulateListOfRecentDialogsInCorrectOrder() throws Exception {
    new NestingTestDialog().show();

    assertEquals(TestDialog.class, ShadowDialog.getLatestDialog().getClass());
  }

  @Test
  public void shouldFindViewsWithinAContentViewThatWasPreviouslySet() throws Exception {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    dialog.setContentView(dialog.getLayoutInflater().inflate(R.layout.main, null));
    assertInstanceOf(TextView.class, dialog.findViewById(R.id.title));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void show_shouldWorkWithAPI19() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    dialog.show();
  }

  @Test
  public void canSetAndGetOnCancelListener() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);
    DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {

      }
    };
    dialog.setOnCancelListener(onCancelListener);
    assertThat(onCancelListener).isSameAs(shadowOf(dialog).getOnCancelListener());
  }

  private static class TestDialog extends Dialog {
    boolean onStartCalled = false;
    boolean wasDismissed = false;

    public TestDialog(Context context) {
      super(context);
    }

    @Override
    protected void onStart() {
      onStartCalled = true;
    }

    @Override public void dismiss() {
      super.dismiss();
      wasDismissed = true;
    }
  }

  private static class NestingTestDialog extends Dialog {
    public NestingTestDialog() {
      super(RuntimeEnvironment.application);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      new TestDialog(getContext()).show();
    }
  }
}
