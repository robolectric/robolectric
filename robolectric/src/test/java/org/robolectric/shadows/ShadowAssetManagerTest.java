package org.robolectric.shadows;

import static android.R.string.ok;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.R.bool.true_as_item;
import static org.robolectric.R.color.test_ARGB8;
import static org.robolectric.R.color.test_RGB8;
import static org.robolectric.R.dimen.test_px_dimen;
import static org.robolectric.R.fraction.half;
import static org.robolectric.R.integer.hex_int;
import static org.robolectric.R.integer.test_integer1;
import static org.robolectric.R.string.hello;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowArscAssetManager.isLegacyAssetManager;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.google.common.io.CharStreams;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowAssetManagerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AssetManager assetManager;
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
    assetManager = resources.getAssets();
  }

  @Test
  public void assertGetAssetsNotNull() {
    AssetManager.getSystem();
    assertNotNull(assetManager);

    assetManager = RuntimeEnvironment.application.getAssets();
    assertNotNull(assetManager);

    assetManager = resources.getAssets();
    assertNotNull(assetManager);
  }

  @Test
  public void assetsPathListing() throws IOException {
    assertThat(assetManager.list(""))
        .contains(
            "assetsHome.txt",
            "docs",
            "myFont.ttf");

    assertThat(assetManager.list("docs"))
        .contains("extra");

    assertThat(assetManager.list("docs/extra"))
        .contains("testing");

    assertThat(assetManager.list("docs/extra/testing"))
        .contains("hello.txt");

    assertThat(assetManager.list("bogus-dir")).isEmpty();
  }

  @Test
  public void open_shouldOpenFile() throws IOException {
    final String contents =
        CharStreams.toString(new InputStreamReader(assetManager.open("assetsHome.txt"), UTF_8));
    assertThat(contents).isEqualTo("assetsHome!");
  }

  @Test
  public void open_withAccessMode_shouldOpenFile() throws IOException {
    final String contents = CharStreams.toString(
        new InputStreamReader(assetManager.open("assetsHome.txt", AssetManager.ACCESS_BUFFER), UTF_8));
    assertThat(contents).isEqualTo("assetsHome!");
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForAsset() throws Exception {
    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("assetsHome.txt");
    assertThat(CharStreams.toString(new InputStreamReader(assetFileDescriptor.createInputStream(), UTF_8)))
        .isEqualTo("assetsHome!");
    assertThat(assetFileDescriptor.getLength()).isEqualTo(11);
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForDeflatedAsset() throws Exception {
    assumeTrue(!isLegacyAssetManager());
    expectedException.expect(FileNotFoundException.class);
    expectedException.expectMessage("This file can not be opened as a file descriptor; it is probably compressed");

    assetManager.openFd("deflatedAsset.xml");
  }

  @Test
  public void openNonAssetShouldOpenRealAssetFromResources() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/an_image.png", 0);

    // expect different sizes in binary vs file resources
    int expectedFileSize = isLegacyAssetManager() ? 6559 : 5138;
    assertThat(countBytes(inputStream)).isEqualTo(expectedFileSize);
  }

  @Test@Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenRealAssetFromAndroidJar() throws IOException {
    String fileName = "res/drawable-hdpi-v4/bottom_bar.png";
    int expectedFileSize = 231;
    if (isLegacyAssetManager()) {
      // Not the real full path (it's in .m2/repository), but it only cares about the last folder and file name;
      // retrieves the uncompressed, un-version-qualified file from raw-res/...
      fileName = "jar:res/drawable-hdpi/bottom_bar.png";
      expectedFileSize = 389;
    }

    InputStream inputStream = assetManager.openNonAsset(0, fileName, 0);
    assertThat(countBytes(inputStream)).isEqualTo(expectedFileSize);
  }

  @Test
  public void openNonAssetShouldThrowExceptionWhenFileDoesNotExist() throws IOException {
    assumeTrue(isLegacyAssetManager());

    expectedException.expect(FileNotFoundException.class);
    expectedException.expectMessage(
        "./res/drawable/does_not_exist.png");

    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void unknownResourceIdsShouldReportPackagesSearched() throws IOException {
    assumeTrue(isLegacyAssetManager());

    expectedException.expect(Resources.NotFoundException.class);
    expectedException.expectMessage(
        "Unable to find resource ID #0xffffffff in packages [android, org.robolectric]");

    resources.newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void forSystemResources_unknownResourceIdsShouldReportPackagesSearched()
      throws IOException {
    if (!isLegacyAssetManager()) return;
    expectedException.expect(Resources.NotFoundException.class);
    expectedException.expectMessage("Unable to find resource ID #0xffffffff in packages [android]");

    Resources.getSystem().newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  @Config(qualifiers = "mdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierMdpi() throws IOException {
    if (!isLegacyAssetManager()) return;

    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);
    assertThat(countBytes(inputStream)).isEqualTo(8141);
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierHdpi() throws IOException {
    if (!isLegacyAssetManager()) return;

    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);
    assertThat(countBytes(inputStream)).isEqualTo(23447);
  }

  @Test
  public void multiFormatAttributes_integerDecimalValue() {
    AttributeSet attributeSet =
        Robolectric.buildAttributeSet().addAttribute(R.attr.multiformat, "16").build();
    TypedArray typedArray =
        resources.obtainAttributes(attributeSet, new int[] {R.attr.multiformat});
    TypedValue outValue = new TypedValue();
    typedArray.getValue(0, outValue);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_DEC);
  }

  @Test
  public void multiFormatAttributes_integerHexValue() {
    AttributeSet attributeSet =
        Robolectric.buildAttributeSet().addAttribute(R.attr.multiformat, "0x10").build();
    TypedArray typedArray =
        resources.obtainAttributes(attributeSet, new int[] {R.attr.multiformat});
    TypedValue outValue = new TypedValue();
    typedArray.getValue(0, outValue);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_HEX);
  }

  @Test
  public void multiFormatAttributes_stringValue() {
    AttributeSet attributeSet =
        Robolectric.buildAttributeSet().addAttribute(R.attr.multiformat, "Hello World").build();
    TypedArray typedArray =
        resources.obtainAttributes(attributeSet, new int[] {R.attr.multiformat});
    TypedValue outValue = new TypedValue();
    typedArray.getValue(0, outValue);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_STRING);
  }

  @Test
  public void multiFormatAttributes_booleanValue() {
    AttributeSet attributeSet =
        Robolectric.buildAttributeSet().addAttribute(R.attr.multiformat, "true").build();
    TypedArray typedArray =
        resources.obtainAttributes(attributeSet, new int[] {R.attr.multiformat});
    TypedValue outValue = new TypedValue();
    typedArray.getValue(0, outValue);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_BOOLEAN);
  }

  @Test
  public void attrsToTypedArray_shouldAllowMockedAttributeSets() throws Exception {
    if (!isLegacyAssetManager()) return;

    AttributeSet mockAttributeSet = mock(AttributeSet.class);
    when(mockAttributeSet.getAttributeCount()).thenReturn(1);
    when(mockAttributeSet.getAttributeNameResource(0)).thenReturn(android.R.attr.windowBackground);
    when(mockAttributeSet.getAttributeName(0)).thenReturn("android:windowBackground");
    when(mockAttributeSet.getAttributeValue(0)).thenReturn("value");

    resources.obtainAttributes(mockAttributeSet, new int[]{android.R.attr.windowBackground});
  }

  @Test
  public void forUntouchedThemes_copyTheme_shouldCopyNothing() throws Exception {
    Resources.Theme theme1 = resources.newTheme();
    Resources.Theme theme2 = resources.newTheme();
    theme2.setTo(theme1);
  }

 @Test
 public void whenStyleAttrResolutionFails_attrsToTypedArray_returnsNiceErrorMessage() throws Exception {
   if (!isLegacyAssetManager()) return;

   expectedException.expect(RuntimeException.class);
   expectedException.expectMessage(
       "no value for org.robolectric:attr/styleNotSpecifiedInAnyTheme " +
           "in theme with applied styles: [Style org.robolectric:Theme_Robolectric (and parents)]");

   Resources.Theme theme = resources.newTheme();
   theme.applyStyle(R.style.Theme_Robolectric, false);

   ShadowAssetManager shadowAssetManager = Shadow.extract(assetManager);
   shadowAssetManager.attrsToTypedArray(resources,
       Robolectric.buildAttributeSet().setStyleAttribute("?attr/styleNotSpecifiedInAnyTheme").build(),
       new int[]{R.attr.string1}, 0, shadowOf(theme).getNativePtr(), 0);
 }

  @Test
  public void getResourceIdentifier_shouldReturnValueFromRClass() throws Exception {
    assertThat(resources.getIdentifier("id_declared_in_item_tag", "id", "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(resources.getIdentifier("id/id_declared_in_item_tag", null, "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(resources.getIdentifier("org.robolectric:id_declared_in_item_tag", "id", null))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(resources.getIdentifier("org.robolectric:id/id_declared_in_item_tag", "other", "other"))
        .isEqualTo(R.id.id_declared_in_item_tag);
  }

  @Test
  public void testGetResourceNames() throws Exception {
    assertThat(resources.getResourceEntryName(R.layout.activity_main)).isEqualTo("activity_main");
    assertThat(resources.getResourceTypeName(R.layout.activity_main)).isEqualTo("layout");
    assertThat(resources.getResourcePackageName(R.layout.activity_main)).isEqualTo("org.robolectric");
    assertThat(resources.getResourceName(R.layout.activity_main)).isEqualTo("org.robolectric:layout/activity_main");
  }

  @Test
  public void whenPackageIsUnknown_getResourceIdentifier_shouldReturnZero() throws Exception {
    assertThat(resources.getIdentifier("whatever", "id", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("id/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("some.unknown.package:whatever", "id", null))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);

    assertThat(resources.getIdentifier("whatever", "drawable", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("drawable/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("some.unknown.package:whatever", "drawable", null))
        .isEqualTo(0);
    assertThat(resources.getIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);
  }

  @Test @Ignore("currently ids are always automatically assigned a value; to fix this we'd need to check "
      + "layouts for +@id/___, which is expensive")
  public void whenCalledForIdWithNameNotInRClassOrXml_getResourceIdentifier_shouldReturnZero() throws Exception {
    assertThat(resources.getIdentifier("org.robolectric:id/idThatDoesntExistAnywhere", "other", "other"))
        .isEqualTo(0);
  }

  @Test
  public void whenIdIsAbsentInXmlButPresentInRClass_getResourceIdentifier_shouldReturnIdFromRClass_probablyBecauseItWasDeclaredInALayout() throws Exception {
    assertThat(resources.getIdentifier("id_declared_in_layout", "id", "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_layout);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturn0() throws Exception {
    assertThat(resources.getIdentifier("fictitiousDrawable", "drawable", "org.robolectric"))
        .isEqualTo(0);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(resources.getIdentifier("an_image", "drawable", "org.robolectric"))
        .isEqualTo(R.drawable.an_image);
  }

  @Test
  public void whenResourceIsXml_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(resources.getIdentifier("preferences", "xml", "org.robolectric"))
        .isEqualTo(R.xml.preferences);
  }

  @Test
  public void whenResourceIsRaw_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(resources.getIdentifier("raw_resource", "raw", "org.robolectric"))
        .isEqualTo(R.raw.raw_resource);
  }

  @Test
  public void getResourceValue_boolean() {
    TypedValue outValue = new TypedValue();
    resources.getValue(R.bool.false_bool_value, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_BOOLEAN.code());
    assertThat(outValue.data).isEqualTo(0);

    outValue = new TypedValue();
    resources.getValue(true_as_item, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_BOOLEAN.code());
    assertThat(outValue.data).isNotEqualTo(0);
  }

  @Test
  public void getResourceValue_int() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_integer1, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_DEC.code());
    assertThat(outValue.data).isEqualTo(2000);
  }

  @Test
  public void getResourceValue_intHex() {
    TypedValue outValue = new TypedValue();
    resources.getValue(hex_int, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_HEX.code());
    assertThat(outValue.data).isEqualTo(0xFFFF0000);
  }

  @Test
  public void getResourceValue_fraction() {
    TypedValue outValue = new TypedValue();
    resources.getValue(half, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.FRACTION.code());
    assertThat(outValue.getFraction(1, 1)).isEqualTo(0.5f);
  }

  @Test
  public void getResourceValue_dimension() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_px_dimen, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.DIMENSION.code());
    assertThat(outValue.getDimension(new DisplayMetrics())).isEqualTo(15);
  }

  @Test
  public void getResourceValue_colorARGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_ARGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_COLOR_ARGB8.code());
    assertThat(Color.blue(outValue.data)).isEqualTo(2);
  }

  @Test
  public void getResourceValue_colorRGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_RGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.INT_COLOR_RGB8.code());
    assertThat(Color.blue(outValue.data)).isEqualTo(4);
  }

  @Test
  public void getResourceValue_string() {
    TypedValue outValue = new TypedValue();
    resources.getValue(hello, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.STRING.code());
    assertThat(outValue.string).isEqualTo("Hello");
  }

  @Test
  public void getResourceValue_frameworkString() {
    TypedValue outValue = new TypedValue();
    resources.getValue(ok, outValue, false);
    assertThat(outValue.type).isEqualTo(DataType.STRING.code());
    assertThat(outValue.string).isEqualTo("OK");
  }

  @Test
  public void getResourceValue_fromSystem() {
    assumeTrue(!isLegacyAssetManager());
    TypedValue outValue = new TypedValue();
    ShadowArscAssetManager systemShadowAssetManager = shadowOf(AssetManager.getSystem());
    assertThat(systemShadowAssetManager.getResourceValue(android.R.string.ok, 0, outValue, false)).isTrue();
    assertThat(outValue.type).isEqualTo(DataType.STRING.code());
    assertThat(outValue.string).isEqualTo("OK");
  }

  @Test
  public void configuration_default() {
    assumeTrue(!isLegacyAssetManager());
    ShadowArscAssetManager shadowAssetManager = shadowOf(assetManager);
    ResTable_config config = shadowAssetManager.getConfiguration();
    if (VERSION.SDK_INT > 16) {
      assertThat(config.density).isEqualTo(0);
    } else {
      assertThat(config.density).isEqualTo(160);
    }
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void configuration_density() {
    assumeTrue(!isLegacyAssetManager());
    ShadowArscAssetManager shadowAssetManager = shadowOf(assetManager);
    ResTable_config config = shadowAssetManager.getConfiguration();
    assertThat(config.density).isEqualTo(240);
  }

  ///////////////////////////////

  private static int countBytes(InputStream i) throws IOException {
    int count = 0;
    while (i.read() != -1) {
      count++;
    }
    i.close();
    return count;
  }

}
