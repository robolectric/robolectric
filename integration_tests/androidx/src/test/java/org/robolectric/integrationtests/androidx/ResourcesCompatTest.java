package org.robolectric.integrationtests.androidx;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Typeface;
import androidx.core.content.res.ResourcesCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.R;

/** Compatibility test for {@link ResourcesCompat} */
@RunWith(AndroidJUnit4.class)
public class ResourcesCompatTest {

  @Test
  public void getFont() {
    Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.vt323_regular);
    assertThat(typeface).isNotNull();
  }

  @Test
  public void getFontFamily() {
    Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.vt323);
    assertThat(typeface).isNotNull();
  }
}
