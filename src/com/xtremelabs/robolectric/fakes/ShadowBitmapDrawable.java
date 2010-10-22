package com.xtremelabs.robolectric.fakes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.SheepWrangler;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapDrawable.class)
public class ShadowBitmapDrawable extends ShadowDrawable {
    public int loadedFromResourceId;

    @RealObject private BitmapDrawable realBitmapDrawable;
    @SheepWrangler private ProxyDelegatingHandler proxyDelegatingHandler;

    @Implementation
    public void draw(Canvas canvas) {
        canvas.drawBitmap(realBitmapDrawable.getBitmap(), 0, 0, new Paint());
    }
}
