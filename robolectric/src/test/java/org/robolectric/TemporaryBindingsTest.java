package org.robolectric;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowView;

@RunWith(AndroidJUnit4.class)
@Config(sdk = O) // running on all SDKs is unnecessary and can cause OOM GC overhead issues
public class TemporaryBindingsTest {

  @Test
  @Config(shadows = TemporaryShadowView.class)
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTests() throws Exception {
    TemporaryShadowView shadowView =
        Shadow.extract(new View(ApplicationProvider.getApplicationContext()));
    assertThat(shadowView.getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
  }

  @Test
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTestsAgain() throws Exception {
    assertThat(
            shadowOf(new View(ApplicationProvider.getApplicationContext()))
                .getClass()
                .getSimpleName())
        .isEqualTo(ShadowView.class.getSimpleName());
  }

  @Implements(View.class)
  public static class TemporaryShadowView {
  }
}
