package org.robolectric.shadows.support.v4;

import android.content.res.Resources;
import android.support.graphics.drawable.VectorDrawableCompat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "../../robolectric/src/test/resources/AndroidManifest.xml")
public class ShadowVectorDrawableCompatTest {

  @Test
  public void createVectorDrawable() throws Exception {
    Resources resources = RuntimeEnvironment.application.getResources();
    VectorDrawableCompat.create(resources, R.drawable.square, null);
  }
}
