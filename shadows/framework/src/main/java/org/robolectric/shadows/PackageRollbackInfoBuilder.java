package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.content.pm.VersionedPackage;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.PackageRollbackInfo.RestoreInfo;
import android.os.Build.VERSION_CODES;
import android.util.IntArray;
import android.util.SparseLongArray;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/**
 * Builder for {@link PackageRollbackInfo} as PackageRollbackInfo has hidden constructors, this
 * builder class has been added as a way to make custom PackageRollbackInfo objects when needed.
 */
public final class PackageRollbackInfoBuilder {

  @Nullable private VersionedPackage packageRolledBackFrom;
  @Nullable private VersionedPackage packageRolledBackTo;
  private final IntArray pendingBackups = new IntArray();
  private final ArrayList<RestoreInfo> pendingRestores = new ArrayList<>();
  private boolean isApex;
  private boolean isApkInApex;
  private final IntArray installedUsers = new IntArray();
  private final IntArray snapshottedUsers = new IntArray();
  private SparseLongArray ceSnapshotInodes = new SparseLongArray();

  private PackageRollbackInfoBuilder() {}

  /**
   * Start building a new PackageRollbackInfo
   *
   * @return a new instance of {@link PackageRollbackInfoBuilder}.
   */
  public static PackageRollbackInfoBuilder newBuilder() {
    return new PackageRollbackInfoBuilder();
  }

  /** Sets the version packaged rolled back from. */
  public PackageRollbackInfoBuilder setPackageRolledBackFrom(
      VersionedPackage packageRolledBackFrom) {
    this.packageRolledBackFrom = packageRolledBackFrom;
    return this;
  }

  /** Sets the version packaged rolled back to. */
  public PackageRollbackInfoBuilder setPackageRolledBackTo(VersionedPackage packageRolledBackTo) {
    this.packageRolledBackTo = packageRolledBackTo;
    return this;
  }

  /** Adds pending backup. We choose this API because IntArray is not publicly available. */
  public PackageRollbackInfoBuilder addPendingBackup(int pendingBackup) {
    this.pendingBackups.add(pendingBackup);
    return this;
  }

  /** Adds pending restores. We choose this API because RestoreInfo is not publicly available. */
  public PackageRollbackInfoBuilder addPendingRestore(int userId, int appId, String seInfo) {
    this.pendingRestores.add(new PackageRollbackInfo.RestoreInfo(userId, appId, seInfo));
    return this;
  }

  /** Sets is apex. */
  public PackageRollbackInfoBuilder setIsApex(boolean isApex) {
    this.isApex = isApex;
    return this;
  }

  /** Sets is apk in apex. */
  public PackageRollbackInfoBuilder setIsApkInApex(boolean isApkInApex) {
    this.isApkInApex = isApkInApex;
    return this;
  }

  /** Adds installed user. We choose this API because IntArray is not publicly available. */
  public PackageRollbackInfoBuilder addInstalledUser(int installedUser) {
    this.installedUsers.add(installedUser);
    return this;
  }

  /** Adds snapshotted user. We choose this API because IntArray is not publicly available. */
  public PackageRollbackInfoBuilder addSnapshottedUser(int snapshottedUser) {
    this.snapshottedUsers.add(snapshottedUser);
    return this;
  }

  /** Sets ce snapshot inodes. */
  public PackageRollbackInfoBuilder setCeSnapshotInodes(SparseLongArray ceSnapshotInodes) {
    checkNotNull(ceSnapshotInodes, "Field 'packageRolledBackFrom' not allowed to be null.");
    this.ceSnapshotInodes = ceSnapshotInodes;
    return this;
  }

  private List<Integer> getPendingBackupsList() {
    List<Integer> pendingBackupsList = new ArrayList<>();
    for (int pendingBackup : pendingBackups.toArray()) {
      pendingBackupsList.add(pendingBackup);
    }
    return pendingBackupsList;
  }

  private List<Integer> getSnapshottedUsersList() {
    List<Integer> snapshottedUsersList = new ArrayList<>();
    for (int snapshottedUser : snapshottedUsers.toArray()) {
      snapshottedUsersList.add(snapshottedUser);
    }
    return snapshottedUsersList;
  }

  /** Returns a {@link PackageRollbackInfo} with the data that was given. */
  public PackageRollbackInfo build() {
    // Check mandatory fields.
    checkNotNull(packageRolledBackFrom, "Mandatory field 'packageRolledBackFrom' missing.");
    checkNotNull(packageRolledBackTo, "Mandatory field 'packageRolledBackTo' missing.");
    checkState(RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q);

    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel == VERSION_CODES.Q) {
      return ReflectionHelpers.callConstructor(
          PackageRollbackInfo.class,
          ReflectionHelpers.ClassParameter.from(VersionedPackage.class, packageRolledBackFrom),
          ReflectionHelpers.ClassParameter.from(VersionedPackage.class, packageRolledBackTo),
          ReflectionHelpers.ClassParameter.from(IntArray.class, pendingBackups),
          ReflectionHelpers.ClassParameter.from(ArrayList.class, pendingRestores),
          ReflectionHelpers.ClassParameter.from(Boolean.TYPE, isApex),
          ReflectionHelpers.ClassParameter.from(IntArray.class, installedUsers),
          ReflectionHelpers.ClassParameter.from(SparseLongArray.class, ceSnapshotInodes));
    } else if (apiLevel == VERSION_CODES.R) {
      // We only have access to constructor on R. For all other SDKs, we will need
      // ReflectionHelper.
      return new PackageRollbackInfo(
          packageRolledBackFrom,
          packageRolledBackTo,
          pendingBackups,
          pendingRestores,
          isApex,
          isApkInApex,
          snapshottedUsers,
          ceSnapshotInodes);
    } else if (apiLevel > VERSION_CODES.R) {
      return ReflectionHelpers.callConstructor(
          PackageRollbackInfo.class,
          ReflectionHelpers.ClassParameter.from(VersionedPackage.class, packageRolledBackFrom),
          ReflectionHelpers.ClassParameter.from(VersionedPackage.class, packageRolledBackTo),
          ReflectionHelpers.ClassParameter.from(List.class, getPendingBackupsList()),
          ReflectionHelpers.ClassParameter.from(ArrayList.class, pendingRestores),
          ReflectionHelpers.ClassParameter.from(Boolean.TYPE, isApex),
          ReflectionHelpers.ClassParameter.from(Boolean.TYPE, isApkInApex),
          ReflectionHelpers.ClassParameter.from(List.class, getSnapshottedUsersList()));
    } else {
      throw new UnsupportedOperationException("PackageRollbacakInfoBuilder requires SDK >= Q");
    }
  }
}
