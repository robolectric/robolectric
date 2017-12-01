package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
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
