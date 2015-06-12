package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TestUtil;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP })
public class ShadowResourcesTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = new Activity().getResources();
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
  public void getText_withHtml() throws Exception {
    assertThat(resources.getText(R.string.some_html, "value")).isEqualTo("Hello, world");
  }

  @Test
  public void getText_withLayoutId() throws Exception {
    assertThat(resources.getText(R.layout.different_screen_sizes, "value")).isEqualTo("." + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "res" + File.separator + "layout" + File.separator + "different_screen_sizes.xml");
  }

  @Test
  public void getStringArray() throws Exception {
    assertThat(resources.getStringArray(R.array.items)).isEqualTo(new String[]{"foo", "bar"});
    assertThat(resources.getStringArray(R.array.greetings)).isEqualTo(new String[]{"hola", "Hello"});
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
    assertThat(resources.getBoolean(R.bool.integers_are_true)).isEqualTo(true);
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
    assertThat(ShadowApplication.getInstance().getResources().getDrawable(R.drawable.nine_patch_drawable)).isInstanceOf(NinePatchDrawable.class);
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
  public void testDensity() {
    Activity activity = new Activity();
    assertThat(activity.getResources().getDisplayMetrics().density).isEqualTo(1f);

    shadowOf(activity.getResources()).setDensity(1.5f);
    assertThat(activity.getResources().getDisplayMetrics().density).isEqualTo(1.5f);

    Activity anotherActivity = new Activity();
    assertThat(anotherActivity.getResources().getDisplayMetrics().density).isEqualTo(1.5f);
  }

  @Test
  public void displayMetricsShouldNotHaveLotsOfZeros() throws Exception {
    Activity activity = new Activity();
    assertThat(activity.getResources().getDisplayMetrics().heightPixels).isEqualTo(800);
    assertThat(activity.getResources().getDisplayMetrics().widthPixels).isEqualTo(480);
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
    Activity activity = new Activity();
    assertThat(activity.getResources().getString(android.R.string.copy)).isEqualTo("Copy");
    assertThat(activity.getResources().getString(R.string.copy)).isEqualTo("Local Copy");
  }

  @Test
  public void systemResourcesShouldReturnCorrectSystemId() throws Exception {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", "android")).isEqualTo(android.R.string.copy);
  }

  @Test
  public void systemResourcesShouldReturnZeroForLocalId() throws Exception {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", TestUtil.TEST_PACKAGE)).isEqualTo(0);
  }

  @Test
  public void testGetXml() throws Exception {
    XmlResourceParser parser = resources.getXml(R.xml.preferences);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("PreferenceScreen");

    parser = resources.getXml(R.layout.custom_layout);
    assertThat(parser).isNotNull();
    assertThat(findRootTag(parser)).isEqualTo("org.robolectric.util.CustomView");

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
  public void shouldLoadRawResources() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.raw_resource);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("raw txt file contents");
  }

  @Test
  public void shouldLoadRawResourcesFromLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("from lib3");
  }

  @Test
  public void shouldLoadRawResourcesFromSecondaryLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource_from_2);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("I'm only defined in lib2");
  }

  @Test
  public void shouldLoadRawResourcesFromTertiaryLibraries() throws Exception {
    InputStream resourceStream = resources.openRawResource(R.raw.lib_raw_resource_from_3);
    assertThat(resourceStream).isNotNull();
    assertThat(TestUtil.readString(resourceStream)).isEqualTo("I'm only defined in lib3");
  }

  @Test
  public void setScaledDensityShouldSetScaledDensityInDisplayMetrics() {
    final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

    assertThat(displayMetrics.scaledDensity).isEqualTo(1f);
    shadowOf(resources).setScaledDensity(2.5f);
    assertThat(displayMetrics.scaledDensity).isEqualTo(2.5f);
  }

  @Test
  public void getThemeValueShouldSupportDereferenceResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);
    long internalId = getInternalId(theme);

    ShadowAssetManager shadow = Shadows.shadowOf(resources.getAssets());
    shadow.getThemeValue(internalId, android.R.attr.windowBackground, out, true);
    assertThat(out.type).isNotEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.type).isGreaterThanOrEqualTo(TypedValue.TYPE_FIRST_COLOR_INT);
    assertThat(out.type).isLessThanOrEqualTo(TypedValue.TYPE_LAST_COLOR_INT);

    TypedValue expected = new TypedValue();
    shadow.getResourceValue(android.R.color.black, TypedValue.DENSITY_DEFAULT, expected, false);
    assertThat(out.type).isEqualTo(expected.type);
    assertThat(out.data).isEqualTo(expected.data);
  }

  @Test
  public void getThemeValueShouldSupportNotDereferencingResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);
    long internalId = getInternalId(theme);

    ShadowAssetManager shadow = Shadows.shadowOf(resources.getAssets());
    shadow.getThemeValue(internalId, android.R.attr.windowBackground, out, false);
    assertThat(out.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.resourceId).isEqualTo(android.R.color.black);
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
  public void subClassInitializedOK() {
    SubClassResources subClassResources = new SubClassResources(ShadowApplication.getInstance().getResources());
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

  private long getInternalId(Resources.Theme theme) {
    return ReflectionHelpers.getField(theme, "mTheme");
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
