package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.content.res.Resources;
import android.graphics.drawable.AnimatedImageDrawable;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = P)
public class ShadowNativeAnimatedImageDrawableTest {
  private Resources resources;

  @Before
  public void setup() {
    // The native code behind AnimatedImageDrawable makes use of Linux-specific APIs (epoll),
    // so it doesn't work on Mac at the moment.
    assume().that(OS_NAME.value().toLowerCase(Locale.US)).doesNotContain("mac");
    resources = RuntimeEnvironment.getApplication().getResources();
  }

  @Test
  public void testInflate() throws Exception {
    AnimatedImageDrawable aid = (AnimatedImageDrawable) resources.getDrawable(R.drawable.animated);
    assertThat(aid).isNotNull();
  }
}
