package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.ResourceTypes.ANDROID_NS;
import static org.robolectric.res.android.ResourceTypes.AUTO_NS;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(AndroidJUnit4.class)
public class XmlPullParserTest {

  // emulator output:
    /*
http://schemas.android.com/apk/res/android:id(resId=16842960) type=CDATA: value=@16908308 (resId=16908308)
http://schemas.android.com/apk/res/android:height(resId=16843093) type=CDATA: value=1234.0px (resId=-1)
http://schemas.android.com/apk/res/android:width(resId=16843097) type=CDATA: value=1234.0px (resId=-1)
http://schemas.android.com/apk/res/android:title(resId=16843233) type=CDATA: value=Android Title (resId=-1)
http://schemas.android.com/apk/res/android:scrollbarFadeDuration(resId=16843432) type=CDATA: value=1111 (resId=-1)
http://schemas.android.com/apk/res-auto:title(resId=2130771971) type=CDATA: value=App Title (resId=-1)
:style(resId=0) type=CDATA: value=@android:style/TextAppearance.Small (resId=16973894)
*/

  @Test
  public void xmlParser() throws IOException, XmlPullParserException {
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    XmlResourceParser parser = resources.getXml(R.xml.xml_attrs);
    assertThat(parser).isNotNull();

    assertThat(parser.getAttributeCount()).isEqualTo(-1);

    assertThat(parser.next()).isEqualTo(XmlPullParser.START_DOCUMENT);
    assertThat(parser.next()).isEqualTo(XmlPullParser.START_TAG);

    assertThat(parser.getName()).isEqualTo("whatever");
    int attributeCount = parser.getAttributeCount();

    List<String> attrNames = new ArrayList<>();
    for (int i = 0; i < attributeCount; i++) {
      String namespace = parser.getAttributeNamespace(i);
      if (!"http://www.w3.org/2000/xmlns/".equals(namespace)) {
        attrNames.add(namespace + ":" + parser.getAttributeName(i));
      }
    }

    assertThat(attrNames).containsExactly(
        ANDROID_NS + ":id",
        ANDROID_NS + ":height",
        ANDROID_NS + ":width",
        ANDROID_NS + ":title",
        ANDROID_NS + ":scrollbarFadeDuration",
        AUTO_NS + ":title",
        ":style",
        ":class",
        ":id"
    );

    if (!RuntimeEnvironment.useLegacyResources()) {
      // doesn't work in legacy mode, sorry
      assertAttribute(parser,
          ANDROID_NS, "id", android.R.attr.id, "@" + android.R.id.text1, android.R.id.text1);
      assertAttribute(parser,
          ANDROID_NS, "height", android.R.attr.height, "@" + android.R.dimen.app_icon_size,
          android.R.dimen.app_icon_size);
      assertAttribute(parser,
          ANDROID_NS, "width", android.R.attr.width, "1234.0px", -1);
      assertAttribute(parser,
          "", "style", 0, "@android:style/TextAppearance.Small",
          android.R.style.TextAppearance_Small);
    }
    assertAttribute(parser,
        ANDROID_NS, "title", android.R.attr.title, "Android Title", -1);
    assertAttribute(parser,
        ANDROID_NS, "scrollbarFadeDuration", android.R.attr.scrollbarFadeDuration, "1111", -1);
    assertAttribute(parser,
        AUTO_NS, "title", R.attr.title, "App Title", -1);

    if (!RuntimeEnvironment.useLegacyResources()) {
      // doesn't work in legacy mode, sorry
      assertThat(parser.getStyleAttribute()).isEqualTo(android.R.style.TextAppearance_Small);
    }
    assertThat(parser.getIdAttribute()).isEqualTo("@android:id/text2");
    assertThat(parser.getClassAttribute()).isEqualTo("none");
  }

  @Test
  public void buildAttrSet() throws Exception {
    XmlResourceParser parser = (XmlResourceParser) Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.width, "1234px")
        .addAttribute(android.R.attr.height, "@android:dimen/app_icon_size")
        .addAttribute(android.R.attr.scrollbarFadeDuration, "1111")
        .addAttribute(android.R.attr.title, "Android Title")
        .addAttribute(R.attr.title, "App Title")
        .addAttribute(android.R.attr.id, "@android:id/text1")
        .setStyleAttribute("@android:style/TextAppearance.Small")
        .setClassAttribute("none")
        .setIdAttribute("@android:id/text2")
        .build();

    assertThat(parser.getName()).isEqualTo("dummy");
    int attributeCount = parser.getAttributeCount();

    List<String> attrNames = new ArrayList<>();
    for (int i = 0; i < attributeCount; i++) {
      attrNames.add(parser.getAttributeNamespace(i) + ":" + parser.getAttributeName(i));
    }
    assertThat(attrNames).containsExactly(
        ANDROID_NS + ":id",
        ANDROID_NS + ":height",
        ANDROID_NS + ":width",
        ANDROID_NS + ":title",
        ANDROID_NS + ":scrollbarFadeDuration",
        AUTO_NS + ":title",
        ":style",
        ":class",
        ":id"
    );

    assertAttribute(parser,
        ANDROID_NS, "id", android.R.attr.id, "@" + android.R.id.text1, android.R.id.text1);
    assertAttribute(parser,
        ANDROID_NS, "height", android.R.attr.height, "@" + android.R.dimen.app_icon_size,
        android.R.dimen.app_icon_size);
    assertAttribute(parser,
        ANDROID_NS, "width", android.R.attr.width, "1234.0px", -1);
    assertAttribute(parser,
        "", "style", 0, "@android:style/TextAppearance.Small",
        android.R.style.TextAppearance_Small);
    assertAttribute(parser,
        ANDROID_NS, "title", android.R.attr.title, "Android Title", -1);
    assertAttribute(parser,
        ANDROID_NS, "scrollbarFadeDuration", android.R.attr.scrollbarFadeDuration, "1111", -1);
    assertAttribute(parser,
        AUTO_NS, "title", R.attr.title, "App Title", -1);

    assertThat(parser.getStyleAttribute()).isEqualTo(android.R.style.TextAppearance_Small);
    assertThat(parser.getIdAttribute()).isEqualTo("@android:id/text2");
    assertThat(parser.getClassAttribute()).isEqualTo("none");
  }

  void assertAttribute(XmlResourceParser parser,
      String attrNs, String attrName, int resId, String value, int valueResId) {
    assertThat(format(parser, attrNs, attrName))
        .isEqualTo(format(attrNs, attrName, resId, "CDATA", value, valueResId));
  }

  private String format(XmlResourceParser parser, String namespace, String name) {
    int attributeCount = parser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      if (namespace.equals(parser.getAttributeNamespace(i))
          && name.equals(parser.getAttributeName(i))) {
        return format(parser.getAttributeNamespace(i), parser.getAttributeName(i),
            parser.getAttributeNameResource(i), parser.getAttributeType(i),
            parser.getAttributeValue(i), parser.getAttributeResourceValue(i, -1));
      }
    }
    throw new RuntimeException("not found: " + namespace + ":" + name);
  }

  private String format(String attrNs, String attrName,
      int attrResId, String type, String value, int valueResId) {
    return attrNs + ":" + attrName
        + "(resId=" + attrResId
        + "): type=" + type
        + ": value=" + value
        + "(resId=" + valueResId
        + ")";
  }
}
