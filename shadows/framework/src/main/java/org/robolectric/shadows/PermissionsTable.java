package org.robolectric.shadows;

import com.google.auto.value.AutoValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * The permissions explicitly granted or denied in a test, shared by {@code Context.checkPermission}
 * and {@code PackageManager.checkPermission}.
 *
 * <p>Each decision is for a device, a process and a uid. {@link #ANY_PID} stands for every process
 * of the uid, which is what package-level grants, such as {@code
 * PackageManager.grantRuntimePermission}, apply to. A decision for a specific process takes
 * precedence over one for {@link #ANY_PID}, and setting a decision for {@link #ANY_PID} replaces
 * the decisions for the specific processes of the uid.
 *
 * <p>The table only holds explicit decisions. A permission that no test granted or denied has no
 * decision, and each caller applies its own default: {@code Context.checkPermission} denies it,
 * while {@code PackageManager.checkPermission} keeps its existing behavior, e.g. granting the
 * permissions that the test application requests in its manifest.
 */
final class PermissionsTable {
  /** Stands for every process of a uid. */
  static final int ANY_PID = -1;

  @GuardedBy("this")
  private final Map<Key, Map<String, Boolean>> decisions = new HashMap<>();

  /** Grants or denies {@code permission}. */
  synchronized void set(int deviceId, int pid, int uid, String permission, boolean granted) {
    if (pid == ANY_PID) {
      for (Map.Entry<Key, Map<String, Boolean>> entry : decisions.entrySet()) {
        if (entry.getKey().deviceId() == deviceId && entry.getKey().uid() == uid) {
          entry.getValue().remove(permission);
        }
      }
    }
    decisions
        .computeIfAbsent(Key.create(deviceId, pid, uid), unused -> new HashMap<>())
        .put(permission, granted);
  }

  /**
   * Returns whether {@code permission} was granted ({@code true}) or denied ({@code false}), or
   * {@code null} if it has no decision.
   *
   * <p>For {@link #ANY_PID}, the permission is granted if it is granted to any process of the uid.
   */
  @Nullable
  synchronized Boolean get(int deviceId, int pid, int uid, String permission) {
    if (pid == ANY_PID) {
      Boolean result = null;
      for (Map.Entry<Key, Map<String, Boolean>> entry : decisions.entrySet()) {
        if (entry.getKey().deviceId() == deviceId && entry.getKey().uid() == uid) {
          Boolean decision = entry.getValue().get(permission);
          if (Boolean.TRUE.equals(decision)) {
            return true;
          }
          if (decision != null) {
            result = false;
          }
        }
      }
      return result;
    }
    Boolean decision = decisionFor(Key.create(deviceId, pid, uid), permission);
    return decision != null
        ? decision
        : decisionFor(Key.create(deviceId, ANY_PID, uid), permission);
  }

  @GuardedBy("this")
  @Nullable
  private Boolean decisionFor(Key key, String permission) {
    Map<String, Boolean> forKey = decisions.get(key);
    return forKey == null ? null : forKey.get(permission);
  }

  @AutoValue
  abstract static class Key {
    abstract int deviceId();

    abstract int pid();

    abstract int uid();

    static Key create(int deviceId, int pid, int uid) {
      return new AutoValue_PermissionsTable_Key(deviceId, pid, uid);
    }
  }
}
