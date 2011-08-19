package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(LayerDrawable.class)
public class ShadowLayerDrawable {
    @RealObject
    protected LayerDrawable realLayerDrawable;

    protected Drawable[] drawables;

    public void __constructor__(Drawable[] drawables) {
        this.drawables = drawables;
    }

    @Implementation
    public int getNumberOfLayers() {
        return drawables.length;
    }
    
    @Implementation
    public Drawable getDrawable( int idx ) {
    	Drawable d = null;
    	if( idx < drawables.length && idx >= 0 ) {
    		d = drawables[ idx ];
    	}
    	return d;
    }
}
