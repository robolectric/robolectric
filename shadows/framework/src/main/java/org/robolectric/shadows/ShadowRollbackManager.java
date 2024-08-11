package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.content.rollback.RollbackInfo;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** A Shadow for android.content.rollback.RollbackManager added in Android Q. */
@Implements(
    className = "android.content.rollback.RollbackManager",
    minSdk = Q,
    isInAndroidSdk = false)
public final class ShadowRollbackManager {

  private static final List<RollbackInfo> availableRollbacks = new ArrayList<>();
  private static final List<RollbackInfo> recentlyCommittedRollbacks = new ArrayList<>();

  public void addAvailableRollbacks(RollbackInfo rollbackInfo) {
    availableRollbacks.add(rollbackInfo);
  }

  public void addRecentlyCommittedRollbacks(RollbackInfo rollbackInfo) {
    recentlyCommittedRollbacks.add(rollbackInfo);
  }

  @Implementation
  protected List<RollbackInfo> getAvailableRollbacks() {
    return availableRollbacks;
  }

  @Implementation
  protected List<RollbackInfo> getRecentlyCommittedRollbacks() {
    return recentlyCommittedRollbacks;
  }

  @Resetter
  public static void reset() {
    availableRollbacks.clear();
    recentlyCommittedRollbacks.clear();
  }
}
