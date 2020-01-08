package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import android.service.notification.StatusBarNotification;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;

/** Unit tests for {@link ShadowStatusBarNotification}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowStatusBarNotificationTest {
  private StatusBarNotification statusBarNotification;
  private ShadowStatusBarNotification shadowStatusBarNotification;

  @Implementation(minSdk = JELLY_BEAN_MR2)
  private static StatusBarNotification createNotification() {
    Notification notification =
        new Notification.Builder(ApplicationProvider.getApplicationContext())
            .setContentTitle("TestTitle")
            .setContentText("Test Text")
            .build();
    return new StatusBarNotification(
        /* pkg= */ "TestPackage",
        /* opPkg= */ "TestPackage.app_ops",
        /* id= */ 1,
        /* tag= */ "notification_tag",
        /* uid= */ 18,
        /* pid= */ 1,
        /* score= */ 0,
        notification,
        Process.myUserHandle(),
        ShadowSystemClock.currentTimeMillis());
  }

  @Before
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void setUp() {
    statusBarNotification = createNotification();
    shadowStatusBarNotification = shadowOf(statusBarNotification);
  }

  @Test
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void setKey() {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
      assertThat(statusBarNotification.getKey()).isEqualTo("0|TestPackage|1|notification_tag|18");
      assertThat(shadowStatusBarNotification.getKey())
          .isEqualTo("0|TestPackage|1|notification_tag|18");
    } else {
      assertThat(shadowStatusBarNotification.getKey()).isNull();
    }

    shadowStatusBarNotification.setKey("NewKey");

    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
      assertThat(statusBarNotification.getKey()).isEqualTo("NewKey");
      assertThat(shadowStatusBarNotification.getKey()).isEqualTo("NewKey");
    } else {
      assertThat(shadowStatusBarNotification.getKey()).isNull();
    }
  }

  @Test
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void getKey() {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
      assertThat(shadowStatusBarNotification.getKey())
          .isEqualTo("0|TestPackage|1|notification_tag|18");
      assertThat(statusBarNotification.getKey()).isEqualTo("0|TestPackage|1|notification_tag|18");
    } else {
      assertThat(shadowStatusBarNotification.getKey()).isNull();
    }
  }
}
