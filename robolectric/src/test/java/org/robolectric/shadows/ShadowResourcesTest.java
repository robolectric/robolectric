package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Range;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

@RunWith(AndroidJUnit4.class)
public class ShadowResourcesTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = ApplicationProvider.getApplicationContext().getResources();
  }

  @Test
  @Config(qualifiers = "fr")
  public void testGetValuesResFromSpecificQualifiers() {
    assertThat(resources.getString(R.string.hello)).isEqualTo("Bonjour");
  }

  /**
   * Public framework symbols are defined here: https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/public.xml
   * Private framework symbols are defined here: https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/symbols.xml
   *
   * These generate android.R and com.android.internal.R respectively, when Framework Java code does not need to reference a framework resource
   * it will not have an R value generated. Robolectric is then missing an identifier for this resource so we must generate a placeholder ourselves.
   */
  @Test
  @Config(sdk = Build.VERSION_CODES.LOLLIPOP) // android:color/secondary_text_material_dark was added in API 21
  public void shouldGenerateIdsForResourcesThatAreMissingRValues() throws Exception {
    int identifier_missing_from_r_file = resources.getIdentifier("secondary_text_material_dark", "color", "android");

    // We expect Robolectric to generate a placeholder identifier where one was not generated in the android R files.
    assertThat(identifier_missing_from_r_file).isNotEqualTo(0);

    // We expect to be able to successfully android:color/secondary_text_material_dark to a ColorStateList.
    assertThat(resources.getColorStateList(identifier_missing_from_r_file)).isNotNull();
  }

  @Test @Config(qualifiers = "fr")
  public void openRawResource_shouldLoadDrawableWithQualifiers() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.drawable.an_image);
    Bitmap bitmap = BitmapFactory.decodeStream(resourceStream);
    assertThat(bitmap.getHeight()).isEqualTo(100);
    assertThat(bitmap.getWidth()).isEqualTo(100);
  }

  @Test
  public void openRawResourceFd_returnsNull_todo_FIX() throws Exception {
    if (useLegacy()) {
      assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNull();
    } else {
      assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNotNull();
    }
  }

  @Test
  @Config
  public void themeResolveAttribute_shouldSupportDereferenceResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, true);
    assertThat(out.type).isNotEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.type).isIn(Range.closed(TypedValue.TYPE_FIRST_COLOR_INT, TypedValue.TYPE_LAST_COLOR_INT));

    int value = resources.getColor(android.R.color.black);
    assertThat(out.data).isEqualTo(value);
  }

  @Test
  public void themeResolveAttribute_shouldSupportNotDereferencingResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, false);
    assertThat(out.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.data).isEqualTo(android.R.color.black);
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainAttributes_shouldUseReferencedIdFromAttributeSet() throws Exception {
    // android:id/mask was introduced in API 21, but it's still possible for apps built against API 21 to refer to it
    // in older runtimes because referenced resource ids are compiled (by aapt) into the binary XML format.
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@android:id/mask").build();
    TypedArray typedArray = resources.obtainAttributes(attributeSet, new int[]{android.R.attr.id});
    assertThat(typedArray.getResourceId(0, -9)).isEqualTo(android.R.id.mask);
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainAttributes() {
    TypedArray typedArray = resources.obtainAttributes(Robolectric.buildAttributeSet()
        .addAttribute(R.attr.styleReference, "@xml/shortcuts")
        .build(), new int[]{R.attr.styleReference});
    assertThat(typedArray).isNotNull();
    assertThat(typedArray.peekValue(0).resourceId).isEqualTo(R.xml.shortcuts);
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainAttributes_shouldReturnValuesFromAttributeSet() throws Exception {
    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.title, "A title!")
        .addAttribute(android.R.attr.width, "12px")
        .addAttribute(android.R.attr.height, "1in")
        .build();
    TypedArray typedArray = resources
        .obtainAttributes(attributes, new int[]{android.R.attr.height,
            android.R.attr.width, android.R.attr.title});

    assertThat(typedArray.getDimension(0, 0)).isEqualTo(160f);
    assertThat(typedArray.getDimension(1, 0)).isEqualTo(12f);
    assertThat(typedArray.getString(2)).isEqualTo("A title!");
    typedArray.recycle();
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainAttributes_shouldReturnValuesFromResources() throws Exception {
    XmlPullParser parser = resources.getXml(R.xml.xml_attrs);
    parser.next();
    parser.next();
    AttributeSet attributes = Xml.asAttributeSet(parser);

    TypedArray typedArray = resources
        .obtainAttributes(attributes, new int[]{android.R.attr.title, android.R.attr.scrollbarFadeDuration});

    assertThat(typedArray.getString(0)).isEqualTo("Android Title");
    assertThat(typedArray.getInt(1, 0)).isEqualTo(1111);
    typedArray.recycle();
  }

  @Test
  public void obtainStyledAttributes_shouldCheckXmlFirst_fromAttributeSetBuilder() throws Exception {

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
    // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
    // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.

    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.viewportWidth, "12.0")
        .addAttribute(android.R.attr.viewportHeight, "24.0")
        .build();

    TypedArray typedArray =
        ApplicationProvider.getApplicationContext()
            .getTheme()
            .obtainStyledAttributes(
                attributes,
                new int[] {android.R.attr.viewportWidth, android.R.attr.viewportHeight},
                0,
                0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
    typedArray.recycle();
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainStyledAttributesShouldCheckXmlFirst_andFollowReferences() throws Exception {
    // TODO: investigate failure with binary resources
    assumeTrue(useLegacy());

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
    // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
    // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.

    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.viewportWidth, "@dimen/dimen20px")
        .addAttribute(android.R.attr.viewportHeight, "@dimen/dimen30px")
        .build();

    TypedArray typedArray =
        ApplicationProvider.getApplicationContext()
            .getTheme()
            .obtainStyledAttributes(
                attributes,
                new int[] {android.R.attr.viewportWidth, android.R.attr.viewportHeight},
                0,
                0);
    assertThat(typedArray.getDimension(0, 0)).isEqualTo(20f);
    assertThat(typedArray.getDimension(1, 0)).isEqualTo(30f);
    typedArray.recycle();
  }

  @Test
  public void getXml_shouldHavePackageContextForReferenceResolution() throws Exception {
    if (!useLegacy()) {
      return;
    }
    XmlResourceParserImpl xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?org.robolectric:attr/ref");

    xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(android.R.layout.list_content);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?android:attr/ref");
  }
}
