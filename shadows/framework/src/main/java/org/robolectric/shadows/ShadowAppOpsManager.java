package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedListener;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioAttributes.AttributeUsage;
import android.os.Binder;
import android.os.Build;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;
import com.android.internal.app.IAppOpsService;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = AppOpsManager.class)
public class ShadowAppOpsManager {

  // OpEntry fields that the shadow doesn't currently allow the test to configure.
  protected static final long OP_TIME = 1400000000L;
  protected static final long REJECT_TIME = 0L;
  protected static final int DURATION = 10;
  protected static final int PROXY_UID = 0;
  protected static final String PROXY_PACKAGE = "";

  @RealObject private AppOpsManager realObject;

  // Recorded operations, keyed by "uid|packageName"
  private Multimap<String, Integer> mStoredOps = HashMultimap.create();
  // "uid|packageName|opCode" => opMode
  private Map<String, Integer> appModeMap = new HashMap<>();

  // "packageName|opCode" => listener
  private BiMap<String, OnOpChangedListener> appOpListeners = HashBiMap.create();

  // op | (usage << 8) => ModeAndExcpetion
  private Map<Integer, ModeAndException> audioRestrictions = new HashMap<>();

  private Context context;

  @Implementation(minSdk = KITKAT)
  protected void __constructor__(Context context, IAppOpsService service) {
    this.context = context;
    invokeConstructor(
        AppOpsManager.class,
        realObject,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(IAppOpsService.class, service));
  }

  /**
   * Change the operating mode for the given op in the given app package. You must pass in both the
   * uid and name of the application whose mode is being modified; if these do not match, the
   * modification will not be applied.
   *
   * <p>This method is public for testing {@link #checkOpNoThrow}. If {@link #checkOpNoThrow} is
   * called afterwards with the {@code op}, {@code ui}, and {@code packageName} provided, it will
   * return the {@code mode} set here.
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

  /**
   * Int version of {@link #setMode(String, int, String, int)}.
   *
   * <p>This method is public for testing {@link #checkOpNoThrow}. If {@link #checkOpNoThrow} is *
   * called afterwards with the {@code op}, {@code ui}, and {@code packageName} provided, it will *
   * return the {@code mode} set here.
   */
  @Implementation(minSdk = KITKAT)
  @HiddenApi
  @RequiresPermission(android.Manifest.permission.MANAGE_APP_OPS_MODES)
  public void setMode(int op, int uid, String packageName, int mode) {
    Integer oldMode = appModeMap.put(getOpMapKey(uid, packageName, op), mode);
    OnOpChangedListener listener = appOpListeners.get(getListenerKey(op, packageName));
    if (listener != null && !Objects.equals(oldMode, mode)) {
      String[] sOpToString = ReflectionHelpers.getStaticField(AppOpsManager.class, "sOpToString");
      listener.onOpChanged(sOpToString[op], packageName);
    }
  }

  @Implementation(minSdk = Q)
  public int unsafeCheckOpNoThrow(String op, int uid, String packageName) {
    return checkOpNoThrow(AppOpsManager.strOpToOp(op), uid, packageName);
  }

  private int unsafeCheckOpRawNoThrow(int op, int uid, String packageName) {
    Integer mode = appModeMap.get(getOpMapKey(uid, packageName, op));
    if (mode == null) {
      return AppOpsManager.MODE_ALLOWED;
    }
    return mode;
  }

  /**
   * Like {@link #unsafeCheckOpNoThrow(String, int, String)} but returns the <em>raw</em> mode
   * associated with the op. Does not throw a security exception, does not translate {@link
   * AppOpsManager#MODE_FOREGROUND}.
   */
  @Implementation(minSdk = Q)
  public int unsafeCheckOpRawNoThrow(String op, int uid, String packageName) {
    return unsafeCheckOpRawNoThrow(AppOpsManager.strOpToOp(op), uid, packageName);
  }

  @Implementation(minSdk = P)
  @Deprecated // renamed to unsafeCheckOpNoThrow
  protected int checkOpNoThrow(String op, int uid, String packageName) {
    return checkOpNoThrow(AppOpsManager.strOpToOp(op), uid, packageName);
  }

  /**
   * Like {@link AppOpsManager#checkOp} but instead of throwing a {@link SecurityException} it
   * returns {@link AppOpsManager#MODE_ERRORED}.
   *
   * <p>Made public for testing {@link #setMode} as the method is {@coe @hide}.
   */
  @Implementation(minSdk = KITKAT)
  @HiddenApi
  public int checkOpNoThrow(int op, int uid, String packageName) {
    int mode = unsafeCheckOpRawNoThrow(op, uid, packageName);
    return mode == AppOpsManager.MODE_FOREGROUND ? AppOpsManager.MODE_ALLOWED : mode;
  }

  @Implementation(minSdk = KITKAT)
  public int noteOp(int op, int uid, String packageName) {
    mStoredOps.put(getInternalKey(uid, packageName), op);

    // Permission check not currently implemented in this shadow.
    return AppOpsManager.MODE_ALLOWED;
  }

  @Implementation(minSdk = KITKAT)
  protected int noteOpNoThrow(int op, int uid, String packageName) {
    mStoredOps.put(getInternalKey(uid, packageName), op);
    return checkOpNoThrow(op, uid, packageName);
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected int noteProxyOpNoThrow(int op, String proxiedPackageName) {
    mStoredOps.put(getInternalKey(Binder.getCallingUid(), proxiedPackageName), op);
    return checkOpNoThrow(op, Binder.getCallingUid(), proxiedPackageName);
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

  @Implementation(minSdk = Q)
  @HiddenApi
  @SystemApi
  @RequiresPermission(android.Manifest.permission.GET_APP_OPS_STATS)
  protected List<PackageOps> getOpsForPackage(int uid, String packageName, String[] ops) {
    if (ops == null) {
      int[] intOps = null;
      return getOpsForPackage(uid, packageName, intOps);
    }
    Map<String, Integer> strOpToIntOp =
        ReflectionHelpers.getStaticField(AppOpsManager.class, "sOpStrToOp");
    List<Integer> intOpsList = new ArrayList<>();
    for (String op : ops) {
      Integer intOp = strOpToIntOp.get(op);
      if (intOp != null) {
        intOpsList.add(intOp);
      }
    }

    return getOpsForPackage(uid, packageName, intOpsList.stream().mapToInt(i -> i).toArray());
  }

  @Implementation(minSdk = KITKAT)
  protected void checkPackage(int uid, String packageName) {
    try {
      // getPackageUid was introduced in API 24, so we call it on the shadow class
      ShadowApplicationPackageManager shadowApplicationPackageManager =
          Shadow.extract(context.getPackageManager());
      int packageUid = shadowApplicationPackageManager.getPackageUid(packageName, 0);
      if (packageUid == uid) {
        return;
      }
      throw new SecurityException("Package " + packageName + " belongs to " + packageUid);
    } catch (NameNotFoundException e) {
      throw new SecurityException("Package " + packageName + " doesn't belong to " + uid, e);
    }
  }

  /**
   * Sets audio restrictions.
   *
   * <p>This method is public for testing, as the original method is {@code @hide}.
   */
  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  public void setRestriction(
      int code, @AttributeUsage int usage, int mode, String[] exceptionPackages) {
    audioRestrictions.put(
        getAudioRestrictionKey(code, usage), new ModeAndException(mode, exceptionPackages));
  }

  @Nullable
  public ModeAndException getRestriction(int code, @AttributeUsage int usage) {
    // this gives us room for 256 op_codes. There are 78 as of P.
    return audioRestrictions.get(getAudioRestrictionKey(code, usage));
  }

  @Implementation(minSdk = KITKAT)
  @HiddenApi
  @RequiresPermission(value = android.Manifest.permission.WATCH_APPOPS)
  protected void startWatchingMode(int op, String packageName, OnOpChangedListener callback) {
    appOpListeners.put(getListenerKey(op, packageName), callback);
  }

  @Implementation(minSdk = KITKAT)
  @RequiresPermission(value = android.Manifest.permission.WATCH_APPOPS)
  protected void stopWatchingMode(OnOpChangedListener callback) {
    appOpListeners.inverse().remove(callback);
  }

  protected OpEntry toOpEntry(Integer op) {
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.M) {
      return ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(int.class, AppOpsManager.MODE_ALLOWED),
          ClassParameter.from(long.class, OP_TIME),
          ClassParameter.from(long.class, REJECT_TIME),
          ClassParameter.from(int.class, DURATION));
    } else if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.Q) {
      return ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(int.class, AppOpsManager.MODE_ALLOWED),
          ClassParameter.from(long.class, OP_TIME),
          ClassParameter.from(long.class, REJECT_TIME),
          ClassParameter.from(int.class, DURATION),
          ClassParameter.from(int.class, PROXY_UID),
          ClassParameter.from(String.class, PROXY_PACKAGE));
    } else {
      final long key =
          AppOpsManager.makeKey(AppOpsManager.UID_STATE_TOP, AppOpsManager.OP_FLAG_SELF);

      final LongSparseLongArray accessTimes = new LongSparseLongArray();
      accessTimes.put(key, OP_TIME);

      final LongSparseLongArray rejectTimes = new LongSparseLongArray();
      rejectTimes.put(key, REJECT_TIME);

      final LongSparseLongArray durations = new LongSparseLongArray();
      durations.put(key, DURATION);

      final LongSparseLongArray proxyUids = new LongSparseLongArray();
      proxyUids.put(key, PROXY_UID);

      final LongSparseArray<String> proxyPackages = new LongSparseArray<>();
      proxyPackages.put(key, PROXY_PACKAGE);

      return new OpEntry(
          op,
          false,
          AppOpsManager.MODE_ALLOWED,
          accessTimes,
          rejectTimes,
          durations,
          proxyUids,
          proxyPackages);
    }
  }

  private static String getInternalKey(int uid, String packageName) {
    return uid + "|" + packageName;
  }

  private static String getOpMapKey(int uid, String packageName, int opInt) {
    return String.format("%s|%s|%s", uid, packageName, opInt);
  }

  private static int getAudioRestrictionKey(int code, @AttributeUsage int usage) {
    return code | (usage << 8);
  }

  private static String getListenerKey(int op, String packageName) {
    return String.format("%s|%s", op, packageName);
  }

  /** Class holding usage mode and excpetion packages. */
  public static class ModeAndException {
    public final int mode;
    public final List<String> exceptionPackages;

    public ModeAndException(int mode, String[] exceptionPackages) {
      this.mode = mode;
      this.exceptionPackages =
          exceptionPackages == null
              ? Collections.emptyList()
              : Collections.unmodifiableList(Arrays.asList(exceptionPackages));
    }
  }
}
