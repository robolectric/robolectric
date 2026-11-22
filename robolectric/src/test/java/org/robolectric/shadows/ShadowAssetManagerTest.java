package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.ResourcesMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.android.ResourceTypes;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.Res_value;

@RunWith(AndroidJUnit4.class)
@ResourcesMode(Mode.BINARY)
public class ShadowAssetManagerTest {

  @Rule public final SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();
  @Rule public final TemporaryFolder tempFolder = new TemporaryFolder();

  private AssetManager assetManager;
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    resources = ApplicationProvider.getApplicationContext().getResources();
    assetManager = resources.getAssets();
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForDeflatedAsset() throws IOException {
    setSystemPropertyRule.set("robolectric.supportsCompressedAssetFileData", "true");
    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("deflatedAsset.xml");
    assertThat(assetFileDescriptor).isNotNull();
    assertThat(assetFileDescriptor.getLength()).isEqualTo(13);
    try (InputStream inputStream = assetFileDescriptor.createInputStream()) {
      byte[] buf = new byte[13];
      assertThat(inputStream.read(buf)).isEqualTo(13);
    }
  }

  @Test
  public void openFd_systemPropertyDisabled_shouldThrowException() throws IOException {
    setSystemPropertyRule.set("robolectric.supportsCompressedAssetFileData", "false");
    assertThrows(IOException.class, () -> assetManager.openFd("deflatedAsset.xml"));
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

  @Test
  public void getAssignedPackageIdentifiers_highPackageId_usesUnsignedKey() throws Exception {
    File featureApk = createApkWithHighPackageId(0x80, "com.example.feature", 42);
    AssetManager featureAssetManager = new AssetManager();
    int cookie = featureAssetManager.addAssetPath(featureApk.getAbsolutePath());
    assertThat(cookie).isGreaterThan(0);

    SparseArray<String> packages = featureAssetManager.getAssignedPackageIdentifiers();
    assertThat(packages.get(-128)).isNull();
    assertThat(packages.get(0x80)).isEqualTo("com.example.feature");

    Resources featureResources = new Resources(featureAssetManager, null, null);
    assertThat(featureResources.getInteger(0x80010000)).isEqualTo(42);
  }

  ///////////////////////////////

  private File createApkWithHighPackageId(int packageId, String packageName, int intValue)
      throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
    ResChunk_header.write(
        buffer,
        (short) ResourceTypes.RES_TABLE_TYPE,
        () -> buffer.putInt(1),
        () -> {
          writeEmptyStringPool(buffer);
          ResChunk_header.write(
              buffer,
              (short) ResourceTypes.RES_TABLE_PACKAGE_TYPE,
              () -> {
                buffer.putInt(packageId);
                for (int i = 0; i < 128; i++) {
                  buffer.putChar(i < packageName.length() ? packageName.charAt(i) : '\0');
                }
                buffer.putInt(288); // typeStrings
                buffer.putInt(0); // lastPublicType
                buffer.putInt(288 + 28); // keyStrings
                buffer.putInt(0); // lastPublicKey
                buffer.putInt(0); // typeIdOffset
              },
              () -> {
                writeEmptyStringPool(buffer);
                writeEmptyStringPool(buffer);
                ResChunk_header.write(
                    buffer,
                    (short) ResourceTypes.RES_TABLE_LIBRARY_TYPE,
                    () -> buffer.putInt(1),
                    () -> {
                      buffer.putInt(packageId);
                      for (int i = 0; i < 128; i++) {
                        buffer.putChar(i < packageName.length() ? packageName.charAt(i) : '\0');
                      }
                    });
                ResChunk_header.write(
                    buffer,
                    (short) ResourceTypes.RES_TABLE_TYPE_SPEC_TYPE,
                    () -> buffer.putInt(1).putInt(1),
                    () -> buffer.putInt(0));
                ResChunk_header.write(
                    buffer,
                    (short) ResourceTypes.RES_TABLE_TYPE_TYPE,
                    () -> {
                      buffer.putInt(1).putInt(1);
                      // 20 (ResTable_type header without config) + ResTable_config + 1 entry offset
                      buffer.putInt(20 + ResTable_config.SIZEOF + 4);
                      buffer
                          .putInt(ResTable_config.SIZEOF)
                          .put(new byte[ResTable_config.SIZEOF - 4]);
                    },
                    () -> {
                      buffer.putInt(0);
                      // ResTable_entry (size=8, flags=0, key=0)
                      buffer.putShort((short) 8).putShort((short) 0).putInt(0);
                      Res_value.write(buffer, Res_value.TYPE_INT_DEC, intValue);
                    });
              });
        });
    byte[] arsc = Arrays.copyOf(buffer.array(), buffer.position());

    File apk = tempFolder.newFile("feature.apk");
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apk))) {
      ZipEntry entry = new ZipEntry("resources.arsc");
      entry.setMethod(ZipEntry.STORED);
      entry.setSize(arsc.length);
      entry.setCompressedSize(arsc.length);
      CRC32 crc = new CRC32();
      crc.update(arsc);
      entry.setCrc(crc.getValue());
      zos.putNextEntry(entry);
      zos.write(arsc);
      zos.closeEntry();
    }
    return apk;
  }

  private static void writeEmptyStringPool(ByteBuffer buffer) {
    ResChunk_header.write(
        buffer,
        (short) ResourceTypes.RES_STRING_POOL_TYPE,
        () -> {
          buffer.putInt(0);
          buffer.putInt(0);
          buffer.putInt(0x100);
          buffer.putInt(0);
          buffer.putInt(0);
        },
        () -> {});
  }

  private static int countBytes(InputStream i) throws IOException {
    int count = 0;
    while (i.read() != -1) {
      count++;
    }
    i.close();
    return count;
  }
}
