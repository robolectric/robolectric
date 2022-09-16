package android.content.res;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.COMPLEX_UNIT_IN;
import static android.util.TypedValue.COMPLEX_UNIT_MM;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.util.TypedValue.TYPE_FIRST_COLOR_INT;
import static android.util.TypedValue.TYPE_INT_BOOLEAN;
import static android.util.TypedValue.TYPE_INT_COLOR_ARGB8;
import static android.util.TypedValue.TYPE_INT_COLOR_RGB8;
import static android.util.TypedValue.TYPE_INT_DEC;
import static android.util.TypedValue.TYPE_LAST_INT;
import static android.util.TypedValue.TYPE_REFERENCE;
import static android.util.TypedValue.TYPE_STRING;
import static android.util.TypedValue.applyDimension;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.robolectric.testapp.R.color.test_ARGB8;
import static org.robolectric.testapp.R.color.test_RGB8;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import com.google.common.collect.Range;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Compatibility test for {@link Resources}
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ResourcesTest {
  private Resources resources;
  private Context context;

  @Before
  public void setup() {
    context = ApplicationProvider.getApplicationContext();
    resources = context.getResources();
  }

  @Test
  public void getString() {
    assertThat(resources.getString(R.string.hello)).isEqualTo("Hello");
    assertThat(resources.getString(R.string.say_it_with_item)).isEqualTo("flowers");
  }

  @Test
  public void getString_withReference() {
    assertThat(resources.getString(R.string.greeting)).isEqualTo("Howdy");
  }

  @Test
  public void getString_withInterpolation() {
    assertThat(resources.getString(R.string.interpolate, "value")).isEqualTo("Here is a value!");
  }

  @Test
  public void getString_withHtml() {
    assertThat(resources.getString(R.string.some_html, "value")).isEqualTo("Hello, world");
  }

  @Test
  public void getString_withSurroundingQuotes() {
    assertThat(resources.getString(R.string.surrounding_quotes, "value")).isEqualTo("This'll work");
  }

  @Test
  public void getStringWithEscapedApostrophes() {
    assertThat(resources.getString(R.string.escaped_apostrophe)).isEqualTo("This'll also work");
  }

  @Test
  public void getStringWithEscapedQuotes() {
    assertThat(resources.getString(R.string.escaped_quotes)).isEqualTo("Click \"OK\"");
  }

  @Test
  public void getString_StringWithInlinedQuotesAreStripped() {
    assertThat(resources.getString(R.string.bad_example)).isEqualTo("This is a bad string.");
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
    assertThat(resources.getString(R.string.non_breaking_space)).isEqualTo("Closing"
                                                                               + " soon:\u00A05pm");
    assertThat(resources.getString(R.string.space)).isEqualTo("Closing soon: 5pm");
  }

  @Test
  public void getMultilineLayoutResource_shouldResolveLayoutReferencesWithLineBreaks() {
    // multiline_layout is a layout reference to activity_main layout.
    TypedValue multilineLayoutValue = new TypedValue();
    resources.getValue(R.layout.multiline_layout, multilineLayoutValue, true /* resolveRefs */);
    TypedValue mainActivityLayoutValue = new TypedValue();
    resources.getValue(R.layout.activity_main, mainActivityLayoutValue, false /* resolveRefs */);
    assertThat(multilineLayoutValue.string).isEqualTo(mainActivityLayoutValue.string);
  }

  @Test
  public void getText_withHtml() {
    assertThat(resources.getText(R.string.some_html, "value").toString()).isEqualTo("Hello, world");
    // TODO: Raw resources have lost the tags early, but the following call should return a
    // SpannedString
    // assertThat(resources.getText(R.string.some_html)).isInstanceOf(SpannedString.class);
  }

  @Test
  public void getText_plainString() {
    assertThat(resources.getText(R.string.hello, "value").toString()).isEqualTo("Hello");
    assertThat(resources.getText(R.string.hello)).isInstanceOf(String.class);
  }

  @Test
  public void getText_withLayoutId() {
    // This isn't _really_ supported by the platform (gives a lint warning that getText() expects a
    // String resource type
    // but the actual platform behaviour is to return a string that equals
    // "res/layout/layout_file.xml" so the current
    // Robolectric behaviour deviates from the platform as we append the full file path from the
    // current working directory.
    String textString = resources.getText(R.layout.different_screen_sizes, "value").toString();
    assertThat(textString).containsMatch("/different_screen_sizes.xml$");
    // If we run tests on devices with different config, the resource system will select different
    // layout directories.
    assertThat(textString).containsMatch("^res/layout");
  }

  @Test
  public void getStringArray() {
    assertThat(resources.getStringArray(R.array.items)).isEqualTo(new String[]{"foo", "bar"});
    assertThat(resources.getStringArray(R.array.greetings))
        .isEqualTo(new String[] {"hola", "Hello"});
  }

  @Test
  public void withIdReferenceEntry_obtainTypedArray() {
    TypedArray typedArray = resources.obtainTypedArray(R.array.typed_array_with_resource_id);
    assertThat(typedArray.length()).isEqualTo(2);

    assertThat(typedArray.getResourceId(0, 0)).isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(typedArray.getResourceId(1, 0)).isEqualTo(R.id.id_declared_in_layout);
  }

  @Test
  public void obtainTypedArray() {
    final TypedArray valuesTypedArray = resources.obtainTypedArray(R.array.typed_array_values);
    assertThat(valuesTypedArray.getString(0)).isEqualTo("abcdefg");
    assertThat(valuesTypedArray.getInt(1, 0)).isEqualTo(3875);
    assertThat(valuesTypedArray.getInteger(1, 0)).isEqualTo(3875);
    assertThat(valuesTypedArray.getFloat(2, 0.0f)).isEqualTo(2.0f);
    assertThat(valuesTypedArray.getColor(3, Color.BLACK)).isEqualTo(Color.MAGENTA);
    assertThat(valuesTypedArray.getColor(4, Color.BLACK)).isEqualTo(Color.parseColor("#00ffff"));
    assertThat(valuesTypedArray.getDimension(5, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_PX, 8, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(6, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_DIP, 12, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(7, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_DIP, 6, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(8, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_MM, 3, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(9, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_IN, 4, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(10, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_SP, 36, resources.getDisplayMetrics()));
    assertThat(valuesTypedArray.getDimension(11, 0.0f))
        .isEqualTo(applyDimension(COMPLEX_UNIT_PT, 18, resources.getDisplayMetrics()));

    final TypedArray refsTypedArray = resources.obtainTypedArray(R.array.typed_array_references);
    assertThat(refsTypedArray.getString(0)).isEqualTo("apple");
    assertThat(refsTypedArray.getString(1)).isEqualTo("banana");
    assertThat(refsTypedArray.getInt(2, 0)).isEqualTo(5);
    assertThat(refsTypedArray.getBoolean(3, false)).isTrue();

    assertThat(refsTypedArray.getResourceId(8, 0)).isEqualTo(R.array.string_array_values);
    assertThat(refsTypedArray.getTextArray(8))
        .asList()
        .containsAtLeast(
            "abcdefg",
            "3875",
            "2.0",
            "#ffff00ff",
            "#00ffff",
            "8px",
            "12dp",
            "6dip",
            "3mm",
            "4in",
            "36sp",
            "18pt");

    assertThat(refsTypedArray.getResourceId(9, 0)).isEqualTo(R.style.Theme_Robolectric);
  }

  @Test
  public void getInt() {
    assertThat(resources.getInteger(R.integer.meaning_of_life)).isEqualTo(42);
    assertThat(resources.getInteger(R.integer.test_integer1)).isEqualTo(2000);
    assertThat(resources.getInteger(R.integer.test_integer2)).isEqualTo(9);
    assertThat(resources.getInteger(R.integer.test_large_hex)).isEqualTo(-65536);
    assertThat(resources.getInteger(R.integer.test_value_with_zero)).isEqualTo(7210);
    assertThat(resources.getInteger(R.integer.meaning_of_life_as_item)).isEqualTo(42);
  }

  @Test
  public void getInt_withReference() {
    assertThat(resources.getInteger(R.integer.reference_to_meaning_of_life)).isEqualTo(42);
  }

  @Test
  public void getIntArray() {
    assertThat(resources.getIntArray(R.array.empty_int_array)).isEqualTo(new int[]{});
    assertThat(resources.getIntArray(R.array.zero_to_four_int_array)).isEqualTo(new int[]{0, 1, 2, 3, 4});
    assertThat(resources.getIntArray(R.array.with_references_int_array)).isEqualTo(new int[]{0, 2000, 1});
    assertThat(resources.getIntArray(R.array.referenced_colors_int_array)).isEqualTo(new int[]{0x1, 0xFFFFFFFF, 0xFF000000, 0xFFF5F5F5, 0x802C76AD});
  }

  @Test
  public void getBoolean() {
    assertThat(resources.getBoolean(R.bool.false_bool_value)).isEqualTo(false);
    assertThat(resources.getBoolean(R.bool.true_as_item)).isEqualTo(true);
  }

  @Test
  public void getBoolean_withReference() {
    assertThat(resources.getBoolean(R.bool.reference_to_true)).isEqualTo(true);
  }

  @Test
  public void getDimension() {
    assertThat(resources.getDimension(R.dimen.test_dip_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_DIP, 20, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_dp_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_in_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_IN, 99, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_mm_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_MM, 42, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_px_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_PX, 15, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_pt_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_PT, 12, resources.getDisplayMetrics()));
    assertThat(resources.getDimension(R.dimen.test_sp_dimen))
        .isEqualTo(applyDimension(COMPLEX_UNIT_SP, 5, resources.getDisplayMetrics()));
  }

  @Test
  public void getDimensionPixelSize() {
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dip_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_DIP, 20)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_dp_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_DIP, 8)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_in_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_IN, 99)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_mm_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_MM, 42)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_px_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_PX, 15)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_pt_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_PT, 12)));
    assertThat(resources.getDimensionPixelSize(R.dimen.test_sp_dimen))
        .isIn(onePixelOf(convertDimension(COMPLEX_UNIT_SP, 5)));
  }

  private static Range<Integer> onePixelOf(int i) {
    return Range.closed(i - 1, i + 1);
  }

  @Test
  public void getDimensionPixelOffset() {
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dip_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_DIP, 20));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_dp_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_DIP, 8));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_in_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_IN, 99));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_mm_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_MM, 42));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_px_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_PX, 15));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_pt_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_PT, 12));
    assertThat(resources.getDimensionPixelOffset(R.dimen.test_sp_dimen))
        .isEqualTo(convertDimension(COMPLEX_UNIT_SP, 5));
  }

  private int convertDimension(int unit, float value) {
    return (int) applyDimension(unit, value, resources.getDisplayMetrics());
  }

  @Test
  public void getDimension_withReference() {
    assertThat(resources.getBoolean(R.bool.reference_to_true)).isEqualTo(true);
  }

  @Test
  public void getStringArray_shouldThrowExceptionIfNotFound() {
    assertThrows(Resources.NotFoundException.class, () -> resources.getStringArray(-1));
  }

  @Test
  public void getIntegerArray_shouldThrowExceptionIfNotFound() {
    assertThrows(Resources.NotFoundException.class, () -> resources.getIntArray(-1));
  }

  @Test
  public void getQuantityString() {
    assertThat(resources.getQuantityString(R.plurals.beer, 1)).isEqualTo("a beer");
    assertThat(resources.getQuantityString(R.plurals.beer, 2)).isEqualTo("some beers");
    assertThat(resources.getQuantityString(R.plurals.beer, 3)).isEqualTo("some beers");
  }

  @Test
  public void getQuantityText() {
    // Feature not supported in legacy (raw) resource mode.
    assumeFalse(isRobolectricLegacyMode());

    assertThat(resources.getQuantityText(R.plurals.beer, 1)).isEqualTo("a beer");
    assertThat(resources.getQuantityText(R.plurals.beer, 2)).isEqualTo("some beers");
    assertThat(resources.getQuantityText(R.plurals.beer, 3)).isEqualTo("some beers");
  }

  @Test
  public void getFraction() {
    final int myself = 300;
    final int myParent = 600;
    assertThat(resources.getFraction(R.fraction.half, myself, myParent)).isEqualTo(150f);
    assertThat(resources.getFraction(R.fraction.half_of_parent, myself, myParent)).isEqualTo(300f);

    assertThat(resources.getFraction(R.fraction.quarter_as_item, myself, myParent)).isEqualTo(75f);
    assertThat(resources.getFraction(R.fraction.quarter_of_parent_as_item, myself, myParent)).isEqualTo(150f);

    assertThat(resources.getFraction(R.fraction.fifth_as_reference, myself, myParent)).isWithin(0.01f).of(60f);
    assertThat(resources.getFraction(R.fraction.fifth_of_parent_as_reference, myself, myParent)).isWithin(0.01f).of(120f);
  }

  @Test
  public void testConfiguration() {
    Configuration configuration = resources.getConfiguration();
    assertThat(configuration).isNotNull();
    assertThat(configuration.locale).isNotNull();
  }

  @Test
  public void testConfigurationReturnsTheSameInstance() {
    assertThat(resources.getConfiguration()).isSameInstanceAs(resources.getConfiguration());
  }

  @Test
  public void testNewTheme() {
    assertThat(resources.newTheme()).isNotNull();
  }

  @Test
  public void testGetDrawableNullRClass() {
    assertThrows(
        Resources.NotFoundException.class,
        () -> assertThat(resources.getDrawable(-12345)).isInstanceOf(BitmapDrawable.class));
  }

  @Test
  public void testGetAnimationDrawable() {
    assertThat(resources.getDrawable(R.anim.animation_list)).isInstanceOf(AnimationDrawable.class);
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

  @Test
  public void testGetColor_Missing() {
    assertThrows(Resources.NotFoundException.class, () -> resources.getColor(11234));
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

  @Test
  public void testGetBitmapDrawableForUnknownId() {
    assertThrows(
        Resources.NotFoundException.class,
        () ->
            assertThat(resources.getDrawable(Integer.MAX_VALUE))
                .isInstanceOf(BitmapDrawable.class));
  }

  @Test
  public void testGetNinePatchDrawableIntrinsicWidth() {
    float density = resources.getDisplayMetrics().density;
    NinePatchDrawable ninePatchDrawable =
        (NinePatchDrawable) resources.getDrawable(R.drawable.nine_patch_drawable);
    // Use Math.round to convert calculated float width to int,
    // see NinePatchDrawable#scaleFromDensity.
    assertThat(ninePatchDrawable.getIntrinsicWidth()).isEqualTo(Math.round(98.0f * density));
  }

  @Test
  public void testGetIdentifier() {

    final String resourceType = "string";
    final String packageName = context.getPackageName();

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


    int id = resources.getIdentifier("hello", "string", context.getPackageName());
    assertThat(id).isEqualTo(R.string.hello);

    String hello = resources.getString(id);
    assertThat(hello).isEqualTo("Hello");
  }

  @Test
  public void getIdentifier_nonExistantResource() {
    int id = resources.getIdentifier("just_alot_of_crap", "string", context.getPackageName());
    assertThat(id).isEqualTo(0);
  }

  /**
   * Public framework symbols are defined here:
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/public.xml
   * Private framework symbols are defined here:
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/symbols.xml
   *
   * <p>These generate android.R and com.android.internal.R respectively, when Framework Java code
   * does not need to reference a framework resource it will not have an R value generated.
   * Robolectric is then missing an identifier for this resource so we must generate a placeholder
   * ourselves.
   */
  @Test
  // @Config(sdk = Build.VERSION_CODES.LOLLIPOP) // android:color/secondary_text_material_dark was
  // added in API 21
  @SdkSuppress(minSdkVersion = LOLLIPOP)
  @Config(minSdk = LOLLIPOP)
  public void shouldGenerateIdsForResourcesThatAreMissingRValues() {
    int identifierMissingFromRFile =
        resources.getIdentifier("secondary_text_material_dark", "color", "android");

    // We expect Robolectric to generate a placeholder identifier where one was not generated in the
    // android R files.
    assertThat(identifierMissingFromRFile).isNotEqualTo(0);

    // We expect to be able to successfully android:color/secondary_text_material_dark to a
    // ColorStateList.
    assertThat(resources.getColorStateList(identifierMissingFromRFile)).isNotNull();
  }

  @Test
  public void getSystemShouldReturnSystemResources() {
    assertThat(Resources.getSystem()).isInstanceOf(Resources.class);
  }

  @Test
  public void multipleCallsToGetSystemShouldReturnSameInstance() {
    assertThat(Resources.getSystem()).isEqualTo(Resources.getSystem());
  }

  @Test
  public void applicationResourcesShouldHaveBothSystemAndLocalValues() {
    assertThat(context.getResources().getString(android.R.string.copy)).isEqualTo("Copy");
    assertThat(context.getResources().getString(R.string.copy)).isEqualTo("Local Copy");
  }

  @Test
  public void systemResourcesShouldReturnCorrectSystemId() {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", "android"))
        .isEqualTo(android.R.string.copy);
  }

  @Test
  public void systemResourcesShouldReturnZeroForLocalId() {
    assertThat(Resources.getSystem().getIdentifier("copy", "string", context.getPackageName()))
        .isEqualTo(0);
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

  @Test
  public void testGetXml_nonexistentResource() {
    assertThrows(Resources.NotFoundException.class, () -> resources.getXml(0));
  }

  @Test
  public void testGetXml_nonxmlfile() {
    assertThrows(Resources.NotFoundException.class, () -> resources.getXml(R.drawable.an_image));
  }

  @Test
  public void testGetXml_notNPEAfterClose() {
    XmlResourceParser parser = resources.getXml(R.xml.preferences);
    parser.close();
    // the following methods should not NPE if the XmlResourceParser has been closed.
    assertThat(parser.getName()).isNull();
    assertThat(parser.getNamespace()).isEmpty();
    assertThat(parser.getText()).isNull();
  }

  @Test
  public void openRawResource_shouldLoadRawResources() {
    InputStream resourceStream = resources.openRawResource(R.raw.raw_resource);
    assertThat(resourceStream).isNotNull();
    // assertThat(TestUtil.readString(resourceStream)).isEqualTo("raw txt file contents");
  }

  @Test
  public void openRawResource_shouldLoadDrawables() {
    InputStream resourceStream = resources.openRawResource(R.drawable.an_image);
    Bitmap bitmap = BitmapFactory.decodeStream(resourceStream);
    assertThat(bitmap.getHeight()).isEqualTo(53);
    assertThat(bitmap.getWidth()).isEqualTo(64);
  }

  @Test
  public void openRawResource_withNonFile_throwsNotFoundException() {
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
  @Ignore("todo: incorrect behavior on robolectric vs framework?")
  public void openRawResourceFd_returnsNull_todo_FIX() {
    assertThat(resources.openRawResourceFd(R.raw.raw_resource)).isNull();
  }

  @Test
  public void openRawResourceFd_withNonFile_throwsNotFoundException() {
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
  public void getXml_withNonFile_throwsNotFoundException() {
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
  public void themeResolveAttribute_shouldSupportNotDereferencingResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, false);
    assertThat(out.type).isEqualTo(TYPE_REFERENCE);
    assertThat(out.data).isEqualTo(android.R.color.black);
  }

  @Test
  public void obtainAttributes_shouldReturnValuesFromResources() throws Exception {
    XmlPullParser parser = resources.getXml(R.xml.xml_attrs);
    parser.next();
    parser.next();
    AttributeSet attributes = Xml.asAttributeSet(parser);

    TypedArray typedArray =
        resources.obtainAttributes(
            attributes, new int[] {android.R.attr.title, android.R.attr.scrollbarFadeDuration});

    assertThat(typedArray.getString(0)).isEqualTo("Android Title");
    assertThat(typedArray.getInt(1, 0)).isEqualTo(1111);
    typedArray.recycle();
  }

  // @Test
  // public void obtainAttributes_shouldUseReferencedIdFromAttributeSet() throws Exception {
  //   // android:id/mask was introduced in API 21, but it's still possible for apps built against API 21 to refer to it
  //   // in older runtimes because referenced resource ids are compiled (by aapt) into the binary XML format.
  //   AttributeSet attributeSet = Robolectric.buildAttributeSet()
  //       .addAttribute(android.R.attr.id, "@android:id/mask").build();
  //   TypedArray typedArray = resources.obtainAttributes(attributeSet, new int[]{android.R.attr.id});
  //   assertThat(typedArray.getResourceId(0, -9)).isEqualTo(android.R.id.mask);
  // }

  @Test
  public void obtainStyledAttributesShouldDereferenceValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);
    TypedArray arr = theme.obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
    TypedValue value = new TypedValue();
    arr.getValue(0, value);
    arr.recycle();

    assertThat(value.type).isAtLeast(TYPE_FIRST_COLOR_INT);
    assertThat(value.type).isAtMost(TYPE_LAST_INT);
  }

  // @Test
  // public void obtainStyledAttributes_shouldCheckXmlFirst_fromAttributeSetBuilder() throws Exception {
  //
  //   // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
  //   // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
  //   // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
  //   // resource ID values in the AttributeSet before checking the theme.
  //
  //   AttributeSet attributes = Robolectric.buildAttributeSet()
  //       .addAttribute(android.R.attr.viewportWidth, "12.0")
  //       .addAttribute(android.R.attr.viewportHeight, "24.0")
  //       .build();
  //
  //   TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributes, new int[] {
  //       android.R.attr.viewportWidth,
  //       android.R.attr.viewportHeight
  //   }, 0, 0);
  //   assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
  //   assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
  //   typedArray.recycle();
  // }

  @Test
  public void obtainStyledAttributes_shouldCheckXmlFirst_fromXmlLoadedFromResources() throws Exception {

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were introduced in API 21
    // but the public ID values they are assigned clash with private com.android.internal.R values on older SDKs. This
    // test ensures that even on older SDKs, on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.

    XmlResourceParser xml = context.getResources().getXml(R.drawable.vector);
    xml.next();
    xml.next();
    AttributeSet attributeSet = Xml.asAttributeSet(xml);

    TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, new int[] {
        android.R.attr.viewportWidth,
        android.R.attr.viewportHeight
    }, 0, 0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
    typedArray.recycle();
  }

  @Test
  @SdkSuppress(minSdkVersion = LOLLIPOP)
  @Config(minSdk = LOLLIPOP)
  public void whenAttrIsDefinedInRuntimeSdk_getResourceName_findsResource() {
    assertThat(context.getResources().getResourceName(android.R.attr.viewportHeight))
        .isEqualTo("android:attr/viewportHeight");
  }

  @Test
  @SdkSuppress(maxSdkVersion = KITKAT)
  @Config(maxSdk = KITKAT_WATCH)
  public void whenAttrIsNotDefinedInRuntimeSdk_getResourceName_doesntFindRequestedResourceButInsteadFindsInternalResourceWithSameId() {
    // asking for an attr defined after the current SDK doesn't have a defined result; in this case it returns
    //   numberPickerStyle from com.internal.android.R
    assertThat(context.getResources().getResourceName(android.R.attr.viewportHeight))
        .isNotEqualTo("android:attr/viewportHeight");

    assertThat(context.getResources().getIdentifier("viewportHeight", "attr", "android"))
        .isEqualTo(0);
  }

  @Test
  public void subClassInitializedOK() {
    SubClassResources subClassResources = new SubClassResources(resources);
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
  public void getValueShouldClearTypedArrayBetweenCalls() {
    TypedValue outValue = new TypedValue();

    resources.getValue(R.string.hello, outValue, true);
    assertThat(outValue.type).isEqualTo(TYPE_STRING);
    assertThat(outValue.string).isEqualTo(resources.getString(R.string.hello));
    // outValue.data is an index into the String block which we don't know for raw xml resources.
    assertThat(outValue.assetCookie).isNotEqualTo(0);

    resources.getValue(R.color.blue, outValue, true);
    assertThat(outValue.type).isEqualTo(TYPE_INT_COLOR_RGB8);
    assertThat(outValue.data).isEqualTo(0xFF0000FF);
    assertThat(outValue.string).isNull();
    // outValue.assetCookie is not supported with raw XML

    resources.getValue(R.integer.loneliest_number, outValue, true);
    assertThat(outValue.type).isEqualTo(TYPE_INT_DEC);
    assertThat(outValue.data).isEqualTo(1);
    assertThat(outValue.string).isNull();

    resources.getValue(R.bool.true_bool_value, outValue, true);
    assertThat(outValue.type).isEqualTo(TYPE_INT_BOOLEAN);
    assertThat(outValue.data).isNotEqualTo(0); // true == traditionally 0xffffffff, -1 in Java but
    // tests should be checking for non-zero
    assertThat(outValue.string).isNull();
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
  public void getXml_shouldParseEmojiCorrectly() throws IOException, XmlPullParserException {
    XmlResourceParser xmlResourceParser = resources.getXml(R.xml.has_emoji);
    xmlResourceParser.next();
    xmlResourceParser.next();
    assertThat(xmlResourceParser.getName()).isEqualTo("EmojiRoot");
    AttributeSet attributeSet = Xml.asAttributeSet(xmlResourceParser);
    assertThat(attributeSet.getAttributeValue(null, "label1")).isEqualTo("no emoji");
    String pureEmoji = "\uD83D\uDE00";
    assertThat(attributeSet.getAttributeValue(null, "label2")).isEqualTo(pureEmoji);
    assertThat(attributeSet.getAttributeValue(null, "label3")).isEqualTo(pureEmoji);
    String mixEmojiAndText = "\uD83D\uDE00internal1\uD83D\uDE00internal2\uD83D\uDE00";
    assertThat(attributeSet.getAttributeValue(null, "label4")).isEqualTo(mixEmojiAndText);
    assertThat(attributeSet.getAttributeValue(null, "label5")).isEqualTo(mixEmojiAndText);
    assertThat(attributeSet.getAttributeValue(null, "label6"))
        .isEqualTo("don't worry be \uD83D\uDE00");
  }

  @Test
  public void whenMissingXml_throwNotFoundException() {
    try {
      resources.getXml(0x3038);
      fail();
    } catch (Resources.NotFoundException e) {
      assertThat(e.getMessage()).contains("Resource ID #0x3038");
    }
  }

  @Test
  public void stringWithSpaces() {
    // this differs from actual Android behavior, which collapses whitespace as "Up to 25 USD"
    assertThat(resources.getString(R.string.string_with_spaces, "25", "USD"))
        .isEqualTo("Up to 25 USD");
  }

  @Test
  public void internalWhiteSpaceShouldBeCollapsed() {
    assertThat(resources.getString(R.string.internal_whitespace_blocks))
        .isEqualTo("Whitespace in" + " the middle");
    assertThat(resources.getString(R.string.internal_newlines)).isEqualTo("Some Newlines");
  }

  @Test
  public void fontTagWithAttributesShouldBeRead() {
    assertThat(resources.getString(R.string.font_tag_with_attribute))
        .isEqualTo("This string has a font tag");
  }

  @Test
  public void linkTagWithAttributesShouldBeRead() {
    assertThat(resources.getString(R.string.link_tag_with_attribute))
        .isEqualTo("This string has a link tag");
  }

  @Test
  public void getResourceTypeName_mipmap() {
    assertThat(resources.getResourceTypeName(R.mipmap.mipmap_reference)).isEqualTo("mipmap");
    assertThat(resources.getResourceTypeName(R.mipmap.robolectric)).isEqualTo("mipmap");
  }

  @Test
  public void getDrawable_mipmapReferencesResolve() {
    Drawable reference = resources.getDrawable(R.mipmap.mipmap_reference);
    Drawable original = resources.getDrawable(R.mipmap.robolectric);

    assertThat(reference.getMinimumHeight()).isEqualTo(original.getMinimumHeight());
    assertThat(reference.getMinimumWidth()).isEqualTo(original.getMinimumWidth());
  }

  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getDrawable_mipmapReferencesResolveXml() {
    Drawable reference = resources.getDrawable(R.mipmap.robolectric_xml);
    Drawable original = resources.getDrawable(R.mipmap.mipmap_reference_xml);

    assertThat(reference.getMinimumHeight()).isEqualTo(original.getMinimumHeight());
    assertThat(reference.getMinimumWidth()).isEqualTo(original.getMinimumWidth());
  }

  @Test
  public void forUntouchedThemes_copyTheme_shouldCopyNothing() {
    Resources.Theme theme1 = resources.newTheme();
    Resources.Theme theme2 = resources.newTheme();
    theme2.setTo(theme1);
  }

  @Test
  public void getResourceIdentifier_shouldReturnValueFromRClass() {
    assertThat(
        resources.getIdentifier("id_declared_in_item_tag", "id", context.getPackageName()))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
        resources.getIdentifier("id/id_declared_in_item_tag", null, context.getPackageName()))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
        resources.getIdentifier(context.getPackageName() + ":id_declared_in_item_tag", "id", null))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
            resources.getIdentifier(
                context.getPackageName() + ":id/id_declared_in_item_tag", "other", "other"))
        .isEqualTo(R.id.id_declared_in_item_tag);
  }

  @Test
  public void whenPackageIsUnknown_getResourceIdentifier_shouldReturnZero() {
    assertThat(
        resources.getIdentifier("whatever", "id", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("id/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("some.unknown.package:whatever", "id", null))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);

    assertThat(
        resources.getIdentifier("whatever", "drawable", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("drawable/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("some.unknown.package:whatever", "drawable", null))
        .isEqualTo(0);
    assertThat(
        resources.getIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);
  }

  @Test
  @Ignore(
      "currently ids are always automatically assigned a value; to fix this we'd need to check "
          + "layouts for +@id/___, which is expensive")
  public void whenCalledForIdWithNameNotInRClassOrXml_getResourceIdentifier_shouldReturnZero() {
    assertThat(
        resources.getIdentifier(
            "org.robolectric:id/idThatDoesntExistAnywhere", "other", "other"))
        .isEqualTo(0);
  }

  @Test
  public void
      whenIdIsAbsentInXmlButPresentInRClass_getResourceIdentifier_shouldReturnIdFromRClass_probablyBecauseItWasDeclaredInALayout() {
    assertThat(
        resources.getIdentifier("id_declared_in_layout", "id", context.getPackageName()))
        .isEqualTo(R.id.id_declared_in_layout);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturn0() {
    assertThat(
        resources.getIdentifier("fictitiousDrawable", "drawable", context.getPackageName()))
        .isEqualTo(0);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturnId() {
    assertThat(
        resources.getIdentifier("an_image", "drawable", context.getPackageName()))
        .isEqualTo(R.drawable.an_image);
  }

  @Test
  public void whenResourceIsXml_getResourceIdentifier_shouldReturnId() {
    assertThat(
        resources.getIdentifier("preferences", "xml", context.getPackageName()))
        .isEqualTo(R.xml.preferences);
  }

  @Test
  public void whenResourceIsRaw_getResourceIdentifier_shouldReturnId() {
    assertThat(
        resources.getIdentifier("raw_resource", "raw", context.getPackageName()))
        .isEqualTo(R.raw.raw_resource);
  }

  @Test
  public void getResourceValue_colorARGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_ARGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(TYPE_INT_COLOR_ARGB8);
    assertThat(Color.blue(outValue.data)).isEqualTo(2);
  }

  @Test
  public void getResourceValue_colorRGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_RGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(TYPE_INT_COLOR_RGB8);
    assertThat(Color.blue(outValue.data)).isEqualTo(4);
  }

  @Test
  public void getResourceEntryName_forStyle() {
    assertThat(resources.getResourceEntryName(android.R.style.TextAppearance_Small))
        .isEqualTo("TextAppearance.Small");
  }

  @Test
  @SdkSuppress(minSdkVersion = O)
  @Config(minSdk = O)
  public void getFont() {
    // Feature not supported in legacy (raw) resource mode.
    assumeFalse(isRobolectricLegacyMode());

    Typeface typeface = resources.getFont(R.font.vt323_regular);
    assertThat(typeface).isNotNull();
  }

  @Test
  @SdkSuppress(minSdkVersion = O)
  @Config(minSdk = O)
  public void getFontFamily() {
    // Feature not supported in legacy (raw) resource mode.
    assumeFalse(isRobolectricLegacyMode());

    Typeface typeface = resources.getFont(R.font.vt323);
    assertThat(typeface).isNotNull();
  }

  @Test
  @SdkSuppress(minSdkVersion = O)
  @Config(minSdk = O)
  public void getFontFamily_downloadable() {
    // Feature not supported in legacy (raw) resource mode.
    assumeFalse(isRobolectricLegacyMode());

    Typeface typeface = resources.getFont(R.font.downloadable);
    assertThat(typeface).isNotNull();
  }

  @Test
  @SdkSuppress(minSdkVersion = Q)
  @Config(minSdk = Q)
  public void testFontBuilder() throws Exception {
    // Used to throw `java.io.IOException: Failed to read font contents`
    new Font.Builder(context.getResources(), R.font.vt323_regular).build();
  }

  @Test
  @SdkSuppress(minSdkVersion = Q)
  @Config(minSdk = Q)
  public void fontFamily_getFont() throws Exception {
    Font platformFont = new Font.Builder(resources, R.font.vt323_regular).build();
    FontFamily fontFamily = new FontFamily.Builder(platformFont).build();
    assertThat(fontFamily.getFont(0)).isNotNull();
  }

  @Test
  @SdkSuppress(minSdkVersion = Q)
  @Config(minSdk = Q)
  public void getAttributeSetSourceResId() {
    XmlResourceParser xmlResourceParser = resources.getXml(R.xml.preferences);

    int sourceResId = Resources.getAttributeSetSourceResId(xmlResourceParser);

    assertThat(sourceResId).isEqualTo(R.xml.preferences);
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

  private static boolean isRobolectricLegacyMode() {
    try {
      Class<?> runtimeEnvironmentClass = Class.forName("org.robolectric.RuntimeEnvironment");
      Method useLegacyResourcesMethod =
          runtimeEnvironmentClass.getDeclaredMethod("useLegacyResources");
      return (boolean) useLegacyResourcesMethod.invoke(null);
    } catch (Exception e) {
      return false;
    }
  }
}
