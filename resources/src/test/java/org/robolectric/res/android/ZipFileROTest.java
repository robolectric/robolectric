package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.file.Files.newOutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test for {@link ZipFileRO}. */
@RunWith(JUnit4.class)
public final class ZipFileROTest {

  /** Verify that ZIP entries can be located when there are gaps between them. */
  //
  // The test data (zip_with_gap.zip) was crafted by creating a regular ZIP file with two
  // uncompressed entries, adding 32 spaces between them, and adjusting offsets accordingly
  // (in particular, the contents of the Central Directory and EOCD).
  //
  // 00000000: 504b 0304 0a00 0000 0000 0000 2100 a865  PK..........!..e
  // 00000010: 327e 0400 0000 0400 0000 0200 0000 6630  2~............f0
  // 00000020: 666f 6f0a 2020 2020 2020 2020 2020 2020  foo.
  // 00000030: 2020 2020 2020 2020 2020 2020 2020 2020
  // 00000040: 2020 2020 504b 0304 0a00 0000 0000 0000      PK..........
  // 00000050: 2100 e9b3 a204 0400 0000 0400 0000 0200  !...............
  // 00000060: 0000 6631 6261 720a 504b 0102 1e03 0a00  ..f1bar.PK......
  // 00000070: 0000 0000 0000 2100 a865 327e 0400 0000  ......!..e2~....
  // 00000080: 0400 0000 0200 0000 0000 0000 0000 0000  ................
  // 00000090: a081 0000 0000 6630 504b 0102 1e03 0a00  ......f0PK......
  // 000000a0: 0000 0000 0000 2100 e9b3 a204 0400 0000  ......!.........
  // 000000b0: 0400 0000 0200 0000 0000 0000 0000 0000  ................
  // 000000c0: a081 4400 0000 6631 504b 0506 0000 0000  ..D...f1PK......
  // 000000d0: 0200 0200 6000 0000 6800 0000 0000       ....`...h.....
  @Test
  public void createEntryFileMap_yieldsCorrectOffset() throws Exception {
    // Write the test data (provided as a JAR resource) to a regular file.
    // JAR resources are preferred as input since they don't depend on the working directory, but we
    // want a real file for testing.
    File blob = File.createTempFile("prefix", "zip");
    try (InputStream input = getClass().getResourceAsStream("/zip_with_gap.zip");
        FileOutputStream output = new FileOutputStream(blob)) {
      ByteStreams.copy(input, output);
    }

    ZipFileRO zipFile = ZipFileRO.open(blob.toString());
    ZipFileRO.ZipEntryRO entry = zipFile.findEntryByName("f1");
    FileMap fileMap = zipFile.createEntryFileMap(entry);

    // The contents of file "f1" (i.e. "bar") appears at offset 0x64.
    assertThat(fileMap.getDataOffset()).isEqualTo(0x64);
  }

  @Test
  public void open_emptyZip() throws Exception {
    // ensure ZipFileRO cam handle an empty zip file with no central directory
    File blob = File.createTempFile("prefix", "zip");
    try (ZipOutputStream zip = new ZipOutputStream(newOutputStream(blob.toPath()))) {}

    ZipFileRO zipFile = ZipFileRO.open(blob.toString());
    assertThat(zipFile).isNotNull();
  }

  @Test
  public void testCreateJar() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ZipOutputStream out = new ZipOutputStream(byteArrayOutputStream);
    // Write 2^16 + 1 entries, forcing zip64 EOCD to be written.
    for (int i = 0; i < 65537; i++) {
      out.putNextEntry(new ZipEntry(Integer.toString(i)));
      out.closeEntry();
    }
    out.close();
    byte[] zipBytes = byteArrayOutputStream.toByteArray();
    // Write 0xff for the following fields in the EOCD, which some zip libraries do.
    // Entries in this disk (2 bytes)
    // Total Entries (2 byte)
    // Size of Central Dir (4 bytes)
    // Offset to Central Dir (4 bytes)
    // Total: 12 bytes
    for (int i = 0; i < 12; i++) {
      zipBytes[zipBytes.length - 3 - i] = (byte) 0xff;
    }
    File tmpFile = File.createTempFile("zip64eocd", "zip");
    Files.write(zipBytes, tmpFile);
    ZipFileRO zro = ZipFileRO.open(tmpFile.getAbsolutePath());
    assertThat(zro).isNotNull();
    assertThat(zro.findEntryByName("0")).isNotNull();
    assertThat(zro.findEntryByName("65536")).isNotNull();
    assertThat(zro.findEntryByName("65537")).isNull();
  }
}
