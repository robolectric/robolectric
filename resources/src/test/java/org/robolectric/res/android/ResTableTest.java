package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable.Entry;
import org.robolectric.res.arsc.Chunk.TableChunk;
import org.robolectric.resources.R;

/**
 * Created by jongerrish on 7/13/17.
 */
@RunWith(JUnit4.class)
public class ResTableTest {

  private ByteBuffer order;
  private TableChunk chunk;
  private ResTable resTable;

  @Before
  public void setup() throws IOException {
    URL resource = getClass().getResource("/binaryresources/resources.ap_");
    ZipFile zipFile = new ZipFile(resource.getFile());

    ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
    InputStream inputStream = zipFile.getInputStream(arscEntry);
//
//    byte[] buf = ByteStreams.toByteArray(inputStream);
//
//    ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
//    chunk = Chunk.newInstance(buffer);
    resTable = new ResTable();
    resTable.add(inputStream);
  }

  @Test
  public void testGetEntry_intType() {
    Entry entry = resTable.getEntry(R.integer.flock_size, null);
    assertThat(entry.entry.value.dataType).isEqualTo(Type.INT_DEC.code());
    assertThat(entry.entry.value.data).isEqualTo(1234);
  }

  @Test
  public void testGetEntry_intType_large() {
    Entry entry = resTable.getEntry(R.integer.flock_size, "large");
    assertThat(entry.entry.value.dataType).isEqualTo(Type.INT_DEC.code());
    assertThat(entry.entry.value.data).isEqualTo(1000000);
  }

  @Test
  public void testGetEntry_stringType() throws Exception {
    assertThat(resTable.getEntry(R.string.first_string, null).entry.value.dataType).isEqualTo(Type.STRING.code());
  }

  @Test
  public void testGetEntry_boolType() throws Exception {
    assertThat(resTable.getEntry(R.bool.is_verizon, null).entry.value.dataType).isEqualTo(Type.INT_BOOLEAN.code());
    // Uncomment when we start selecting correct configuration
//    assertThat(resTable.getEntry(R.bool.is_verizon, 0).value.dataType).isEqualTo(0);
  }

//  @Test
//  public void getPackageName() {
//    assertThat(resTable.getPackageName(R.string.first_string)).isEqualTo("org.robolectric.resources");
//    assertThat(resTable.getPackageName(R.string.second_string)).isEqualTo("org.robolectric.resources");
//  }
//
//  @Test
//  public void shouldResolveResIdToType() throws Exception {
//    assertThat(resTable.getTypeName(R.string.first_string)).isEqualTo("string");
//
// assertThat(resTable.getTypeName(R.string.second_string)).isEqualTo("string");
//  }
}
