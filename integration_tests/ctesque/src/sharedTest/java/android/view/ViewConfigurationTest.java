package android.view;

import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests that {@link android.view.ViewConfiguration} behavior is consistent with real Android. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public final class ViewConfigurationTest {

  private float density;

  @Before
  public void setUp() {
    density =
        ApplicationProvider.getApplicationContext().getResources().getDisplayMetrics().density;
  }

  @Test
  public void scrollbar_configuration() {
    ViewConfiguration viewConfiguration =
        ViewConfiguration.get(ApplicationProvider.getApplicationContext());
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

  public int pxToDp(int px) {
    return Math.round(px / density);
  }
}
