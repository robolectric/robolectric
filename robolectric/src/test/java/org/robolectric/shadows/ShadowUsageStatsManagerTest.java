package org.robolectric.shadows;

import static android.content.Context.USAGE_STATS_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUsageStatsManager.AppUsageObserver;

/** Test for {@link ShadowUsageStatsManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class ShadowUsageStatsManagerTest {

  private static final String TEST_PACKAGE_NAME1 = "com.company1.pkg1";
  private static final String TEST_PACKAGE_NAME2 = "com.company2.pkg2";

  private UsageStatsManager usageStatsManager;
  private ShadowUsageStatsManager shadowUsageStatsManager;

  @Before
  public void setUp() throws Exception {
    usageStatsManager =
        (UsageStatsManager) RuntimeEnvironment.application.getSystemService(USAGE_STATS_SERVICE);
    shadowUsageStatsManager = shadowOf(usageStatsManager);
  }

  @Test
  public void testQueryEvents_emptyEvents() throws Exception {
    UsageEvents events = usageStatsManager.queryEvents(1000L, 2000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isFalse();
    assertThat(events.getNextEvent(event)).isFalse();
  }

  @Test
  public void testQueryEvents_appendEventData_shouldCombineWithPreviousData() throws Exception {
    shadowUsageStatsManager.addEvent(TEST_PACKAGE_NAME1, 500L, Event.MOVE_TO_FOREGROUND);
    shadowUsageStatsManager.addEvent(TEST_PACKAGE_NAME1, 1000L, Event.MOVE_TO_BACKGROUND);
    shadowUsageStatsManager.addEvent(TEST_PACKAGE_NAME2, 1500L, Event.MOVE_TO_FOREGROUND);
    shadowUsageStatsManager.addEvent(TEST_PACKAGE_NAME2, 2000L, Event.MOVE_TO_BACKGROUND);
    shadowUsageStatsManager.addEvent(TEST_PACKAGE_NAME1, 2500L, Event.MOVE_TO_FOREGROUND);

    UsageEvents events = usageStatsManager.queryEvents(1000L, 2000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME1);
    assertThat(event.getTimeStamp()).isEqualTo(1000L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_BACKGROUND);

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME2);
    assertThat(event.getTimeStamp()).isEqualTo(1500L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_FOREGROUND);

    assertThat(events.hasNextEvent()).isFalse();
    assertThat(events.getNextEvent(event)).isFalse();
  }


  
  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testGetAppStandbyBucket_withPackageName() throws Exception {
    shadowUsageStatsManager.setAppStandbyBucket("app1", UsageStatsManager.STANDBY_BUCKET_RARE);
    assertThat(shadowUsageStatsManager.getAppStandbyBucket("app1"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);

    assertThat(shadowUsageStatsManager.getAppStandbyBucket("app_unset"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_ACTIVE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testGetAppStandbyBucket_currentApp() throws Exception {
    shadowUsageStatsManager.setCurrentAppStandbyBucket(UsageStatsManager.STANDBY_BUCKET_RARE);
    assertThat(shadowUsageStatsManager.getAppStandbyBucket())
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);
    ShadowUsageStatsManager.reset();
    assertThat(shadowUsageStatsManager.getAppStandbyBucket())
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_ACTIVE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testRegisterAppUsageObserver_uniqueObserverIds_shouldAddBothObservers() {
    PendingIntent pendingIntent1 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    assertThat(shadowUsageStatsManager.getRegisteredAppUsageObservers())
        .containsExactly(
            new AppUsageObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1),
            new AppUsageObserver(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testRegisterAppUsageObserver_duplicateObserverIds_shouldOverrideExistingObserver() {
    PendingIntent pendingIntent1 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    assertThat(shadowUsageStatsManager.getRegisteredAppUsageObservers())
        .containsExactly(
            new AppUsageObserver(
                12, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testUnregisterAppUsageObserver_existingObserverId_shouldRemoveObserver() {
    PendingIntent pendingIntent1 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    usageStatsManager.unregisterAppUsageObserver(12);

    assertThat(shadowUsageStatsManager.getRegisteredAppUsageObservers())
        .containsExactly(
            new AppUsageObserver(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testUnregisterAppUsageObserver_nonExistentObserverId_shouldBeNoOp() {
    PendingIntent pendingIntent1 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    usageStatsManager.unregisterAppUsageObserver(36);

    assertThat(shadowUsageStatsManager.getRegisteredAppUsageObservers())
        .containsExactly(
            new AppUsageObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1),
            new AppUsageObserver(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testTriggerRegisteredAppUsageObserver_shouldSendIntentAndRemoveObserver() {
    PendingIntent pendingIntent1 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    shadowUsageStatsManager.triggerRegisteredAppUsageObserver(24, 500000L);

    List<Intent> broadcastIntents = ShadowApplication.getInstance().getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    Intent broadcastIntent = broadcastIntents.get(0);
    assertThat(broadcastIntent.getAction()).isEqualTo("ACTION2");
    assertThat(broadcastIntent.getIntExtra(UsageStatsManager.EXTRA_OBSERVER_ID, 0)).isEqualTo(24);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_LIMIT, 0))
        .isEqualTo(456000L);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_USED, 0))
        .isEqualTo(500000L);
    assertThat(shadowUsageStatsManager.getRegisteredAppUsageObservers())
        .containsExactly(
            new AppUsageObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1));
  }

}
