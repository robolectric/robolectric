package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeAnimatedVectorDrawableTest {
  private static final int IMAGE_WIDTH = 64;
  private static final int IMAGE_HEIGHT = 64;

  private static final int RES_ID = R.drawable.animation_vector_drawable_grouping_1;

  private Resources resources;

  @Before
  public void setup() {
    resources = RuntimeEnvironment.getApplication().getResources();
  }

  @Test
  public void testInflate() throws Exception {
    // Setup AnimatedVectorDrawable from xml file
    XmlPullParser parser = resources.getXml(RES_ID);
    AttributeSet attrs = Xml.asAttributeSet(parser);

    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }

    if (type != XmlPullParser.START_TAG) {
      throw new XmlPullParserException("No start tag found");
    }
    Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    AnimatedVectorDrawable drawable = new AnimatedVectorDrawable();
    drawable.inflate(resources, parser, attrs);
    drawable.setBounds(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    bitmap.eraseColor(0);
    drawable.draw(canvas);
    int sunColor = bitmap.getPixel(IMAGE_WIDTH / 2, IMAGE_HEIGHT / 2);
    int earthColor = bitmap.getPixel(IMAGE_WIDTH * 3 / 4 + 2, IMAGE_HEIGHT / 2);
    assertEquals(0xFFFF8000, sunColor);
    assertEquals(0xFF5656EA, earthColor);
  }

  @Test
  public void legacyShadowDrawableAPI() {
    Drawable drawable = resources.getDrawable(RES_ID);
    ShadowDrawable shadowDrawable = Shadow.extract(drawable);
    assertEquals(
        R.drawable.animation_vector_drawable_grouping_1, shadowDrawable.getCreatedFromResId());
  }

  @Test
  public void start_isRunning_returnsTrue() throws Exception {
    // Setup AnimatedVectorDrawable from xml file
    XmlPullParser parser = resources.getXml(RES_ID);
    AttributeSet attrs = Xml.asAttributeSet(parser);

    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }

    if (type != XmlPullParser.START_TAG) {
      throw new XmlPullParserException("No start tag found");
    }
    AnimatedVectorDrawable drawable = new AnimatedVectorDrawable();
    drawable.inflate(resources, parser, attrs);

    drawable.start();

    assertTrue(Shadows.shadowOf(drawable).isStartInitiated());
  }

  @Test
  public void stop_returnsFalse() throws Exception {
    // Setup AnimatedVectorDrawable from xml file
    XmlPullParser parser = resources.getXml(RES_ID);
    AttributeSet attrs = Xml.asAttributeSet(parser);

    int type;
    while ((type = parser.next()) != XmlPullParser.START_TAG
        && type != XmlPullParser.END_DOCUMENT) {
      // Empty loop
    }

    if (type != XmlPullParser.START_TAG) {
      throw new XmlPullParserException("No start tag found");
    }
    AnimatedVectorDrawable drawable = new AnimatedVectorDrawable();
    drawable.inflate(resources, parser, attrs);

    drawable.start();
    drawable.stop();

    assertFalse(Shadows.shadowOf(drawable).isStartInitiated());
  }
}
