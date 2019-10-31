package android.text.format;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests that Robolectric's android.text.format.DateFormat support is consistent with device. */
@RunWith(AndroidJUnit4.class)
@DoNotInstrument
public class DateFormatTest {

  private Date date;

  @Before
  public void setDate() {
    Calendar c = Calendar.getInstance();
    c.set(2000, 10, 25, 8, 24, 30);
    date = c.getTime();
  }

  @Test
  public void getLongDateFormat() {
    assertThat(DateFormat.getLongDateFormat(getApplicationContext()).format(date))
        .isEqualTo("November 25, 2000");
  }

  @Test
  public void getMediumDateFormat() {
    assertThat(DateFormat.getMediumDateFormat(getApplicationContext()).format(date))
        .isEqualTo("Nov 25, 2000");
  }

  @SdkSuppress(maxSdkVersion = 22)
  @Config(maxSdk = 22)
  @Test
  public void getDateFormat_pre23() {
    assertThat(DateFormat.getDateFormat(getApplicationContext()).format(date))
        .isEqualTo("11/25/2000");
  }

  @SdkSuppress(minSdkVersion = 23)
  @Config(minSdk = 23)
  @Test
  public void getDateFormat() {
    assertThat(DateFormat.getDateFormat(getApplicationContext()).format(date))
        .isEqualTo("11/25/00");
  }
}
