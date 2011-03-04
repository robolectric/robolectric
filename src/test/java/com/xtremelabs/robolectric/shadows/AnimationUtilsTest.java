package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.view.animation.AnimationUtils;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationUtilsTest {
	
	private static Context context;

    @Before public void setUp() throws Exception {
        context = new Activity();
    }
		
	@Test
	public void TestLoadAnimation() {
		assertThat(AnimationUtils.loadAnimation(context, 1), notNullValue());		
	}

}
