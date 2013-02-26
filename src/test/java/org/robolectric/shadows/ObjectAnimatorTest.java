package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
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
    public void floatAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
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
    public void intAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
        View target = new View(null);
        ObjectAnimator animator = ObjectAnimator.ofInt(target, "bottom", 1, 4);
        animator.setDuration(1000);

        animator.start();
        assertThat(target.getBottom(), equalTo(1));
        Robolectric.idleMainLooper(1000);
        assertThat(target.getBottom(), equalTo(4));
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

    @Test
    public void testIsRunning() throws Exception {
        View target = new View(null);
        ObjectAnimator expectedAnimator = ObjectAnimator.ofFloat(target, "translationX", 0f, 1f);
        long duration = 70;
        expectedAnimator.setDuration(duration);

        assertThat(expectedAnimator.isRunning(), is(false));
        expectedAnimator.start();
        assertThat(expectedAnimator.isRunning(), is(true));
        Robolectric.idleMainLooper(duration);
        assertThat(expectedAnimator.isRunning(), is(false));
    }

    @Test
    public void pauseAndRunEndNotifications() throws Exception {
        View target = new View(null);
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
        animator.setDuration(1);
        TestAnimatorListener endListener = new TestAnimatorListener();
        animator.addListener(endListener);

        animator.start();

        assertThat(endListener.endWasCalled, equalTo(false));
        ShadowObjectAnimator.pauseEndNotifications();
        Robolectric.idleMainLooper(1);
        assertThat(endListener.endWasCalled, equalTo(false));
        ShadowObjectAnimator.unpauseEndNotifications();
        assertThat(endListener.endWasCalled, equalTo(true));
    }
}
