package org.robolectric.nativeruntime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.robolectric.util.Logger;

/**
 * Gives an extracted copy of the V+ native runtime (AOSP's host libandroid_runtime) a name of its
 * own, so it cannot stand in for another libandroid_runtime.so in the same process.
 *
 * <p>Two places name the library: its DT_SONAME, which glibc uses to satisfy other libraries'
 * DT_NEEDED entries, and hwui's lookup of its own ANativeWindow_fromSurface, which does
 * dlopen("libandroid_runtime.so"). If that lookup finds this copy, glibc records the bare name on
 * it, so both have to change together. The lookup name is built from the 16-byte {@link
 * #LOOKUP_PREFIX} in .rodata followed by "me.so" in code, so the new name keeps that tail.
 *
 * <p>Every rewrite keeps the length of the bytes it replaces. Anything unexpected (not ELF64 LE, no
 * DT_SONAME, not exactly one lookup string) leaves the file untouched and returns false.
 */
final class ElfSoname {
  static final String LOOKUP_PREFIX = "libandroid_runti";
  private static final byte[] LOOKUP_STRING =
      "libandroid_runtime\0".getBytes(StandardCharsets.US_ASCII);
  private static final int PT_LOAD = 1;
  private static final int PT_DYNAMIC = 2;
  private static final long DT_NULL = 0;
  private static final long DT_STRTAB = 5;
  private static final long DT_SONAME = 14;

  private ElfSoname() {}

  /** The 16-byte replacement for {@link #LOOKUP_PREFIX}, e.g. {@code librobolectric37}. */
  static String uniquePrefix(int sdk) {
    StringBuilder prefix = new StringBuilder("librobolectric" + sdk);
    prefix.setLength(Math.min(prefix.length(), LOOKUP_PREFIX.length()));
    while (prefix.length() < LOOKUP_PREFIX.length()) {
      prefix.append('_');
    }
    return prefix.toString();
  }

  /** The library name hwui looks up once its prefix is {@code prefix}: {@code <prefix>me.so}. */
  static String libraryName(String prefix) {
    return prefix + "me.so";
  }

  /**
   * Points DT_SONAME and hwui's self-lookup of {@code library} at {@code libraryName(prefix)}.
   * Returns whether both were rewritten; on false the file is unchanged.
   */
  static boolean rename(Path library, String prefix) throws IOException {
    String newName = libraryName(prefix);
    try (FileChannel channel =
        FileChannel.open(library, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
      ByteBuffer elf = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());
      elf.order(ByteOrder.LITTLE_ENDIAN);
      if (elf.limit() < 64 || elf.getInt(0) != 0x464c457f || elf.get(4) != 2 || elf.get(5) != 1) {
        return skip(library, "not an ELF64 little-endian file");
      }
      int soname = sonameOffset(elf);
      if (soname < 0) {
        return skip(library, "no DT_STRTAB or DT_SONAME");
      }
      int sonameEnd = soname;
      while (elf.get(sonameEnd) != 0) {
        sonameEnd++;
      }
      if (sonameEnd - soname != newName.length()) {
        return skip(library, "soname is not " + newName.length() + " bytes");
      }
      int lookup = onlyOccurrence(elf, LOOKUP_STRING);
      if (lookup < 0) {
        return skip(library, "hwui's lookup string is not present exactly once");
      }
      put(elf, soname, newName);
      put(elf, lookup, prefix);
      return true;
    }
  }

  private static int sonameOffset(ByteBuffer elf) {
    long phoff = elf.getLong(0x20);
    int phentsize = Short.toUnsignedInt(elf.getShort(0x36));
    int phnum = Short.toUnsignedInt(elf.getShort(0x38));
    long strtab = -1;
    long soname = -1;
    for (int i = 0; i < phnum; i++) {
      int ph = Math.toIntExact(phoff + (long) i * phentsize);
      if (elf.getInt(ph) != PT_DYNAMIC) {
        continue;
      }
      long dynamic = elf.getLong(ph + 8);
      long entries = elf.getLong(ph + 32) / 16;
      for (long e = 0; e < entries; e++) {
        int entry = Math.toIntExact(dynamic + e * 16);
        long tag = elf.getLong(entry);
        if (tag == DT_NULL) {
          break;
        } else if (tag == DT_STRTAB) {
          strtab = elf.getLong(entry + 8);
        } else if (tag == DT_SONAME) {
          soname = elf.getLong(entry + 8);
        }
      }
    }
    if (strtab < 0 || soname < 0) {
      return -1;
    }
    for (int i = 0; i < phnum; i++) {
      int ph = Math.toIntExact(phoff + (long) i * phentsize);
      long start = elf.getLong(ph + 16);
      if (elf.getInt(ph) == PT_LOAD && strtab >= start && strtab < start + elf.getLong(ph + 32)) {
        return Math.toIntExact(elf.getLong(ph + 8) + (strtab - start) + soname);
      }
    }
    return -1;
  }

  private static int onlyOccurrence(ByteBuffer elf, byte[] needle) {
    int found = -1;
    for (int i = 0; i + needle.length <= elf.limit(); i++) {
      int j = 0;
      while (j < needle.length && elf.get(i + j) == needle[j]) {
        j++;
      }
      if (j == needle.length) {
        if (found >= 0) {
          return -1;
        }
        found = i;
      }
    }
    return found;
  }

  private static void put(ByteBuffer elf, int offset, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
    for (int i = 0; i < bytes.length; i++) {
      elf.put(offset + i, bytes[i]);
    }
  }

  private static boolean skip(Path library, String reason) {
    Logger.info("Not renaming the native runtime at %s: %s", library, reason);
    return false;
  }
}
