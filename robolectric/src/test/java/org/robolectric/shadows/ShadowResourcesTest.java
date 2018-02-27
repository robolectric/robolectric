package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowArscAssetManager.isLegacyAssetManager;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;
import java.io.File;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

@RunWith(RobolectricTestRunner.class)
public class ShadowResourcesTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  public void getText_withHtml() throws Exception {
    assertThat(resources.getText(R.string.some_html, "value").toString()).isEqualTo("Hello, world");
  }

  @Test
  public void getText_withLayoutId() throws Exception {
    // This isn't _really_ supported by the platform (gives a lint warning that getText() expects a String resource type
    // but the actual platform behaviour is to return a string that equals "res/layout/layout_file.xml" so the current
    // Robolectric behaviour deviates from the platform as we append the full file path from the current working directory.
    if (isLegacyAssetManager()) {
      assertThat(resources.getText(R.layout.different_screen_sizes, "value"))
          .endsWith("res" + File.separator + "layout-xlarge" + File.separator + "different_screen_sizes.xml");
    } else {
      assertThat(resources.getText(R.layout.different_screen_sizes, "value"))
          .endsWith("res" + File.separator + "layout-xlarge-v4" + File.separator + "different_screen_sizes.xml");
    }
  }

  @Test
  //@Config(sdk = 16) // todo: unpin
  public void getDimension() throws Exception {
    DisplayMetrics dm = RuntimeEnvironment.application.getResources().getDisplayMetrics();
    assertThat(dm.density).isEqualTo(1.0f);
    assertThat(resources.getDimension(R.dimen.test_dip_dimen)).isEqualTo(20f);
    assertThat(resources.getDimension(R.dimen.test_dp_dimen)).isEqualTo(8f);
    assertThat(resources.getDimension(R.dimen.test_in_dimen)).isEqualTo(99f * 160);
    assertThat(resources.getDimension(R.dimen.test_mm_dimen)).isEqualTo(((float) (42f / 25.4 * 160)));
    assertThat(resources.getDimension(R.dimen.test_px_dimen)).isEqualTo(15f);
    assertThat(resources.getDimension(R.dimen.test_pt_dimen)).isEqualTo(12f * 160 / 72);
    assertThat(resources.getDimension(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  public void getDimensionPixelSize() throws Exception {
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dip_dimen)).isEqualTo(20);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dp_dimen)).isEqualTo(8);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_in_dimen)).isEqualTo(99 * 160);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_mm_dimen)).isEqualTo(265);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_px_dimen)).isEqualTo(15);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_pt_dimen)).isEqualTo(27);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  public void getDimensionPixelOffset() throws Exception {
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dip_dimen)).isEqualTo(20);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dp_dimen)).isEqualTo(8);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_in_dimen)).isEqualTo(99 * 160);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_mm_dimen)).isEqualTo(264);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_px_dimen)).isEqualTo(15);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_pt_dimen)).isEqualTo(26);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  @Config(qualifiers = "en")
  public void getQuantityString() throws Exception {
    assertThat(resources.getQuantityString(R.plurals.beer, 0)).isEqualTo("beers");
    assertThat(resources.getQuantityString(R.plurals.beer, 1)).isEqualTo("beer");
    assertThat(resources.getQuantityString(R.plurals.beer, 2)).isEqualTo("beers");
    assertThat(resources.getQuantityString(R.plurals.beer, 3)).isEqualTo("beers");
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

  @Test
  public void testDensity() {
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().density).isEqualTo(1f);

    shadowOf(RuntimeEnvironment.application.getResources()).setDensity(1.5f);
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().density).isEqualTo(1.5f);
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
    if (isLegacyAssetManager()) {
      assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNull();
    } else {
      assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNotNull();
    }

  }

  @Test
  public void setScaledDensityShouldSetScaledDensityInDisplayMetrics() {
    final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

    assertThat(displayMetrics.scaledDensity).isEqualTo(1f);
    shadowOf(resources).setScaledDensity(2.5f);
    assertThat(displayMetrics.scaledDensity).isEqualTo(2.5f);
  }

  @Test
  @Config
  public void themeResolveAttribute_shouldSupportDereferenceResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, true);
    assertThat(out.type).isNotEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.type).isBetween(TypedValue.TYPE_FIRST_COLOR_INT, TypedValue.TYPE_LAST_COLOR_INT);

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

    assertThat(typedArray.getDimension(0, 0)).isEqualTo(240f);
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

    TypedArray typedArray = RuntimeEnvironment.application.getTheme().obtainStyledAttributes(attributes, new int[] {
        android.R.attr.viewportWidth,
        android.R.attr.viewportHeight
    }, 0, 0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
    typedArray.recycle();
  }

  // todo: port to ResourcesTest
  @Test
  public void obtainStyledAttributesShouldCheckXmlFirst_andFollowReferences() throws Exception {
    // TODO: investigate failure with binary resources
    assumeTrue(isLegacyAssetManager());

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
    // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
    // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.

    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.viewportWidth, "@integer/test_integer1")
        .addAttribute(android.R.attr.viewportHeight, "@integer/test_integer2")
        .build();

    TypedArray typedArray = RuntimeEnvironment.application.getTheme().obtainStyledAttributes(attributes, new int[] {
        android.R.attr.viewportWidth,
        android.R.attr.viewportHeight
    }, 0, 0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(2000);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(9);
    typedArray.recycle();
  }

  @Test
  public void getValueShouldClearTypedArrayBetweenCalls() throws Exception {
    if (!isLegacyAssetManager()) {
      return;
    }
    TypedValue outValue = new TypedValue();

    resources.getValue(R.string.hello, outValue, true);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_STRING);
    assertThat(outValue.string).isEqualTo(resources.getString(R.string.hello));
    assertThat(outValue.data).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);
    assertThat(outValue.assetCookie).isNotEqualTo(0);

    resources.getValue(R.color.blue, outValue, true);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB8);
    assertThat(outValue.data).isEqualTo(ResourceHelper.getColor("#0000ff"));
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);

    resources.getValue(R.integer.loneliest_number, outValue, true);
    if (isLegacyAssetManager()) {
      assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_DEC);
    } else {
      // wtf?
      assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_DEC);
    }
    assertThat(outValue.data).isEqualTo(1);
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);

    resources.getValue(R.bool.true_bool_value, outValue, true);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_BOOLEAN);
    assertThat(outValue.data).isEqualTo(1);
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);
  }

  @Test @Config(sdk = 25)
  public void getXml() throws Exception {
    XmlResourceParser xmlResourceParser = resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser).isNotNull();
    assertThat(xmlResourceParser.next()).isEqualTo(XmlResourceParser.START_DOCUMENT);
    assertThat(xmlResourceParser.next()).isEqualTo(XmlResourceParser.START_TAG);
    assertThat(xmlResourceParser.getName()).isEqualTo("PreferenceScreen");
  }

  @Test
  public void getXml_shouldHavePackageContextForReferenceResolution() throws Exception {
    if (!isLegacyAssetManager()) {
      return;
    }
    XmlResourceParserImpl xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?org.robolectric:attr/ref");

    xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(android.R.layout.list_content);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?android:attr/ref");
  }

  @Test
  public void stringWithSpaces() throws Exception {
    if (isLegacyAssetManager()) return;

    assertThat(resources.getString(R.string.string_with_spaces, "25", "USD"))
        .isEqualTo("Up to 25 USD");
  }

  // todo: port to ResourcesTest
  @Test
  public void getResourceName() {
    assertThat(resources.getResourceName(R.string.hello)).isEqualTo("org.robolectric:string/hello");
  }

  // todo: port to ResourcesTest
  @Test
  public void getResourceName_system() {
    assertThat(resources.getResourceName(android.R.string.ok)).isEqualTo("android:string/ok");
  }

  // todo: port to ResourcesTest
  @Test
  public void getTextArray() {
    assertThat(resources.getTextArray(R.array.more_items)).containsExactly("baz", "bang");
  }

  // todo: port to ResourcesTest
  @Test
  public void getResourceTypeName_mipmap() {
    assertThat(resources.getResourceTypeName(R.mipmap.mipmap_reference)).isEqualTo("mipmap");
    assertThat(resources.getResourceTypeName(R.mipmap.robolectric)).isEqualTo("mipmap");
  }

  // todo: port to ResourcesTest
  @Test
  public void getDrawable_mipmapReferencesResolve() {
    Drawable reference = resources.getDrawable(R.mipmap.mipmap_reference);
    Drawable original = resources.getDrawable(R.mipmap.robolectric);

    assertThat(reference.getMinimumHeight()).isEqualTo(original.getMinimumHeight());
    assertThat(reference.getMinimumWidth()).isEqualTo(original.getMinimumWidth());
  }

  // todo: port to ResourcesTest
  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getDrawable_mipmapReferencesResolveXml() {
    Drawable reference = resources.getDrawable(R.mipmap.robolectric_xml);
    Drawable original = resources.getDrawable(R.mipmap.mipmap_reference_xml);

    assertThat(reference.getMinimumHeight()).isEqualTo(original.getMinimumHeight());
    assertThat(reference.getMinimumWidth()).isEqualTo(original.getMinimumWidth());
  }
}
