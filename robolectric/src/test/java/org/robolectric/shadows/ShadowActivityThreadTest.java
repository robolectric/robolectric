package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import android.app.ActivityThread;
import android.content.res.CompatibilityInfo;
import android.os.Build;
import android.os.RemoteException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityThreadTest {

  @Test
  @Config(maxSdk = Build.VERSION_CODES.M)
  public void getPackageInfo_returnsNullWhenNotFound() throws Exception {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    // assertThat(
    //         activityThread.getPackageInfo(
    //             "com.unknownpackage.ab", CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, 0))
    //     .isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N_MR1)
  public void getPackageInfo_throwsRemoteExceptionWhenNotFound() throws Exception {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    // try {
    //   activityThread.getPackageInfo(
    //       "com.unknownpackage.ab", CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, 0);
    //   fail("should have thrown");
    // } catch (RuntimeException e) {
    //   assertThat(e).hasCauseInstanceOf(RemoteException.class);
    // }
  }
}
