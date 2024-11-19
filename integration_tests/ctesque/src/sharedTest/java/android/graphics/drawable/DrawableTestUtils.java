package android.graphics.drawable;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Partially copied from <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:cts/tests/tests/graphics/src/android/graphics/drawable/cts/DrawableTestUtils.java">DrawableTestUtils</a>
 */
public class DrawableTestUtils {
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
}
