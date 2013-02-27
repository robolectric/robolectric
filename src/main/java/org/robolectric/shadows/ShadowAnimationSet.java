package org.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

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
