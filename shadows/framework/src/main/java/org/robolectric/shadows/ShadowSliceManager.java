package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.app.slice.SliceManager;
import android.app.slice.SliceSpec;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link SliceManager}. */
@Implements(value = SliceManager.class, minSdk = P)
public class ShadowSliceManager {

  private static final Map<Integer, Collection<Uri>> packageUidsToPermissionGrantedSliceUris =
      new HashMap<>();
  private final Map<Uri, Set<SliceSpec>> pinnedUriMap = new HashMap<>();
  private Context context;

  @Implementation
  protected void __constructor__(Context context, Handler handler) {
    this.context = context;
  }

  @Implementation
  protected synchronized List<Uri> getPinnedSlices() {
    return new ArrayList<>(pinnedUriMap.keySet());
  }

  @Implementation
  protected synchronized void grantSlicePermission(String toPackage, Uri uri) {
    int packageUid = getUidForPackage(toPackage);
    Collection<Uri> uris = packageUidsToPermissionGrantedSliceUris.get(packageUid);
    if (uris == null) {
      uris = new ArrayList<>();
      packageUidsToPermissionGrantedSliceUris.put(packageUid, uris);
    }
    uris.add(uri);
  }

  @Implementation
  protected synchronized void revokeSlicePermission(String toPackage, Uri uri) {
    int packageUid = getUidForPackage(toPackage);
    Collection<Uri> uris = packageUidsToPermissionGrantedSliceUris.get(packageUid);
    if (uris != null) {
      uris.remove(uri);
      if (uris.isEmpty()) {
        packageUidsToPermissionGrantedSliceUris.remove(packageUid);
      }
    }
  }

  @Implementation
  protected synchronized int checkSlicePermission(Uri uri, int pid, int uid) {
    if (uid == 0) {
      return PackageManager.PERMISSION_GRANTED;
    }
    Collection<Uri> uris = packageUidsToPermissionGrantedSliceUris.get(uid);
    if (uris != null && uris.contains(uri)) {
      return PackageManager.PERMISSION_GRANTED;
    }
    return PackageManager.PERMISSION_DENIED;
  }

  @Implementation
  protected void pinSlice(@NonNull Uri uri, @NonNull Set<SliceSpec> specs) {
    pinnedUriMap.put(uri, specs);
  }

  @Implementation
  protected void unpinSlice(@NonNull Uri uri) {
    pinnedUriMap.remove(uri);
  }

  @Implementation
  @NonNull
  protected Set<SliceSpec> getPinnedSpecs(Uri uri) {
    if (pinnedUriMap.containsKey(uri)) {
      return pinnedUriMap.get(uri);
    } else {
      return ImmutableSet.of();
    }
  }

  private int getUidForPackage(String packageName) {
    PackageManager packageManager = context.getPackageManager();
    try {
      return packageManager.getPackageUid(packageName, 0);
    } catch (NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Resetter
  public static synchronized void reset() {
    packageUidsToPermissionGrantedSliceUris.clear();
  }
}
