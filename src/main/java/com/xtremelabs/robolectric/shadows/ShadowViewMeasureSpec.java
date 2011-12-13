package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.view.View;

/**
 * Shadow for {@code View.MeasureSpec} inner class.
 * 
 * As the implementation is very simple, it is taken from the AOSP source.
 */
@Implements(View.MeasureSpec.class)
public class ShadowViewMeasureSpec {
	
	private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK = 0x3 << MODE_SHIFT;
    
	@Implementation
	public static int getMode (int measureSpec) {
		return (measureSpec & MODE_MASK);
	}
	
	@Implementation	
	public static int getSize (int measureSpec) {
		return (measureSpec & ~MODE_MASK);
	}
	
	@Implementation
	public static int makeMeasureSpec (int size, int mode) {
		return size + mode;
	}
	
	@Implementation
	public static String toString (int measureSpec) {
		int mode = getMode(measureSpec);
        int size = getSize(measureSpec);

        StringBuilder sb = new StringBuilder("MeasureSpec: ");

        if (mode == View.MeasureSpec.UNSPECIFIED)
            sb.append("UNSPECIFIED ");
        else if (mode == View.MeasureSpec.EXACTLY)
            sb.append("EXACTLY ");
        else if (mode == View.MeasureSpec.AT_MOST)
            sb.append("AT_MOST ");
        else
            sb.append(mode).append(" ");

        sb.append(size);
        return sb.toString();
	}

}
