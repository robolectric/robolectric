package org.robolectric.shadows;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowNotificationBuilder25Test extends ShadowNotificationBuilderTest {

  /**
   * run 'em all again with android:targetSdkVersion=25 - behavior of NotificationBuilder
   * varies based on version specified in Manifest rather than runtime framework version.
   */
  @Before
  public void setup() throws Exception {
    RuntimeEnvironment.application.getPackageManager().getPackageInfo("org.robolectric", 0).applicationInfo.targetSdkVersion = Build.VERSION_CODES.N_MR1;
  }

}
