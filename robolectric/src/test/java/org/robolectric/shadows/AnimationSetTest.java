package org.robolectric.shadows;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AnimationSetTest {
  @Test
  public void testAnimationList() {
    AnimationSet set = new AnimationSet(true);

    Animation alpha = new AlphaAnimation(1f, 2f);
    Animation translate =  new TranslateAnimation(1f, 2f, 3f, 4f);
    Animation rotate = new RotateAnimation(1f, 2f);
    set.addAnimation(alpha);
    set.addAnimation(translate);
    set.addAnimation(rotate);

    List<Animation> list = set.getAnimations();
    assertThat(list.size()).isEqualTo(3);
    assertThat(list.get(0)).isSameAs(alpha);
    assertThat(list.get(1)).isSameAs(translate);
    assertThat(list.get(2)).isSameAs(rotate);
  }
}
