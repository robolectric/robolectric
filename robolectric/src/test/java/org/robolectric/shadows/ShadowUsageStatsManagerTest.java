package org.robolectric.shadows;

import static android.content.Context.USAGE_STATS_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.os.Build;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

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

}
