package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import java.io.File;
import java.io.InputStream;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.TestUtil;
import org.xmlpull.v1.XmlPullParser;

@RunWith(RobolectricTestRunner.class)
public class ShadowResourcesTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  public void getString() throws Exception {
    assertThat(resources.getString(R.string.hello)).isEqualTo("Hello");
    assertThat(resources.getString(R.string.say_it_with_item)).isEqualTo("flowers");
  }

  @Test
  public void getString_withReference() throws Exception {
    assertThat(resources.getString(R.string.greeting)).isEqualTo("Howdy");
  }

  @Test
  public void getString_withInterpolation() throws Exception {
    assertThat(resources.getString(R.string.interpolate, "value")).isEqualTo("Here is a value!");
  }

  @Test
  public void getString_withHtml() throws Exception {
    assertThat(resources.getString(R.string.some_html, "value")).isEqualTo("Hello, world");
  }

  @Test
  public void getString_withSurroundingQuotes() throws Exception {
    assertThat(resources.getString(R.string.surrounding_quotes, "value")).isEqualTo("This'll work");
  }

  @Test
  public void getStringWithEscapedApostrophes() throws Exception {
    assertThat(resources.getString(R.string.escaped_apostrophe)).isEqualTo("This'll also work");
  }

  @Test
  public void getStringWithEscapedQuotes() throws Exception {
    assertThat(resources.getString(R.string.escaped_quotes)).isEqualTo("Click \"OK\"");
  }

  @Test
  public void getStringShouldStripNewLines() {
    assertThat(resources.getString(R.string.leading_and_trailing_new_lines)).isEqualTo("Some text");
  }

  @Test
  public void preserveEscapedNewlineAndTab() {
    assertThat(resources.getString(R.string.new_lines_and_tabs, 4)).isEqualTo("4\tmph\nfaster");
  }

  @Test
  public void getStringShouldConvertCodePoints() {
    assertThat(resources.getString(R.string.non_breaking_space)).isEqualTo("Closing soon:\u00A05pm");
    assertThat(resources.getString(R.string.space)).isEqualTo("Closing soon: 5pm");
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
  public void getStringArray() throws Exception {
    assertThat(resources.getStringArray(R.array.items)).isEqualTo(new String[]{"foo", "bar"});
    assertThat(resources.getStringArray(R.array.greetings)).isEqualTo(new String[]{"hola", "Hello"});
  }

  @Test
  public void withIdReferenceEntry_obtainTypedArray() {
    TypedArray typedArray = resources.obtainTypedArray(R.array.typed_array_with_resource_id);
    assertThat(typedArray.length()).isEqualTo(2);

    assertThat(typedArray.getResourceId(0, 0)).isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(typedArray.getResourceId(1, 0)).isEqualTo(R.id.id_declared_in_layout);
  }

  @Test
  public void obtainTypedArray() throws Exception {
    final Display display = Shadow.newInstanceOf(Display.class);
    final ShadowDisplay shadowDisplay = shadowOf(display);
    // Standard xxhdpi screen
    shadowDisplay.setDensityDpi(480);
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    display.getMetrics(displayMetrics);

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
  public void getInt() throws Exception {
    assertThat(resources.getInteger(R.integer.meaning_of_life)).isEqualTo(42);
    assertThat(resources.getInteger(R.integer.test_integer1)).isEqualTo(2000);
    assertThat(resources.getInteger(R.integer.test_integer2)).isEqualTo(9);
    assertThat(resources.getInteger(R.integer.test_large_hex)).isEqualTo(-65536);
    assertThat(resources.getInteger(R.integer.test_value_with_zero)).isEqualTo(7210);
    assertThat(resources.getInteger(R.integer.meaning_of_life_as_item)).isEqualTo(42);
  }

  @Test
  public void getInt_withReference() throws Exception {
    assertThat(resources.getInteger(R.integer.reference_to_meaning_of_life)).isEqualTo(42);
  }

  @Test
  public void getIntArray() throws Exception {
    assertThat(resources.getIntArray(R.array.empty_int_array)).isEqualTo(new int[]{});
    assertThat(resources.getIntArray(R.array.zero_to_four_int_array)).isEqualTo(new int[]{0, 1, 2, 3, 4});
    assertThat(resources.getIntArray(R.array.with_references_int_array)).isEqualTo(new int[]{0, 2000, 1});
    assertThat(resources.getIntArray(R.array.referenced_colors_int_array)).isEqualTo(new int[]{0x1, 0xFFFFFFFF, 0xFF000000, 0xFFF5F5F5, 0x802C76AD});
  }

  @Test
  public void getBoolean() throws Exception {
    assertThat(resources.getBoolean(R.bool.false_bool_value)).isEqualTo(false);
    assertThat(resources.getBoolean(R.bool.true_as_item)).isEqualTo(true);
  }

  @Test
  public void getBoolean_withReference() throws Exception {
    assertThat(resources.getBoolean(R.bool.reference_to_true)).isEqualTo(true);
  }

  @Test
  public void getDimension() throws Exception {
    assertThat(resources.getDimension(R.dimen.test_dip_dimen)).isEqualTo(20f);
    assertThat(resources.getDimension(R.dimen.test_dp_dimen)).isEqualTo(8f);
    assertThat(resources.getDimension(R.dimen.test_in_dimen)).isEqualTo(99f * 240);
    assertThat(resources.getDimension(R.dimen.test_mm_dimen)).isEqualTo(((float) (42f / 25.4 * 240)));
    assertThat(resources.getDimension(R.dimen.test_px_dimen)).isEqualTo(15f);
    assertThat(resources.getDimension(R.dimen.test_pt_dimen)).isEqualTo(12 / 0.3f);
    assertThat(resources.getDimension(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  public void getDimensionPixelSize() throws Exception {
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dip_dimen)).isEqualTo(20);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dp_dimen)).isEqualTo(8);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_in_dimen)).isEqualTo(99 * 240);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_mm_dimen)).isEqualTo(397);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_px_dimen)).isEqualTo(15);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_pt_dimen)).isEqualTo(40);
    assertThat(resources.getDimensionPixelSize(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  public void getDimensionPixelOffset() throws Exception {
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dip_dimen)).isEqualTo(20);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dp_dimen)).isEqualTo(8);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_in_dimen)).isEqualTo(99 * 240);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_mm_dimen)).isEqualTo(396);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_px_dimen)).isEqualTo(15);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_pt_dimen)).isEqualTo(40);
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_sp_dimen)).isEqualTo(5);
  }

  @Test
  public void getDimension_withReference() throws Exception {
    assertThat(resources.getBoolean(R.bool.reference_to_true)).isEqualTo(true);
  }

  @Test(expected = Resources.NotFoundException.class)
  public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
    resources.getStringArray(-1);
  }

  @Test(expected = Resources.NotFoundException.class)
  public void getIntegerArray_shouldThrowExceptionIfNotFound() throws Exception {
    resources.getIntArray(-1);
  }

  @Test
  public void getQuantityString() throws Exception {
    assertThat(resources.getQuantityString(R.plurals.beer, 0)).isEqualTo("Howdy");
    assertThat(resources.getQuantityString(R.plurals.beer, 1)).isEqualTo("One beer");
    assertThat(resources.getQuantityString(R.plurals.beer, 2)).isEqualTo("Two beers");
    assertThat(resources.getQuantityString(R.plurals.beer, 3)).isEqualTo("%d beers, yay!");
  }

  @Test
  public void getFraction() throws Exception {
    final int myself = 300;
    final int myParent = 600;
    assertThat(resources.getFraction(R.fraction.half, myself, myParent)).isEqualTo(150f);
    assertThat(resources.getFraction(R.fraction.half_of_parent, myself, myParent)).isEqualTo(300f);

    assertThat(resources.getFraction(R.fraction.quarter_as_item, myself, myParent)).isEqualTo(75f);
    assertThat(resources.getFraction(R.fraction.quarter_of_parent_as_item, myself, myParent)).isEqualTo(150f);

    assertThat(resources.getFraction(R.fraction.fifth_as_reference, myself, myParent)).isEqualTo(60f, Offset.offset(0.01f));
    assertThat(resources.getFraction(R.fraction.fifth_of_parent_as_reference, myself, myParent)).isEqualTo(120f, Offset.offset(0.01f));
  }

  @Test
  public void testConfiguration() {
    Configuration configuration = resources.getConfiguration();
    assertThat(configuration).isNotNull();
    assertThat(configuration.locale).isNotNull();
  }

  @Test
  public void testConfigurationReturnsTheSameInstance() {
    assertThat(resources.getConfiguration()).isSameAs(resources.getConfiguration());
  }

  @Test
  public void testNewTheme() {
    assertThat(resources.newTheme()).isNotNull();
  }

  @Test(expected = Resources.NotFoundException.class)
  public void testGetDrawableNullRClass() throws Exception {
    assertThat(resources.getDrawable(-12345)).isInstanceOf(BitmapDrawable.class);
  }

  @Test
  public void testGetAnimationDrawable() {
    assertThat(resources.getDrawable(R.anim.animation_list)).isInstanceOf(AnimationDrawable.class);
  }

  @Test
  @Config(qualifiers = "fr")
  public void testGetValuesResFromSpecificQualifiers() {
    assertThat(resources.getString(R.string.hello)).isEqualTo("Bonjour");
  }

  @Test
  public void testGetColorDrawable() {
    Drawable drawable = resources.getDrawable(R.color.color_with_alpha);
    assertThat(drawable).isInstanceOf(ColorDrawable.class);
    assertThat(((ColorDrawable) drawable).getColor()).isEqualTo(0x802C76AD);
  }

  @Test
  public void getColor() {
    assertThat(resources.getColor(R.color.color_with_alpha)).isEqualTo(0x802C76AD);
  }

  @Test
  public void getColor_withReference() {
    assertThat(resources.getColor(R.color.background)).isEqualTo(0xfff5f5f5);
  }

  @Test(expected = Resources.NotFoundException.class)
  public void testGetColor_Missing() {
    resources.getColor(R.color.test_color_1);
  }

  @Test
  public void testGetColorStateList() {
    assertThat(resources.getColorStateList(R.color.color_state_list)).isInstanceOf(ColorStateList.class);
  }

  @Test
  public void testGetBitmapDrawable() {
    assertThat(resources.getDrawable(R.drawable.an_image)).isInstanceOf(BitmapDrawable.class);
  }

  @Test
  public void testGetNinePatchDrawable() {
    assertThat(resources.getDrawable(R.drawable.nine_patch_drawable)).isInstanceOf(NinePatchDrawable.class);
  }

  @Test(expected = Resources.NotFoundException.class)
  public void testGetBitmapDrawableForUnknownId() {
    assertThat(resources.getDrawable(Integer.MAX_VALUE)).isInstanceOf(BitmapDrawable.class);
  }

  @Test
  public void testGetIdentifier() throws Exception {

    final String resourceType = "string";
    final String packageName = RuntimeEnvironment.application.getPackageName();

    final String resourceName = "hello";
    final int resId1 = resources.getIdentifier(resourceName, resourceType, packageName);
    assertThat(resId1).isEqualTo(R.string.hello);

    final String typedResourceName = resourceType + "/" + resourceName;
    final int resId2 = resources.getIdentifier(typedResourceName, resourceType, packageName);
    assertThat(resId2).isEqualTo(R.string.hello);

    final String fqn = packageName + ":" + typedResourceName;
    final int resId3 = resources.getIdentifier(fqn, resourceType, packageName);
    assertThat(resId3).isEqualTo(R.string.hello);
  }

  @Test
  public void getIdentifier() {
    String string = resources.getString(R.string.hello);
    assertThat(string).isEqualTo("Hello");

    int id = resources.getIdentifier("hello", "string", "org.robolectric");
    assertThat(id).isEqualTo(R.string.hello);

    String hello = resources.getString(id);
    assertThat(hello).isEqualTo("Hello");
  }

  @Test
  public void getIdentifier_nonExistantResource() {
    int id = resources.getIdentifier("just_alot_of_crap", "string", "org.robolectric");
    assertThat(id).isEqualTo(0);
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
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().heightPixels).isEqualTo(800);
    assertThat(RuntimeEnvironment.application.getResources().getDisplayMetrics().widthPixels).isEqualTo(480);
  }

  @Test
  public void getSystemShouldReturnSystemResources() throws Exception {
    assertThat(Resources.getSystem()).isInstanceOf(Resources.class);
  }

  @Test
  public void multipleCallsToGetSystemShouldReturnSameInstance() throws Exception {
    assertThat(Resources.getSystem()).isEqualTo(Resources.getSystem());
  }

  @Test
  public void applicationResourcesShouldHaveBothSystemAndLocalValues() throws Exception {
    assertThat(RuntimeEnvironment.application.getResources().getString(android.R.string.copy)).isEqualTo("Copy");
    assertThat(RuntimeEnvironment.application.getResources().getString(R.string.copy)).isEqualTo("Local Copy");
  }

  @Test
  public void systemResourcesShouldReturnCorrectSystemId() throws Exception {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", "android")).isEqualTo(android.R.string.copy);
  }

  @Test
  public void systemResourcesShouldReturnZeroForLocalId() throws Exception {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", RuntimeEnvironment.application.getPackageName())).isEqualTo(0);
  }

  @Test
  public void testGetXml() throws Exception {
    XmlResourceParser parser = resources.getXml(R.xml.preferences);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("PreferenceScreen");

    parser = resources.getXml(R.layout.custom_layout);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("org.robolectric.android.CustomView");

    parser = resources.getXml(R.menu.test);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("menu");

    parser = resources.getXml(R.drawable.rainbow);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("layer-list");

    parser = resources.getXml(R.anim.test_anim_1);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("set");

    parser = resources.getXml(R.color.color_state_list);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("selector");
  }

  @Test(expected = Resources.NotFoundException.class)
  public void testGetXml_nonexistentResource() {
    resources.getXml(0);
  }

  @Test(expected = Resources.NotFoundException.class)
  public void testGetXml_nonxmlfile() {
    resources.getXml(R.drawable.an_image);
  }

  @Test
  public void openRawResource_shouldLoadRawResources() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.raw_resource);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("raw txt file contents");
  }

  @Test
  public void openRawResource_shouldLoadRawResourcesFromLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("from lib3");
  }

  @Test
  public void openRawResource_shouldLoadRawResourcesFromSecondaryLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource_from_2);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("I'm only defined in lib2");
  }

  @Test
  public void openRawResource_shouldLoadRawResourcesFromTertiaryLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource_from_3);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("I'm only defined in lib3");
  }

  @Test
  public void openRawResource_shouldLoadDrawables() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.drawable.text_file_posing_as_image);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("drawable.png image\n");
  }

  @Test @Config(qualifiers = "hdpi")
  public void openRawResource_shouldLoadDrawableWithQualifiers() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.drawable.text_file_posing_as_image);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("drawable-hdpi.png image\n");
  }

  @Test
  public void openRawResource_withNonFile_throwsNotFoundException() throws Exception {
    try {
      resources.openRawResource(R.string.hello);
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }

    try {
      resources.openRawResource(R.string.hello, new TypedValue());
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }

    try {
      resources.openRawResource(-1234, new TypedValue());
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }
  }

  @Test
  public void openRawResourceFd_returnsNull_todo_FIX() throws Exception {
    assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNull();
  }

  @Test
  public void openRawResourceFd_withNonFile_throwsNotFoundException() throws Exception {
    try {
      resources.openRawResourceFd(R.string.hello);
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }

    try {
      resources.openRawResourceFd(-1234);
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }
  }

  @Test
  public void getXml_withNonFile_throwsNotFoundException() throws Exception {
    try {
      resources.getXml(R.string.hello);
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
    }

    try {
      resources.getXml(-1234);
      fail("should throw");
    } catch (Resources.NotFoundException e) {
      // cool
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

  @Test
  public void obtainAttributes_shouldUseReferencedIdFromAttributeSet() throws Exception {
    // android:id/mask was introduced in API 21, but it's still possible for apps built against API 21 to refer to it
    // in older runtimes because referenced resource ids are compiled (by aapt) into the binary XML format.
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@android:id/mask").build();
    TypedArray typedArray = resources.obtainAttributes(attributeSet, new int[]{android.R.attr.id});
    assertThat(typedArray.getResourceId(0, -9)).isEqualTo(android.R.id.mask);
  }

  @Test
  public void obtainStyledAttributesShouldDereferenceValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);
    TypedArray arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
    TypedValue value = new TypedValue();
    arr.getValue(0, value);
    arr.recycle();

    assertThat(value.type).isGreaterThanOrEqualTo(TypedValue.TYPE_FIRST_COLOR_INT).isLessThanOrEqualTo(TypedValue.TYPE_LAST_INT);
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

  @Test
  public void obtainStyledAttributes_shouldCheckXmlFirst_fromXmlLoadedFromResources() throws Exception {

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
    // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
    // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.

    XmlResourceParser xml = RuntimeEnvironment.application.getResources().getXml(R.drawable.vector);
    xml.next();
    xml.next();
    AttributeSet attributeSet = Xml.asAttributeSet(xml);

    TypedArray typedArray = RuntimeEnvironment.application.getTheme().obtainStyledAttributes(attributeSet, new int[] {
        android.R.attr.viewportWidth,
        android.R.attr.viewportHeight
    }, 0, 0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
    typedArray.recycle();
  }

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
  @Config(minSdk = LOLLIPOP)
  public void whenAttrIsDefinedInRuntimeSdk_getResourceName_findsResource() {
    assertThat(RuntimeEnvironment.application.getResources().getResourceName(android.R.attr.viewportHeight))
        .isEqualTo("android:attr/viewportHeight");
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
  public void subClassInitializedOK() {
    SubClassResources subClassResources = new SubClassResources(RuntimeEnvironment.application.getResources());
    assertThat(subClassResources.openRawResource(R.raw.raw_resource)).isNotNull();
  }

  @Test
  public void applyStyleForced() {
    final Resources.Theme theme = resources.newTheme();

    theme.applyStyle(R.style.MyBlackTheme, true);
    TypedArray arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground, android.R.attr.textColorHint});

    final TypedValue blackBackgroundColor = new TypedValue();
    arr.getValue(0, blackBackgroundColor);
    assertThat(blackBackgroundColor.resourceId).isEqualTo(android.R.color.black);
    arr.recycle();

    theme.applyStyle(R.style.MyBlueTheme, true);
    arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground, android.R.attr.textColor, android.R.attr.textColorHint});

    final TypedValue blueBackgroundColor = new TypedValue();
    arr.getValue(0, blueBackgroundColor);
    assertThat(blueBackgroundColor.resourceId).isEqualTo(R.color.blue);

    final TypedValue blueTextColor = new TypedValue();
    arr.getValue(1, blueTextColor);
    assertThat(blueTextColor.resourceId).isEqualTo(R.color.white);

    final TypedValue blueTextColorHint = new TypedValue();
    arr.getValue(2, blueTextColorHint);
    assertThat(blueTextColorHint.resourceId).isEqualTo(android.R.color.darker_gray);

    arr.recycle();
  }

  @Test
  public void applyStyleNotForced() {
    final Resources.Theme theme = resources.newTheme();

    // Apply black theme
    theme.applyStyle(R.style.MyBlackTheme, true);
    TypedArray arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground, android.R.attr.textColorHint});

    final TypedValue blackBackgroundColor = new TypedValue();
    arr.getValue(0, blackBackgroundColor);
    assertThat(blackBackgroundColor.resourceId).isEqualTo(android.R.color.black);

    final TypedValue blackTextColorHint = new TypedValue();
    arr.getValue(1, blackTextColorHint);
    assertThat(blackTextColorHint.resourceId).isEqualTo(android.R.color.darker_gray);

    arr.recycle();

    // Apply blue theme
    theme.applyStyle(R.style.MyBlueTheme, false);
    arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground, android.R.attr.textColor, android.R.attr.textColorHint});

    final TypedValue blueBackgroundColor = new TypedValue();
    arr.getValue(0, blueBackgroundColor);
    assertThat(blueBackgroundColor.resourceId).isEqualTo(android.R.color.black);

    final TypedValue blueTextColor = new TypedValue();
    arr.getValue(1, blueTextColor);
    assertThat(blueTextColor.resourceId).isEqualTo(R.color.white);

    final TypedValue blueTextColorHint = new TypedValue();
    arr.getValue(2, blueTextColorHint);
    assertThat(blueTextColorHint.resourceId).isEqualTo(android.R.color.darker_gray);

    arr.recycle();
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
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
    assertThat(outValue.data).isEqualTo(ResourceHelper.getColor("#0000ff"));
    assertThat(outValue.string).isNull();
    assertThat(outValue.assetCookie).isEqualTo(TypedValue.DATA_NULL_UNDEFINED);

    resources.getValue(R.integer.loneliest_number, outValue, true);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_HEX);
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
  public void getXml() throws Exception {
    XmlResourceParser xmlResourceParser = resources.getXml(R.xml.preferences);
    assertThat(xmlResourceParser).isNotNull();
    assertThat(xmlResourceParser.next()).isEqualTo(XmlResourceParser.START_DOCUMENT);
    assertThat(xmlResourceParser.next()).isEqualTo(XmlResourceParser.START_TAG);
    assertThat(xmlResourceParser.getName()).isEqualTo("PreferenceScreen");
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

  private static String findRootTag(XmlResourceParser parser) throws Exception {
    int event;
    do {
      event = parser.next();
    } while (event != XmlPullParser.START_TAG);
    return parser.getName();
  }

  private static class SubClassResources extends Resources {
    public SubClassResources(Resources res) {
      super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
    }
  }
}
