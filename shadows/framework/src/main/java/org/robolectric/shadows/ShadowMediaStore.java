package org.robolectric.shadows;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MediaStore.class)
public class ShadowMediaStore {

  @Implements(MediaStore.Images.class)
  public static class ShadowImages {

    @Implements(MediaStore.Images.Media.class)
    public static class ShadowMedia {

      @Implementation
      protected static Bitmap getBitmap(ContentResolver cr, Uri url) {
        return ShadowBitmapFactory.create(url.toString());
      }
    }
  }
}
