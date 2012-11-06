package com.xtremelabs.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationUtilsTest {
	
	@Test
	public void testLoadAnimation() {
		assertThat(AnimationUtils.loadAnimation(new Activity(), 1), notNullValue());
	}

	@Test
	public void testLoadAnimationResourceId() {
		Animation anim = AnimationUtils.loadAnimation(new Activity(), R.anim.fade_in); 
		assertThat(Robolectric.shadowOf(anim).getLoadedFromResourceId(), equalTo(R.anim.fade_in));
	}
	
	@Test
	public void testLoadLayoutAnimation() {
		assertThat(AnimationUtils.loadLayoutAnimation(new Activity(), 1), notNullValue());
	}
	
	@Test
	public void testLoadLayoutAnimationControllerResourceId() {
		LayoutAnimationController layoutAnim = AnimationUtils.loadLayoutAnimation(new Activity(), R.anim.fade_in);
		assertThat(Robolectric.shadowOf(layoutAnim).getLoadedFromResourceId(), equalTo(R.anim.fade_in));
	}
}
