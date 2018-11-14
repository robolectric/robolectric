package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import libcore.icu.ICU;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowICUTest {
  @Test
  public void getBestDateTimePattern_returnsReasonableValue() {
    assertThat(ICU.getBestDateTimePattern("hm", null)).isEqualTo("hm");
  }

  @Test
  public void getBestDateTimePattern_returns_jmm_US() {
    assertThat(ICU.getBestDateTimePattern("jmm", Locale.US)).isEqualTo("h:mm a");
  }

  @Test
  public void getBestDateTimePattern_returns_jmm_UK() {
    assertThat(ICU.getBestDateTimePattern("jmm", Locale.UK)).isEqualTo("H:mm");
  }

  @Test
  public void getBestDateTimePattern_returns_jmm_ptBR() {
    assertThat(ICU.getBestDateTimePattern("jmm", new Locale("pt", "BR"))).isEqualTo("H:mm");
  }


  @Test
  public void datePickerShouldNotCrashWhenAskingForBestDateTimePattern() {
    ActivityController<DatePickerActivity> activityController = Robolectric.buildActivity(DatePickerActivity.class);
    activityController.setup();
  }

  private static class DatePickerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);
      DatePicker datePicker = new DatePicker(this);
      view.addView(datePicker);

      setContentView(view);
    }
  }
}