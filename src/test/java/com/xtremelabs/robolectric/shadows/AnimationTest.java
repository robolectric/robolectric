package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;


import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestAnimationListener;

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationTest {
	
	private Animation animation;
	private ShadowAnimation shadow;
	private TestAnimationListener listener;

	@Before
	public void setUp() throws Exception {
		animation = new TestAnimation();
		shadow = Robolectric.shadowOf(animation);
		listener = new TestAnimationListener();
		animation.setAnimationListener(listener);
	}

	@After
	public void tearDown() throws Exception {
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
	
	
	
	private class TestAnimation extends Animation {
		
	}
}
