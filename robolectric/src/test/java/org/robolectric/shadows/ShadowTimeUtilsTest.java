package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.TimeUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowTimeUtilsTest {
  @Test
  public void testGetTimeZoneIdsForCountryCode() {
    ShadowTimeUtils.setTimeZoneIdsForCountryCode(
        "us", new String[] {"America/Los_Angeles", "America/New_York"});
    List<String> usTimeZones = TimeUtils.getTimeZoneIdsForCountryCode("us");

    // Sample the content without being too exact.
    assertCollectionContains(usTimeZones, "America/Los_Angeles");
    assertCollectionContains(usTimeZones, "America/New_York");

    // Assert we don't care about casing of the country code.
    assertEquals(usTimeZones, TimeUtils.getTimeZoneIdsForCountryCode("US"));
    assertEquals(usTimeZones, TimeUtils.getTimeZoneIdsForCountryCode("uS"));
    assertEquals(usTimeZones, TimeUtils.getTimeZoneIdsForCountryCode("Us"));
  }

  @Test
  public void testGetTimeZoneIdsForCountryCode_unknownCountryCode() {
    String unknownCountryCode = "zx81";
    assertNull(TimeUtils.getTimeZoneIdsForCountryCode(unknownCountryCode));
  }

  @Test
  public void testGetTimeZoneIdsForCountryCode_nullCountryCode() {
    try {
      TimeUtils.getTimeZoneIdsForCountryCode(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  private static <T> void assertCollectionContains(Collection<? super T> collection, T value) {
    assertTrue(collection + " should contain " + value, collection.contains(value));
  }
}
