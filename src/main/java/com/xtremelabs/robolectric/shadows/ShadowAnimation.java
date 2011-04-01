package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.view.animation.Animation;

/**
 * Shadow implementation of {@code Animation} that provides support for invoking listener callbacks. 
 */
@SuppressWarnings( { "UnusedDeclaration" })
@Implements(Animation.class)
public class ShadowAnimation {
	
	private Animation.AnimationListener listener;
	private boolean startFlag = false;
	
	@RealObject
	private Animation realAnimation;
	
	@Implementation
	public void setAnimationListener(Animation.AnimationListener l) {
		listener = l;
	}
	
	@Implementation
	public void start() {
		startFlag = true;
		if ( listener != null ) {
			listener.onAnimationStart(realAnimation);
		}
	}
	
	@Implementation
	public void cancel() {
		startFlag = false;
		if ( listener != null ) {
			listener.onAnimationEnd(realAnimation);
		}
	}
	
	@Implementation
	public boolean hasStarted() {
		return startFlag;
	}
	
	/**
	 * Non-Android accessor.  Returns most recently set animation listener.
	 * 
	 * @return
	 */
	public Animation.AnimationListener getAnimationListener() {
		return listener;
	}
	
	/**
	 * Non-Android accessor.  Use to simulate repeat loops of animation.
	 */
	public void invokeRepeat() {
		if ( listener != null ) {
			listener.onAnimationRepeat(realAnimation);
		}
	}

	/**
	 * Non-Android accessor.  Use to simulate end of animation.
	 */
	public void invokeEnd() {
		if ( listener != null ) {
			listener.onAnimationEnd(realAnimation);
		}
	}
	
	
}
