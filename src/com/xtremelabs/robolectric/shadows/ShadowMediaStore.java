package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MediaStore.class)
public class ShadowMediaStore {
    @Implements(MediaStore.Images.class)
    public static class ShadowImages {
        @Implements(MediaStore.Images.Media.class)
        public static class ShadowMedia {
            @Implementation
            public static Bitmap getBitmap(ContentResolver cr, Uri url) {
                return ShadowBitmapFactory.create("uri " + url);
            }
        }
    }
}
