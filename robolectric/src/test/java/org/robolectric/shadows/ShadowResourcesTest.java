package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
  public void getXml_shouldHavePackageContextForReferenceResolution() throws Exception {
    XmlResourceParserImpl xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?org.robolectric:attr/ref");

    xmlResourceParser =
        (XmlResourceParserImpl) resources.getXml(android.R.layout.list_content);
    assertThat(xmlResourceParser.qualify("?ref")).isEqualTo("?android:attr/ref");
  }
}
