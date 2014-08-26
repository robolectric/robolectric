package org.robolectric;

import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowView;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class TemporaryBindingsTest {

  @Test
  @Config(shadows = TemporaryShadowView.class)
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTests() throws Exception {
//        assertThat(shadowOf(new View(Robolectric.application)).getClass().getSimpleName()).isEqualTo(ShadowView.class.getSimpleName());
    assertThat(Robolectric.shadowOf_(new View(Robolectric.application)).getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
  }

  @Test
//    @Values(shadows = TemporaryShadowView.class)
  public void overridingShadowBindingsShouldNotAffectBindingsInLaterTestsAgain() throws Exception {
// todo test this properly
    assertThat(shadowOf(new View(Robolectric.application)).getClass().getSimpleName()).isEqualTo(ShadowView.class.getSimpleName());
//        assertThat(Robolectric.shadowOf_(new View(Robolectric.application)).getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
  }

  @Implements(View.class)
  public static class TemporaryShadowView {
  }
}
