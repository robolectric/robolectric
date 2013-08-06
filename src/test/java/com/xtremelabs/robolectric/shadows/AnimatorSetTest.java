package com.xtremelabs.robolectric.shadows;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.RobolectricShadowOfLevel16.shadowOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

@RunWith(WithTestDefaultsRunner.class)
public class AnimatorSetTest {
    private Context context;

    @Before
    public void setup() throws Exception {
        context = Robolectric.application;
    }

    @Test
    public void start_withPlayTogether_shouldSetTheInitialValuesOfAllChildAnimators() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        View target = new View(context);
        ObjectAnimator childAnimator1 = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
        ObjectAnimator childAnimator2 = ObjectAnimator.ofFloat(target, "scaleX", 0.5f, 0.0f);
        subject.playTogether(childAnimator1, childAnimator2);
        subject.setDuration(70);

        subject.start();

        assertThat(target.getAlpha(), equalTo(0.0f));
        assertThat(target.getScaleX(), equalTo(0.5f));
    }

    @Test
    public void startAndWaitForAnimationEnd_withPlayTogether_shouldSetTheFinalValuesOfAllChildAnimators() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        View target = new View(context);
        ObjectAnimator childAnimator1 = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
        ObjectAnimator childAnimator2 = ObjectAnimator.ofFloat(target, "scaleX", 0.5f, 0.0f);
        subject.playTogether(childAnimator1, childAnimator2);
        subject.setDuration(70);

        subject.start();
        Robolectric.idleMainLooper(70);

        assertThat(target.getAlpha(), equalTo(3.0f));
        assertThat(target.getScaleX(), equalTo(0.0f));
    }

    @Test
    public void setInterpolator_shouldImmediatelySetInterpolatorsOfAllChildren() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        View target = new View(context);
        ObjectAnimator childAnimator = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
        subject.playTogether(childAnimator);
        TimeInterpolator expectedInterpolator = new LinearInterpolator();
        subject.setInterpolator(expectedInterpolator);

        assertThat(childAnimator.getInterpolator(), sameInstance(expectedInterpolator));
    }

    @Test
    public void doesNothingWhenNoAnimatorsAddedToSet() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        subject.start();
        // does not crash
    }

    @Test
    public void canAnswerLastStartedSet() throws Exception {
        AnimatorSet set1 = new AnimatorSet();
        AnimatorSet set2 = new AnimatorSet();
        set1.start();
        set2.start();
        assertThat(ShadowAnimatorSet.getLastStartedSet(), sameInstance(set2));
    }

    @Test
    public void size_returnsNumberOfAnimatorsInSet() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        subject.playTogether(new ObjectAnimator(), new ObjectAnimator());
        assertThat(shadowOf(subject).size(), equalTo(2));
    }

    @Test
    public void get_fetchesAnimatorAtPosition() throws Exception {
        AnimatorSet subject = new AnimatorSet();
        Animator animator0 = new ObjectAnimator();
        Animator animator1 = new ObjectAnimator();
        subject.playTogether(animator0, animator1);
        assertThat(shadowOf(subject).get(0), sameInstance(animator0));
        assertThat(shadowOf(subject).get(1), sameInstance(animator1));
    }
}
