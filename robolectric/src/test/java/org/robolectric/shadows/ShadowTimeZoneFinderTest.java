package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Build.VERSION;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit tests for {@link ShadowTimeZoneFinder}. */
@RunWith(AndroidJUnit4.class)
public class ShadowTimeZoneFinderTest {

  @Test
  @Config(minSdk = O, maxSdk = Q)
  public void lookupTimeZonesByCountry_shouldReturnExpectedTimeZones() throws Exception {
    String className =
        isExperimental() ? "libcore.timezone.TimeZoneFinder" : "libcore.util.TimeZoneFinder";
    Class<?> cls = Class.forName(className);
    Object timeZoneFinder = ReflectionHelpers.callStaticMethod(cls, "getInstance");
    List<TimeZone> timezones =
        ReflectionHelpers.callInstanceMethod(
            cls,
            timeZoneFinder,
            "lookupTimeZonesByCountry",
            ClassParameter.from(String.class, "us"));

    assertThat(timezones.stream().map(TimeZone::getID).collect(Collectors.toList()))
        .containsAllOf("America/Los_Angeles", "America/New_York", "Pacific/Honolulu");
  }

  private boolean isExperimental() {
    try {
      return Integer.parseInt(VERSION.INCREMENTAL) > 5283294;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
