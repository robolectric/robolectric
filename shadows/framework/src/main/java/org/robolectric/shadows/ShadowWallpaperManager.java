package org.robolectric.shadows;

import android.Manifest.permission;
import android.annotation.FloatRange;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.MathUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Logger;
import org.xmlpull.v1.XmlPullParserException;

@Implements(WallpaperManager.class)
public class ShadowWallpaperManager {
  private static final String TAG = "ShadowWallpaperManager";
  private Bitmap lockScreenImage = null;
  private Bitmap homeScreenImage = null;
  private boolean isWallpaperAllowed = true;
  private boolean isWallpaperSupported = true;
  private WallpaperInfo wallpaperInfo = null;
  private final List<WallpaperCommandRecord> wallpaperCommandRecords = new ArrayList<>();
  private AtomicInteger wallpaperId = new AtomicInteger(0);
  private int lockScreenId;
  private int homeScreenId;

  private float wallpaperDimAmount = 0.0f;

  @Implementation
  protected void sendWallpaperCommand(
      IBinder windowToken, String action, int x, int y, int z, Bundle extras) {
    wallpaperCommandRecords.add(new WallpaperCommandRecord(windowToken, action, x, y, z, extras));
  }

  /**
   * Sets a resource id as the current wallpaper.
   *
   * <p>This only caches the resource id in memory. Calling this will override any previously set
   * resource and does not differentiate between users.
   */
  @Implementation(maxSdk = VERSION_CODES.M)
  protected void setResource(int resid) {
    setResource(resid, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected int setResource(int resid, int which) {
    if ((which & (WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK)) == 0) {
      return 0;
    }
    if ((which & WallpaperManager.FLAG_SYSTEM) == WallpaperManager.FLAG_SYSTEM) {
      homeScreenId = resid;
    }
    if ((which & WallpaperManager.FLAG_LOCK) == WallpaperManager.FLAG_LOCK) {
      lockScreenId = resid;
    }
    return wallpaperId.incrementAndGet();
  }

  /**
   * Returns whether the current wallpaper has been set through {@link #setResource(int)} or {@link
   * #setResource(int, int)} with the same resource id.
   */
  @Implementation(minSdk = VERSION_CODES.JELLY_BEAN_MR1)
  protected boolean hasResourceWallpaper(int resid) {
    return resid == this.lockScreenId || resid == this.homeScreenId;
  }

  /**
   * Caches {@code fullImage} in the memory based on {@code which}.
   *
   * <p>After a success call, any previously set live wallpaper is removed,
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
      wallpaperInfo = null;
      return 1;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      homeScreenImage = fullImage;
      wallpaperInfo = null;
      return 1;
    }
    return 0;
  }

  /**
   * Returns the memory cached {@link Bitmap} associated with {@code which}.
   *
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}.
   * @return The memory cached {@link Bitmap} associated with {@code which}. {@code null} if no
   *     bitmap was set.
   */
  @Nullable
  public Bitmap getBitmap(int which) {
    if (which == WallpaperManager.FLAG_LOCK) {
      return lockScreenImage;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      return homeScreenImage;
    }
    return null;
  }

  /**
   * Gets a wallpaper file associated with {@code which}.
   *
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return An open, readable file descriptor to the requested wallpaper image file; {@code null}
   *     if no such wallpaper is configured.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  @Nullable
  protected ParcelFileDescriptor getWallpaperFile(int which) {
    if (which == WallpaperManager.FLAG_SYSTEM && homeScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(homeScreenImage, "home_wallpaper");
    } else if (which == WallpaperManager.FLAG_LOCK && lockScreenImage != null) {
      return createParcelFileDescriptorFromBitmap(lockScreenImage, "lock_screen_wallpaper");
    }
    return null;
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected boolean isSetWallpaperAllowed() {
    return isWallpaperAllowed;
  }

  public void setIsSetWallpaperAllowed(boolean allowed) {
    isWallpaperAllowed = allowed;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected boolean isWallpaperSupported() {
    return isWallpaperSupported;
  }

  public void setIsWallpaperSupported(boolean supported) {
    isWallpaperSupported = supported;
  }

  /**
   * Caches {@code bitmapData} in the memory based on {@code which}.
   *
   * @param bitmapData the input stream which contains a bitmap image to be cached in the memory
   * @param visibleCropHint not used
   * @param allowBackup not used
   * @param which either {@link WallpaperManager#FLAG_LOCK} or {WallpaperManager#FLAG_SYSTEM}
   * @return 0 if fails to cache. Otherwise, 1.
   */
  @Implementation(minSdk = VERSION_CODES.N)
  protected int setStream(
      InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) {
    if (which == WallpaperManager.FLAG_LOCK) {
      lockScreenImage = BitmapFactory.decodeStream(bitmapData);
      return 1;
    } else if (which == WallpaperManager.FLAG_SYSTEM) {
      homeScreenImage = BitmapFactory.decodeStream(bitmapData);
      return 1;
    }
    return 0;
  }

  /**
   * Sets a live wallpaper, {@code wallpaperService}, as the current wallpaper.
   *
   * <p>This only caches the live wallpaper info in the memory. Calling this will remove any
   * previously set static wallpaper.
   */
  @SystemApi
  @Implementation(minSdk = VERSION_CODES.M)
  @RequiresPermission(permission.SET_WALLPAPER_COMPONENT)
  protected boolean setWallpaperComponent(ComponentName wallpaperService)
      throws IOException, XmlPullParserException {
    enforceWallpaperComponentPermission();

    Intent wallpaperServiceIntent = new Intent().setComponent(wallpaperService);
    List<ResolveInfo> resolveInfoList =
        RuntimeEnvironment.getApplication()
            .getPackageManager()
            .queryIntentServices(wallpaperServiceIntent, PackageManager.GET_META_DATA);
    if (resolveInfoList.size() != 1) {
      throw new IllegalArgumentException(
          "Can't locate the given wallpaper service: " + wallpaperService);
    }

    wallpaperInfo = new WallpaperInfo(RuntimeEnvironment.getApplication(), resolveInfoList.get(0));
    lockScreenImage = null;
    homeScreenImage = null;
    return true;
  }

  /**
   * Returns the information about the wallpaper if the current wallpaper is a live wallpaper
   * component. Otherwise, if the wallpaper is a static image, this returns null.
   */
  @Implementation(minSdk = VERSION_CODES.M)
  protected WallpaperInfo getWallpaperInfo() {
    return wallpaperInfo;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void setWallpaperDimAmount(@FloatRange(from = 0f, to = 1f) float dimAmount) {
    wallpaperDimAmount = MathUtils.saturate(dimAmount);
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  @FloatRange(from = 0f, to = 1f)
  protected float getWallpaperDimAmount() {
    return wallpaperDimAmount;
  }

  /** Returns all the invocation records to {@link WallpaperManager#sendWallpaperCommand} */
  public List<WallpaperCommandRecord> getWallpaperCommandRecords() {
    return Collections.unmodifiableList(wallpaperCommandRecords);
  }

  /**
   * Throws {@link SecurityException} if the caller doesn't have {@link
   * permission.SET_WALLPAPER_COMPONENT}.
   */
  private static void enforceWallpaperComponentPermission() {
    // Robolectric doesn't stimulate IPC calls. When this code is executed, it will still be running
    // in the caller process.
    if (RuntimeEnvironment.getApplication().checkSelfPermission(permission.SET_WALLPAPER_COMPONENT)
        != PackageManager.PERMISSION_GRANTED) {
      throw new SecurityException(
          "Permission " + permission.SET_WALLPAPER_COMPONENT + " isn't granted.");
    }
  }

  /**
   * Returns an open, readable file descriptor to the given {@code image} or {@code null} if there
   * is an {@link IOException}.
   */
  private static ParcelFileDescriptor createParcelFileDescriptorFromBitmap(
      Bitmap image, String fileName) {
    File tmpFile = new File(RuntimeEnvironment.getApplication().getCacheDir(), fileName);
    try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
      image.compress(CompressFormat.PNG, /* quality= */ 0, fileOutputStream);
      return ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (IOException e) {
      Logger.error("Fail to close file output stream when reading wallpaper from file", e);
      return null;
    }
  }

  /** Represents an invocation record of {@link WallpaperManager#sendWallpaperCommand} */
  public static class WallpaperCommandRecord {
    /** The first parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final IBinder windowToken;

    /** The second parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final String action;

    /** The third parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final int x;

    /** The forth parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final int y;

    /** The fifth parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final int z;

    /** The sixth parameter of {@link WallpaperManager#sendWallpaperCommand} */
    public final Bundle extras;

    WallpaperCommandRecord(IBinder windowToken, String action, int x, int y, int z, Bundle extras) {
      this.windowToken = windowToken;
      this.action = action;
      this.x = x;
      this.y = y;
      this.z = z;
      this.extras = extras;
    }
  }
}
