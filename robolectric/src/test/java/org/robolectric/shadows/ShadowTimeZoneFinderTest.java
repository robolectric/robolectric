package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.icu.util.TimeZone;
import java.util.stream.Collectors;
import libcore.util.TimeZoneFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowTimeZoneFinder}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowTimeZoneFinderTest {

  @Test
  @Config(minSdk = O)
  public void test() {
    TimeZoneFinder timeZoneFinder = TimeZoneFinder.getInstance();
    assertThat(
            timeZoneFinder
                .lookupTimeZonesByCountry("us")
                .stream()
                .map(TimeZone::getID)
                .collect(Collectors.toList()))
        .containsAllOf("America/Los_Angeles", "America/New_York", "Pacific/Honolulu");
  }
}
