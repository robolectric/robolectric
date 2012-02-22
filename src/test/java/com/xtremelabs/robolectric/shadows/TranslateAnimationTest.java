package com.xtremelabs.robolectric.shadows;

import android.view.animation.TranslateAnimation;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TranslateAnimationTest {

    private TranslateAnimation animation;
    private ShadowTranslateAnimation shadow;

    @Before
    public void setUp() throws Exception {
        animation = new TranslateAnimation(1, 2, 3, 4, 5, 6, 7, 8);
        shadow = shadowOf(animation);
    }

    @Test
    public void animationParametersFromConstructor() throws Exception {
        assertThat(shadow.getFromXType(), equalTo(1));
        assertThat(shadow.getFromXValue(), equalTo(2f));
        assertThat(shadow.getToXType(), equalTo(3));
        assertThat(shadow.getToXValue(), equalTo(4f));
        assertThat(shadow.getFromYType(), equalTo(5));
        assertThat(shadow.getFromYValue(), equalTo(6f));
        assertThat(shadow.getToYType(), equalTo(7));
        assertThat(shadow.getToYValue(), equalTo(8f));
    }
}
