/*
 * Copyright (C) 2022 The Android Open Source Project
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

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.ColorSpace;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeColorSpaceTest {
  // Column-major RGB->XYZ transform matrix for the sRGB color space
  private static final float[] SRGB_TO_XYZ = {
    0.412391f, 0.212639f, 0.019331f,
    0.357584f, 0.715169f, 0.119195f,
    0.180481f, 0.072192f, 0.950532f
  };
  // Column-major XYZ->RGB transform matrix for the sRGB color space
  private static final float[] XYZ_TO_SRGB = {
    3.240970f, -0.969244f, 0.055630f,
    -1.537383f, 1.875968f, -0.203977f,
    -0.498611f, 0.041555f, 1.056971f
  };

  // Column-major RGB->XYZ transform matrix for the sRGB color space and a D50 white point
  private static final float[] SRGB_TO_XYZ_D50 = {
    0.4360747f, 0.2225045f, 0.0139322f,
    0.3850649f, 0.7168786f, 0.0971045f,
    0.1430804f, 0.0606169f, 0.7141733f
  };

  private static final float[] rGBPRIMARIESXyY = {0.640f, 0.330f, 0.300f, 0.600f, 0.150f, 0.060f};
  private static final float[] sRGBWHITEPOINTXyY = {0.3127f, 0.3290f};

  private static final float[] SRGB_PRIMARIES_XYZ = {
    1.939394f, 1.000000f, 0.090909f,
    0.500000f, 1.000000f, 0.166667f,
    2.500000f, 1.000000f, 13.166667f
  };
  private static final float[] SRGB_WHITE_POINT_XYZ = {0.950456f, 1.000f, 1.089058f};

  private static final DoubleUnaryOperator identity = DoubleUnaryOperator.identity();

  @Test
  public void testNamedColorSpaces() {
    for (ColorSpace.Named named : ColorSpace.Named.values()) {
      // OK_LAB is behind a feature flag that is not yet enabled by default.
      if (Objects.equals(named.toString(), "OK_LAB")) {
        continue;
      }

      ColorSpace colorSpace = ColorSpace.get(named);
      assertNotNull(colorSpace.getName());
      assertNotNull(colorSpace);
      assertEquals(named.ordinal(), colorSpace.getId());
      assertTrue(colorSpace.getComponentCount() >= 1);
      assertTrue(colorSpace.getComponentCount() <= 4);
    }
  }

  @Test
  public void testNullName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ColorSpace.Rgb(null, new float[6], new float[2], identity, identity, 0.0f, 1.0f));
  }

  @Test
  public void testEmptyName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ColorSpace.Rgb("", new float[6], new float[2], identity, identity, 0.0f, 1.0f));
  }

  @Test
  public void testName() {
    ColorSpace.Rgb cs =
        new ColorSpace.Rgb("Test", new float[6], new float[2], identity, identity, 0.0f, 1.0f);
    assertEquals("Test", cs.getName());
  }

  @Test
  public void testPrimariesLength() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ColorSpace.Rgb("Test", new float[7], new float[2], identity, identity, 0.0f, 1.0f));
  }

  @Test
  public void testWhitePointLength() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ColorSpace.Rgb("Test", new float[6], new float[1], identity, identity, 0.0f, 1.0f));
  }

  @Test
  public void testNullOETF() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ColorSpace.Rgb("Test", new float[6], new float[2], null, identity, 0.0f, 1.0f));
  }

  @Test
  public void testOETF() {
    DoubleUnaryOperator op = Math::sqrt;
    ColorSpace.Rgb cs =
        new ColorSpace.Rgb("Test", new float[6], new float[2], op, identity, 0.0f, 1.0f);
    assertEquals(0.5, cs.getOetf().applyAsDouble(0.25), 1e-5);
  }

  @Test
  public void testNullEOTF() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ColorSpace.Rgb("Test", new float[6], new float[2], identity, null, 0.0f, 1.0f));
  }

  @Test
  public void testEOTF() {
    DoubleUnaryOperator op = x -> x * x;
    ColorSpace.Rgb cs =
        new ColorSpace.Rgb("Test", new float[6], new float[2], identity, op, 0.0f, 1.0f);
    assertEquals(0.0625, cs.getEotf().applyAsDouble(0.25), 1e-5);
  }

  @Test
  public void testInvalidRange() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ColorSpace.Rgb("Test", new float[6], new float[2], identity, identity, 2.0f, 1.0f));
  }

  @Test
  public void testRanges() {
    ColorSpace cs = ColorSpace.get(ColorSpace.Named.SRGB);

    float m1 = cs.getMinValue(0);
    float m2 = cs.getMinValue(1);
    float m3 = cs.getMinValue(2);

    assertEquals(0.0f, m1, 1e-9f);
    assertEquals(0.0f, m2, 1e-9f);
    assertEquals(0.0f, m3, 1e-9f);

    m1 = cs.getMaxValue(0);
    m2 = cs.getMaxValue(1);
    m3 = cs.getMaxValue(2);

    assertEquals(1.0f, m1, 0);
    assertEquals(1.0f, m2, 0);
    assertEquals(1.0f, m3, 0);

    cs = ColorSpace.get(ColorSpace.Named.CIE_LAB);

    m1 = cs.getMinValue(0);
    m2 = cs.getMinValue(1);
    m3 = cs.getMinValue(2);

    assertEquals(0.0f, m1, 1e-9f);
    assertEquals(-128.0f, m2, 0);
    assertEquals(-128.0f, m3, 0);

    m1 = cs.getMaxValue(0);
    m2 = cs.getMaxValue(1);
    m3 = cs.getMaxValue(2);

    assertEquals(100.0f, m1, 0);
    assertEquals(128.0f, m2, 0);
    assertEquals(128.0f, m3, 0);

    cs = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    m1 = cs.getMinValue(0);
    m2 = cs.getMinValue(1);
    m3 = cs.getMinValue(2);

    assertEquals(-2.0f, m1, 0);
    assertEquals(-2.0f, m2, 0);
    assertEquals(-2.0f, m3, 0);

    m1 = cs.getMaxValue(0);
    m2 = cs.getMaxValue(1);
    m3 = cs.getMaxValue(2);

    assertEquals(2.0f, m1, 0);
    assertEquals(2.0f, m2, 0);
    assertEquals(2.0f, m3, 0);
  }

  @Test
  public void testMat3x3() {
    ColorSpace.Rgb cs = new ColorSpace.Rgb("Test", SRGB_TO_XYZ, identity, identity);

    float[] rgbToXYZ = cs.getTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(SRGB_TO_XYZ[i], rgbToXYZ[i], 1e-5f);
    }
  }

  @Test
  public void testMat3x3Inverse() {
    ColorSpace.Rgb cs = new ColorSpace.Rgb("Test", SRGB_TO_XYZ, identity, identity);

    float[] xyzToRGB = cs.getInverseTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(XYZ_TO_SRGB[i], xyzToRGB[i], 1e-5f);
    }
  }

  @Test
  public void testMat3x3Primaries() {
    ColorSpace.Rgb cs = new ColorSpace.Rgb("Test", SRGB_TO_XYZ, identity, identity);

    float[] primaries = cs.getPrimaries();

    assertNotNull(primaries);
    assertEquals(6, primaries.length);

    assertEquals(rGBPRIMARIESXyY[0], primaries[0], 1e-5f);
    assertEquals(rGBPRIMARIESXyY[1], primaries[1], 1e-5f);
    assertEquals(rGBPRIMARIESXyY[2], primaries[2], 1e-5f);
    assertEquals(rGBPRIMARIESXyY[3], primaries[3], 1e-5f);
    assertEquals(rGBPRIMARIESXyY[4], primaries[4], 1e-5f);
    assertEquals(rGBPRIMARIESXyY[5], primaries[5], 1e-5f);
  }

  @Test
  public void testMat3x3WhitePoint() {
    ColorSpace.Rgb cs = new ColorSpace.Rgb("Test", SRGB_TO_XYZ, identity, identity);

    float[] whitePoint = cs.getWhitePoint();

    assertNotNull(whitePoint);
    assertEquals(2, whitePoint.length);

    assertEquals(sRGBWHITEPOINTXyY[0], whitePoint[0], 1e-5f);
    assertEquals(sRGBWHITEPOINTXyY[1], whitePoint[1], 1e-5f);
  }

  @Test
  public void testXYZFromPrimaries_xyY() {
    ColorSpace.Rgb cs =
        new ColorSpace.Rgb(
            "Test", rGBPRIMARIESXyY, sRGBWHITEPOINTXyY, identity, identity, 0.0f, 1.0f);

    float[] rgbToXYZ = cs.getTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(SRGB_TO_XYZ[i], rgbToXYZ[i], 1e-5f);
    }

    float[] xyzToRGB = cs.getInverseTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(XYZ_TO_SRGB[i], xyzToRGB[i], 1e-5f);
    }
  }

  @Test
  public void testXYZFromPrimaries_xYZ() {
    ColorSpace.Rgb cs =
        new ColorSpace.Rgb(
            "Test", SRGB_PRIMARIES_XYZ, SRGB_WHITE_POINT_XYZ, identity, identity, 0.0f, 1.0f);

    float[] primaries = cs.getPrimaries();

    assertNotNull(primaries);
    assertEquals(6, primaries.length);

    // SRGB_PRIMARIES_xyY only has 1e-3 of precision, match it
    assertEquals(rGBPRIMARIESXyY[0], primaries[0], 1e-3f);
    assertEquals(rGBPRIMARIESXyY[1], primaries[1], 1e-3f);
    assertEquals(rGBPRIMARIESXyY[2], primaries[2], 1e-3f);
    assertEquals(rGBPRIMARIESXyY[3], primaries[3], 1e-3f);
    assertEquals(rGBPRIMARIESXyY[4], primaries[4], 1e-3f);
    assertEquals(rGBPRIMARIESXyY[5], primaries[5], 1e-3f);

    float[] whitePoint = cs.getWhitePoint();

    assertNotNull(whitePoint);
    assertEquals(2, whitePoint.length);

    // SRGB_WHITE_POINT_xyY only has 1e-3 of precision, match it
    assertEquals(sRGBWHITEPOINTXyY[0], whitePoint[0], 1e-3f);
    assertEquals(sRGBWHITEPOINTXyY[1], whitePoint[1], 1e-3f);

    float[] rgbToXYZ = cs.getTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(SRGB_TO_XYZ[i], rgbToXYZ[i], 1e-5f);
    }

    float[] xyzToRGB = cs.getInverseTransform();
    for (int i = 0; i < 9; i++) {
      assertEquals(XYZ_TO_SRGB[i], xyzToRGB[i], 1e-5f);
    }
  }

  @Test
  public void testGetComponentCount() {
    assertEquals(3, ColorSpace.get(ColorSpace.Named.SRGB).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.LINEAR_SRGB).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.DISPLAY_P3).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.CIE_LAB).getComponentCount());
    assertEquals(3, ColorSpace.get(ColorSpace.Named.CIE_XYZ).getComponentCount());
  }

  @Test
  @Config(minSdk = Q)
  public void testIsSRGB() {
    for (ColorSpace.Named e : ColorSpace.Named.values()) {
      // OK_LAB is behind a feature flag that is not yet enabled by default.
      if (Objects.equals(e.toString(), "OK_LAB")) {
        continue;
      }

      ColorSpace colorSpace = ColorSpace.get(e);
      if (e == ColorSpace.Named.SRGB) {
        assertTrue(colorSpace.isSrgb());
      } else {
        assertFalse("Incorrectly treating " + colorSpace + " as SRGB!", colorSpace.isSrgb());
      }
    }

    ColorSpace.Rgb cs =
        new ColorSpace.Rgb(
            "Almost sRGB", SRGB_TO_XYZ, x -> Math.pow(x, 1.0f / 2.2f), x -> Math.pow(x, 2.2f));
    assertFalse(cs.isSrgb());
  }

  @Test
  @Config(minSdk = O, maxSdk = P)
  public void testIsSRGB_o_p() {
    assertTrue(ColorSpace.get(ColorSpace.Named.SRGB).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.LINEAR_SRGB).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.DISPLAY_P3).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.CIE_LAB).isSrgb());
    assertFalse(ColorSpace.get(ColorSpace.Named.CIE_XYZ).isSrgb());

    ColorSpace.Rgb cs =
        new ColorSpace.Rgb(
            "My sRGB", SRGB_TO_XYZ, x -> Math.pow(x, 1.0f / 2.2f), x -> Math.pow(x, 2.2f));
    assertTrue(cs.isSrgb());
  }

  @Test
  public void testIsWideGamut() {
    assertFalse(ColorSpace.get(ColorSpace.Named.SRGB).isWideGamut());
    assertFalse(ColorSpace.get(ColorSpace.Named.BT709).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.DCI_P3).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.BT2020).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.ACES).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.CIE_LAB).isWideGamut());
    assertTrue(ColorSpace.get(ColorSpace.Named.CIE_XYZ).isWideGamut());
  }

  @Test
  public void testWhitePoint() {
    ColorSpace.Rgb cs = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] whitePoint = cs.getWhitePoint();

    assertNotNull(whitePoint);
    assertEquals(2, whitePoint.length);

    // Make sure a copy is returned
    Arrays.fill(whitePoint, Float.NaN);
    assertArrayNotEquals(whitePoint, cs.getWhitePoint(), 1e-5f);
    assertSame(whitePoint, cs.getWhitePoint(whitePoint));
    assertArrayEquals(whitePoint, cs.getWhitePoint(), 1e-5f);
  }

  @Test
  public void testPrimaries() {
    ColorSpace.Rgb cs = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] primaries = cs.getPrimaries();

    assertNotNull(primaries);
    assertEquals(6, primaries.length);

    // Make sure a copy is returned
    Arrays.fill(primaries, Float.NaN);
    assertArrayNotEquals(primaries, cs.getPrimaries(), 1e-5f);
    assertSame(primaries, cs.getPrimaries(primaries));
    assertArrayEquals(primaries, cs.getPrimaries(), 1e-5f);
  }

  @Test
  public void testRGBtoXYZMatrix() {
    ColorSpace.Rgb cs = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] rgbToXYZ = cs.getTransform();

    assertNotNull(rgbToXYZ);
    assertEquals(9, rgbToXYZ.length);

    // Make sure a copy is returned
    Arrays.fill(rgbToXYZ, Float.NaN);
    assertArrayNotEquals(rgbToXYZ, cs.getTransform(), 1e-5f);
    assertSame(rgbToXYZ, cs.getTransform(rgbToXYZ));
    assertArrayEquals(rgbToXYZ, cs.getTransform(), 1e-5f);
  }

  @Test
  public void testXYZtoRGBMatrix() {
    ColorSpace.Rgb cs = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] xyzToRGB = cs.getInverseTransform();

    assertNotNull(xyzToRGB);
    assertEquals(9, xyzToRGB.length);

    // Make sure a copy is returned
    Arrays.fill(xyzToRGB, Float.NaN);
    assertArrayNotEquals(xyzToRGB, cs.getInverseTransform(), 1e-5f);
    assertSame(xyzToRGB, cs.getInverseTransform(xyzToRGB));
    assertArrayEquals(xyzToRGB, cs.getInverseTransform(), 1e-5f);
  }

  @Test
  public void testRGBtoXYZ() {
    ColorSpace cs = ColorSpace.get(ColorSpace.Named.SRGB);

    float[] source = {0.75f, 0.5f, 0.25f};
    float[] expected = {0.3012f, 0.2679f, 0.0840f};

    float[] r1 = cs.toXyz(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayNotEquals(source, r1, 1e-5f);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r3 = {source[0], source[1], source[2]};
    assertSame(r3, cs.toXyz(r3));
    assertEquals(3, r3.length);
    assertArrayEquals(r1, r3, 1e-5f);
  }

  @Test
  public void testXYZtoRGB() {
    ColorSpace cs = ColorSpace.get(ColorSpace.Named.SRGB);

    float[] source = {0.3012f, 0.2679f, 0.0840f};
    float[] expected = {0.75f, 0.5f, 0.25f};

    float[] r1 = cs.fromXyz(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayNotEquals(source, r1, 1e-5f);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r3 = {source[0], source[1], source[2]};
    assertSame(r3, cs.fromXyz(r3));
    assertEquals(3, r3.length);
    assertArrayEquals(r1, r3, 1e-5f);
  }

  @Test
  public void testConnect() {
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.DCI_P3));

    assertSame(ColorSpace.get(ColorSpace.Named.SRGB), connector.getSource());
    assertSame(ColorSpace.get(ColorSpace.Named.DCI_P3), connector.getDestination());
    assertSame(ColorSpace.RenderIntent.PERCEPTUAL, connector.getRenderIntent());

    connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.SRGB));

    assertSame(connector.getDestination(), connector.getSource());
    assertSame(ColorSpace.RenderIntent.RELATIVE, connector.getRenderIntent());

    connector = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.DCI_P3));
    assertSame(ColorSpace.get(ColorSpace.Named.SRGB), connector.getDestination());

    connector = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.SRGB));
    assertSame(connector.getSource(), connector.getDestination());
  }

  @Test
  public void testConnector() {
    // Connect color spaces with same white points
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.ADOBE_RGB));

    float[] source = {1.0f, 0.5f, 0.0f};
    float[] expected = {0.8912f, 0.4962f, 0.1164f};

    float[] r1 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayNotEquals(source, r1, 1e-5f);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r3 = {source[0], source[1], source[2]};
    assertSame(r3, connector.transform(r3));
    assertEquals(3, r3.length);
    assertArrayEquals(r1, r3, 1e-5f);

    connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.ADOBE_RGB), ColorSpace.get(ColorSpace.Named.SRGB));

    float[] tmp = source;
    source = expected;
    expected = tmp;

    r1 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayNotEquals(source, r1, 1e-5f);
    assertArrayEquals(expected, r1, 1e-3f);

    r3 = new float[] {source[0], source[1], source[2]};
    assertSame(r3, connector.transform(r3));
    assertEquals(3, r3.length);
    assertArrayEquals(r1, r3, 1e-5f);
  }

  @Test
  public void testAdaptedConnector() {
    // Connect color spaces with different white points
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.PRO_PHOTO_RGB));

    float[] source = new float[] {1.0f, 0.0f, 0.0f};
    float[] expected = new float[] {0.70226f, 0.2757f, 0.1036f};

    float[] r = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r);
    assertEquals(3, r.length);
    assertArrayNotEquals(source, r, 1e-5f);
    assertArrayEquals(expected, r, 1e-4f);
  }

  @Test
  public void testAdaptedConnectorWithRenderIntent() {
    // Connect a wider color space to a narrow color space
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.DCI_P3),
            ColorSpace.get(ColorSpace.Named.SRGB),
            ColorSpace.RenderIntent.RELATIVE);

    float[] source = {0.9f, 0.9f, 0.9f};

    float[] relative = connector.transform(source[0], source[1], source[2]);
    assertNotNull(relative);
    assertEquals(3, relative.length);
    assertArrayNotEquals(source, relative, 1e-5f);
    assertArrayEquals(new float[] {0.8862f, 0.8862f, 0.8862f}, relative, 1e-4f);

    connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.DCI_P3),
            ColorSpace.get(ColorSpace.Named.SRGB),
            ColorSpace.RenderIntent.ABSOLUTE);

    float[] absolute = connector.transform(source[0], source[1], source[2]);
    assertNotNull(absolute);
    assertEquals(3, absolute.length);
    assertArrayNotEquals(source, absolute, 1e-5f);
    assertArrayNotEquals(relative, absolute, 1e-5f);
    assertArrayEquals(new float[] {0.8475f, 0.9217f, 0.8203f}, absolute, 1e-4f);
  }

  @Test
  public void testIdentityConnector() {
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.SRGB));

    assertSame(connector.getSource(), connector.getDestination());
    assertSame(ColorSpace.RenderIntent.RELATIVE, connector.getRenderIntent());

    float[] source = new float[] {0.11112f, 0.22227f, 0.444448f};

    float[] r = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r);
    assertEquals(3, r.length);
    assertArrayEquals(source, r, 1e-5f);
  }

  @Test
  public void testConnectorTransformIdentity() {
    ColorSpace.Connector connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.DCI_P3), ColorSpace.get(ColorSpace.Named.DCI_P3));

    float[] source = {1.0f, 0.0f, 0.0f};
    float[] expected = {1.0f, 0.0f, 0.0f};

    float[] r1 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r3 = {source[0], source[1], source[2]};
    assertSame(r3, connector.transform(r3));
    assertEquals(3, r3.length);
    assertArrayEquals(r1, r3, 1e-5f);
  }

  @Test
  public void testAdaptation() {
    ColorSpace adapted =
        ColorSpace.adapt(ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.ILLUMINANT_D50);

    float[] sRGBD50 = {
      0.43602175f, 0.22247513f, 0.01392813f,
      0.38510883f, 0.71690667f, 0.09710153f,
      0.14308129f, 0.06061824f, 0.71415880f
    };

    assertArrayEquals(sRGBD50, ((ColorSpace.Rgb) adapted).getTransform(), 1e-7f);

    adapted =
        ColorSpace.adapt(
            ColorSpace.get(ColorSpace.Named.SRGB),
            ColorSpace.ILLUMINANT_D50,
            ColorSpace.Adaptation.BRADFORD);
    assertArrayEquals(sRGBD50, ((ColorSpace.Rgb) adapted).getTransform(), 1e-7f);
  }

  @Test
  public void testImplicitSRGBConnector() {
    ColorSpace.Connector connector1 = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.DCI_P3));

    assertSame(ColorSpace.get(ColorSpace.Named.SRGB), connector1.getDestination());

    ColorSpace.Connector connector2 =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.DCI_P3), ColorSpace.get(ColorSpace.Named.SRGB));

    float[] source = {0.6f, 0.9f, 0.7f};
    assertArrayEquals(
        connector1.transform(source[0], source[1], source[2]),
        connector2.transform(source[0], source[1], source[2]),
        1e-7f);
  }

  @Test
  public void testLab() {
    ColorSpace.Connector connector = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.CIE_LAB));

    float[] source = {100.0f, 0.0f, 0.0f};
    float[] expected = {1.0f, 1.0f, 1.0f};

    float[] r1 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(expected, r1, 1e-3f);

    source = new float[] {100.0f, 0.0f, 54.0f};
    expected = new float[] {1.0f, 0.9925f, 0.5762f};

    float[] r2 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r2);
    assertEquals(3, r2.length);
    assertArrayEquals(expected, r2, 1e-3f);

    connector =
        ColorSpace.connect(
            ColorSpace.get(ColorSpace.Named.CIE_LAB), ColorSpace.RenderIntent.ABSOLUTE);

    source = new float[] {100.0f, 0.0f, 0.0f};
    expected = new float[] {1.0f, 0.9910f, 0.8651f};

    r1 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(expected, r1, 1e-3f);

    source = new float[] {100.0f, 0.0f, 54.0f};
    expected = new float[] {1.0f, 0.9853f, 0.4652f};

    r2 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r2);
    assertEquals(3, r2.length);
    assertArrayEquals(expected, r2, 1e-3f);
  }

  @Test
  public void testXYZ() {
    ColorSpace xyz = ColorSpace.get(ColorSpace.Named.CIE_XYZ);

    float[] source = {0.32f, 0.43f, 0.54f};

    float[] r1 = xyz.toXyz(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(source, r1, 1e-7f);

    float[] r2 = xyz.fromXyz(source[0], source[1], source[2]);
    assertNotNull(r2);
    assertEquals(3, r2.length);
    assertArrayEquals(source, r2, 1e-7f);

    ColorSpace.Connector connector = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.CIE_XYZ));

    float[] expected = {0.2280f, 0.7541f, 0.8453f};

    float[] r3 = connector.transform(source[0], source[1], source[2]);
    assertNotNull(r3);
    assertEquals(3, r3.length);
    assertArrayEquals(expected, r3, 1e-3f);
  }

  @Test
  public void testIDs() {
    // These cannot change
    assertEquals(0, ColorSpace.get(ColorSpace.Named.SRGB).getId());
    assertEquals(-1, ColorSpace.MIN_ID);
    assertEquals(63, ColorSpace.MAX_ID);
  }

  @Test
  public void testFromLinear() {
    ColorSpace.Rgb colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] source = {0.0f, 0.5f, 1.0f};
    float[] expected = {0.0f, 0.7354f, 1.0f};

    float[] r1 = colorSpace.fromLinear(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r2 = {source[0], source[1], source[2]};
    assertSame(r2, colorSpace.fromLinear(r2));
    assertEquals(3, r2.length);
    assertArrayEquals(r1, r2, 1e-5f);
  }

  @Test
  public void testToLinear() {
    ColorSpace.Rgb colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);

    float[] source = {0.0f, 0.5f, 1.0f};
    float[] expected = new float[] {0.0f, 0.2140f, 1.0f};

    float[] r1 = colorSpace.toLinear(source[0], source[1], source[2]);
    assertNotNull(r1);
    assertEquals(3, r1.length);
    assertArrayEquals(expected, r1, 1e-3f);

    float[] r2 = new float[] {source[0], source[1], source[2]};
    assertSame(r2, colorSpace.toLinear(r2));
    assertEquals(3, r2.length);
    assertArrayEquals(r1, r2, 1e-5f);
  }

  @Test
  @Config(minSdk = Q)
  public void testTransferParameters() {
    ColorSpace.Rgb colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);
    assertNotNull(colorSpace.getTransferParameters());

    colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB);
    assertNotNull(colorSpace.getTransferParameters());

    colorSpace =
        new ColorSpace.Rgb(
            "Almost sRGB", SRGB_TO_XYZ, x -> Math.pow(x, 1.0f / 2.2f), x -> Math.pow(x, 2.2f));
    assertNull(colorSpace.getTransferParameters());
  }

  @Test
  @Config(minSdk = O, maxSdk = P)
  public void testTransferParameters_o_p() {
    ColorSpace.Rgb colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB);
    assertNotNull(colorSpace.getTransferParameters());

    colorSpace = (ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB);
    assertNull(colorSpace.getTransferParameters());
  }

  @Test
  public void testIdempotentTransferFunctions() {
    Arrays.stream(ColorSpace.Named.values())
        .map(ColorSpace::get)
        .filter(cs -> cs.getModel() == ColorSpace.Model.RGB)
        .map(cs -> (ColorSpace.Rgb) cs)
        .forEach(
            cs -> {
              float[] source = {0.0f, 0.5f, 1.0f};
              float[] r = cs.fromLinear(cs.toLinear(source[0], source[1], source[2]));
              assertArrayEquals(source, r, 1e-3f);
            });
  }

  // TODO: update test with latest
  // cts/tests/tests/graphics/src/android/graphics/cts/ColorSpaceTest.java from U
  @Test
  @Config(maxSdk = TIRAMISU)
  public void testMatch() {
    for (ColorSpace.Named named : ColorSpace.Named.values()) {
      ColorSpace cs = ColorSpace.get(named);
      if (cs.getModel() == ColorSpace.Model.RGB) {
        ColorSpace.Rgb rgb = (ColorSpace.Rgb) cs;
        // match() cannot match extended sRGB
        if (!rgb.equals(ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB))
            && !rgb.equals(ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB))) {

          // match() uses CIE XYZ D50
          rgb = (ColorSpace.Rgb) ColorSpace.adapt(rgb, ColorSpace.ILLUMINANT_D50);
          assertSame(cs, ColorSpace.match(rgb.getTransform(), rgb.getTransferParameters()));
        }
      }
    }

    assertSame(
        ColorSpace.get(ColorSpace.Named.SRGB),
        ColorSpace.match(
            SRGB_TO_XYZ_D50,
            new ColorSpace.Rgb.TransferParameters(
                1 / 1.055, 0.055 / 1.055, 1 / 12.92, 0.04045, 2.4)));
  }

  @Test
  @Config(minSdk = Q) // This does not exist on API O-P
  public void testCctToXyz() {
    // Verify that range listed as meaningful by the API return float arrays as expected.
    for (int i = 1667; i <= 25000; i++) {
      float[] result = ColorSpace.cctToXyz(i);
      assertNotNull(result);
      assertEquals(3, result.length);
    }
  }

  private static float[] identityMatrix =
      new float[] {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f
      };

  @Test
  @Config(minSdk = Q) // This does not exist on API O-P
  public void testChromaticAdaptation() {
    for (ColorSpace.Adaptation adaptation : ColorSpace.Adaptation.values()) {
      float[][] whitePoints = {
        ColorSpace.ILLUMINANT_A,
        ColorSpace.ILLUMINANT_B,
        ColorSpace.ILLUMINANT_C,
        ColorSpace.ILLUMINANT_D50,
        ColorSpace.ILLUMINANT_D55,
        ColorSpace.ILLUMINANT_D60,
        ColorSpace.ILLUMINANT_D65,
        ColorSpace.ILLUMINANT_D75,
        ColorSpace.ILLUMINANT_E,
      };
      for (float[] srcWhitePoint : whitePoints) {
        for (float[] dstWhitePoint : whitePoints) {
          float[] result = ColorSpace.chromaticAdaptation(adaptation, srcWhitePoint, dstWhitePoint);
          assertNotNull(result);
          assertEquals(9, result.length);
          if (Arrays.equals(srcWhitePoint, dstWhitePoint)) {
            assertArrayEquals(identityMatrix, result, 0f);
          }
        }
      }
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void assertArrayNotEquals(float[] a, float[] b, float eps) {
    for (int i = 0; i < a.length; i++) {
      if (Float.compare(a[i], b[i]) == 0 || Math.abs(a[i] - b[i]) < eps) {
        fail("Expected " + a[i] + ", received " + b[i]);
      }
    }
  }

  private void assertArrayEquals(float[] a, float[] b, float eps) {
    for (int i = 0; i < a.length; i++) {
      if (Float.compare(a[i], b[i]) != 0 && Math.abs(a[i] - b[i]) > eps) {
        fail("Expected " + a[i] + ", received " + b[i]);
      }
    }
  }
}
