package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowTest {

  private ClassLoader myClassLoader;

  @Before
  public void setUp() throws Exception {
    myClassLoader = getClass().getClassLoader();
  }

  @Test
  public void newInstanceOf() throws Exception {
    assertThat(Shadow.newInstanceOf(Activity.class.getName()).getClass().getClassLoader())
        .isSameAs(myClassLoader);
  }

  @Test
  public void extractor() throws Exception {
    Activity activity = new Activity();
    assertThat((ShadowActivity) Shadow.extract(activity)).isSameAs(shadowOf(activity));
  }

  @Test
  public void otherDeprecated_extractor() throws Exception {
    Activity activity = new Activity();
    assertThat(Shadow.<Object>extract(activity)).isSameAs(shadowOf(activity));
  }
}
