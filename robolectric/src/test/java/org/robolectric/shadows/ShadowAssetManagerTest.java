package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
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
  public void openNonAssetShouldOpenRealAssetFromResources() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/an_image.png", 0);
    assertThat(inputStream.available()).isEqualTo(6559);
  }

  @Test @Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenRealAssetFromAndroidJar() throws IOException {
    // Not the real full path (it's in .m2/repository), but it only cares about the last folder and file name
    String fileName = "jar:res/drawable-hdpi/bottom_bar.png";
    int expectedFileSize = 389;

    InputStream inputStream = assetManager.openNonAsset(0, fileName, 0);
    assertThat(inputStream.available()).isEqualTo(expectedFileSize);
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
    expectedException.expectMessage("Resource ID #0xffffffff");

    resources.newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  public void forSystemResources_unknownResourceIdsShouldReportPackagesSearched()
      throws IOException {
    expectedException.expect(Resources.NotFoundException.class);
    expectedException.expectMessage("Resource ID #0xffffffff");

    Resources.getSystem().newTheme().applyStyle(-1, false);
    assetManager.openNonAsset(0, "./res/drawable/does_not_exist.png", 0);
  }

  @Test
  @Config(qualifiers = "mdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierMdpi() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);
    assertThat(inputStream.available()).isEqualTo(8141);
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void openNonAssetShouldOpenCorrectAssetBasedOnQualifierHdpi() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "./res/drawable/robolectric.png", 0);
    assertThat(inputStream.available()).isEqualTo(23447);
  }

  // todo: port to ResourcesTest
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

  // todo: port to ResourcesTest
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

  // todo: port to ResourcesTest
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

  // todo: port to ResourcesTest
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
}
