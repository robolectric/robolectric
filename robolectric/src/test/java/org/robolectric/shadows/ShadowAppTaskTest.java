package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowAppTaskTest {

  @Test
  public void finishAndRemoveTask_marksTaskFinished() {
    final AppTask appTask = ShadowAppTask.newInstance();

    appTask.finishAndRemoveTask();

    assertThat(shadowOf(appTask).isFinishedAndRemoved()).isTrue();
  }

  @Test
  public void taskIsNotFinishedInitially() {
    assertThat(shadowOf(ShadowAppTask.newInstance()).isFinishedAndRemoved()).isFalse();
  }

  @Test
  public void getTaskInfo_returnsNullInitially() {
    assertThat(ShadowAppTask.newInstance().getTaskInfo()).isNull();
  }

  @Test
  public void getTaskInfo_returnsCorrectValue() {
    final AppTask appTask = ShadowAppTask.newInstance();
    final RecentTaskInfo recentTaskInfo = new RecentTaskInfo();
    recentTaskInfo.description = "com.google.test";

    shadowOf(appTask).setTaskInfo(recentTaskInfo);

    assertThat(appTask.getTaskInfo()).isSameAs(recentTaskInfo);
  }

  @Test
  public void moveToFront_movesTaskToFront() {
    final AppTask appTask = ShadowAppTask.newInstance();

    appTask.moveToFront();

    assertThat(shadowOf(appTask).hasMovedToFront()).isTrue();
  }

  @Test
  public void taskIsNotMovedToFrontInitially() {
    assertThat(shadowOf(ShadowAppTask.newInstance()).hasMovedToFront()).isFalse();
  }

  @Test
  public void startActivity() {
    final AppTask appTask = ShadowAppTask.newInstance();
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final Intent intent = new Intent(Intent.ACTION_VIEW);

    appTask.startActivity(activity, intent, null);

    assertThat(shadowOf(activity).peekNextStartedActivity()).isNotNull();
    assertThat(shadowOf(activity).peekNextStartedActivity().getAction())
        .isEqualTo(Intent.ACTION_VIEW);
  }

  @Test
  public void setExcludeFromRecents_excludesFromRecents() {
    final AppTask appTask = ShadowAppTask.newInstance();

    appTask.setExcludeFromRecents(true);

    assertThat(shadowOf(appTask).isExcludedFromRecents()).isTrue();
  }

  @Test
  public void taskIsNotExcludedFromRecentsInitially() {
    assertThat(shadowOf(ShadowAppTask.newInstance()).isExcludedFromRecents()).isFalse();
  }
}
