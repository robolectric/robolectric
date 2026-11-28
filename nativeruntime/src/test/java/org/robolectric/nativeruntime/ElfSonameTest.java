package org.robolectric.nativeruntime;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ElfSonameTest {
  private static final String SONAME = "libandroid_runtime.so";
  private static final String LOOKUP = "libandroid_runtime\0";

  @Rule public final TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void uniqueNames_keepTheLengthsOfTheAospNames() {
    assertThat(ElfSoname.uniquePrefix(37)).isEqualTo("librobolectric37");
    assertThat(ElfSoname.uniquePrefix(35)).hasLength(ElfSoname.LOOKUP_PREFIX.length());
    assertThat(ElfSoname.libraryName(ElfSoname.uniquePrefix(37))).isEqualTo("librobolectric37me.so");
    assertThat(ElfSoname.libraryName(ElfSoname.uniquePrefix(37))).hasLength(SONAME.length());
  }

  @Test
  public void rename_rewritesTheSonameAndHwuisSelfLookup() throws IOException {
    Path library = write(elf(SONAME, "libc.so.6", LOOKUP));

    assertThat(ElfSoname.rename(library, "librobolectric37")).isTrue();

    String bytes = new String(Files.readAllBytes(library), StandardCharsets.US_ASCII);
    assertThat(bytes).contains("\0librobolectric37me.so\0");
    assertThat(bytes).contains("rodata:librobolectric37me\0");
    assertThat(bytes).doesNotContain("libandroid_runt");
    assertThat(bytes).contains("libc.so.6");
  }

  @Test
  public void rename_leavesTheFileAloneWithoutExactlyOneLookupString() throws IOException {
    for (String lookups : new String[] {"", LOOKUP + LOOKUP}) {
      byte[] original = elf(SONAME, "libc.so.6", lookups);
      Path library = write(original);

      assertThat(ElfSoname.rename(library, "librobolectric37")).isFalse();

      assertThat(Files.readAllBytes(library)).isEqualTo(original);
    }
  }

  @Test
  public void rename_leavesNonElfFilesAlone() throws IOException {
    byte[] original = new byte[128];
    Path library = write(original);

    assertThat(ElfSoname.rename(library, "librobolectric37")).isFalse();

    assertThat(Files.readAllBytes(library)).isEqualTo(original);
  }

  private Path write(byte[] bytes) throws IOException {
    Path path = temp.newFile().toPath();
    Files.write(path, bytes);
    return path;
  }

  /**
   * A minimal ELF64 LE shared object: one PT_LOAD mapping the whole file at address 0, a
   * PT_DYNAMIC with DT_STRTAB, DT_NEEDED, DT_SONAME and DT_NULL, the string table, and {@code
   * rodata} standing in for the read-only data that holds hwui's lookup string.
   */
  private static byte[] elf(String soname, String needed, String rodata) {
    int phoff = 64;
    int dynamic = phoff + 2 * 56;
    int strtab = dynamic + 4 * 16;
    byte[] strings =
        ("\0" + needed + "\0" + soname + "\0" + "\0rodata:" + rodata).getBytes(StandardCharsets.US_ASCII);
    ByteBuffer elf = ByteBuffer.allocate(strtab + strings.length).order(ByteOrder.LITTLE_ENDIAN);
    elf.putInt(0, 0x464c457f).put(4, (byte) 2).put(5, (byte) 1).put(6, (byte) 1);
    elf.putShort(0x10, (short) 3).putLong(0x20, phoff);
    elf.putShort(0x34, (short) 64).putShort(0x36, (short) 56).putShort(0x38, (short) 2);
    programHeader(elf, phoff, 1, 0, elf.capacity());
    programHeader(elf, phoff + 56, 2, dynamic, 4 * 16);
    dynamicEntry(elf, dynamic, 5, strtab);
    dynamicEntry(elf, dynamic + 16, 1, 1);
    dynamicEntry(elf, dynamic + 32, 14, 1 + needed.length() + 1);
    dynamicEntry(elf, dynamic + 48, 0, 0);
    elf.position(strtab);
    elf.put(strings);
    return elf.array();
  }

  private static void programHeader(ByteBuffer elf, int at, int type, long offset, long size) {
    elf.putInt(at, type).putLong(at + 8, offset).putLong(at + 16, offset);
    elf.putLong(at + 32, size).putLong(at + 40, size);
  }

  private static void dynamicEntry(ByteBuffer elf, int at, long tag, long value) {
    elf.putLong(at, tag).putLong(at + 8, value);
  }
}
