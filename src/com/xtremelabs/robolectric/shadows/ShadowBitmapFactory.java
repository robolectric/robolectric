package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapFactory.class)
public class ShadowBitmapFactory {
    @Implementation
    public static Bitmap decodeResource(Resources res, int id) {
        String resourceName = shadowOf(Robolectric.application).getResourceLoader().getNameForId(id);
        Bitmap bitmap = create("Bitmap for resource " + resourceName);
        shadowOf(bitmap).setLoadedFromResourceId(id);
        return bitmap;
    }

    @Implementation
    public static Bitmap decodeFile(String pathName) {
        return create("Bitmap for file " + pathName);
    }

    static Bitmap create(String description) {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        ShadowBitmap shadowBitmap = shadowOf(bitmap);
        shadowBitmap.appendDescription(description);
        shadowBitmap.setWidth(100);
        shadowBitmap.setHeight(100);
        return bitmap;
    }
}
