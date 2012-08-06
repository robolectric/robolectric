package com.xtremelabs.robolectric.shadows;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.view.animation.*;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AnimationSetTest {
	private AnimationSet set;
	private ShadowAnimationSet shadow;
	
	@Before
	public void setUp() {
		set = new AnimationSet(true);
		shadow = shadowOf(set);
	}
	
	
	@Test
	public void testAnimationList() {
		Animation alpha = new AlphaAnimation(1f, 2f);
		Animation translate =  new TranslateAnimation(1f, 2f, 3f, 4f);
		Animation rotate = new RotateAnimation(1f, 2f);
		set.addAnimation(alpha);
		set.addAnimation(translate);
		set.addAnimation(rotate);
		
		List<Animation> list = shadow.getAnimations();
		assertThat(list.size(), equalTo(3));
		assertThat(list.get(0), sameInstance(alpha));
		assertThat(list.get(1), sameInstance(translate));
		assertThat(list.get(2), sameInstance(rotate));
	}

}
