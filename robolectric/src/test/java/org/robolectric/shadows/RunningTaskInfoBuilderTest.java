package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link RunningTaskInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
public final class RunningTaskInfoBuilderTest {
  @Test
  public void build() {
    ComponentName baseActivity = new ComponentName("package", "BaseActivity");
    ComponentName topActivity = new ComponentName("package", "TopActivity");

    RunningTaskInfo info =
        RunningTaskInfoBuilder.newBuilder()
            .setBaseActivity(baseActivity)
            .setTopActivity(topActivity)
            .build();
    assertThat(info.baseActivity).isEqualTo(baseActivity);
    assertThat(info.topActivity).isEqualTo(topActivity);
  }

  @Test
  @Config(minSdk = Q)
  public void build_taskId() {
    RunningTaskInfo info = RunningTaskInfoBuilder.newBuilder().setTaskId(100).build();
    assertThat(info.taskId).isEqualTo(100);
  }

  @Test
  @Config(minSdk = S)
  public void build_isVisible() {
    RunningTaskInfo info = RunningTaskInfoBuilder.newBuilder().setIsVisible(true).build();
    assertThat(info.isVisible).isTrue();
  }
}
