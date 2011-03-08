package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.view.animation.AnimationUtils;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationUtilsTest {
	
	@Test
	public void testLoadAnimation() {
		assertThat(AnimationUtils.loadAnimation(new Activity(), 1), notNullValue());
	}
}
