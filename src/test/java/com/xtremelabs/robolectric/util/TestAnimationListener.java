package com.xtremelabs.robolectric.util;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class TestAnimationListener implements AnimationListener {

	public boolean wasStartCalled = false;
	public boolean wasEndCalled = false;
	public boolean wasRepeatCalled = false;

	@Override
	public void onAnimationStart(Animation animation) {
		wasStartCalled = true;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		wasEndCalled = true;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		wasRepeatCalled = true;
	}
}
