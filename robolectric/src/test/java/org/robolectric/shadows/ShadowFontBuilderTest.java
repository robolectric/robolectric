package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.fonts.Font;
import android.graphics.fonts.FontStyle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.TestUtil;

/** Tests for {@link org.robolectric.shadows.ShadowFontBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowFontBuilderTest {
  private File fontFile;

  @Before
  public void setup() {
    fontFile = TestUtil.resourcesBaseDir().resolve("assets/myFont.ttf").toFile();
  }

  @Test
  public void fontBuilder_defaultWeightAndSlant() throws IOException {
    Font font = new Font.Builder(fontFile).build();
    assertThat(font.getStyle().getWeight()).isEqualTo(FontStyle.FONT_WEIGHT_NORMAL);
    assertThat(font.getStyle().getSlant()).isEqualTo(FontStyle.FONT_SLANT_UPRIGHT);
  }

  @Test
  public void fontBuilder_toString() throws IOException {
    Font font = new Font.Builder(fontFile).build();
    assertThat(font.toString()).isNotNull();
  }
}
