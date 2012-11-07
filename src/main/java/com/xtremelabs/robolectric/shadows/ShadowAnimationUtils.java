package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(AnimationUtils.class)
public class ShadowAnimationUtils {

	@Implementation
	public static Animation loadAnimation(Context context, int id) {
		Animation anim = new TranslateAnimation(0, 0, 30, 0);
		Robolectric.shadowOf(anim).setLoadedFromResourceId(id);
		return anim; 
	}
	
	@Implementation
	public static LayoutAnimationController loadLayoutAnimation(Context context, int id) {
		Animation anim = new TranslateAnimation(0, 0, 30, 0);
		LayoutAnimationController layoutAnim = new LayoutAnimationController(anim);
		Robolectric.shadowOf(layoutAnim).setLoadedFromResourceId(id);
		return layoutAnim; 
	}
	
}
