package android.text.format;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests that Robolectric's android.text.format.DateFormat support is consistent with device. */
@RunWith(AndroidJUnit4.class)
@DoNotInstrument
@Config(qualifiers = "+en-rUS")
public class DateFormatTest {

  private Locale originalLocale;
  private TimeZone originalTimeZone;
  private Date dateAM;
  private Date datePM;

  @Before
  public void setDate() {
    // Always set default Locale+Timezone in any time-related unit test to
    // avoid flakiness when testing in non-US environments.
    originalLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    originalTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    Calendar c = Calendar.getInstance();
    c.set(2000, 10, 25, 8, 24, 30);
    dateAM = c.getTime();
    c.set(2000, 10, 25, 16, 24, 30);
    datePM = c.getTime();
  }

  @After
  public void tearDown() throws Exception {
    if (originalTimeZone != null) {
      TimeZone.setDefault(originalTimeZone);
    }
    if (originalLocale != null) {
      Locale.setDefault(originalLocale);
    }
  }

  @Test
  public void getLongDateFormat() {
    assertThat(DateFormat.getLongDateFormat(getApplicationContext()).format(dateAM))
        .isEqualTo("November 25, 2000");
  }

  @Test
  public void getMediumDateFormat() {
    assertThat(DateFormat.getMediumDateFormat(getApplicationContext()).format(dateAM))
        .isEqualTo("Nov 25, 2000");
  }

  @SdkSuppress(maxSdkVersion = 22)
  @Config(maxSdk = 22)
  @Test
  public void getDateFormat_pre23() {
    assertThat(DateFormat.getDateFormat(getApplicationContext()).format(dateAM))
        .isEqualTo("11/25/2000");
  }

  @SdkSuppress(minSdkVersion = 23)
  @Config(minSdk = 23)
  @Test
  public void getDateFormat() {
    assertThat(DateFormat.getDateFormat(getApplicationContext()).format(dateAM))
        .isEqualTo("11/25/00");
  }

  @Test
  public void getTimeFormat_am() {
    // Allow both ASCII and Unicode space-class separators.
    // Output may also contain U+202F (UTF-8 E2 80 AF), a.k.a. NARROW NO-BREAK SPACE
    // which is in the \p{Zs} Unicode category.
    assertThat(DateFormat.getTimeFormat(getApplicationContext()).format(dateAM))
        .matches("8:24\\p{Z}AM");
  }

  @Test
  public void getTimeFormat_pm() {
    // Allow both ASCII and Unicode space-class separators.
    assertThat(DateFormat.getTimeFormat(getApplicationContext()).format(datePM))
        .matches("4:24\\p{Z}PM");
  }
}
