package androidx.core.content.res;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Typeface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.R;

/**
 * Compatibility test for {@link ResourcesCompat}
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ResourcesCompatTest {

  @Test
  public void getFont() {
    Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.vt323_regular);
    assertThat(typeface).isNotNull();
  }
}
