package org.robolectric.res;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.arsc.Chunk;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by jongerrish on 7/13/17.
 */
@RunWith(JUnit4.class)
public class ArscTableTest {

  @Test
  public void testSomething() throws Exception {
    ZipFile zipFile = new ZipFile("/tmp/resources.ap_");

    ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
    InputStream inputStream = zipFile.getInputStream(arscEntry);

    byte[] buf = ByteStreams.toByteArray(inputStream);


    ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);

    Chunk.TableChunk chunk = Chunk.newInstance(buffer);
    chunk.dump();
  }


}
