package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.HashMap;
import java.util.Map;

@Implements(LayerDrawable.class)
public class ShadowLayerDrawable extends ShadowDrawable {
    @RealObject
    protected LayerDrawable realLayerDrawable;

    protected Drawable[] drawables;
    private Map<Integer, Integer> indexForId = new HashMap<Integer, Integer>();

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

    @Implementation
    public boolean setDrawableByLayerId(int id, Drawable drawable) {
        if (!indexForId.containsKey(id)) {
            return false;
        }
        drawables[indexForId.get(id)] = drawable;
        return true;
    }

    @Implementation
    public void setId(int index, int id) {
        indexForId.put(id, index);
    }
}
