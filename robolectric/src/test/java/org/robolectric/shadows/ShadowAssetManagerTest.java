package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowAssetManagerTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  private AssetManager assetManager;
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    resources = ApplicationProvider.getApplicationContext().getResources();
    assetManager = resources.getAssets();
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForDeflatedAsset() throws Exception {
    expectedException.expect(FileNotFoundException.class);
    expectedException.expectMessage(
        "This file can not be opened as a file descriptor; it is probably compressed");

    assetManager.openFd("deflatedAsset.xml");
  }

  @Test
  public void openNonAssetShouldOpenRealAssetFromResources() throws IOException {
    InputStream inputStream = assetManager.openNonAsset(0, "res/drawable/an_image.png", 0);

    // expect different sizes in binary vs file resources
    int bytes = countBytes(inputStream);
    if (bytes != 6559 && bytes != 5138) {
      fail("Expected 5138 or 6559 bytes for image but got " + bytes);
    }
  }

  @Test
  public void openNonAssetShouldOpenFileFromAndroidJar() throws IOException {
    String fileName = "res/raw/fallbackring.ogg";
    InputStream inputStream = assetManager.openNonAsset(0, fileName, 0);
    assertThat(countBytes(inputStream)).isEqualTo(14611);
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
