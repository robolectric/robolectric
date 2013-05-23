package org.robolectric.res;

import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.I18nException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ResourceLoaderTest {
  @Test(expected = I18nException.class)
  public void shouldThrowExceptionOnI18nStrictModeInflatePreferences() throws Exception {
    shadowOf(Robolectric.application).setStrictI18n(true);
    PreferenceActivity preferenceActivity = new PreferenceActivity() {
    };
    preferenceActivity.addPreferencesFromResource(R.xml.preferences);
  }

  @Test
  @Config(qualifiers = "doesnotexist-land-xlarge")
  public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
    View view = LayoutInflater.from(Robolectric.application).inflate(R.layout.different_screen_sizes, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("land");
  }

  @Test
  public void checkForPollution1() throws Exception {
    checkForPollutionHelper();
  }

  @Test
  public void checkForPollution2() throws Exception {
    checkForPollutionHelper();
  }

  private void checkForPollutionHelper() {
    View view = LayoutInflater.from(Robolectric.application).inflate(R.layout.different_screen_sizes, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("default");
    Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).overrideQualifiers("land"); // testing if this pollutes the other test
  }

  @Test
  public void shouldMakeInternalResourcesAvailable() throws Exception {
    ResourceLoader resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
    ResName internalResource = new ResName("android", "string", "badPin");
    Integer resId = resourceLoader.getResourceIndex().getResourceId(internalResource);
    assertThat(resId).isNotNull();
    assertThat(resourceLoader.getResourceIndex().getResName(resId)).isEqualTo(internalResource);

    Class<?> internalRIdClass = Robolectric.class.getClassLoader().loadClass("com.android.internal.R$" + internalResource.type);
    assertThat(resId).isEqualTo(field(internalResource.name).ofType(int.class).in(internalRIdClass).get());

    assertThat(Robolectric.application.getResources().getString(resId)).isEqualTo("The old PIN you typed isn\\'t correct.");
  }
}
