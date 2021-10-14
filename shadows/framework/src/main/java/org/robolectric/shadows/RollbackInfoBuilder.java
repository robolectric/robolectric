package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.content.pm.VersionedPackage;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.RollbackInfo;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;

/**
 * Builder for {@link RollbackInfo} as RollbackInfo has hidden constructors, this builder class has
 * been added as a way to make custom RollbackInfo objects when needed.
 */
public final class RollbackInfoBuilder {

  private int rollbackId;
  private List<PackageRollbackInfo> packages = ImmutableList.of();
  private boolean isStaged;
  private List<VersionedPackage> causePackages = ImmutableList.of();
  private int committedSessionId;

  private RollbackInfoBuilder() {}

  /**
   * Start building a new RollbackInfo
   *
   * @return a new instance of {@link RollbackInfoBuilder}.
   */
  public static RollbackInfoBuilder newBuilder() {
    return new RollbackInfoBuilder();
  }

  /** Sets the id of the rollback. */
  public RollbackInfoBuilder setRollbackId(int rollbackId) {
    this.rollbackId = rollbackId;
    return this;
  }

  /** Sets the packages of the rollback. */
  public RollbackInfoBuilder setPackages(List<PackageRollbackInfo> packages) {
    checkNotNull(packages, "Field 'packages' not allowed to be null.");
    this.packages = packages;
    return this;
  }

  /** Sets the staged status of the rollback. */
  public RollbackInfoBuilder setIsStaged(boolean isStaged) {
    this.isStaged = isStaged;
    return this;
  }

  /** Sets the cause packages of the rollback. */
  public RollbackInfoBuilder setCausePackages(List<VersionedPackage> causePackages) {
    checkNotNull(causePackages, "Field 'causePackages' not allowed to be null.");
    this.causePackages = causePackages;
    return this;
  }

  /** Sets the committed session id of the rollback. */
  public RollbackInfoBuilder setCommittedSessionId(int committedSessionId) {
    this.committedSessionId = committedSessionId;
    return this;
  }

  /** Returns a {@link RollbackInfo} with the data that was given. */
  public RollbackInfo build() {
    checkState(RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q);

    return new RollbackInfo(rollbackId, packages, isStaged, causePackages, committedSessionId);
  }
}
