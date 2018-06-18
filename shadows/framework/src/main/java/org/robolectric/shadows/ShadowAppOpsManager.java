package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.os.Build;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for the {@link AppOpsManager}.
 */
@Implements(value = AppOpsManager.class)
public class ShadowAppOpsManager {

  // OpEntry fields that the shadow doesn't currently allow the test to configure.
  private static final long OP_TIME = 1400000000L;
  private static final long REJECT_TIME = 0L;
  private static final int DURATION = 10;
  private static final int PROXY_UID = 0;
  private static final String PROXY_PACKAGE = "";

  // Recorded operations, keyed by "uid|packageName"
  Multimap<String, Integer> mStoredOps = HashMultimap.create();

  @Implementation(minSdk = KITKAT)
  public int noteOp(int op, int uid, String packageName) {
    mStoredOps.put(getInternalKey(uid, packageName), op);

    // Permission check not currently implemented in this shadow.
    return AppOpsManager.MODE_ALLOWED;
  }

  @Implementation(minSdk = KITKAT)
  @HiddenApi
  public List<PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
    Set<Integer> opFilter = new HashSet<>();
    if (ops != null) {
      for (int op : ops) {
        opFilter.add(op);
      }
    }

    List<OpEntry> opEntries = new ArrayList<>();
    for (Integer op : mStoredOps.get(getInternalKey(uid, packageName))) {
      if (opFilter.isEmpty() || opFilter.contains(op)) {
        opEntries.add(toOpEntry(op));
      }
    }

    return ImmutableList.of(new PackageOps(packageName, uid, opEntries));
  }

  private static OpEntry toOpEntry(Integer op) {
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.M) {
      return (OpEntry) ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(int.class, AppOpsManager.MODE_ALLOWED),
          ClassParameter.from(long.class, OP_TIME),
          ClassParameter.from(long.class, REJECT_TIME),
          ClassParameter.from(int.class, DURATION));
    }

    return new OpEntry(
        op, AppOpsManager.MODE_ALLOWED, OP_TIME, REJECT_TIME, DURATION, PROXY_UID, PROXY_PACKAGE);
  }

  private static String getInternalKey(int uid, String packageName) {
    return uid + "|" + packageName;
  }
}
