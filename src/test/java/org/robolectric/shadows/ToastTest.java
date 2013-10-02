package org.robolectric.shadows;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ToastTest {
  @Test
  public void shouldHaveShortDuration() throws Exception {
    Toast toast = Toast.makeText(new Activity(), "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_SHORT);
  }

  @Test
  public void shouldHaveLongDuration() throws Exception {
    Toast toast = Toast.makeText(new Activity(), "long toast", Toast.LENGTH_LONG);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_LONG);
  }

  @Test
  public void shouldSetTextCorrectly() throws Exception {
    Toast toast = Toast.makeText(new Activity(), "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_SHORT);
    toast.show();
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast);
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("short toast");
    assertThat(ShadowToast.showedToast("short toast")).isTrue();
  }

  @Test
  public void shouldSetViewCorrectly() throws Exception {
    final Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().get();
    Toast toast = new Toast(activity);
    toast.setDuration(Toast.LENGTH_SHORT);
    final View view = new TextView(activity);
    toast.setView(view);
    assertThat(toast.getView()).isSameAs(view);
  }

  @Test
  public void shouldSetGravityCorrectly() throws Exception {
    Toast toast = Toast.makeText(new Activity(), "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    toast.setGravity(Gravity.CENTER, 0, 0);
    assertThat(toast.getGravity()).isEqualTo(Gravity.CENTER);
  }

  @Test
  public void shouldCountToastsCorrectly() throws Exception {
    assertThat(ShadowToast.shownToastCount()).isEqualTo(0);
    Toast toast = Toast.makeText(new Activity(), "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    toast.show();
    toast.show();
    toast.show();
    assertThat(ShadowToast.shownToastCount()).isEqualTo(3);
    ShadowToast.reset();
    assertThat(ShadowToast.shownToastCount()).isEqualTo(0);
    toast.show();
    toast.show();
    assertThat(ShadowToast.shownToastCount()).isEqualTo(2);
  }
}
