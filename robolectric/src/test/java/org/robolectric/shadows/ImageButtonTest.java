package org.robolectric.shadows;

import android.widget.ImageButton;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResourceLoader;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ImageButtonTest {

  @Test @Ignore("Not how Android behaves as of Jelly Bean.")
  public void testBackground() throws Exception {
    ResourceLoader resourceLoader = shadowOf(RuntimeEnvironment.application).getResourceLoader();
    RoboAttributeSet attrs = new RoboAttributeSet(new ArrayList<Attribute>(), resourceLoader);
    ImageButton button = new ImageButton(RuntimeEnvironment.application, attrs);
    assertThat(button.getBackground()).isNotNull();
  }
}
