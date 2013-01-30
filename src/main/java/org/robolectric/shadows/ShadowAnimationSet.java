package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;

import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(AnimationSet.class)
public class ShadowAnimationSet extends ShadowAnimation {
	private ArrayList<Animation> animationList = new ArrayList<Animation>();
	
    @RealObject
    private AnimationSet realAnimationSet;


	@Implementation
    public void addAnimation(Animation anim) {
    	animationList.add(anim);
    }
	
	@Implementation
	public List<Animation> getAnimations() {
		return animationList;
	}
}
