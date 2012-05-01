package com.xtremelabs.robolectric.shadows;

import android.R;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestAnimationListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationTest {
	
	private TestAnimation animation;
	private ShadowAnimation shadow;
	private TestAnimationListener listener;

	@Before
	public void setUp() throws Exception {
		animation = new TestAnimation();
		shadow = shadowOf(animation);
		listener = new TestAnimationListener();
		animation.setAnimationListener(listener);
	}

	@Test
	public void startShouldInvokeStartCallback() throws Exception {
		assertThat(listener.wasStartCalled, equalTo(false));
		animation.start();
		assertThat(listener.wasStartCalled, equalTo(true));
		assertThat(listener.wasEndCalled, equalTo(false));
		assertThat(listener.wasRepeatCalled, equalTo(false));
	}
	
	@Test
	public void cancelShouldInvokeEndCallback() throws Exception {
		assertThat(listener.wasEndCalled, equalTo(false));
		animation.cancel();
		assertThat(listener.wasStartCalled, equalTo(false));
		assertThat(listener.wasEndCalled, equalTo(true));
		assertThat(listener.wasRepeatCalled, equalTo(false));		
	}
	
	@Test
	public void invokeRepeatShouldInvokeRepeatCallback() throws Exception {
		assertThat(listener.wasRepeatCalled, equalTo(false));
		shadow.invokeRepeat();
		assertThat(listener.wasStartCalled, equalTo(false));
		assertThat(listener.wasEndCalled, equalTo(false));
		assertThat(listener.wasRepeatCalled, equalTo(true));	
	}
	
	@Test
	public void invokeEndShouldInvokeEndCallback() throws Exception {
		assertThat(listener.wasEndCalled, equalTo(false));
		shadow.invokeEnd();
		assertThat(listener.wasStartCalled, equalTo(false));
		assertThat(listener.wasEndCalled, equalTo(true));
		assertThat(listener.wasRepeatCalled, equalTo(false));		
	}

    @Test
	public void simulateAnimationEndShouldInvokeApplyTransformationWith1() throws Exception {
		assertThat(animation.interpolatedTime, equalTo(0f));
		shadow.invokeEnd();
        assertThat(animation.interpolatedTime, equalTo(1f));
	}

	@Test
	public void testHasStarted() throws Exception {
		assertThat(animation.hasStarted(), equalTo(false));
		animation.start();
		assertThat(animation.hasStarted(), equalTo(true));
		animation.cancel();
		assertThat(animation.hasStarted(), equalTo(false));
	}
	
	@Test
	public void testDuration() throws Exception {
		assertThat(animation.getDuration(), not(equalTo(1000l)));
		animation.setDuration(1000);
		assertThat(animation.getDuration(), equalTo(1000l));
	}
	
	@Test
	public void testInterpolation() throws Exception {
		assertThat(animation.getInterpolator(), nullValue());
		LinearInterpolator i = new LinearInterpolator();
		animation.setInterpolator(i);
		assertThat((LinearInterpolator)animation.getInterpolator(), sameInstance(i));
	}

    @Test
    public void testRepeatCount() throws Exception {
        assertThat(animation.getRepeatCount(), not(equalTo(5)));
        animation.setRepeatCount(5);
        assertThat(animation.getRepeatCount(), equalTo(5));
    }

    @Test
    public void testRepeatMode() throws Exception {
        assertThat(animation.getRepeatMode(), not(equalTo(Animation.REVERSE)));
        animation.setRepeatMode(Animation.REVERSE);
        assertThat(animation.getRepeatMode(), equalTo(Animation.REVERSE));
    }

    @Test
    public void testStartOffset() throws Exception {
        assertThat(animation.getStartOffset(), not(equalTo(500l)));
        animation.setStartOffset(500l);
        assertThat(animation.getStartOffset(), equalTo(500l));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testNotLoadedFromResourceId() throws Exception {
        shadow.getLoadedFromResourceId();
    }

    @Test
    public void testLoadedFromResourceId() throws Exception {
        shadow.setLoadedFromResourceId(R.anim.fade_in);
        assertThat(shadow.getLoadedFromResourceId(), equalTo(R.anim.fade_in));
    }
    
	private class TestAnimation extends Animation {
        float interpolatedTime;
        Transformation t;

        @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
            this.interpolatedTime = interpolatedTime;
            this.t = t;
        }
    }
}
