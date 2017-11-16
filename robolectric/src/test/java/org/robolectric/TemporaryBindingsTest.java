package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowView;

@RunWith(RobolectricTestRunner.class)
public class TemporaryBindingsTest {

  @Test
  @Config(shadows = TemporaryShadowView.class)
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTests() throws Exception {
    TemporaryShadowView shadowView = Shadow.extract(new View(RuntimeEnvironment.application));
    assertThat(shadowView.getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
  }

  @Test
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTestsAgain() throws Exception {
    assertThat(shadowOf(new View(RuntimeEnvironment.application)).getClass().getSimpleName()).isEqualTo(ShadowView.class.getSimpleName());
  }

  @Implements(View.class)
  public static class TemporaryShadowView {
  }
}
