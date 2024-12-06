package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
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
  private static final List<CloudMediaChangedEvent> cloudMediaChangedEventList = new ArrayList<>();
  private static final List<String> supportedCloudMediaProviderAuthorities = new ArrayList<>();
  @Nullable private static String currentCloudMediaProviderAuthority = null;

  @Resetter
  public static void reset() {
    stubBitmap = null;
    cloudMediaChangedEventList.clear();
    supportedCloudMediaProviderAuthorities.clear();
    currentCloudMediaProviderAuthority = null;
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

  @Implementation(minSdk = TIRAMISU)
  protected static void notifyCloudMediaChangedEvent(
      ContentResolver resolver, String authority, String currentMediaCollectionId) {
    cloudMediaChangedEventList.add(
        CloudMediaChangedEvent.create(authority, currentMediaCollectionId));
  }

  /**
   * Returns an {@link ImmutableList} of all {@link CloudMediaChangedEvent} objects that {@link
   * MediaStore} has been notified of.
   */
  public static ImmutableList<CloudMediaChangedEvent> getCloudMediaChangedEvents() {
    return ImmutableList.copyOf(cloudMediaChangedEventList);
  }

  public static void clearCloudMediaChangedEventList() {
    cloudMediaChangedEventList.clear();
  }

  /** Event info for {@link MediaStore#notifyCloudMediaChangedEvent} notify events. */
  @AutoValue
  public abstract static class CloudMediaChangedEvent {
    public static CloudMediaChangedEvent create(String authority, String currentMediaCollectionId) {
      return new AutoValue_ShadowMediaStore_CloudMediaChangedEvent(
          authority, currentMediaCollectionId);
    }

    public abstract String authority();

    public abstract String currentMediaCollectionId();
  }

  @Implementation(minSdk = TIRAMISU)
  protected static boolean isSupportedCloudMediaProviderAuthority(
      @NonNull ContentResolver resolver, @NonNull String authority) {
    return supportedCloudMediaProviderAuthorities.contains(authority);
  }

  /**
   * Mutator method to add the input {@code authorities} to the list of supported cloud media
   * provider authorities.
   */
  public static void addSupportedCloudMediaProviderAuthorities(@NonNull List<String> authorities) {
    supportedCloudMediaProviderAuthorities.addAll(authorities);
  }

  /** Mutator method to clear the list of supported cloud media provider authorities. */
  public static void clearSupportedCloudMediaProviderAuthorities() {
    supportedCloudMediaProviderAuthorities.clear();
  }

  @Implementation(minSdk = TIRAMISU)
  protected static boolean isCurrentCloudMediaProviderAuthority(
      @NonNull ContentResolver resolver, @NonNull String authority) {
    return currentCloudMediaProviderAuthority.equals(authority);
  }

  /** Mutator method to set the value of the current cloud media provider authority. */
  public static void setCurrentCloudMediaProviderAuthority(@Nullable String authority) {
    currentCloudMediaProviderAuthority = authority;
  }
}
