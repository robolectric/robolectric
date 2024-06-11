/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.res.AssetManager;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.graphics.fonts.FontStyle;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeFontFamilyTest {
  private static final String FONT_DIR = "fonts/family_selection/ttf/";

  @Test
  public void testBuilder_singleFont() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font font = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    FontFamily family = new FontFamily.Builder(font).build();
    assertNotNull(family);
    assertEquals(1, family.getSize());
    assertEquals(font, family.getFont(0));
  }

  @Test
  @Config(sdk = 29)
  public void testBuilder_multipleFont() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font boldFont = new Font.Builder(am, FONT_DIR + "ascii_m3em_weight700_upright.ttf").build();
    FontFamily family = new FontFamily.Builder(regularFont).addFont(boldFont).build();
    assertNotNull(family);
    assertEquals(2, family.getSize());
    assertNotSame(family.getFont(0), family.getFont(1));
    assertTrue(family.getFont(0).equals(regularFont) || family.getFont(0).equals(boldFont));
    assertTrue(family.getFont(1).equals(regularFont) || family.getFont(1).equals(boldFont));
  }

  @Test
  public void testBuilder_multipleFont_overrideWeight() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font boldFont =
        new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").setWeight(700).build();
    FontFamily family = new FontFamily.Builder(regularFont).addFont(boldFont).build();
    assertNotNull(family);
    assertEquals(2, family.getSize());
    assertNotSame(family.getFont(0), family.getFont(1));
    assertTrue(family.getFont(0).equals(regularFont) || family.getFont(0).equals(boldFont));
    assertTrue(family.getFont(1).equals(regularFont) || family.getFont(1).equals(boldFont));
  }

  @Test
  public void testBuilder_multipleFont_overrideItalic() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font italicFont =
        new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf")
            .setSlant(FontStyle.FONT_SLANT_ITALIC)
            .build();
    FontFamily family = new FontFamily.Builder(regularFont).addFont(italicFont).build();
    assertNotNull(family);
    assertEquals(2, family.getSize());
    assertNotSame(family.getFont(0), family.getFont(1));
    assertTrue(family.getFont(0).equals(regularFont) || family.getFont(0).equals(italicFont));
    assertTrue(family.getFont(1).equals(regularFont) || family.getFont(1).equals(italicFont));
  }

  @Test
  public void testBuilder_multipleFont_sameStyle() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font regularFont2 = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    assertThrows(
        IllegalArgumentException.class,
        () -> new FontFamily.Builder(regularFont).addFont(regularFont2).build());
  }

  @Test
  public void testBuilder_multipleFont_sameStyle_overrideWeight() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font regularFont2 =
        new Font.Builder(am, FONT_DIR + "ascii_m3em_weight700_upright.ttf").setWeight(400).build();
    assertThrows(
        IllegalArgumentException.class,
        () -> new FontFamily.Builder(regularFont).addFont(regularFont2).build());
  }

  @Test
  public void testBuilder_multipleFont_sameStyle_overrideItalic() throws IOException {
    AssetManager am = RuntimeEnvironment.getApplication().getAssets();
    Font regularFont = new Font.Builder(am, FONT_DIR + "ascii_g3em_weight400_upright.ttf").build();
    Font regularFont2 =
        new Font.Builder(am, FONT_DIR + "ascii_h3em_weight400_italic.ttf")
            .setSlant(FontStyle.FONT_SLANT_UPRIGHT)
            .build();
    assertThrows(
        IllegalArgumentException.class,
        () -> new FontFamily.Builder(regularFont).addFont(regularFont2).build());
  }
}
