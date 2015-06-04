package org.robolectric.shadows;

import android.app.ProgressDialog;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowProgressDialogTest {

  private ProgressDialog dialog;
  private ShadowProgressDialog shadow;

  @Before
  public void setUp() {
    dialog = new ProgressDialog(RuntimeEnvironment.application);
    shadow = Shadows.shadowOf(dialog);
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
}
