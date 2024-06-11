package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.fonts.Font;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeFontTest {
  private static final String FONT_DIR = "fonts/family_selection/ttf/";

  @Test
  public void initializeBuilderWithPath() throws Exception {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font font = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    assertThat(font).isNotNull();
    assertThat(font.getStyle().getWeight()).isEqualTo(400);
  }

  @Test
  public void initializeBuilderWithResource() throws Exception {
    Resources res = RuntimeEnvironment.getApplication().getResources();
    Font font = new Font.Builder(res, R.font.a3em).build();
    assertThat(font).isNotNull();
    assertThat(font.getStyle().getWeight()).isEqualTo(400);
  }
}
