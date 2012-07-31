package com.xtremelabs.robolectric.shadows;

import android.view.animation.Animation;
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
    
    @Test
    public void animationParametersFromConstructor2() throws Exception {
    	TranslateAnimation animation2 = new TranslateAnimation(1, 2, 3, 4);
    	ShadowTranslateAnimation shadow2 = shadowOf(animation2);
    	int defType = Animation.ABSOLUTE;
        assertThat(shadow2.getFromXType(), equalTo(defType));
        assertThat(shadow2.getFromXValue(), equalTo(1f));
        assertThat(shadow2.getToXType(), equalTo(defType));
        assertThat(shadow2.getToXValue(), equalTo(2f));
        assertThat(shadow2.getFromYType(), equalTo(defType));
        assertThat(shadow2.getFromYValue(), equalTo(3f));
        assertThat(shadow2.getToYType(), equalTo(defType));
        assertThat(shadow2.getToYValue(), equalTo(4f));
    }
}
