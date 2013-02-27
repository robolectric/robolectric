package org.robolectric;

import android.view.View;
import org.robolectric.internal.Implements;
import org.robolectric.shadows.ShadowView;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TemporaryBindingsTest {

    @Test
    public void overridingShadowBindingsShouldNotAffectBindingsInLaterTests() throws Exception {
        assertThat(shadowOf(new View(null)).getClass().getSimpleName()).isEqualTo(ShadowView.class.getSimpleName());

        Robolectric.bindShadowClass(TemporaryShadowView.class);

        assertThat(Robolectric.shadowOf_(new View(null)).getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
    }

    @Test
    public void overridingShadowBindingsShouldNotAffectBindingsInLaterTestsAgain() throws Exception {
        assertThat(shadowOf(new View(null)).getClass().getSimpleName()).isEqualTo(ShadowView.class.getSimpleName());

        Robolectric.bindShadowClass(TemporaryShadowView.class);

        assertThat(Robolectric.shadowOf_(new View(null)).getClass().getSimpleName()).isEqualTo(TemporaryShadowView.class.getSimpleName());
    }

    @Implements(View.class)
    public static class TemporaryShadowView {
    }
}
