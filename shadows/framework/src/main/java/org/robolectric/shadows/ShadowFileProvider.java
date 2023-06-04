package org.robolectric.shadows;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import java.io.File;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * A shadow implementation of androidx.core.content.FileProvider.
 *
 * <p>Currently only supports getUriForFile(). The returned Uri will always have "content" as its
 * scheme, authority matching to that of the given argument, and path matching to the absolute path
 * of the given file instance.
 */
@Implements(
    className = "androidx.core.content.FileProvider",
    isInAndroidSdk = false,
    minSdk = Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("robolectric.internal.IgnoreMissingClass")
public class ShadowFileProvider extends ShadowContentProvider {

  @SuppressWarnings("unused")
  @Implementation
  public static Uri getUriForFile(
      @NonNull Context context, @NonNull String authority, @NonNull File file) {
    return new Uri.Builder()
        .scheme("content") // URIs produced by FileProvider always have "content" as its scheme.
        .authority(authority)
        .path(file.getAbsolutePath())
        .build();
  }
}
