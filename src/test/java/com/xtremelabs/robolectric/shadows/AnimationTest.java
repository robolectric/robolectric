package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestAnimationListener;

import android.view.animation.Animation;

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
	
	
	private class TestAnimation extends Animation {
		
	}
}
