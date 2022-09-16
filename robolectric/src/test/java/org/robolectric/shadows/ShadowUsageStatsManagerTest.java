package org.robolectric.shadows;

import static android.app.usage.UsageStatsManager.INTERVAL_DAILY;
import static android.app.usage.UsageStatsManager.INTERVAL_WEEKLY;
import static android.content.Context.USAGE_STATS_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.PendingIntent;
import android.app.usage.BroadcastResponseStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUsageStatsManager.AppUsageLimitObserver;
import org.robolectric.shadows.ShadowUsageStatsManager.AppUsageObserver;
import org.robolectric.shadows.ShadowUsageStatsManager.UsageSessionObserver;
import org.robolectric.shadows.ShadowUsageStatsManager.UsageStatsBuilder;

/** Test for {@link ShadowUsageStatsManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowUsageStatsManagerTest {

  private static final String TEST_PACKAGE_NAME1 = "com.company1.pkg1";
  private static final String TEST_PACKAGE_NAME2 = "com.company2.pkg2";
  private static final String TEST_ACTIVITY_NAME = "com.company2.pkg2.Activity";
  private static final String APP_1 = "test.app";
  private static final String APP_2 = "some.other.app";
  private static final int BUCKET_ID_1 = 10;
  private static final int BUCKET_ID_2 = 42;

  private UsageStatsManager usageStatsManager;
  private Application context;

  @Before
  public void setUp() throws Exception {
    usageStatsManager =
        (UsageStatsManager)
            ApplicationProvider.getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testQueryEvents_emptyEvents() throws Exception {
    UsageEvents events = usageStatsManager.queryEvents(1000L, 2000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isFalse();
    assertThat(events.getNextEvent(event)).isFalse();
  }

  @Test
  public void testQueryEvents_overlappingEvents() throws Exception {
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 1000L, Event.MOVE_TO_BACKGROUND);
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(1000L)
                .setEventType(Event.SYSTEM_INTERACTION)
                .build());
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 1000L, Event.MOVE_TO_FOREGROUND);

    UsageEvents events = usageStatsManager.queryEvents(1000L, 2000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME1);
    assertThat(event.getTimeStamp()).isEqualTo(1000L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_BACKGROUND);

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isNull();
    assertThat(event.getTimeStamp()).isEqualTo(1000L);
    assertThat(event.getEventType()).isEqualTo(Event.SYSTEM_INTERACTION);

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME1);
    assertThat(event.getTimeStamp()).isEqualTo(1000L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_FOREGROUND);
  }

  @Test
  public void testQueryEvents_appendEventData_shouldCombineWithPreviousData() throws Exception {
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 500L, Event.MOVE_TO_FOREGROUND);
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 1000L, Event.MOVE_TO_BACKGROUND);
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(1500L)
                .setPackage(TEST_PACKAGE_NAME2)
                .setClass(TEST_ACTIVITY_NAME)
                .setEventType(Event.MOVE_TO_FOREGROUND)
                .build());
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME2, 2000L, Event.MOVE_TO_BACKGROUND);
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(2500L)
                .setPackage(TEST_PACKAGE_NAME1)
                .setEventType(Event.MOVE_TO_FOREGROUND)
                .setClass(TEST_ACTIVITY_NAME)
                .build());

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
    assertThat(event.getClassName()).isEqualTo(TEST_ACTIVITY_NAME);

    assertThat(events.hasNextEvent()).isFalse();
    assertThat(events.getNextEvent(event)).isFalse();
  }

  @Test
  public void testQueryEvents_appendEventData_simulateTimeChange_shouldAddOffsetToPreviousData()
      throws Exception {
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 500L, Event.MOVE_TO_FOREGROUND);
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME1, 1000L, Event.MOVE_TO_BACKGROUND);
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(1500L)
                .setPackage(TEST_PACKAGE_NAME2)
                .setClass(TEST_ACTIVITY_NAME)
                .setEventType(Event.MOVE_TO_FOREGROUND)
                .build());
    shadowOf(usageStatsManager).addEvent(TEST_PACKAGE_NAME2, 2000L, Event.MOVE_TO_BACKGROUND);
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(2500L)
                .setPackage(TEST_PACKAGE_NAME1)
                .setEventType(Event.MOVE_TO_FOREGROUND)
                .setClass(TEST_ACTIVITY_NAME)
                .build());
    shadowOf(usageStatsManager).simulateTimeChange(10000L);

    UsageEvents events = usageStatsManager.queryEvents(11000L, 12000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME1);
    assertThat(event.getTimeStamp()).isEqualTo(11000L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_BACKGROUND);

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(TEST_PACKAGE_NAME2);
    assertThat(event.getTimeStamp()).isEqualTo(11500L);
    assertThat(event.getEventType()).isEqualTo(Event.MOVE_TO_FOREGROUND);
    assertThat(event.getClassName()).isEqualTo(TEST_ACTIVITY_NAME);

    assertThat(events.hasNextEvent()).isFalse();
    assertThat(events.getNextEvent(event)).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testGetAppStandbyBucket_withPackageName() throws Exception {
    assertThat(shadowOf(usageStatsManager).getAppStandbyBuckets()).isEmpty();

    shadowOf(usageStatsManager).setAppStandbyBucket("app1", UsageStatsManager.STANDBY_BUCKET_RARE);
    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket("app1"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);
    assertThat(shadowOf(usageStatsManager).getAppStandbyBuckets().keySet()).containsExactly("app1");
    assertThat(shadowOf(usageStatsManager).getAppStandbyBuckets().get("app1"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);

    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket("app_unset"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_ACTIVE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testSetAppStandbyBuckets() throws Exception {
    assertThat(shadowOf(usageStatsManager).getAppStandbyBuckets()).isEmpty();
    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket("app1"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_ACTIVE);

    Map<String, Integer> appBuckets =
        Collections.singletonMap("app1", UsageStatsManager.STANDBY_BUCKET_RARE);
    shadowOf(usageStatsManager).setAppStandbyBuckets(appBuckets);

    assertThat(shadowOf(usageStatsManager).getAppStandbyBuckets()).isEqualTo(appBuckets);
    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket("app1"))
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testGetAppStandbyBucket_currentApp() throws Exception {
    shadowOf(usageStatsManager).setCurrentAppStandbyBucket(UsageStatsManager.STANDBY_BUCKET_RARE);
    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket())
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_RARE);
    ShadowUsageStatsManager.reset();
    assertThat(shadowOf(usageStatsManager).getAppStandbyBucket())
        .isEqualTo(UsageStatsManager.STANDBY_BUCKET_ACTIVE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testRegisterAppUsageObserver_uniqueObserverIds_shouldAddBothObservers() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageObservers())
        .containsExactly(
            AppUsageObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1),
            AppUsageObserver.build(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testRegisterAppUsageObserver_duplicateObserverIds_shouldOverrideExistingObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageObservers())
        .containsExactly(
            AppUsageObserver.build(
                12, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testUnregisterAppUsageObserver_existingObserverId_shouldRemoveObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    usageStatsManager.unregisterAppUsageObserver(12);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageObservers())
        .containsExactly(
            AppUsageObserver.build(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testUnregisterAppUsageObserver_nonExistentObserverId_shouldBeNoOp() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    usageStatsManager.unregisterAppUsageObserver(36);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageObservers())
        .containsExactly(
            AppUsageObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1),
            AppUsageObserver.build(
                24, ImmutableList.of("com.package3"), 456L, TimeUnit.SECONDS, pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testTriggerRegisteredAppUsageObserver_shouldSendIntentAndRemoveObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageObserver(
        12, new String[] {"com.package1", "com.package2"}, 123L, TimeUnit.MINUTES, pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageObserver(
        24, new String[] {"com.package3"}, 456L, TimeUnit.SECONDS, pendingIntent2);

    shadowOf(usageStatsManager).triggerRegisteredAppUsageObserver(24, 500000L);

    List<Intent> broadcastIntents = shadowOf(context).getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    Intent broadcastIntent = broadcastIntents.get(0);
    assertThat(broadcastIntent.getAction()).isEqualTo("ACTION2");
    assertThat(broadcastIntent.getIntExtra(UsageStatsManager.EXTRA_OBSERVER_ID, 0)).isEqualTo(24);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_LIMIT, 0))
        .isEqualTo(456000L);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_USED, 0))
        .isEqualTo(500000L);
    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageObservers())
        .containsExactly(
            AppUsageObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                123L,
                TimeUnit.MINUTES,
                pendingIntent1));
  }


  @Test
  public void queryUsageStats_noStatsAdded() {
    List<UsageStats> results = usageStatsManager.queryUsageStats(INTERVAL_WEEKLY, 0, 3000);
    assertThat(results).isEmpty();
  }

  @Test
  public void queryUsageStats() {
    UsageStats usageStats1 = newUsageStats(TEST_PACKAGE_NAME1, 0, 1000);
    UsageStats usageStats2 = newUsageStats(TEST_PACKAGE_NAME1, 1001, 2000);
    UsageStats usageStats3 = newUsageStats(TEST_PACKAGE_NAME1, 2001, 3000);
    UsageStats usageStats4 = newUsageStats(TEST_PACKAGE_NAME1, 3001, 4000);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats1);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats2);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats3);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats4);
    // Query fully covers usageStats 2 and 3, and partially overlaps with 4.
    List<UsageStats> results = usageStatsManager.queryUsageStats(INTERVAL_WEEKLY, 1001, 3500);
    assertThat(results).containsExactly(usageStats2, usageStats3, usageStats4);
  }

  @Test
  public void queryUsageStats_multipleIntervalTypes() {
    // Weekly data.
    UsageStats usageStats1 = newUsageStats(TEST_PACKAGE_NAME1, 1000, 2000);
    UsageStats usageStats2 = newUsageStats(TEST_PACKAGE_NAME1, 2001, 3000);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats1);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_WEEKLY, usageStats2);

    // Daily data.
    UsageStats usageStats3 = newUsageStats(TEST_PACKAGE_NAME1, 2001, 3000);
    shadowOf(usageStatsManager).addUsageStats(INTERVAL_DAILY, usageStats3);

    List<UsageStats> results = usageStatsManager.queryUsageStats(INTERVAL_WEEKLY, 0, 3000);
    assertThat(results).containsExactly(usageStats1, usageStats2);
    results = usageStatsManager.queryUsageStats(INTERVAL_DAILY, 0, 3000);
    assertThat(results).containsExactly(usageStats3);
  }

  @Test
  @Config(minSdk = Q)
  public void getUsageSource_setUsageSourceNotCalled_shouldReturnTaskRootActivityByDefault() {
    assertThat(usageStatsManager.getUsageSource())
        .isEqualTo(UsageStatsManager.USAGE_SOURCE_TASK_ROOT_ACTIVITY);
  }

  @Test
  @Config(minSdk = Q)
  public void getUsageSource_setUsageSourceCalled_shouldReturnSetValue() {
    shadowOf(usageStatsManager).setUsageSource(UsageStatsManager.USAGE_SOURCE_CURRENT_ACTIVITY);
    assertThat(usageStatsManager.getUsageSource())
        .isEqualTo(UsageStatsManager.USAGE_SOURCE_CURRENT_ACTIVITY);
  }

  private UsageStats newUsageStats(String packageName, long firstTimeStamp, long lastTimeStamp) {
    return UsageStatsBuilder.newBuilder()
        .setPackageName(packageName)
        .setFirstTimeStamp(firstTimeStamp)
        .setLastTimeStamp(lastTimeStamp)
        .build();
  }

  @Test
  public void usageStatsBuilder_noFieldsSet() {
    UsageStats usage =
        UsageStatsBuilder.newBuilder()
            // Don't set any fields; the object should still build.
            .build();
    assertThat(usage.getPackageName()).isNull();
    assertThat(usage.getFirstTimeStamp()).isEqualTo(0);
    assertThat(usage.getLastTimeStamp()).isEqualTo(0);
    assertThat(usage.getLastTimeUsed()).isEqualTo(0);
    assertThat(usage.getTotalTimeInForeground()).isEqualTo(0);
  }

  @Test
  public void usageStatsBuilder() {
    long firstTimestamp = 1_500_000_000_000L;
    long lastTimestamp = firstTimestamp + 10000;
    long lastTimeUsed = firstTimestamp + 100;
    long totalTimeInForeground = HOURS.toMillis(10);

    UsageStats usage =
        UsageStatsBuilder.newBuilder()
            // Set all fields
            .setPackageName(TEST_PACKAGE_NAME1)
            .setFirstTimeStamp(firstTimestamp)
            .setLastTimeStamp(lastTimestamp)
            .setLastTimeUsed(lastTimeUsed)
            .setTotalTimeInForeground(totalTimeInForeground)
            .build();
    assertThat(usage.getPackageName()).isEqualTo(TEST_PACKAGE_NAME1);
    assertThat(usage.getFirstTimeStamp()).isEqualTo(firstTimestamp);
    assertThat(usage.getLastTimeStamp()).isEqualTo(lastTimestamp);
    assertThat(usage.getLastTimeUsed()).isEqualTo(lastTimeUsed);
    assertThat(usage.getTotalTimeInForeground()).isEqualTo(totalTimeInForeground);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void eventBuilder() {
    Event event =
        ShadowUsageStatsManager.EventBuilder.buildEvent()
            .setEventType(Event.ACTIVITY_RESUMED)
            .setTimeStamp(123456)
            .setPackage("com.sample.pkg")
            .setClass("SampleClass")
            .setInstanceId(999)
            .setTaskRootPackage("org.example.root")
            .setTaskRootClass("RootKlass")
            .setAppStandbyBucket(50)
            .build();
    assertThat(event.getEventType()).isEqualTo(Event.ACTIVITY_RESUMED);
    assertThat(event.getTimeStamp()).isEqualTo(123456);
    assertThat(event.getPackageName()).isEqualTo("com.sample.pkg");
    assertThat(event.getClassName()).isEqualTo("SampleClass");
    assertThat(event.getInstanceId()).isEqualTo(999);
    assertThat(event.getTaskRootPackageName()).isEqualTo("org.example.root");
    assertThat(event.getTaskRootClassName()).isEqualTo("RootKlass");
    assertThat(event.getAppStandbyBucket()).isEqualTo(50);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testRegisterUsageSessionObserver_uniqueObserverIds_shouldAddBothObservers() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        24,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                Duration.ofMinutes(123L),
                Duration.ofSeconds(1L),
                sessionStepIntent1,
                sessionEndedIntent1),
            UsageSessionObserver.build(
                24,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void
  testRegisterUsageSessionObserver_duplicateObserverIds_shouldOverrideExistingObserver() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                12,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testUnregisterUsageSessionObserver_existingObserverId_shouldRemoveObserver() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        24,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    usageStatsManager.unregisterUsageSessionObserver(12);

    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                24,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testUnregisterUsageSessionObserver_nonExistentObserverId_shouldBeNoOp() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        24,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    usageStatsManager.unregisterUsageSessionObserver(36);

    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                Duration.ofMinutes(123L),
                Duration.ofSeconds(1L),
                sessionStepIntent1,
                sessionEndedIntent1),
            UsageSessionObserver.build(
                24,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testTriggerRegisteredSessionStepObserver_shouldSendIntentAndKeepObserver() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        24,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    shadowOf(usageStatsManager).triggerRegisteredSessionStepObserver(24, 500000L);

    List<Intent> broadcastIntents = shadowOf(context).getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    Intent broadcastIntent = broadcastIntents.get(0);
    assertThat(broadcastIntent.getAction()).isEqualTo("SESSION_STEP_ACTION2");
    assertThat(broadcastIntent.getIntExtra(UsageStatsManager.EXTRA_OBSERVER_ID, 0)).isEqualTo(24);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_LIMIT, 0))
        .isEqualTo(456000L);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_USED, 0))
        .isEqualTo(500000L);
    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                Duration.ofMinutes(123L),
                Duration.ofSeconds(1L),
                sessionStepIntent1,
                sessionEndedIntent1),
            UsageSessionObserver.build(
                24,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testTriggerRegisteredSessionEndedObserver_shouldSendIntentAndKeepObserver() {
    PendingIntent sessionStepIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION1"), 0);
    PendingIntent sessionEndedIntent1 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION1"), 0);
    usageStatsManager.registerUsageSessionObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        Duration.ofMinutes(123L),
        Duration.ofSeconds(1L),
        sessionStepIntent1,
        sessionEndedIntent1);
    PendingIntent sessionStepIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_STEP_ACTION2"), 0);
    PendingIntent sessionEndedIntent2 =
        PendingIntent.getBroadcast(context, 0, new Intent("SESSION_ENDED_ACTION2"), 0);
    usageStatsManager.registerUsageSessionObserver(
        24,
        new String[] {"com.package3"},
        Duration.ofSeconds(456L),
        Duration.ofMinutes(1L),
        sessionStepIntent2,
        sessionEndedIntent2);

    shadowOf(usageStatsManager).triggerRegisteredSessionEndedObserver(24);

    List<Intent> broadcastIntents = shadowOf(context).getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    Intent broadcastIntent = broadcastIntents.get(0);
    assertThat(broadcastIntent.getAction()).isEqualTo("SESSION_ENDED_ACTION2");
    assertThat(broadcastIntent.getIntExtra(UsageStatsManager.EXTRA_OBSERVER_ID, 0)).isEqualTo(24);
    assertThat(shadowOf(usageStatsManager).getRegisteredUsageSessionObservers())
        .containsExactly(
            UsageSessionObserver.build(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                Duration.ofMinutes(123L),
                Duration.ofSeconds(1L),
                sessionStepIntent1,
                sessionEndedIntent1),
            UsageSessionObserver.build(
                24,
                ImmutableList.of("com.package3"),
                Duration.ofSeconds(456L),
                Duration.ofMinutes(1L),
                sessionStepIntent2,
                sessionEndedIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testRegisterAppUsageLimitObserver_uniqueObserverIds_shouldAddBothObservers() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        /* timeLimit= */ Duration.ofMinutes(30),
        /* timeUsed= */ Duration.ofMinutes(10),
        pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        24,
        new String[] {"com.package3"},
        /* timeLimit= */ Duration.ofMinutes(5),
        /* timeUsed= */ Duration.ofMinutes(1),
        pendingIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageLimitObservers())
        .containsExactly(
            new AppUsageLimitObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                /* timeLimit= */ Duration.ofMinutes(30),
                /* timeUsed= */ Duration.ofMinutes(10),
                pendingIntent1),
            new AppUsageLimitObserver(
                24,
                ImmutableList.of("com.package3"),
                /* timeLimit= */ Duration.ofMinutes(5),
                /* timeUsed= */ Duration.ofMinutes(1),
                pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void
      testRegisterAppUsageLimitObserver_duplicateObserverIds_shouldOverrideExistingObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        /* timeLimit= */ Duration.ofMinutes(30),
        /* timeUsed= */ Duration.ofMinutes(10),
        pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package3"},
        /* timeLimit= */ Duration.ofMinutes(5),
        /* timeUsed= */ Duration.ofMinutes(1),
        pendingIntent2);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageLimitObservers())
        .containsExactly(
            new AppUsageLimitObserver(
                12,
                ImmutableList.of("com.package3"),
                /* timeLimit= */ Duration.ofMinutes(5),
                /* timeUsed= */ Duration.ofMinutes(1),
                pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testUnregisterAppUsageLimitObserver_existingObserverId_shouldRemoveObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        /* timeLimit= */ Duration.ofMinutes(30),
        /* timeUsed= */ Duration.ofMinutes(10),
        pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        24,
        new String[] {"com.package3"},
        /* timeLimit= */ Duration.ofMinutes(5),
        /* timeUsed= */ Duration.ofMinutes(1),
        pendingIntent2);

    usageStatsManager.unregisterAppUsageLimitObserver(12);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageLimitObservers())
        .containsExactly(
            new AppUsageLimitObserver(
                24,
                ImmutableList.of("com.package3"),
                /* timeLimit= */ Duration.ofMinutes(5),
                /* timeUsed= */ Duration.ofMinutes(1),
                pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testUnregisterAppUsageLimitObserver_nonExistentObserverId_shouldBeNoOp() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        /* timeLimit= */ Duration.ofMinutes(30),
        /* timeUsed= */ Duration.ofMinutes(10),
        pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        24,
        new String[] {"com.package3"},
        /* timeLimit= */ Duration.ofMinutes(5),
        /* timeUsed= */ Duration.ofMinutes(1),
        pendingIntent2);

    usageStatsManager.unregisterAppUsageLimitObserver(36);

    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageLimitObservers())
        .containsExactly(
            new AppUsageLimitObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                /* timeLimit= */ Duration.ofMinutes(30),
                /* timeUsed= */ Duration.ofMinutes(10),
                pendingIntent1),
            new AppUsageLimitObserver(
                24,
                ImmutableList.of("com.package3"),
                /* timeLimit= */ Duration.ofMinutes(5),
                /* timeUsed= */ Duration.ofMinutes(1),
                pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testTriggerRegisteredAppUsageLimitObserver_shouldSendIntentAndKeepObserver() {
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION1"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        12,
        new String[] {"com.package1", "com.package2"},
        /* timeLimit= */ Duration.ofMinutes(30),
        /* timeUsed= */ Duration.ofMinutes(10),
        pendingIntent1);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, new Intent("ACTION2"), 0);
    usageStatsManager.registerAppUsageLimitObserver(
        24,
        new String[] {"com.package3"},
        /* timeLimit= */ Duration.ofMinutes(5),
        /* timeUsed= */ Duration.ofMinutes(1),
        pendingIntent2);

    shadowOf(usageStatsManager).triggerRegisteredAppUsageLimitObserver(24, Duration.ofMinutes(3));

    List<Intent> broadcastIntents = shadowOf(context).getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    Intent broadcastIntent = broadcastIntents.get(0);
    assertThat(broadcastIntent.getAction()).isEqualTo("ACTION2");
    assertThat(broadcastIntent.getIntExtra(UsageStatsManager.EXTRA_OBSERVER_ID, 0)).isEqualTo(24);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_LIMIT, 0))
        .isEqualTo(300_000L);
    assertThat(broadcastIntent.getLongExtra(UsageStatsManager.EXTRA_TIME_USED, 0))
        .isEqualTo(180_000L);
    assertThat(shadowOf(usageStatsManager).getRegisteredAppUsageLimitObservers())
        .containsExactly(
            new AppUsageLimitObserver(
                12,
                ImmutableList.of("com.package1", "com.package2"),
                /* timeLimit= */ Duration.ofMinutes(30),
                /* timeUsed= */ Duration.ofMinutes(10),
                pendingIntent1),
            new AppUsageLimitObserver(
                24,
                ImmutableList.of("com.package3"),
                /* timeLimit= */ Duration.ofMinutes(5),
                /* timeUsed= */ Duration.ofMinutes(1),
                pendingIntent2));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void testQueryEventsForSelf_shouldReturnsEventsForCurrentPackageOnly() {
    String packageName = context.getOpPackageName();
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(1500L)
                .setPackage(TEST_PACKAGE_NAME2)
                .setClass(TEST_ACTIVITY_NAME)
                .setEventType(Event.ACTIVITY_PAUSED)
                .build());
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(2200L)
                .setPackage(packageName)
                .setEventType(Event.ACTIVITY_RESUMED)
                .setClass(TEST_ACTIVITY_NAME)
                .build());
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(2500L)
                .setPackage(TEST_PACKAGE_NAME1)
                .setEventType(Event.ACTIVITY_RESUMED)
                .setClass(TEST_ACTIVITY_NAME)
                .build());
    shadowOf(usageStatsManager)
        .addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setTimeStamp(2800L)
                .setPackage(packageName)
                .setEventType(Event.ACTIVITY_STOPPED)
                .setClass(TEST_ACTIVITY_NAME)
                .build());

    UsageEvents events = usageStatsManager.queryEventsForSelf(0L, 3000L);
    Event event = new Event();

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(packageName);
    assertThat(event.getTimeStamp()).isEqualTo(2200L);
    assertThat(event.getEventType()).isEqualTo(Event.ACTIVITY_RESUMED);

    assertThat(events.hasNextEvent()).isTrue();
    assertThat(events.getNextEvent(event)).isTrue();
    assertThat(event.getPackageName()).isEqualTo(packageName);
    assertThat(event.getTimeStamp()).isEqualTo(2800L);
    assertThat(event.getEventType()).isEqualTo(Event.ACTIVITY_STOPPED);

    assertThat(events.hasNextEvent()).isFalse();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addBroadcastResponseStats_returnsSeededData() {
    BroadcastResponseStats stats = new BroadcastResponseStats(APP_1, BUCKET_ID_1);

    shadowOf(usageStatsManager).addBroadcastResponseStats(stats);

    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_1, BUCKET_ID_1))
        .containsExactly(stats);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addBroadcastResponseStats_multipleApps_returnsCorrectStats() {
    BroadcastResponseStats app1Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_1);
    BroadcastResponseStats app2Stats = new BroadcastResponseStats(APP_2, BUCKET_ID_1);

    shadowOf(usageStatsManager).addBroadcastResponseStats(app1Stats);
    shadowOf(usageStatsManager).addBroadcastResponseStats(app2Stats);

    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_1, BUCKET_ID_1))
        .containsExactly(app1Stats);
    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_2, BUCKET_ID_1))
        .containsExactly(app2Stats);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addBroadcastResponseStats_multipleBuckets_returnsCorrectStats() {
    BroadcastResponseStats bucket1Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_1);
    BroadcastResponseStats bucket2Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_2);

    shadowOf(usageStatsManager).addBroadcastResponseStats(bucket1Stats);
    shadowOf(usageStatsManager).addBroadcastResponseStats(bucket2Stats);

    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_1, BUCKET_ID_1))
        .containsExactly(bucket1Stats);
    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_1, BUCKET_ID_2))
        .containsExactly(bucket2Stats);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void queryBroadcastResponseStats_zeroBucketId_returnsAllBuckets() {
    BroadcastResponseStats bucket1Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_1);
    BroadcastResponseStats bucket2Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_2);

    shadowOf(usageStatsManager).addBroadcastResponseStats(bucket1Stats);
    shadowOf(usageStatsManager).addBroadcastResponseStats(bucket2Stats);

    assertThat(usageStatsManager.queryBroadcastResponseStats(APP_1, 0))
        .containsExactly(bucket1Stats, bucket2Stats);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void addBroadcastResponseStats_nullApp_returnsAllApps() {
    BroadcastResponseStats app1Stats = new BroadcastResponseStats(APP_1, BUCKET_ID_1);
    BroadcastResponseStats app2Stats = new BroadcastResponseStats(APP_2, BUCKET_ID_1);

    shadowOf(usageStatsManager).addBroadcastResponseStats(app1Stats);
    shadowOf(usageStatsManager).addBroadcastResponseStats(app2Stats);

    assertThat(usageStatsManager.queryBroadcastResponseStats(null, BUCKET_ID_1))
        .containsExactly(app1Stats, app2Stats);
  }
}
