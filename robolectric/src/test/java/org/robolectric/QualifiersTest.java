package org.robolectric;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@Config(qualifiers = "en")
@RunWith(TestRunners.SelfTest.class)
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

  @Config(qualifiers = "en")
  @Test public void shouldBeEnglish() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("en");
  }

  @Config(qualifiers = "ja")
  @Test public void shouldBeJapanese() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("ja");
  }

  @Config(qualifiers = "fr")
  @Test public void shouldBeFrench() {
    Locale locale = resources.getConfiguration().locale;
    assertThat(locale.getLanguage()).isEqualTo("fr");
  }

  @Test
  public void shouldGetFromClass() throws Exception {
    assertThat(RuntimeEnvironment.getQualifiers()).contains("en");
  }

  @Test @Config(qualifiers = "fr")
  public void shouldGetFromMethod() throws Exception {
    assertThat(RuntimeEnvironment.getQualifiers()).contains("fr");
  }

  @Test @Config(qualifiers = "de")
  public void getQuantityString() throws Exception {
    assertThat(RuntimeEnvironment.application.getResources().getQuantityString(R.plurals.minute, 2)).isEqualTo(RuntimeEnvironment.application.getResources().getString(R.string.minute_plural));
  }

  @Test
  public void inflateLayout_defaultsTo_sw320dp() throws Exception {
    View view = Robolectric.setupActivity(Activity.class).getLayoutInflater().inflate(R.layout.layout_smallest_width, null);
    TextView textView = (TextView) view.findViewById(R.id.text1);
    assertThat(textView.getText()).isEqualTo("320");

    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().smallestScreenWidthDp).isEqualTo(320);
  }

  @Test @Config(qualifiers = "sw720dp")
  public void inflateLayout_overridesTo_sw720dp() throws Exception {
    View view = Robolectric.setupActivity(Activity.class).getLayoutInflater().inflate(R.layout.layout_smallest_width, null);
    TextView textView = (TextView) view.findViewById(R.id.text1);
    assertThat(textView.getText()).isEqualTo("720");

    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().smallestScreenWidthDp).isEqualTo(720);
  }

  @Test
  public void defaultScreenWidth() {
    assertThat(RuntimeEnvironment.application.getResources().getBoolean(R.bool.value_only_present_in_w320dp)).isTrue();
    assertThat(RuntimeEnvironment.application.getResources().getConfiguration().screenWidthDp).isEqualTo(320);
  }
}
