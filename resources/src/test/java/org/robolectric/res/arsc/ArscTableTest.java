package org.robolectric.res.arsc;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipInputStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.arsc.Chunk;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.robolectric.res.arsc.Chunk.TableChunk;
import org.robolectric.res.arsc.ArscTable;
import org.robolectric.resources.R;

/**
 * Created by jongerrish on 7/13/17.
 */
@RunWith(JUnit4.class)
public class ArscTableTest {

  private ByteBuffer order;
  private TableChunk chunk;
  private ArscTable arscTable;

  @Before
  public void setup() throws IOException {
    URL resource = getClass().getResource("/binaryresources/resources.ap_");
    ZipFile zipFile = new ZipFile(resource.getFile());

    ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
    InputStream inputStream = zipFile.getInputStream(arscEntry);

    byte[] buf = ByteStreams.toByteArray(inputStream);

    ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
    chunk = Chunk.newInstance(buffer);
    arscTable = new ArscTable(chunk);
  }

  @Test
  public void testSomething() throws Exception {
    chunk.dump();
  }

  @Test
  public void getPackageName() {
    assertThat(arscTable.getPackageName()).isEqualTo("org.robolectric.resources");
  }

  @Test @Ignore
  public void testGetString() throws Exception {
    assertThat(arscTable.getString(R.string.first_string)).isEqualTo("sheep");
  }

  @Test
  public void shouldResolveResIdToTypeAndKey() throws Exception {
    assertThat(arscTable.getTypeName(R.string.first_string)).isEqualTo("string");
    assertThat(arscTable.getKeyName(R.string.first_string)).isEqualTo("first_string");
  }

}
