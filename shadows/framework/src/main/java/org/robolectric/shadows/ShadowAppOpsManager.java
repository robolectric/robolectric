package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static java.util.stream.Collectors.toSet;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.AppOpsManager;
import android.app.AppOpsManager.AttributedOpEntry;
import android.app.AppOpsManager.NoteOpEvent;
import android.app.AppOpsManager.OnOpChangedListener;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.OpEventProxyInfo;
import android.app.AppOpsManager.PackageOps;
import android.app.SyncNotedAppOp;
import android.content.AttributionSource;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioAttributes.AttributeUsage;
import android.os.Binder;
import android.os.Build;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;
import androidx.annotation.RequiresApi;
import com.android.internal.app.IAppOpsService;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for {@link AppOpsManager}. */
@Implements(value = AppOpsManager.class, minSdk = KITKAT, looseSignatures = true)
public class ShadowAppOpsManager {

  // OpEntry fields that the shadow doesn't currently allow the test to configure.
  protected static final long OP_TIME = 1400000000L;
  protected static final long REJECT_TIME = 0L;
  protected static final int DURATION = 10;
  protected static final int PROXY_UID = 0;
  protected static final String PROXY_PACKAGE = "";

  @RealObject private AppOpsManager realObject;

  private static boolean staticallyInitialized = false;

  // Recorded operations, keyed by (uid, packageName)
  private final Multimap<Key, Integer> storedOps = HashMultimap.create();
  // (uid, packageName, opCode) => opMode
  private final Map<Key, Integer> appModeMap = new HashMap<>();

  // (uid, packageName, opCode)
  private final Set<Key> longRunningOp = new HashSet<>();

  private final Map<OnOpChangedListener, Set<Key>> appOpListeners = new ArrayMap<>();

  // op | (usage << 8) => ModeAndExcpetion
  private final Map<Integer, ModeAndException> audioRestrictions = new HashMap<>();

  private Context context;

  @Implementation
  protected void __constructor__(Context context, IAppOpsService service) {
    this.context = context;
    invokeConstructor(
        AppOpsManager.class,
        realObject,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(IAppOpsService.class, service));
  }

  @Implementation
  protected static void __staticInitializer__() {
    staticallyInitialized = true;
    Shadow.directInitialize(AppOpsManager.class);
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
  @Implementation
  @HiddenApi
  public void setMode(int op, int uid, String packageName, int mode) {
    Integer oldMode = appModeMap.put(Key.create(uid, packageName, op), mode);
    if (Objects.equals(oldMode, mode)) {
      return;
    }

    for (Map.Entry<OnOpChangedListener, Set<Key>> entry : appOpListeners.entrySet()) {
      for (Key key : entry.getValue()) {
        if (op == key.getOpCode()
            && (key.getPackageName() == null || key.getPackageName().equals(packageName))) {
          String[] sOpToString =
              ReflectionHelpers.getStaticField(AppOpsManager.class, "sOpToString");
          entry.getKey().onOpChanged(sOpToString[op], packageName);
        }
      }
    }
  }

  /**
   * Returns app op details for all packages for which one of {@link #setMode} methods was used to
   * set the value of one of the given app ops (it does return those set to 'default' mode, while
   * the true implementation usually doesn't). Also, we don't enforce any permission checks which
   * might be needed in the true implementation.
   *
   * @param ops The set of operations you are interested in, or null if you want all of them.
   * @return app ops information about each package, containing only ops that were specified as an
   *     argument
   */
  @Implementation(minSdk = Q)
  @HiddenApi
  @SystemApi
  @NonNull
  protected List<PackageOps> getPackagesForOps(@Nullable String[] ops) {
    List<PackageOps> result = null;

    if (ops == null) {
      int[] intOps = null;
      result = getPackagesForOps(intOps);
    } else {
      List<Integer> intOpsList = new ArrayList<>();
      for (String op : ops) {
        intOpsList.add(AppOpsManager.strOpToOp(op));
      }
      result = getPackagesForOps(intOpsList.stream().mapToInt(i -> i).toArray());
    }

    return result != null ? result : new ArrayList<>();
  }

  /**
   * Returns app op details for all packages for which one of {@link #setMode} methods was used to
   * set the value of one of the given app ops (it does return those set to 'default' mode, while
   * the true implementation usually doesn't). Also, we don't enforce any permission checks which
   * might be needed in the true implementation.
   *
   * @param ops The set of operations you are interested in, or null if you want all of them.
   * @return app ops information about each package, containing only ops that were specified as an
   *     argument
   */
  @Implementation
  @HiddenApi
  protected List<PackageOps> getPackagesForOps(int[] ops) {
    Set<Integer> relevantOps;
    if (ops != null) {
      relevantOps = IntStream.of(ops).boxed().collect(toSet());
    } else {
      relevantOps = new HashSet<>();
    }

    // Aggregating op data per each package.
    // (uid, packageName) => [(op, mode)]
    Multimap<Key, OpEntry> perPackageMap = MultimapBuilder.hashKeys().hashSetValues().build();
    for (Map.Entry<Key, Integer> appOpInfo : appModeMap.entrySet()) {
      Key key = appOpInfo.getKey();
      if (ops == null || relevantOps.contains(key.getOpCode())) {
        Key packageKey = Key.create(key.getUid(), key.getPackageName(), null);
        OpEntry opEntry = toOpEntry(key.getOpCode(), appOpInfo.getValue());
        perPackageMap.put(packageKey, opEntry);
      }
    }

    List<PackageOps> result = new ArrayList<>();
    // Creating resulting PackageOps objects using all op info collected per package.
    for (Map.Entry<Key, Collection<OpEntry>> packageInfo : perPackageMap.asMap().entrySet()) {
      Key key = packageInfo.getKey();
      result.add(
          new PackageOps(
              key.getPackageName(), key.getUid(), new ArrayList<>(packageInfo.getValue())));
    }

    return result.isEmpty() ? null : result;
  }

  @Implementation(minSdk = Q)
  public int unsafeCheckOpNoThrow(String op, int uid, String packageName) {
    return checkOpNoThrow(AppOpsManager.strOpToOp(op), uid, packageName);
  }

  @Implementation(minSdk = R)
  protected int unsafeCheckOpRawNoThrow(int op, int uid, String packageName) {
    Integer mode = appModeMap.get(Key.create(uid, packageName, op));
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

  /** Stores a fake long-running operation. It does not throw if a wrong uid is passed. */
  @Implementation(minSdk = R)
  protected int startOp(
      String op, int uid, String packageName, String attributionTag, String message) {
    int mode = unsafeCheckOpRawNoThrow(op, uid, packageName);
    if (mode == AppOpsManager.MODE_ALLOWED) {
      longRunningOp.add(Key.create(uid, packageName, AppOpsManager.strOpToOp(op)));
    }
    return mode;
  }

  /** Stores a fake long-running operation. It does not throw if a wrong uid is passed. */
  @Implementation(minSdk = KITKAT, maxSdk = Q)
  protected int startOpNoThrow(int op, int uid, String packageName) {
    int mode = unsafeCheckOpRawNoThrow(op, uid, packageName);
    if (mode == AppOpsManager.MODE_ALLOWED) {
      longRunningOp.add(Key.create(uid, packageName, op));
    }
    return mode;
  }

  /** Stores a fake long-running operation. It does not throw if a wrong uid is passed. */
  @Implementation(minSdk = R)
  protected int startOpNoThrow(
      String op, int uid, String packageName, String attributionTag, String message) {
    int mode = unsafeCheckOpRawNoThrow(op, uid, packageName);
    if (mode == AppOpsManager.MODE_ALLOWED) {
      longRunningOp.add(Key.create(uid, packageName, AppOpsManager.strOpToOp(op)));
    }
    return mode;
  }

  /** Removes a fake long-running operation from the set. */
  @Implementation(maxSdk = Q)
  protected void finishOp(int op, int uid, String packageName) {
    longRunningOp.remove(Key.create(uid, packageName, op));
  }

  /** Removes a fake long-running operation from the set. */
  @Implementation(minSdk = R)
  protected void finishOp(String op, int uid, String packageName, String attributionTag) {
    longRunningOp.remove(Key.create(uid, packageName, AppOpsManager.strOpToOp(op)));
  }

  /** Checks whether op was previously set using {@link #setMode} */
  @Implementation(minSdk = R)
  protected int checkOp(String op, int uid, String packageName) {
    return checkOpNoThrow(op, uid, packageName);
  }

  /**
   * Checks whether the given op is active, i.e. did someone call {@link #startOp(String, int,
   * String, String, String)} without {@link #finishOp(String, int, String, String)} yet.
   */
  @Implementation(minSdk = R)
  public boolean isOpActive(String op, int uid, String packageName) {
    return longRunningOp.contains(Key.create(uid, packageName, AppOpsManager.strOpToOp(op)));
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
   * <p>Made public for testing {@link #setMode} as the method is {@code @hide}.
   */
  @Implementation
  @HiddenApi
  public int checkOpNoThrow(int op, int uid, String packageName) {
    int mode = unsafeCheckOpRawNoThrow(op, uid, packageName);
    return mode == AppOpsManager.MODE_FOREGROUND ? AppOpsManager.MODE_ALLOWED : mode;
  }

  @Implementation
  public int noteOp(int op, int uid, String packageName) {
    return noteOpInternal(op, uid, packageName, "", "");
  }

  private int noteOpInternal(
      int op, int uid, String packageName, String attributionTag, String message) {
    storedOps.put(Key.create(uid, packageName, null), op);
    if (RuntimeEnvironment.getApiLevel() >= R) {
      Object lock = ReflectionHelpers.getStaticField(AppOpsManager.class, "sLock");
      synchronized (lock) {
        AppOpsManager.OnOpNotedCallback callback =
            ReflectionHelpers.getStaticField(AppOpsManager.class, "sOnOpNotedCallback");
        if (callback != null) {
          callback.onSelfNoted(new SyncNotedAppOp(op, attributionTag));
        }
      }
    }

    // Permission check not currently implemented in this shadow.
    return AppOpsManager.MODE_ALLOWED;
  }

  @Implementation(minSdk = R)
  protected int noteOp(int op, int uid, String packageName, String attributionTag, String message) {
    return noteOpInternal(op, uid, packageName, attributionTag, message);
  }

  @Implementation
  protected int noteOpNoThrow(int op, int uid, String packageName) {
    storedOps.put(Key.create(uid, packageName, null), op);
    return checkOpNoThrow(op, uid, packageName);
  }

  @Implementation(minSdk = R)
  protected int noteOpNoThrow(
      int op,
      int uid,
      @Nullable String packageName,
      @Nullable String attributionTag,
      @Nullable String message) {
    return noteOpNoThrow(op, uid, packageName);
  }

  @Implementation(minSdk = M, maxSdk = Q)
  @HiddenApi
  protected int noteProxyOpNoThrow(int op, String proxiedPackageName) {
    storedOps.put(Key.create(Binder.getCallingUid(), proxiedPackageName, null), op);
    return checkOpNoThrow(op, Binder.getCallingUid(), proxiedPackageName);
  }

  @Implementation(minSdk = Q, maxSdk = Q)
  @HiddenApi
  protected int noteProxyOpNoThrow(int op, String proxiedPackageName, int proxiedUid) {
    storedOps.put(Key.create(proxiedUid, proxiedPackageName, null), op);
    return checkOpNoThrow(op, proxiedUid, proxiedPackageName);
  }

  @Implementation(minSdk = R, maxSdk = R)
  @HiddenApi
  protected int noteProxyOpNoThrow(
      int op,
      String proxiedPackageName,
      int proxiedUid,
      String proxiedAttributionTag,
      String message) {
    storedOps.put(Key.create(proxiedUid, proxiedPackageName, null), op);
    return checkOpNoThrow(op, proxiedUid, proxiedPackageName);
  }

  @RequiresApi(api = S)
  @Implementation(minSdk = S)
  protected int noteProxyOpNoThrow(
      Object op, Object attributionSource, Object message, Object ignoredSkipProxyOperation) {
    Preconditions.checkArgument(op instanceof Integer);
    Preconditions.checkArgument(attributionSource instanceof AttributionSource);
    Preconditions.checkArgument(message == null || message instanceof String);
    Preconditions.checkArgument(ignoredSkipProxyOperation instanceof Boolean);
    AttributionSource castedAttributionSource = (AttributionSource) attributionSource;
    return noteProxyOpNoThrow(
        (int) op,
        castedAttributionSource.getNextPackageName(),
        castedAttributionSource.getNextUid(),
        castedAttributionSource.getNextAttributionTag(),
        (String) message);
  }

  @Implementation
  @HiddenApi
  public List<PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
    Set<Integer> opFilter = new HashSet<>();
    if (ops != null) {
      for (int op : ops) {
        opFilter.add(op);
      }
    }

    List<OpEntry> opEntries = new ArrayList<>();
    for (Integer op : storedOps.get(Key.create(uid, packageName, null))) {
      if (opFilter.isEmpty() || opFilter.contains(op)) {
        opEntries.add(toOpEntry(op, AppOpsManager.MODE_ALLOWED));
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

  @Implementation
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

  @Implementation
  protected void startWatchingMode(int op, String packageName, OnOpChangedListener callback) {
    startWatchingModeImpl(op, packageName, 0, callback);
  }

  @Implementation(minSdk = Q)
  protected void startWatchingMode(
      int op, String packageName, int flags, OnOpChangedListener callback) {
    startWatchingModeImpl(op, packageName, flags, callback);
  }

  private void startWatchingModeImpl(
      int op, String packageName, int flags, OnOpChangedListener callback) {
    Set<Key> keys = appOpListeners.get(callback);
    if (keys == null) {
      keys = new HashSet<>();
      appOpListeners.put(callback, keys);
    }
    keys.add(Key.create(null, packageName, op));
  }

  @Implementation
  protected void stopWatchingMode(OnOpChangedListener callback) {
    appOpListeners.remove(callback);
  }

  protected OpEntry toOpEntry(Integer op, int mode) {
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.M) {
      return ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(int.class, mode),
          ClassParameter.from(long.class, OP_TIME),
          ClassParameter.from(long.class, REJECT_TIME),
          ClassParameter.from(int.class, DURATION));
    } else if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.Q) {
      return ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(int.class, mode),
          ClassParameter.from(long.class, OP_TIME),
          ClassParameter.from(long.class, REJECT_TIME),
          ClassParameter.from(int.class, DURATION),
          ClassParameter.from(int.class, PROXY_UID),
          ClassParameter.from(String.class, PROXY_PACKAGE));
    } else if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.R) {
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

      return ReflectionHelpers.callConstructor(
          OpEntry.class,
          ClassParameter.from(int.class, op),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(int.class, mode),
          ClassParameter.from(LongSparseLongArray.class, accessTimes),
          ClassParameter.from(LongSparseLongArray.class, rejectTimes),
          ClassParameter.from(LongSparseLongArray.class, durations),
          ClassParameter.from(LongSparseLongArray.class, proxyUids),
          ClassParameter.from(LongSparseArray.class, proxyPackages));
    } else {
      final long key =
          AppOpsManager.makeKey(AppOpsManager.UID_STATE_TOP, AppOpsManager.OP_FLAG_SELF);

      LongSparseArray<NoteOpEvent> accessEvents = new LongSparseArray<>();
      LongSparseArray<NoteOpEvent> rejectEvents = new LongSparseArray<>();

      accessEvents.put(
          key,
          new NoteOpEvent(OP_TIME, DURATION, new OpEventProxyInfo(PROXY_UID, PROXY_PACKAGE, null)));
      rejectEvents.put(key, new NoteOpEvent(REJECT_TIME, -1, null));

      return new OpEntry(
          op,
          mode,
          Collections.singletonMap(
              null, new AttributedOpEntry(op, false, accessEvents, rejectEvents)));
    }
  }

  private static int getAudioRestrictionKey(int code, @AttributeUsage int usage) {
    return code | (usage << 8);
  }

  @AutoValue
  abstract static class Key {
    @Nullable
    abstract Integer getUid();

    @Nullable
    abstract String getPackageName();

    @Nullable
    abstract Integer getOpCode();

    static Key create(Integer uid, String packageName, Integer opCode) {
      return new AutoValue_ShadowAppOpsManager_Key(uid, packageName, opCode);
    }
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

  @Resetter
  public static void reset() {
    // The callback passed in AppOpsManager#setOnOpNotedCallback is stored statically.
    // The check for staticallyInitialized is to make it so that we don't load AppOpsManager if it
    // hadn't already been loaded (both to save time and to also avoid any errors that might
    // happen if we tried to lazy load the class during reset)
    if (RuntimeEnvironment.getApiLevel() >= R && staticallyInitialized) {
      ReflectionHelpers.setStaticField(AppOpsManager.class, "sOnOpNotedCallback", null);
    }
  }
}
