package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowToastTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void shouldHaveShortDuration() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_SHORT);
  }

  @Test
  public void shouldHaveLongDuration() throws Exception {
    Toast toast = Toast.makeText(context, "long toast", Toast.LENGTH_LONG);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_LONG);
  }

  @Test
  public void shouldMakeTextCorrectly() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    assertThat(toast.getDuration()).isEqualTo(Toast.LENGTH_SHORT);
    toast.show();
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast);
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("short toast");
    assertThat(ShadowToast.showedToast("short toast")).isTrue();
  }

  @Test
  public void shouldSetTextCorrectly() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    toast.setText("other toast");
    toast.show();
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast);
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("other toast");
    assertThat(ShadowToast.showedToast("other toast")).isTrue();
  }

  @Test
  public void shouldSetTextWithIdCorrectly() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    toast.setText(R.string.hello);
    toast.show();
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast);
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Hello");
    assertThat(ShadowToast.showedToast("Hello")).isTrue();
  }

  @Test
  public void shouldSetViewCorrectly() throws Exception {
    Toast toast = new Toast(context);
    toast.setDuration(Toast.LENGTH_SHORT);
    final View view = new TextView(context);
    toast.setView(view);
    assertThat(toast.getView()).isSameAs(view);
  }

  @Test
  public void shouldSetGravityCorrectly() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    assertThat(toast).isNotNull();
    toast.setGravity(Gravity.CENTER, 0, 0);
    assertThat(toast.getGravity()).isEqualTo(Gravity.CENTER);
  }

  @Test
  public void shouldSetOffsetsCorrectly() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    toast.setGravity(0, 12, 34);
    assertThat(toast.getXOffset()).isEqualTo(12);
    assertThat(toast.getYOffset()).isEqualTo(34);
  }

  @Test
  public void shouldCountToastsCorrectly() throws Exception {
    assertThat(ShadowToast.shownToastCount()).isEqualTo(0);
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
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

  @Test
  public void shouldBeCancelled() throws Exception {
    Toast toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT);
    toast.cancel();
    ShadowToast shadowToast = shadowOf(toast);
    assertThat(shadowToast.isCancelled()).isTrue();
  }
}
