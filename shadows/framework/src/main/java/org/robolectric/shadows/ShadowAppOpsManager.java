package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.P;

import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.os.Build;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private Multimap<String, Integer> mStoredOps = HashMultimap.create();
  // "uid|packageName|opCode" => opMode
  private Map<String, Integer> appModeMap = new HashMap<>();

  /**
   * Change the operating mode for the given op in the given app package. You must pass in both the
   * uid and name of the application whose mode is being modified; if these do not match, the
   * modification will not be applied.
   *
   * <p>This method is public for testing {@link #checkOpNoThrow}. If {@link #checkOpNoThrow} is
   * called afterwards with the {@code op}, {@code ui}, and {@code packageName} provided, it will
   *  return the {@code mode} set here.
   *
   * @param op The operation to modify. One of the OPSTR_* constants.
   * @param uid The user id of the application whose mode will be changed.
   * @param packageName The name of the application package name whose mode will be changed.
   */
  @Implementation(minSdk = P)
  @HiddenApi
  @SystemApi
  @RequiresPermission(android.Manifest.permission.MANAGE_APP_OPS_MODES)
  public void setMode(String op, int uid, String packageName, int mode) {
    setMode(AppOpsManager.strOpToOp(op), uid, packageName, mode);
  }

  /** Int version of {@link #setMode(String, int, String, int)}. Used by system internally. */
  @Implementation(minSdk = KITKAT)
  @HiddenApi
  @RequiresPermission(android.Manifest.permission.MANAGE_APP_OPS_MODES)
  protected void setMode(int op, int uid, String packageName, int mode) {
    appModeMap.put(getOpMapKey(uid, packageName, op), mode);
  }


  @Implementation(minSdk = P)
  @Deprecated // renamed to unsafeCheckOpNoThrow
  protected int checkOpNoThrow(String op, int uid, String packageName) {
    return checkOpNoThrow(AppOpsManager.strOpToOp(op), uid, packageName);
  }

  /**
   * Like {@link AppOpsManager#checkOp} but instead of throwing a {@link SecurityException} it
   * returns {@link AppOpsManager#MODE_ERRORED}.
   */
  @Implementation(minSdk = KITKAT)
  @HiddenApi
  protected int checkOpNoThrow(int op, int uid, String packageName) {
    Integer mode = appModeMap.get(getOpMapKey(uid, packageName, op));
    if (mode == null) {
      return AppOpsManager.MODE_ALLOWED;
    }
    return mode;
  }

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
      return ReflectionHelpers.callConstructor(
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

  private static String getOpMapKey(int uid, String packageName, int opInt) {
    return String.format("%s|%s|%s", uid, packageName, opInt);
  }
}
