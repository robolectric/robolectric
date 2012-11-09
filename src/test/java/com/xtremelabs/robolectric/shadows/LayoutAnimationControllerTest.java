package com.xtremelabs.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import android.app.Activity;
import android.view.animation.LayoutAnimationController;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class LayoutAnimationControllerTest {
	private ShadowLayoutAnimationController shadow;
	
	@Before
	public void setup() {
		LayoutAnimationController controller = new LayoutAnimationController(new Activity(), null);
		shadow = Robolectric.shadowOf(controller);
	}
	
	@Test
	public void testResourceId() {
		int id = 1;
		shadow.setLoadedFromResourceId(1);
		assertThat(shadow.getLoadedFromResourceId(), equalTo(id));
	}

}
