package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S_V2;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.TaskInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowRunningTaskInfo} */
@RunWith(AndroidJUnit4.class)
public class ShadowTaskInfoTest {

  @Test
  @Config(minSdk = S_V2)
  public void setIsVisibleFalse_isVisibleReturnsFalse() {
    final TaskInfo taskInfo = new RunningTaskInfo();

    shadowOf(taskInfo).setIsVisible(false);

    assertThat(taskInfo.isVisible()).isFalse();
  }

  @Test
  @Config(minSdk = S_V2)
  public void setIsVisibleTrue_isVisibleReturnsTrue() {
    final TaskInfo taskInfo = new RunningTaskInfo();

    shadowOf(taskInfo).setIsVisible(true);

    assertThat(taskInfo.isVisible()).isTrue();
  }
}
