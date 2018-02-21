package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.io.File;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowResourcesTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  public void getText_withHtml() throws Exception {
    assertThat(resources.getText(R.string.some_html, "value")).isEqualTo("Hello, world");
  }

  @Test
  public void getText_withLayoutId() throws Exception {
    // This isn't _really_ supported by the platform (gives a lint warning that getText() expects a String resource type
    // but the actual platform behaviour is to return a string that equals "res/layout/layout_file.xml" so the current
    // Robolectric behaviour deviates from the platform as we append the full file path from the current working directory.
    assertThat(resources.getText(R.layout.different_screen_sizes, "value")).endsWith("res" + File.separator + "layout" + File.separator + "different_screen_sizes.xml");
  }

  @Test
  public void obtainTypedArray() throws Exception {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.density = 1;
    displayMetrics.scaledDensity = 1;
    displayMetrics.xdpi = 160;

    final TypedArray valuesTypedArray = resources.obtainTypedArray(R.array.typed_array_values);
    assertThat(valuesTypedArray.getString(0)).isEqualTo("abcdefg");
    assertThat(valuesTypedArray.getInt(1, 0)).isEqualTo(3875);
    assertThat(valuesTypedArray.getInteger(1, 0)).isEqualTo(3875);
    assertThat(valuesTypedArray.getFloat(2, 0.0f)).isEqualTo(2.0f);
    assertThat(valuesTypedArray.getColor(3, Color.BLACK)).isEqualTo(Color.MAGENTA);
    assertThat(valuesTypedArray.getColor(4, Color.BLACK)).isEqualTo(Color.parseColor("#00ffff"));
    assertThat(valuesTypedArray.getDimension(5, 0.0f)).isEqualTo(8.0f);
    assertThat(valuesTypedArray.getDimension(6, 0.0f)).isEqualTo(12.0f);
    assertThat(valuesTypedArray.getDimension(7, 0.0f)).isEqualTo(6.0f);
    assertThat(valuesTypedArray.getDimension(8, 0.0f)).isEqualTo(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 3.0f, displayMetrics));
    assertThat(valuesTypedArray.getDimension(9, 0.0f)).isEqualTo(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 4.0f, displayMetrics));
    assertThat(valuesTypedArray.getDimension(10, 0.0f)).isEqualTo(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 36.0f, displayMetrics));
    assertThat(valuesTypedArray.getDimension(11, 0.0f)).isEqualTo(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, 18.0f, displayMetrics));

    final TypedArray refsTypedArray = resources.obtainTypedArray(R.array.typed_array_references);
    assertThat(refsTypedArray.getString(0)).isEqualTo("apple");
    assertThat(refsTypedArray.getString(1)).isEqualTo("banana");
    assertThat(refsTypedArray.getInt(2, 0)).isEqualTo(5);
    assertThat(refsTypedArray.getBoolean(3, false)).isTrue();

    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      assertThat(refsTypedArray.getType(4)).isEqualTo(TypedValue.TYPE_NULL);
    }

    assertThat(shadowOf(refsTypedArray.getDrawable(5)).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
    assertThat(refsTypedArray.getColor(6, Color.BLACK)).isEqualTo(Color.parseColor("#ff5c00"));

    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      assertThat(refsTypedArray.getThemeAttributeId(7, -1)).isEqualTo(R.attr.animalStyle);
    }

    assertThat(refsTypedArray.getResourceId(8, 0)).isEqualTo(R.array.typed_array_values);
    assertThat(refsTypedArray.getTextArray(8))
        .containsExactly("abcdefg", "3875", "2.0", "#ffff00ff", "#00ffff", "8px",
            "12dp", "6dip", "3mm", "4in", "36sp", "18pt");

    assertThat(refsTypedArray.getResourceId(9, 0)).isEqualTo(R.style.Theme_Robolectric);
  }

  @Test
  public void getQuantityString() throws Exception {
    assertThat(resources.getQuantityString(R.plurals.beer, 0)).isEqualTo("Howdy");
    assertThat(resources.getQuantityString(R.plurals.beer, 1)).isEqualTo("One beer");
    assertThat(resources.getQuantityString(R.plurals.beer, 2)).isEqualTo("Two beers");
    assertThat(resources.getQuantityString(R.plurals.beer, 3)).isEqualTo("%d beers, yay!");
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

    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getResources().getDisplayMetrics().density).isEqualTo(1.5f);
  }

  @Test
  public void displayMetricsShouldNotHaveLotsOfZeros() throws Exception {
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().heightPixels).isEqualTo(470);
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().widthPixels).isEqualTo(320);
  }

  @Test
  public void openRawResource_shouldLoadDrawables() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.drawable.an_image);
    assertThat(resourceStream).isNotNull();
  }

  @Test @Config(qualifiers = "hdpi")
  public void openRawResource_shouldLoadDrawableWithQualifiers() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.drawable.an_image);
    assertThat(resourceStream).isNotNull();
  }

  @Test
  public void openRawResourceFd_returnsNull_todo_FIX() throws Exception {
    assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNull();
  }

  @Test
  public void setScaledDensityShouldSetScaledDensityInDisplayMetrics() {
    final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

    assertThat(displayMetrics.scaledDensity).isEqualTo(1f);
    shadowOf(resources).setScaledDensity(2.5f);
    assertThat(displayMetrics.scaledDensity).isEqualTo(2.5f);
  }

  @Test
  public void themeResolveAttribute_shouldSupportDereferenceResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, true);
    assertThat(out.type).isNotEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.type).isBetween(TypedValue.TYPE_FIRST_COLOR_INT, TypedValue.TYPE_LAST_COLOR_INT);

    TypedValue expected = new TypedValue();
    ShadowAssetManager shadow = Shadows.shadowOf(resources.getAssets());
    shadow.getResourceValue(android.R.color.black, TypedValue.DENSITY_DEFAULT, expected, false);
    assertThat(out.type).isEqualTo(expected.type);
    assertThat(out.data).isEqualTo(expected.data);
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
  @Config(sdk = KITKAT)
  public void whenAttrIsNotDefinedInRuntimeSdk_getResourceName_doesntFindRequestedResourceButInsteadFindsInternalResourceWithSameId() {
    // asking for an attr defined after the current SDK doesn't have a defined result; in this case it returns
    //   numberPickerStyle from com.internal.android.R
    assertThat(RuntimeEnvironment.application.getResources().getResourceName(android.R.attr.viewportHeight))
        .isEqualTo("android:attr/numberPickerStyle");

    assertThat(RuntimeEnvironment.application.getResources().getIdentifier("viewportHeight", "attr", "android")).isEqualTo(0);
  }

  @Test
  public void getValueShouldClearTypedArrayBetweenCalls() throws Exception {
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
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_DEC);
    assertThat(outValue.data).isEqualTo(1);
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);

    resources.getValue(R.bool.true_bool_value, outValue, true);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_BOOLEAN);
    assertThat(outValue.data).isEqualTo(1);
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);
  }

  @Test
  public void getXml_shouldHavePackageContextForReferenceResolution() throws Exception {
    XmlResourceParserImpl xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?org.robolectric:attr/ref");

    xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(android.R.layout.list_content);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?android:attr/ref");
  }

  @Test
  public void whenMissingXml_loadXmlResourceParser() throws Exception {
    try {
      resources.getXml(R.id.ungulate);
      fail();
    } catch (Resources.NotFoundException e) {
      assertThat(e.getMessage()).contains("org.robolectric:id/ungulate");
    }
  }

  @Test
  public void stringWithSpaces() throws Exception {
    // this differs from actual Android behavior, which collapses whitespace as "Up to 25 USD"
    assertThat(resources.getString(R.string.string_with_spaces, "25", "USD"))
        .isEqualTo("Up to 25   USD");
  }
}
