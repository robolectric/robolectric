package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.app.IWallpaperManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import com.android.org.conscrypt.io.IoUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(WallpaperManager.class)
public class ShadowWallpaperManager {
  private Context context;
  private Bitmap lockScreenImage = null;
  private Bitmap homeScreenImage = null;

  @RealObject private WallpaperManager realObject;

  @Implementation(minSdk = VERSION_CODES.P)
  protected void __constructor__(IWallpaperManager service, Context context, Handler handler) {
    this.context = context;
    invokeConstructor(
        WallpaperManager.class,
        realObject,
        ClassParameter.from(IWallpaperManager.class, service),
        ClassParameter.from(Context.class, context),
        ClassParameter.from(Handler.class, handler));
  }

  @Implementation
  protected void sendWallpaperCommand(
      IBinder windowToken, String action, int x, int y, int z, Bundle extras) {}

  /**
   * Caches {@code fullImage} in the memory based on {@code which}
   *
   * @param fullImage the bitmap image to be cached in the memory
   * @param visibleCropHint not used
   * @param allowBackup not used
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return 0 if fails to cache. Otherwise, 1.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  protected int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) {
    if (which == WallpaperManager.FLAG_LOCK) {
      lockScreenImage = fullImage;
      return 1;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      homeScreenImage = fullImage;
      return 1;
    }
    return 0;
  }

  /**
   * Gets a wallpaper file associated with {@code which}.
   *
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return An open, readable file descriptor to the requested wallpaper image file; {@code null}
   *         if no such wallpaper is configured
   */
  @Implementation(minSdk = VERSION_CODES.P)
  protected ParcelFileDescriptor getWallpaperFile(int which) {
    if (which == WallpaperManager.FLAG_SYSTEM && homeScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(homeScreenImage, "home_wallpaper");
    } else if (which == WallpaperManager.FLAG_LOCK && lockScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(lockScreenImage, "lock_screen_wallpaper");
    }
    return null;
  }

  /**
   * Returns an open, readable file descriptor to the given {@code image} or {@code null} if there
   * is an {@link IOException}.
   */
  private ParcelFileDescriptor createParcelFileDescriptorFromBitmap(Bitmap image, String fileName) {
    FileOutputStream fileOutputStream = null;
    try {
      File tmpFile = new File(context.getCacheDir(), fileName);
      fileOutputStream = new FileOutputStream(tmpFile);
      image.compress(CompressFormat.PNG, /* quality= */ 0, fileOutputStream);

      return ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (IOException e) {
      return null;
    } finally {
      IoUtils.closeQuietly(fileOutputStream);
    }
  }
}
