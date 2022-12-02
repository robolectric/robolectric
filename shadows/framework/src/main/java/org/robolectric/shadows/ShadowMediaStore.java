package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link MediaStore}. */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MediaStore.class)
public class ShadowMediaStore {

  private static Bitmap stubBitmap = null;

  @Resetter
  public static void reset() {
    stubBitmap = null;
  }

  /** Shadow for {@link MediaStore.Images}. */
  @Implements(MediaStore.Images.class)
  public static class ShadowImages {

    /** Shadow for {@link MediaStore.Images.Media}. */
    @Implements(MediaStore.Images.Media.class)
    public static class ShadowMedia {

      @Implementation
      protected static Bitmap getBitmap(ContentResolver cr, Uri url) {
        if (ShadowView.useRealGraphics()) {
          return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        } else {
          return ShadowBitmapFactory.create(url.toString(), null, null);
        }
      }
    }

    /** Shadow for {@link MediaStore.Images.Thumbnails}. */
    @Implements(MediaStore.Images.Thumbnails.class)
    public static class ShadowThumbnails {

      @Implementation
      protected static Bitmap getThumbnail(
          ContentResolver cr, long imageId, int kind, Options options) {
        if (stubBitmap != null) {
          return stubBitmap;
        } else {
          return reflector(ImagesThumbnailsReflector.class)
              .getThumbnail(cr, imageId, kind, options);
        }
      }
    }
  }

  /** Shadow for {@link MediaStore.Video}. */
  @Implements(MediaStore.Video.class)
  public static class ShadowVideo {

    /** Shadow for {@link MediaStore.Video.Thumbnails}. */
    @Implements(MediaStore.Video.Thumbnails.class)
    public static class ShadowThumbnails {

      @Implementation
      protected static Bitmap getThumbnail(
          ContentResolver cr, long imageId, int kind, Options options) {
        if (stubBitmap != null) {
          return stubBitmap;
        } else {
          return reflector(VideoThumbnailsReflector.class).getThumbnail(cr, imageId, kind, options);
        }
      }
    }
  }

  public static void setStubBitmapForThumbnails(Bitmap bitmap) {
    stubBitmap = bitmap;
  }

  /** Accessor interface for {@link MediaStore.Images.Thumbnails}'s internals. */
  @ForType(MediaStore.Images.Thumbnails.class)
  interface ImagesThumbnailsReflector {
    @Direct
    Bitmap getThumbnail(ContentResolver cr, long imageId, int kind, Options options);
  }

  /** Accessor interface for {@link MediaStore.Video.Thumbnails}'s internals. */
  @ForType(MediaStore.Video.Thumbnails.class)
  interface VideoThumbnailsReflector {
    @Direct
    Bitmap getThumbnail(ContentResolver cr, long imageId, int kind, Options options);
  }
}
