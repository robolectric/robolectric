/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.graphics.drawable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Compatibility test for {@link GradientDrawable}.
 *
 * <p>Copied from <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:cts/tests/tests/graphics/src/android/graphics/drawable/cts/GradientDrawableTest.java">GradientDrawableTest</a>
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class GradientDrawableTest {
  private Resources mResources;

  @Before
  public void setup() {
    mResources = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
  }

  @SuppressWarnings("CheckReturnValue")
  @Test
  public void testConstructor() {
    int[] color = new int[] {1, 2, 3};

    new GradientDrawable();
    new GradientDrawable(GradientDrawable.Orientation.BL_TR, color);
    new GradientDrawable(null, null);
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.M)
  @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.M)
  public void testGetOpacityPreO() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    assertEquals(
        "Default opacity is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.TRANSPARENT);
    assertEquals(
        "Color.TRANSPARENT is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(0x80FFFFFF);
    assertEquals(
        "0x80FFFFFF is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.TRANSPARENT});
    assertEquals(
        "{ RED, TRANSPARENT } is TRANSLUCENT",
        PixelFormat.TRANSLUCENT,
        gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.BLUE});
    assertEquals(
        "{ RED, BLUE } is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.RED);
    assertEquals("RED is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(10);
    assertEquals(
        "RED with corner radius is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(0);
    assertEquals(
        "RED with no corner radius is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(new float[] {2, 2, 0, 0, 0, 0, 0, 0});
    assertEquals(
        "RED with corner radii is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(null);
    assertEquals(
        "RED with no corner radii is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O, maxSdk = Build.VERSION_CODES.O_MR1)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O, maxSdkVersion = Build.VERSION_CODES.O_MR1)
  public void testGetOpacityFromOToOMR1() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    assertEquals(
        "Default opacity is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.TRANSPARENT);
    assertEquals(
        "Color.TRANSPARENT is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(0x80FFFFFF);
    assertEquals(
        "0x80FFFFFF is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.TRANSPARENT});
    assertEquals(
        "{ RED, TRANSPARENT } is TRANSLUCENT",
        PixelFormat.TRANSLUCENT,
        gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.BLUE});
    assertEquals("{ RED, BLUE } is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.RED);
    assertEquals("RED is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(10);
    assertEquals(
        "RED with corner radius is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(0);
    assertEquals(
        "RED with no corner radius is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(new float[] {2, 2, 0, 0, 0, 0, 0, 0});
    assertEquals(
        "RED with corner radii is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(null);
    assertEquals(
        "RED with no corner radii is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
  public void testGetOpacityFromP() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    assertEquals(
        "Default opacity is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.TRANSPARENT);
    assertEquals(
        "Color.TRANSPARENT is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColor(0x80FFFFFF);
    assertEquals(
        "0x80FFFFFF is TRANSLUCENT", PixelFormat.TRANSLUCENT, gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.TRANSPARENT});
    assertEquals(
        "{ RED, TRANSPARENT } is TRANSLUCENT",
        PixelFormat.TRANSLUCENT,
        gradientDrawable.getOpacity());

    gradientDrawable.setColors(new int[] {Color.RED, Color.BLUE});
    assertEquals("{ RED, BLUE } is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setColor(Color.RED);
    assertEquals("RED is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(10);
    assertEquals(
        "RED with corner radius is TRANSLUCENT",
        PixelFormat.TRANSLUCENT,
        gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadius(0);
    assertEquals(
        "RED with no corner radius is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(new float[] {2, 2, 0, 0, 0, 0, 0, 0});
    assertEquals(
        "RED with corner radii is TRANSLUCENT",
        PixelFormat.TRANSLUCENT,
        gradientDrawable.getOpacity());

    gradientDrawable.setCornerRadii(null);
    assertEquals(
        "RED with no corner radii is OPAQUE", PixelFormat.OPAQUE, gradientDrawable.getOpacity());
  }

  @Test
  public void testSetOrientation() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    Orientation orientation;

    orientation = Orientation.BL_TR;
    gradientDrawable.setOrientation(orientation);
    assertEquals(
        "Orientation set/get are symmetric", orientation, gradientDrawable.getOrientation());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetCornerRadii() {
    float[] radii = new float[] {1.0f, 2.0f, 3.0f};

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setCornerRadii(radii);

    float[] radiiActual = gradientDrawable.getCornerRadii();
    assertArrayEquals("Gradient radius set/get are symmetric", radii, radiiActual, 0);

    ConstantState constantState = gradientDrawable.getConstantState();
    assertNotNull(constantState);

    // input null as param
    gradientDrawable.setCornerRadii(null);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      assertNull("Gradient radii is not null", gradientDrawable.getCornerRadii());
    } else {
      assertThrows(NullPointerException.class, gradientDrawable::getCornerRadii);
    }
  }

  @Test
  public void testSetCornerRadius() {
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setCornerRadius(2.5f);
    gradientDrawable.setCornerRadius(-2.5f);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testGetCornerRadius() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setCornerRadius(5.5f);
    assertEquals(5.5f, gradientDrawable.getCornerRadius(), 0);
    float[] radii = new float[] {1.0f, 2.0f, 3.0f};
    gradientDrawable.setCornerRadii(radii);
    assertEquals(5.5f, gradientDrawable.getCornerRadius(), 0);
    gradientDrawable.setShape(GradientDrawable.OVAL);
    assertEquals(5.5f, gradientDrawable.getCornerRadius(), 0);
    gradientDrawable.setCornerRadii(null);
    assertEquals(0, gradientDrawable.getCornerRadius(), 0);
  }

  @Test
  public void testSetStroke() {
    helpTestSetStroke(2, Color.RED);
    helpTestSetStroke(-2, Color.TRANSPARENT);
    helpTestSetStroke(0, 0);
  }

  private void helpTestSetStroke(int width, int color) {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, color);
    // TODO: Verify stroke properties.
  }

  @Test
  public void testSetStroke_WidthGap() {
    verifySetStroke_WidthGap(2, Color.RED, 3.4f, 5.5f);
    verifySetStroke_WidthGap(-2, Color.TRANSPARENT, -3.4f, -5.5f);
    verifySetStroke_WidthGap(0, 0, 0, 0.0f);
  }

  private void verifySetStroke_WidthGap(int width, int color, float dashWidth, float dashGap) {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, color, dashWidth, dashGap);
    // TODO: Verify stroke properties.
  }

  @Test
  public void testSetStrokeList() {
    verifySetStrokeList(2, ColorStateList.valueOf(Color.RED));
    verifySetStrokeList(-2, ColorStateList.valueOf(Color.TRANSPARENT));
    verifySetStrokeList(0, null);
  }

  private void verifySetStrokeList(int width, ColorStateList colorList) {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, colorList);
    // TODO: Verify stroke properties.
  }

  @Test
  public void testSetStrokeList_WidthGap() {
    verifySetStrokeList_WidthGap(2, ColorStateList.valueOf(Color.RED), 3.4f, 5.5f);
    verifySetStrokeList_WidthGap(-2, ColorStateList.valueOf(Color.TRANSPARENT), -3.4f, -5.5f);
    verifySetStrokeList_WidthGap(0, null, 0.0f, 0.0f);
  }

  private void verifySetStrokeList_WidthGap(
      int width, ColorStateList colorList, float dashWidth, float dashGap) {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, colorList, dashWidth, dashGap);
    // TODO: Verify stroke properties.
  }

  @Test
  public void testSetSize() {
    verifySetSize(6, 4);
    verifySetSize(-30, -40);
    verifySetSize(0, 0);
    verifySetSize(Integer.MAX_VALUE, Integer.MIN_VALUE);
  }

  private void verifySetSize(int width, int height) {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setSize(width, height);
    assertEquals(width, gradientDrawable.getIntrinsicWidth());
    assertEquals(height, gradientDrawable.getIntrinsicHeight());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetShape() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    int shape;

    shape = GradientDrawable.OVAL;
    gradientDrawable.setShape(shape);
    assertEquals("Gradient shape set/get are symmetric", shape, gradientDrawable.getShape());

    shape = -1;
    gradientDrawable.setShape(shape);
    assertEquals(
        "Invalid gradient shape set/get are symmetric", shape, gradientDrawable.getShape());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetGradientType() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    int gradientType;

    gradientType = GradientDrawable.LINEAR_GRADIENT;
    gradientDrawable.setGradientType(gradientType);
    assertEquals(
        "Gradient type set/get are symmetric", gradientType, gradientDrawable.getGradientType());

    gradientType = -1;
    gradientDrawable.setGradientType(gradientType);
    assertEquals(
        "Invalid gradient type set/get are symmetric",
        gradientType,
        gradientDrawable.getGradientType());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetGradientCenter() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    float centerX;
    float centerY;

    centerX = 0.5f;
    centerY = 0.5f;
    assertEquals(centerX, gradientDrawable.getGradientCenterX(), 0.01f);
    assertEquals(centerY, gradientDrawable.getGradientCenterY(), 0.01f);

    centerX = -0.5f;
    centerY = -0.5f;
    gradientDrawable.setGradientCenter(centerX, centerY);
    assertEquals(centerX, gradientDrawable.getGradientCenterX(), 0.01f);
    assertEquals(centerY, gradientDrawable.getGradientCenterY(), 0.01f);

    centerX = 0.0f;
    centerY = 0.0f;
    gradientDrawable.setGradientCenter(centerX, centerY);
    assertEquals(centerX, gradientDrawable.getGradientCenterX(), 0.01f);
    assertEquals(centerY, gradientDrawable.getGradientCenterY(), 0.01f);
  }

  @Test
  public void testSetGradientRadius() {
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setGradientRadius(3.6f);
    gradientDrawable.setGradientRadius(-3.6f);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetUseLevel() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    boolean useLevel;

    assertFalse("Default useLevel is false", gradientDrawable.getUseLevel());

    useLevel = true;
    gradientDrawable.setUseLevel(useLevel);
    assertEquals(
        "Gradient set/get useLevel is symmetric", useLevel, gradientDrawable.getUseLevel());

    useLevel = false;
    gradientDrawable.setUseLevel(useLevel);
    assertEquals(
        "Gradient set/get useLevel is symmetric", useLevel, gradientDrawable.getUseLevel());
  }

  @Test
  public void testDraw() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    Canvas c = new Canvas();
    gradientDrawable.draw(c);

    // input null as param
    gradientDrawable.draw(null);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetColor() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    int color;

    color = Color.RED;
    gradientDrawable.setColor(color);
    assertEquals("Color was set to " + color, color, gradientDrawable.getColor().getDefaultColor());

    color = Color.TRANSPARENT;
    gradientDrawable.setColor(color);
    assertEquals("Color was set to " + color, color, gradientDrawable.getColor().getDefaultColor());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetColors() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    int[] colors;

    colors = new int[] {Color.RED};
    gradientDrawable.setColors(colors);
    assertArrayEquals(
        "Color was set to " + Arrays.toString(colors), colors, gradientDrawable.getColors());

    colors = null;
    gradientDrawable.setColors(colors);
    assertArrayEquals(
        "Color was set to " + Arrays.toString(colors), colors, gradientDrawable.getColors());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testSetColorList() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    ColorStateList color;

    color = ColorStateList.valueOf(Color.RED);
    gradientDrawable.setColor(color);
    assertEquals("Color was set to RED", color, gradientDrawable.getColor());

    gradientDrawable.setColor(null);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      assertEquals(
          "Color was set to null (TRANSPARENT)",
          ColorStateList.valueOf(Color.TRANSPARENT),
          gradientDrawable.getColor());
    } else {
      assertNull("Color was set to null", gradientDrawable.getColor());
    }
  }

  @Test
  public void testGetChangingConfigurations() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    assertEquals(0, gradientDrawable.getChangingConfigurations());

    gradientDrawable.setChangingConfigurations(10);
    assertEquals(10, gradientDrawable.getChangingConfigurations());

    gradientDrawable.setChangingConfigurations(-20);
    assertEquals(-20, gradientDrawable.getChangingConfigurations());
  }

  @Test
  public void testSetAlpha() {
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setAlpha(1);
    gradientDrawable.setAlpha(-1);
  }

  @Test
  public void testSetDither() {
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setDither(true);
    gradientDrawable.setDither(false);
  }

  @Test
  public void testSetColorFilter() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    ColorFilter cf = new ColorFilter();
    gradientDrawable.setColorFilter(cf);

    // input null as param
    gradientDrawable.setColorFilter(null);
  }

  @Test
  public void testInflate() throws XmlPullParserException, IOException {
    GradientDrawable gradientDrawable = new GradientDrawable();
    Rect rect = new Rect();
    assertFalse(gradientDrawable.getPadding(rect));
    assertEquals(0, rect.left);
    assertEquals(0, rect.top);
    assertEquals(0, rect.right);
    assertEquals(0, rect.bottom);

    XmlPullParser parser = mResources.getXml(R.drawable.gradientdrawable);
    AttributeSet attrs = Xml.asAttributeSet(parser);

    // find the START_TAG
    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }
    assertEquals(XmlPullParser.START_TAG, type);

    // padding is set in gradientdrawable.xml
    gradientDrawable.inflate(mResources, parser, attrs);
    assertTrue(gradientDrawable.getPadding(rect));
    assertEquals(4, rect.left);
    assertEquals(2, rect.top);
    assertEquals(6, rect.right);
    assertEquals(10, rect.bottom);

    try {
      gradientDrawable.getPadding(null);
      fail("did not throw NullPointerException when rect is null.");
    } catch (NullPointerException e) {
      // expected, test success
    }

    try {
      gradientDrawable.inflate(null, null, null);
      fail("did not throw NullPointerException when parameters are null.");
    } catch (NullPointerException e) {
      // expected, test success
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientPadding() {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setPadding(1, 2, 3, 4);

    Rect padding = new Rect();
    drawable.getPadding(padding);

    assertEquals(1, padding.left);
    assertEquals(2, padding.top);
    assertEquals(3, padding.right);
    assertEquals(4, padding.bottom);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientThickness() {
    GradientDrawable drawable = new GradientDrawable();
    int thickness = 17;

    drawable.setThickness(thickness);
    assertEquals(thickness, drawable.getThickness());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testNegativeGradientThickness() {
    try {
      new GradientDrawable().setThicknessRatio(-1);
      fail("Did not throw IllegalArgumentException with negative thickness ratio");
    } catch (IllegalArgumentException e) {
      // expected, test success
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testZeroGradientThickness() {
    try {
      new GradientDrawable().setThicknessRatio(0);
      fail("Did not throw IllegalArgumentException with zero thickness ratio");
    } catch (IllegalArgumentException e) {
      // expected, test success
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientThicknessRatio() {
    GradientDrawable drawable = new GradientDrawable();

    float thicknessRatio = 3.9f;

    drawable.setThicknessRatio(thicknessRatio);
    assertEquals(0, Float.compare(thicknessRatio, drawable.getThicknessRatio()));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientInnerRadius() {
    GradientDrawable drawable = new GradientDrawable();
    int innerRadius = 12;
    drawable.setInnerRadius(innerRadius);

    assertEquals(innerRadius, drawable.getInnerRadius());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testNegativeInnerRadiusRatio() {
    try {
      new GradientDrawable().setInnerRadiusRatio(-1);
      fail("Did not throw IllegalArgumentException with negative thickness ratio");
    } catch (IllegalArgumentException e) {
      // expected, test success
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testZeroInnerRadiusRatio() {
    try {
      new GradientDrawable().setInnerRadiusRatio(0);
      fail("Did not throw IllegalArgumentException with zero thickness ratio");
    } catch (IllegalArgumentException e) {
      // expected, test success
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientInnerRadiusRatio() {
    GradientDrawable drawable = new GradientDrawable();
    float innerRadiusRatio = 3.8f;
    drawable.setInnerRadiusRatio(innerRadiusRatio);

    assertEquals(0, Float.compare(innerRadiusRatio, drawable.getInnerRadiusRatio()));
  }

  @Test
  public void testGradientPositions() throws XmlPullParserException, IOException {
    GradientDrawable gradientDrawable = new GradientDrawable();
    XmlPullParser parser = mResources.getXml(R.drawable.gradientdrawable);
    AttributeSet attrs = Xml.asAttributeSet(parser);

    // find the START_TAG
    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }
    assertEquals(XmlPullParser.START_TAG, type);

    // padding is set in gradientdrawable.xml
    gradientDrawable.inflate(mResources, parser, attrs);

    gradientDrawable.setColors(new int[] {Color.RED, Color.BLUE});

    Canvas canvas = new Canvas(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));

    gradientDrawable.setBounds(0, 0, 100, 100);
    // Verify that calling draw does not crash
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      gradientDrawable.draw(canvas);
    } else {
      assertThrows(IllegalArgumentException.class, () -> gradientDrawable.draw(canvas));
    }
  }

  @Test
  public void testInflateGradientRadius() {
    Rect parentBounds = new Rect(0, 0, 100, 100);

    GradientDrawable gradientDrawable;
    float radius;

    gradientDrawable =
        (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable_radius_base);
    gradientDrawable.setBounds(parentBounds);
    radius = gradientDrawable.getGradientRadius();
    assertEquals(25.0f, radius, 0.0f);

    gradientDrawable =
        (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable_radius_parent);
    gradientDrawable.setBounds(parentBounds);
    radius = gradientDrawable.getGradientRadius();
    assertEquals(50.0f, radius, 0.0f);
  }

  @Test
  public void testGetIntrinsicWidth() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setSize(6, 4);
    assertEquals(6, gradientDrawable.getIntrinsicWidth());

    gradientDrawable.setSize(-10, -20);
    assertEquals(-10, gradientDrawable.getIntrinsicWidth());
  }

  @Test
  public void testGetIntrinsicHeight() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setSize(5, 3);
    assertEquals(3, gradientDrawable.getIntrinsicHeight());

    gradientDrawable.setSize(-5, -15);
    assertEquals(-15, gradientDrawable.getIntrinsicHeight());
  }

  @Test
  public void testGetConstantState() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    assertNotNull(gradientDrawable.getConstantState());
  }

  @Test
  public void testMutate() {
    GradientDrawable d1 = (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable);
    GradientDrawable d2 = (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable);
    GradientDrawable d3 = (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable);

    d1.setSize(10, 10);
    assertEquals(10, d1.getIntrinsicHeight());
    assertEquals(10, d1.getIntrinsicWidth());
    assertEquals(10, d2.getIntrinsicHeight());
    assertEquals(10, d2.getIntrinsicWidth());
    assertEquals(10, d3.getIntrinsicHeight());
    assertEquals(10, d3.getIntrinsicWidth());

    d1.mutate();
    d1.setSize(20, 30);
    assertEquals(30, d1.getIntrinsicHeight());
    assertEquals(20, d1.getIntrinsicWidth());
    assertEquals(10, d2.getIntrinsicHeight());
    assertEquals(10, d2.getIntrinsicWidth());
    assertEquals(10, d3.getIntrinsicHeight());
    assertEquals(10, d3.getIntrinsicWidth());

    d2.setSize(40, 50);
    assertEquals(30, d1.getIntrinsicHeight());
    assertEquals(20, d1.getIntrinsicWidth());
    assertEquals(50, d2.getIntrinsicHeight());
    assertEquals(40, d2.getIntrinsicWidth());
    assertEquals(50, d3.getIntrinsicHeight());
    assertEquals(40, d3.getIntrinsicWidth());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testPreloadDensity() throws XmlPullParserException, IOException {
    final int densityDpi = mResources.getConfiguration().densityDpi;
    try {
      DrawableTestUtils.setResourcesDensity(mResources, densityDpi);
      verifyPreloadDensityInner(mResources, densityDpi);
    } finally {
      DrawableTestUtils.setResourcesDensity(mResources, densityDpi);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testPreloadDensity_tvdpi() throws XmlPullParserException, IOException {
    final int densityDpi = mResources.getConfiguration().densityDpi;
    try {
      DrawableTestUtils.setResourcesDensity(mResources, 213);
      verifyPreloadDensityInner(mResources, 213);
    } finally {
      DrawableTestUtils.setResourcesDensity(mResources, densityDpi);
    }
  }

  @Test
  public void testOpticalInsetsPreQ() {
    GradientDrawable drawable =
        (GradientDrawable) mResources.getDrawable(R.drawable.gradientdrawable);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      assertEquals(Insets.of(1, 2, 3, 4), drawable.getOpticalInsets());
    } else {
      assertEquals(Insets.of(0, 0, 0, 0), drawable.getOpticalInsets());
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testInflationWithThemeAndNonThemeResources() {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    final Theme theme = context.getResources().newTheme();
    theme.applyStyle(R.style.Theme_MixedGradientTheme, true);
    final Theme ctxTheme = context.getTheme();
    ctxTheme.setTo(theme);

    GradientDrawable drawable =
        (GradientDrawable) ctxTheme.getDrawable(R.drawable.gradientdrawable_mix_theme);

    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, 10, 10);
    drawable.draw(canvas);
    int[] colors = drawable.getColors();
    assertEquals(3, colors.length);
    assertEquals(0, colors[0]);
    assertEquals(context.getColor(R.color.colorPrimary), colors[1]);
    assertEquals(context.getColor(R.color.colorPrimaryDark), colors[2]);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testGradientColorInflationWithThemeAndNonThemeResources() {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    final Theme theme = context.getResources().newTheme();
    theme.applyStyle(R.style.Theme_MixedGradientTheme, true);
    final Theme ctxTheme = context.getTheme();
    ctxTheme.setTo(theme);

    GradientDrawable drawable =
        (GradientDrawable) ctxTheme.getDrawable(R.drawable.gradientdrawable_color_mix_theme);

    int[] colors = drawable.getColors();
    drawable.setColors(colors);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      assertEquals(3, colors.length);
      assertEquals(context.getColor(R.color.colorAccent), colors[0]);
      assertEquals(context.getColor(R.color.colorPrimary), colors[1]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[2]);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      assertEquals(3, colors.length);
      assertEquals(context.getColor(R.color.colorAccent), colors[0]);
      assertEquals(-65281, colors[1]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[2]);
    } else {
      assertEquals(2, colors.length);
      assertEquals(context.getColor(R.color.colorAccent), colors[0]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[1]);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testGradientColorInflationWithAllThemeResources() {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    final Theme theme = context.getResources().newTheme();
    theme.applyStyle(R.style.Theme_MixedGradientTheme, true);
    final Theme ctxTheme = context.getTheme();
    ctxTheme.setTo(theme);

    GradientDrawable drawable =
        (GradientDrawable) ctxTheme.getDrawable(R.drawable.gradientdrawable_color_all_theme);

    int[] colors = drawable.getColors();
    drawable.setColors(colors);
    assertEquals(3, colors.length);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      assertEquals(context.getColor(R.color.colorPrimary), colors[0]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[1]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[2]);
    } else {
      assertEquals(3, colors.length);
      assertEquals(-65281, colors[0]);
      assertEquals(-65281, colors[1]);
      assertEquals(-65281, colors[2]);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  public void testGradientColorNoCenterColorInflationWithThemeAndNonThemeResources() {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    final Theme theme = context.getResources().newTheme();
    theme.applyStyle(R.style.Theme_MixedGradientTheme, true);
    final Theme ctxTheme = context.getTheme();
    ctxTheme.setTo(theme);

    GradientDrawable drawable =
        (GradientDrawable)
            ctxTheme.getDrawable(R.drawable.gradientdrawable_no_center_color_all_theme);

    int[] colors = drawable.getColors();
    drawable.setColors(colors);
    assertEquals(2, colors.length);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      assertEquals(context.getColor(R.color.colorPrimary), colors[0]);
      assertEquals(context.getColor(R.color.colorPrimaryDark), colors[1]);
    } else {
      assertEquals(-65281, colors[0]);
      assertEquals(-65281, colors[1]);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testRadialInflationWithThemeAndNonThemeResources() {
    final Context context =
        new ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            R.style.Theme_MixedGradientTheme);

    GradientDrawable drawable =
        (GradientDrawable) context.getDrawable(R.drawable.gradientdrawable_mix_theme);

    // Verify that despite multiple inflation passes are done to inflate both
    // the non-theme attributes as well as the themed attributes
    assertEquals(GradientDrawable.RADIAL_GRADIENT, drawable.getGradientType());
    assertEquals(87.0f, drawable.getGradientRadius(), 0.0f);
  }

  @Test
  public void testRadialGradientWithInvalidRadius() {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    GradientDrawable radiusDrawable =
        (GradientDrawable) context.getDrawable(R.drawable.gradientdrawable_invalid_radius);

    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    try {
      radiusDrawable.setBounds(0, 0, 10, 10);
      radiusDrawable.draw(canvas);
    } catch (Exception e) {
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
        assertTrue(e instanceof IllegalArgumentException);
        assertEquals("radius must be > 0", e.getMessage());
      } else {
        fail("Threw exception: " + e + " with negative radius");
      }
    }
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.P)
  @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.P)
  public void testGradientNegativeAnglePreQ() {
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_45, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_90, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_135, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_180, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_225, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_270, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_315, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_360, Orientation.LEFT_RIGHT);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
  public void testGradientNegativeAngleFromQ() {
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle_45, Orientation.TL_BR);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_90, Orientation.TOP_BOTTOM);
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle_135, Orientation.TR_BL);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_180, Orientation.RIGHT_LEFT);
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle_225, Orientation.BR_TL);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_270, Orientation.BOTTOM_TOP);
    verifyGradientOrientation(R.drawable.gradientdrawable_negative_angle_315, Orientation.BL_TR);
    verifyGradientOrientation(
        R.drawable.gradientdrawable_negative_angle_360, Orientation.LEFT_RIGHT);
  }

  private void verifyGradientOrientation(int resId, Orientation expected) {
    final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    assertEquals(expected, ((GradientDrawable) context.getDrawable(resId)).getOrientation());
  }

  @Test
  public void testDynamicGradientDefaultOrientation() {
    // Verify that the default orientation for a programmatically defined GradientDrawable is
    // TOP_BOTTOM. This differs from the default behavior of xml inflated GradientDrawables
    // that default to LEFT_RIGHT
    assertEquals(Orientation.TOP_BOTTOM, new GradientDrawable().getOrientation());
  }

  @Test
  public void testGradientDrawableOrientationConstructor() {
    GradientDrawable drawable = new GradientDrawable(Orientation.TOP_BOTTOM, null);
    assertEquals(Orientation.TOP_BOTTOM, drawable.getOrientation());
  }

  @Test
  public void testInflatedGradientOrientationUpdated() {
    final Context context =
        new ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            R.style.Theme_MixedGradientTheme);

    GradientDrawable drawable = (GradientDrawable) context.getDrawable(R.drawable.gradientdrawable);

    assertEquals(Orientation.BL_TR, drawable.getOrientation());

    drawable.setOrientation(Orientation.BOTTOM_TOP);
    assertEquals(Orientation.BOTTOM_TOP, drawable.getOrientation());
  }

  private void verifyPreloadDensityInner(Resources res, int densityDpi)
      throws XmlPullParserException, IOException {
    final Rect tempPadding = new Rect();

    // Capture initial state at default density.
    final XmlResourceParser parser =
        DrawableTestUtils.getResourceParser(res, R.drawable.gradient_drawable_density);
    final GradientDrawable preloadedDrawable = new GradientDrawable();
    preloadedDrawable.inflate(res, parser, Xml.asAttributeSet(parser));
    final ConstantState preloadedConstantState = preloadedDrawable.getConstantState();
    final int origWidth = preloadedDrawable.getIntrinsicWidth();
    final int origHeight = preloadedDrawable.getIntrinsicHeight();
    final Rect origPadding = new Rect();
    preloadedDrawable.getPadding(origPadding);

    // Set density to approximately half of original. Unlike offsets, which are
    // truncated, dimensions are rounded to the nearest pixel.
    DrawableTestUtils.setResourcesDensity(res, densityDpi / 2);
    final GradientDrawable halfDrawable =
        (GradientDrawable) preloadedConstantState.newDrawable(res);
    // NOTE: densityDpi may not be an even number, so account for *actual* scaling in asserts
    final float approxHalf = (float) (densityDpi / 2) / densityDpi;
    assertEquals(Math.round(origWidth * approxHalf), halfDrawable.getIntrinsicWidth());
    assertEquals(Math.round(origHeight * approxHalf), halfDrawable.getIntrinsicHeight());
    assertTrue(halfDrawable.getPadding(tempPadding));
    assertEquals((int) (origPadding.left * approxHalf), tempPadding.left);

    // Set density to double original.
    DrawableTestUtils.setResourcesDensity(res, densityDpi * 2);
    final GradientDrawable doubleDrawable =
        (GradientDrawable) preloadedConstantState.newDrawable(res);
    assertEquals(origWidth * 2, doubleDrawable.getIntrinsicWidth());
    assertEquals(origHeight * 2, doubleDrawable.getIntrinsicHeight());
    assertTrue(doubleDrawable.getPadding(tempPadding));
    assertEquals(origPadding.left * 2, tempPadding.left);

    // Restore original density.
    DrawableTestUtils.setResourcesDensity(res, densityDpi);
    final GradientDrawable origDrawable = (GradientDrawable) preloadedConstantState.newDrawable();
    assertEquals(origWidth, origDrawable.getIntrinsicWidth());
    assertEquals(origHeight, origDrawable.getIntrinsicHeight());
    assertTrue(origDrawable.getPadding(tempPadding));
    assertEquals(origPadding, tempPadding);

    // Reproduce imprecise truncated scale down, and back up. Note these aren't rounded.
    final float approxDouble = 1 / approxHalf;
    final Rect sloppyOrigPadding = new Rect();
    sloppyOrigPadding.left = (int) (approxDouble * ((int) (origPadding.left * approxHalf)));
    sloppyOrigPadding.top = (int) (approxDouble * ((int) (origPadding.top * approxHalf)));
    sloppyOrigPadding.right = (int) (approxDouble * ((int) (origPadding.right * approxHalf)));
    sloppyOrigPadding.bottom = (int) (approxDouble * ((int) (origPadding.bottom * approxHalf)));

    // Ensure theme density is applied correctly.
    final Theme t = res.newTheme();
    halfDrawable.applyTheme(t);
    assertEquals(
        Math.round(approxDouble * Math.round(origWidth * approxHalf)),
        halfDrawable.getIntrinsicWidth());
    assertEquals(
        Math.round(approxDouble * Math.round(origHeight * approxHalf)),
        halfDrawable.getIntrinsicHeight());
    assertTrue(halfDrawable.getPadding(tempPadding));
    assertEquals(sloppyOrigPadding, tempPadding);
    doubleDrawable.applyTheme(t);
    assertEquals(origWidth, doubleDrawable.getIntrinsicWidth());
    assertEquals(origHeight, doubleDrawable.getIntrinsicHeight());
    assertTrue(doubleDrawable.getPadding(tempPadding));
    assertEquals(origPadding, tempPadding);
  }
}
