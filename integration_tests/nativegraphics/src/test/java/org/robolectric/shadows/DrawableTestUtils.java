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

package org.robolectric.shadows;

import static java.lang.Math.max;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.base.MoreObjects;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.robolectric.RuntimeEnvironment;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/** The useful methods for graphics.drawable test. */
public final class DrawableTestUtils {
  private static final String LOGTAG = "DrawableTestUtils";
  // A small value is actually making sure that the values are matching
  // exactly with the golden image.
  // We can increase the threshold if the Skia is drawing with some variance
  // on different devices. So far, the tests show they are matching correctly.
  static final float PIXEL_ERROR_THRESHOLD = 0.03f;
  static final float PIXEL_ERROR_COUNT_THRESHOLD = 0.005f;
  static final int PIXEL_ERROR_TOLERANCE = 3;

  public static void skipCurrentTag(XmlPullParser parser)
      throws XmlPullParserException, IOException {
    int outerDepth = parser.getDepth();
    int type;
    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
        && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {}
  }

  /**
   * Retrieve an AttributeSet from a XML.
   *
   * @param parser the XmlPullParser to use for the xml parsing.
   * @param searchedNodeName the name of the target node.
   * @return the AttributeSet retrieved from specified node.
   */
  public static AttributeSet getAttributeSet(XmlResourceParser parser, String searchedNodeName)
      throws XmlPullParserException, IOException {
    AttributeSet attrs = null;
    int type;
    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
        && type != XmlPullParser.START_TAG) {}
    String nodeName = parser.getName();
    if (!"alias".equals(nodeName)) {
      throw new RuntimeException();
    }
    int outerDepth = parser.getDepth();
    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
        && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
      if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
        continue;
      }
      nodeName = parser.getName();
      if (searchedNodeName.equals(nodeName)) {
        outerDepth = parser.getDepth();
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
            && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
          if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
            continue;
          }
          nodeName = parser.getName();
          attrs = Xml.asAttributeSet(parser);
          break;
        }
        break;
      } else {
        skipCurrentTag(parser);
      }
    }
    return attrs;
  }

  public static XmlResourceParser getResourceParser(Resources res, int resId)
      throws XmlPullParserException, IOException {
    final XmlResourceParser parser = res.getXml(resId);
    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }
    return parser;
  }

  public static void setResourcesDensity(Resources res, int densityDpi) {
    final Configuration config = new Configuration();
    config.setTo(res.getConfiguration());
    config.densityDpi = densityDpi;
    res.updateConfiguration(config, null);
  }

  /**
   * Implements scaling as used by the Bitmap class. Resulting values are rounded up (as distinct
   * from resource scaling, which truncates or rounds to the nearest pixel).
   *
   * @param size the pixel size to scale
   * @param sdensity the source density that corresponds to the size
   * @param tdensity the target density
   * @return the pixel size scaled for the target density
   */
  public static int scaleBitmapFromDensity(int size, int sdensity, int tdensity) {
    if (sdensity == 0 || tdensity == 0 || sdensity == tdensity) {
      return size;
    }

    // Scale by tdensity / sdensity, rounding up.
    return ((size * tdensity) + (sdensity >> 1)) / sdensity;
  }

  /**
   * Asserts that two images are similar within the given thresholds.
   *
   * @param message Error message
   * @param expected Expected bitmap
   * @param actual Actual bitmap
   * @param pixelThreshold The total difference threshold for a single pixel
   * @param pixelCountThreshold The total different pixel count threshold
   * @param pixelDiffTolerance The pixel value difference tolerance
   */
  public static void compareImages(
      String message,
      Bitmap expected,
      Bitmap actual,
      float pixelThreshold,
      float pixelCountThreshold,
      int pixelDiffTolerance) {
    int idealWidth = expected.getWidth();
    int idealHeight = expected.getHeight();

    assertEquals(actual.getWidth(), idealWidth);
    assertEquals(actual.getHeight(), idealHeight);

    int totalDiffPixelCount = 0;
    float totalPixelCount = idealWidth * idealHeight;

    for (int x = 0; x < idealWidth; x++) {
      for (int y = 0; y < idealHeight; y++) {
        int idealColor = expected.getPixel(x, y);
        int givenColor = actual.getPixel(x, y);
        if (idealColor == givenColor) {
          continue;
        }
        if (Color.alpha(idealColor) + Color.alpha(givenColor) == 0) {
          continue;
        }

        float idealAlpha = Color.alpha(idealColor) / 255.0f;
        float givenAlpha = Color.alpha(givenColor) / 255.0f;

        // compare premultiplied color values
        float totalError = 0;
        totalError +=
            Math.abs((idealAlpha * Color.red(idealColor)) - (givenAlpha * Color.red(givenColor)));
        totalError +=
            Math.abs(
                (idealAlpha * Color.green(idealColor)) - (givenAlpha * Color.green(givenColor)));
        totalError +=
            Math.abs((idealAlpha * Color.blue(idealColor)) - (givenAlpha * Color.blue(givenColor)));
        totalError += Math.abs(Color.alpha(idealColor) - Color.alpha(givenColor));

        if ((totalError / 1024.0f) >= pixelThreshold) {
          Assert.fail(
              (message
                  + ": totalError is "
                  + totalError
                  + " | given: "
                  + givenColor
                  + " ideal: "
                  + idealColor));
        }

        if (totalError > pixelDiffTolerance) {
          totalDiffPixelCount++;
        }
      }
    }

    // TEST_UNDECLARED_OUTPUTS_DIR is better in a Bazel environment because the files show up
    // in test artifacts.
    String outputDir =
        MoreObjects.firstNonNull(
            System.getenv("TEST_UNDECLARED_OUTPUTS_DIR"), System.getProperty("java.io.tmpdir"));
    try {
      File f = new File(outputDir, "expected_" + RuntimeEnvironment.getApiLevel() + ".png");
      f.createNewFile();
      f.deleteOnExit();
      expected.compress(CompressFormat.PNG, 100, new FileOutputStream(f));
    } catch (IOException e) {
      throw new AssertionError(e);
    }

    try {
      File f = new File(outputDir, "actual.png");
      f.createNewFile();
      f.deleteOnExit();
      actual.compress(CompressFormat.PNG, 100, new FileOutputStream(f));
    } catch (IOException e) {
      throw new AssertionError(e);
    }

    if ((totalDiffPixelCount / totalPixelCount) >= pixelCountThreshold) {
      Assert.fail((message + ": totalDiffPixelCount is " + totalDiffPixelCount));
    }
  }

  /** Returns the {@link Color} at the specified location in the {@link Drawable}. */
  public static int getPixel(Drawable d, int x, int y) {
    final int w = max(d.getIntrinsicWidth(), x + 1);
    final int h = max(d.getIntrinsicHeight(), y + 1);
    final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    final Canvas c = new Canvas(b);
    d.setBounds(0, 0, w, h);
    d.draw(c);

    final int pixel = b.getPixel(x, y);
    b.recycle();
    return pixel;
  }

  /**
   * Save a bitmap for debugging or golden image (re)generation purpose. The file name will be
   * referred from the resource id, plus optionally {@code extras}, and "_golden"
   */
  static void saveAutoNamedVectorDrawableIntoPNG(
      @NonNull Context context,
      @NonNull Bitmap bitmap,
      @IntegerRes int resId,
      @Nullable String extras)
      throws IOException {
    String originalFilePath = context.getResources().getString(resId);
    File originalFile = new File(originalFilePath);
    String fileFullName = originalFile.getName();
    String fileTitle = fileFullName.substring(0, fileFullName.lastIndexOf("."));
    String outputFolder = context.getExternalFilesDir(null).getAbsolutePath();
    if (extras != null) {
      fileTitle += "_" + extras;
    }
    saveVectorDrawableIntoPNG(bitmap, outputFolder, fileTitle);
  }

  /** Save a {@code bitmap} to the {@code fileFullName} plus "_golden". */
  static void saveVectorDrawableIntoPNG(
      @NonNull Bitmap bitmap, @NonNull String outputFolder, @NonNull String fileFullName)
      throws IOException {
    // Save the image to the disk.
    FileOutputStream out = null;
    try {
      File folder = new File(outputFolder);
      if (!folder.exists()) {
        folder.mkdir();
      }
      String outputFilename = outputFolder + "/" + fileFullName + "_golden";
      outputFilename += ".png";
      File outputFile = new File(outputFilename);
      if (!outputFile.exists()) {
        outputFile.createNewFile();
      }

      out = new FileOutputStream(outputFile, false);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      Log.v(LOGTAG, "Write test No." + outputFilename + " to file successfully.");
    } catch (Exception e) {
      // Unused
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private DrawableTestUtils() {}
}
