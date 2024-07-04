package android.view;

import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests that {@link android.view.ViewConfiguration} behavior is consistent with real Android. */
@RunWith(AndroidJUnit4.class)
public final class ViewConfigurationTest {

  private float density;
  private ViewConfiguration viewConfiguration;

  @Before
  public void setUp() {
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    metrics.density = 1.5f;
    metrics.densityDpi = 240;
    metrics.setTo(metrics);
    Configuration configuration = resources.getConfiguration();
    configuration.densityDpi = 240;
    configuration.setTo(configuration);
    resources.updateConfiguration(configuration, metrics);
    density =
        ApplicationProvider.getApplicationContext().getResources().getDisplayMetrics().density;
    viewConfiguration = ViewConfiguration.get(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void scrollbar_configuration() {
    int scrollBarSize = ViewConfiguration.getScrollBarSize();
    int scaledScrollBarSizeDp = pxToDp(viewConfiguration.getScaledScrollBarSize());
    if (Build.VERSION.SDK_INT >= O_MR1) {
      assertThat(scrollBarSize).isEqualTo(4);
      assertThat(scaledScrollBarSizeDp).isEqualTo(4);
    } else {
      assertThat(scrollBarSize).isEqualTo(10);
      assertThat(scaledScrollBarSizeDp).isEqualTo(10);
    }
  }

  @Test
  public void isFadingMarqueeEnabled_returnsFalse() throws Exception {
    // isFadingMarqueeEnabled is a '@hide' method.
    boolean isFadingMarqueeEnabled =
        callMethod(viewConfiguration, "isFadingMarqueeEnabled", Boolean.class);
    assertThat(isFadingMarqueeEnabled).isFalse();
  }

  @Config(qualifiers = "hdpi")
  @Test
  public void overfling_distance() {
    assertThat(density).isEqualTo(1.5f);
    assertThat(viewConfiguration.getScaledOverflingDistance()).isEqualTo(9);
  }

  public int pxToDp(int px) {
    return Math.round(px / density);
  }

  public <T> T callMethod(Object obj, String methodName, Class<T> returnType) throws Exception {
    Method method = obj.getClass().getMethod(methodName);
    return returnType.cast(method.invoke(obj));
  }
}
