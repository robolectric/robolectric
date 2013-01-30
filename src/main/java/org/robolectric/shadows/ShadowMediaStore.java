package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import com.xtremelabs.robolectric.Robolectric;
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
                return ShadowBitmapFactory.create(url.toString());
            }
        }
    }

    public static void reset() {
        Robolectric.Reflection.setFinalStaticField(MediaStore.Images.Media.class, "EXTERNAL_CONTENT_URI",
                Uri.parse("content://media/external/images/media"));
        
        Robolectric.Reflection.setFinalStaticField(MediaStore.Images.Media.class, "INTERNAL_CONTENT_URI",
                Uri.parse("content://media/internal/images/media"));

        Robolectric.Reflection.setFinalStaticField(MediaStore.Video.Media.class, "EXTERNAL_CONTENT_URI",
                Uri.parse("content://media/external/video/media"));
        
        Robolectric.Reflection.setFinalStaticField(MediaStore.Video.Media.class, "INTERNAL_CONTENT_URI",
                Uri.parse("content://media/internal/video/media"));
    }
}
