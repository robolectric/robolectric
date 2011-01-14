package com.xtremelabs.robolectric;

import android.view.View;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowView;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TemporaryBindingsTest {

    @Test
    public void overridingShadowBindingsShouldNotAffectBindingsInLaterTests() throws Exception {
        assertThat(shadowOf(new View(null)).getClass().getSimpleName(), equalTo(ShadowView.class.getSimpleName()));

        Robolectric.bindShadowClass(TemporaryShadowView.class);

        assertThat(Robolectric.shadowOf_(new View(null)).getClass().getSimpleName(),
                equalTo(TemporaryShadowView.class.getSimpleName()));
    }

    @Test
    public void overridingShadowBindingsShouldNotAffectBindingsInLaterTestsAgain() throws Exception {
        assertThat(shadowOf(new View(null)).getClass().getSimpleName(), equalTo(ShadowView.class.getSimpleName()));

        Robolectric.bindShadowClass(TemporaryShadowView.class);

        assertThat(Robolectric.shadowOf_(new View(null)).getClass().getSimpleName(),
                equalTo(TemporaryShadowView.class.getSimpleName()));
    }

    @Implements(View.class)
    public static class TemporaryShadowView {
    }
}
