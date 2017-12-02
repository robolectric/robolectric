package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.R.color.test_ARGB8;
import static org.robolectric.R.color.test_RGB8;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
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

@RunWith(RobolectricTestRunner.class)
public class ShadowAssetManagerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AssetManager assetManager;
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    assetManager = Robolectric.buildActivity(Activity.class).create().get().getAssets();
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  public void assertGetAssetsNotNull() {
    assertNotNull(assetManager);

    assetManager = RuntimeEnvironment.application.getAssets();
    assertNotNull(assetManager);

    assetManager = resources.getAssets();
    assertNotNull(assetManager);
  }

  @Test
  public void assetsPathListing() throws IOException {
    List<String> files;
    String testPath;

    testPath = "";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("docs"));
    assertTrue(files.contains("assetsHome.txt"));

    testPath = "docs";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("extra"));

    testPath ="docs/extra";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("testing"));

    testPath = "docs/extra/testing";
    files = Arrays.asList(assetManager.list(testPath));
    assertTrue(files.contains("hello.txt"));

    testPath = "assetsHome.txt";
    files = Arrays.asList(assetManager.list(testPath));
    assertFalse(files.contains(testPath));

    testPath = "bogus.file";
    files = Arrays.asList(assetManager.list(testPath));
    assertEquals(0, files.size());
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
  public void openNonAssetShouldOpenRealAssetFromResources() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/an_image.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(6559);
  }

  @Test @Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenRealAssetFromAndroidJar() throws IOException {
    // Not the real full path (it's in .m2/repository), but it only cares about the last folder and file name
    String fileName = "jar:res/drawable-hdpi/bottom_bar.png";
    int expectedFileSize = 389;

    InputStream inputStream = assetManager.openNonAsset(0, fileName, 0);
    assertThat(((ByteArrayInputStream) inputStream).available()).isEqualTo(expectedFileSize);
  }

  @Test
  public void openNonAssetShouldThrowExceptionWhenFileDoesNotExist() throws IOException {
    expectedException.expect(IOException.class);
    expectedException.expectMessage(
        "Unable to find resource for ./res/drawable/does_not_exist.png");

    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void unknownResourceIdsShouldReportPackagesSearched() throws IOException {
    expectedException.expect(Resources.NotFoundException.class);
    expectedException.expectMessage(
        "Unable to find resource ID #0xffffffff in packages [android, org.robolectric]");

    resources.newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void forSystemResources_unknownResourceIdsShouldReportPackagesSearched()
      throws IOException {
    expectedException.expect(Resources.NotFoundException.class);
    expectedException.expectMessage("Unable to find resource ID #0xffffffff in packages [android]");

    Resources.getSystem().newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  @Config(qualifiers = "mdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierMdpi() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(8141);
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierHdpi() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);

    ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) inputStream;
    assertThat(byteArrayInputStream.available()).isEqualTo(23447);
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
  public void whenStyleAttrResolutionFails_attrsToTypedArray_returnsNiceErrorMessage()
      throws Exception {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage(
        "no value for org.robolectric:attr/styleNotSpecifiedInAnyTheme " +
            "in theme with applied styles: [Style org.robolectric:Theme_Robolectric (and parents)]");

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);

    shadowOf(assetManager)
        .attrsToTypedArray(
            resources,
            Robolectric.buildAttributeSet()
                .setStyleAttribute("?attr/styleNotSpecifiedInAnyTheme")
                .build(),
            new int[] {R.attr.string1},
            0,
            shadowOf(theme).getNativePtr(),
            0);
  }

  @Test
  public void getResourceIdentifier_shouldReturnValueFromRClass() throws Exception {
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("id_declared_in_item_tag", "id", "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("id/id_declared_in_item_tag", null, "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("org.robolectric:id_declared_in_item_tag", "id", null))
        .isEqualTo(R.id.id_declared_in_item_tag);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier(
                    "org.robolectric:id/id_declared_in_item_tag", "other", "other"))
        .isEqualTo(R.id.id_declared_in_item_tag);
  }

  @Test
  public void whenPackageIsUnknown_getResourceIdentifier_shouldReturnZero() throws Exception {
    assertThat(
            shadowOf(assetManager).getResourceIdentifier("whatever", "id", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("id/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("some.unknown.package:whatever", "id", null))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);

    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("whatever", "drawable", "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("drawable/whatever", null, "some.unknown.package"))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("some.unknown.package:whatever", "drawable", null))
        .isEqualTo(0);
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("some.unknown.package:id/whatever", "other", "other"))
        .isEqualTo(0);
  }

  @Test
  @Ignore(
      "currently ids are always automatically assigned a value; to fix this we'd need to check "
      + "layouts for +@id/___, which is expensive")
  public void whenCalledForIdWithNameNotInRClassOrXml_getResourceIdentifier_shouldReturnZero()
      throws Exception {
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier(
                    "org.robolectric:id/idThatDoesntExistAnywhere", "other", "other"))
        .isEqualTo(0);
  }

  @Test
  public void
      whenIdIsAbsentInXmlButPresentInRClass_getResourceIdentifier_shouldReturnIdFromRClass_probablyBecauseItWasDeclaredInALayout()
          throws Exception {
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("id_declared_in_layout", "id", "org.robolectric"))
        .isEqualTo(R.id.id_declared_in_layout);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturn0() throws Exception {
    assertThat(
            shadowOf(assetManager)
                .getResourceIdentifier("fictitiousDrawable", "drawable", "org.robolectric"))
        .isEqualTo(0);
  }

  @Test
  public void whenResourceIsAbsentInXml_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(
            shadowOf(assetManager).getResourceIdentifier("an_image", "drawable", "org.robolectric"))
        .isEqualTo(R.drawable.an_image);
  }

  @Test
  public void whenResourceIsXml_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(
            shadowOf(assetManager).getResourceIdentifier("preferences", "xml", "org.robolectric"))
        .isEqualTo(R.xml.preferences);
  }

  @Test
  public void whenResourceIsRaw_getResourceIdentifier_shouldReturnId() throws Exception {
    assertThat(
            shadowOf(assetManager).getResourceIdentifier("raw_resource", "raw", "org.robolectric"))
        .isEqualTo(R.raw.raw_resource);
  }

  @Test
  public void getResourceValue_colorARGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_ARGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
    assertThat(Color.blue(outValue.data)).isEqualTo(2);
  }

  @Test
  public void getResourceValue_colorRGB8() {
    TypedValue outValue = new TypedValue();
    resources.getValue(test_RGB8, outValue, false);
    assertThat(outValue.type).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB8);
    assertThat(Color.blue(outValue.data)).isEqualTo(4);
  }
}
