package com.xtremelabs.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.view.View;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ObjectAnimatorTest {
    @Test
    public void shouldCreateForFloat() throws Exception {
        Object expectedTarget = new Object();
        String propertyName = "expectedProperty";
        ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, propertyName, 0.5f, 0.4f);
        assertThat(animator, notNullValue());
        assertThat(animator.getTarget(), equalTo(expectedTarget));
        assertThat(animator.getPropertyName(), equalTo(propertyName));
    }

    @Test
    public void shouldSetAndGetDuration() throws Exception {
        Object expectedTarget = new Object();
        String propertyName = "expectedProperty";
        ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, propertyName, 0.5f, 0.4f);

        assertThat(animator.setDuration(2876), equalTo(animator));
        assertThat(animator.getDuration(), equalTo(2876l));
    }

    @Test
    public void shouldAnimate() throws Exception {
        View target = new View(null);
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
        animator.setDuration(1000);

        animator.start();
        assertThat(target.getTranslationX(), equalTo(0.5f));
        Robolectric.idleMainLooper(999);
        // I don't need these values to change gradually. If you do by all means implement that. PBG
        assertThat(target.getTranslationX(), not(0.4f));
        Robolectric.idleMainLooper(1);
        assertThat(target.getTranslationX(), equalTo(0.4f));
    }

    @Test
    public void shouldCallAnimationListenerAtStartAndEnd() throws Exception {
        View target = new View(null);
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
        animator.setDuration(1);
        TestAnimatorListener startListener = new TestAnimatorListener();
        TestAnimatorListener endListener = new TestAnimatorListener();
        animator.addListener(startListener);
        animator.addListener(endListener);
        animator.start();

        assertThat(startListener.startWasCalled, equalTo(true));
        assertThat(endListener.endWasCalled, equalTo(false));
        Robolectric.idleMainLooper(1);
        assertThat(endListener.endWasCalled, equalTo(true));
    }

    @Test
    public void getAnimatorsFor_shouldReturnAMapOfAnimatorsCreatedForTarget() throws Exception {
        View target = new View(null);
        ObjectAnimator expectedAnimator = ObjectAnimator.ofFloat(target, "translationX", 0f, 1f);

        assertThat(ShadowObjectAnimator.getAnimatorsFor(target).get("translationX"), sameInstance(expectedAnimator));
    }
}
