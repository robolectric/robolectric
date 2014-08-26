package org.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
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
    assertThat(shadow.getFromXType()).isEqualTo(1);
    assertThat(shadow.getFromXValue()).isEqualTo(2f);
    assertThat(shadow.getToXType()).isEqualTo(3);
    assertThat(shadow.getToXValue()).isEqualTo(4f);
    assertThat(shadow.getFromYType()).isEqualTo(5);
    assertThat(shadow.getFromYValue()).isEqualTo(6f);
    assertThat(shadow.getToYType()).isEqualTo(7);
    assertThat(shadow.getToYValue()).isEqualTo(8f);
  }

  @Test
  public void animationParametersFromConstructor2() throws Exception {
    TranslateAnimation animation2 = new TranslateAnimation(1, 2, 3, 4);
    ShadowTranslateAnimation shadow2 = shadowOf(animation2);
    int defType = Animation.ABSOLUTE;
    assertThat(shadow2.getFromXType()).isEqualTo(defType);
    assertThat(shadow2.getFromXValue()).isEqualTo(1f);
    assertThat(shadow2.getToXType()).isEqualTo(defType);
    assertThat(shadow2.getToXValue()).isEqualTo(2f);
    assertThat(shadow2.getFromYType()).isEqualTo(defType);
    assertThat(shadow2.getFromYValue()).isEqualTo(3f);
    assertThat(shadow2.getToYType()).isEqualTo(defType);
    assertThat(shadow2.getToYValue()).isEqualTo(4f);
  }
}
