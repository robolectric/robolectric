package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Locale;

@Config(qualifiers = "en")
@RunWith(TestRunners.MultiApiSelfTest.class)
public class QualifiersTest {

  private Resources resources;

  @Before
  public void setUp() {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  @Config(qualifiers = "land")
  public void orientation() throws Exception {
    assertThat(resources.getConfiguration().orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

  @Test public void shouldBeEnglish_fromClassConfig() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("en");
  }

  @Config(qualifiers = "ja")
  @Test public void shouldBeJapanese() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("ja");
  }

  @Config(qualifiers = "fr-rCA")
  @Test
  public void locale() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("fr");
    assertThat(locale.getCountry()).isEqualTo("CA");
  }

  @Config(minSdk = VERSION_CODES.N, qualifiers = "fr-rCA")
  @Test
  public void localeList() {
    LocaleList localeList = resources.getConfiguration().getLocales();
    assertThat(localeList.get(0).getLanguage()).isEqualTo("fr");
    assertThat(localeList.get(0).getCountry()).isEqualTo("CA");  }

  @Test @Config(qualifiers = "de")
  public void getQuantityString() throws Exception {
    assertThat(RuntimeEnvironment.application.getResources().getQuantityString(R.plurals.minute, 2)).isEqualTo(RuntimeEnvironment.application.getResources().getString(R.string.minute_plural));
  }

  @Test
  public void inflateLayout_defaultsTo_sw320dp() throws Exception {
    View view = Robolectric.setupActivity(Activity.class).getLayoutInflater().inflate(R.layout.layout_smallest_width, null);
    TextView textView = view.findViewById(R.id.text1);
    assertThat(textView.getText()).isEqualTo("320");

    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().smallestScreenWidthDp).isEqualTo(320);
  }

  @Test @Config(qualifiers = "sw720dp")
  public void inflateLayout_overridesTo_sw720dp() throws Exception {
    View view = Robolectric.setupActivity(Activity.class).getLayoutInflater().inflate(R.layout.layout_smallest_width, null);
    TextView textView = view.findViewById(R.id.text1);
    assertThat(textView.getText()).isEqualTo("720");

    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().smallestScreenWidthDp).isEqualTo(720);
  }

  @Test
  public void defaultScreenWidth() {
    assertThat(RuntimeEnvironment.application.getResources().getBoolean(R.bool.value_only_present_in_w320dp)).isTrue();
    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().screenWidthDp).isEqualTo(320);
  }
}
