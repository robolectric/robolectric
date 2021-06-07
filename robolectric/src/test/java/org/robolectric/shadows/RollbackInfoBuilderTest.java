package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.VersionedPackage;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.RollbackInfo;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link RollbackInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public final class RollbackInfoBuilderTest {
  @Test
  public void build_withNoSetFields() {
    RollbackInfo rollbackInfo = RollbackInfoBuilder.newBuilder().build();

    assertThat(rollbackInfo).isNotNull();
  }

  @Test
  public void build() {
    VersionedPackage packageRolledBackFrom = new VersionedPackage("test_package", 123);
    VersionedPackage packageRolledBackTo = new VersionedPackage("test_package", 345);
    PackageRollbackInfo packageRollbackInfo =
        PackageRollbackInfoBuilder.newBuilder()
            .setPackageRolledBackFrom(packageRolledBackFrom)
            .setPackageRolledBackTo(packageRolledBackTo)
            .build();
    RollbackInfo rollbackInfo =
        RollbackInfoBuilder.newBuilder()
            .setRollbackId(1)
            .setPackages(ImmutableList.of(packageRollbackInfo))
            .setIsStaged(true)
            .setCausePackages(ImmutableList.of(packageRolledBackFrom))
            .setCommittedSessionId(10)
            .build();

    assertThat(rollbackInfo).isNotNull();
    assertThat(rollbackInfo.getRollbackId()).isEqualTo(1);
    assertThat(rollbackInfo.getPackages()).containsExactly(packageRollbackInfo);
    assertThat(rollbackInfo.isStaged()).isTrue();
    assertThat(rollbackInfo.getCausePackages()).containsExactly(packageRolledBackFrom);
    assertThat(rollbackInfo.getCommittedSessionId()).isEqualTo(10);
  }
}
