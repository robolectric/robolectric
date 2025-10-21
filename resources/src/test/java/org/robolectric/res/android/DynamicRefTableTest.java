package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.UNKNOWN_ERROR;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_LIBRARY_TYPE;

import java.nio.ByteBuffer;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResTable_lib_header;
import org.robolectric.res.android.ResourceTypes.Res_value;

@RunWith(JUnit4.class)
public class DynamicRefTableTest {

  private static final Ref<Res_value> RES_VALUE_OF_BAD_TYPE =
      new Ref<>(new Res_value(/* dataType= */ (byte) 99, /* data= */ 0));

  @Test
  public void lookupResourceValue_returnsBadTypeIfTypeOutOfEnumRange() {
    DynamicRefTable pseudoRefTable =
        new DynamicRefTable(/* packageId= */ (byte) 0, /* appAsLib= */ true);

    assertThat(pseudoRefTable.lookupResourceValue(RES_VALUE_OF_BAD_TYPE)).isEqualTo(BAD_TYPE);
  }

  @Test
  public void testEmptyDynamicRefTable() {
    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    Map<String, Byte> entries = dynamicRefTable.entries();

    assertThat(entries).isEmpty();
  }

  @Test
  public void testLoadValidLibraryTable() {
    // Create a valid library table with one entry
    final String packageName = "com.example.lib";
    final int packageId = 0x02; // Valid package ID

    final ByteBuffer buf = createLibraryTableBuffer(packageId, packageName);
    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(NO_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).hasSize(1);
    assertThat(entries).containsEntry(packageName, (byte) packageId);
  }

  @Test
  public void testLoadMultipleLibraryEntries() {
    // Create a library table with multiple entries
    final String[] packageNames = {"com.example.lib1", "com.example.lib2", "androidx.core"};
    final int[] packageIds = {0x02, 0x03, 0x7f};

    final ByteBuffer buf = createLibraryTableBuffer(packageIds, packageNames);
    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(NO_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).hasSize(3);
    for (int i = 0; i < packageNames.length; i++) {
      assertThat(entries).containsEntry(packageNames[i], (byte) packageIds[i]);
    }
  }

  @Test
  public void testLoadLibraryTableWithInvalidPackageId() {
    // Create a library table with invalid package ID (>= 256)
    final String packageName = "com.example.lib";
    final int invalidPackageId = 0x100; // 256, which is invalid

    final ByteBuffer buf = createLibraryTableBuffer(invalidPackageId, packageName);
    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(UNKNOWN_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).isEmpty(); // Should be cleared after error
  }

  @Test
  public void testLoadLibraryTableWithInvalidSize() {
    // Create a library table header that claims more entries than the buffer contains
    final ByteBuffer buf = ByteBuffer.allocate(1024);

    ResChunk_header.write(
        buf,
        (short) RES_TABLE_LIBRARY_TYPE,
        () -> {
          // Header claims 2 entries but we only write space for the header
          buf.putInt(2); // count = 2
        },
        () -> {
          // Empty contents - no space for any entries
        });

    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(UNKNOWN_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).isEmpty(); // Should be cleared after error
  }

  @Test
  public void testLoadLibraryTableWithNullTerminatedNames() {
    // Test that package names are properly null-terminated
    final String shortName = "lib";
    final int packageId = 0x02;

    final ByteBuffer buf = createLibraryTableBuffer(packageId, shortName);
    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(NO_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).hasSize(1);
    assertThat(entries).containsEntry(shortName, (byte) packageId);
  }

  @Test
  public void testLoadLibraryTableWithLongPackageName() {
    // Test with maximum length package name (127 chars + null terminator)
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 127; i++) {
      sb.append('a');
    }
    final String longName = sb.toString();
    final int packageId = 0x02;

    final ByteBuffer buf = createLibraryTableBuffer(packageId, longName);
    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(NO_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).hasSize(1);
    assertThat(entries).containsEntry(longName, (byte) packageId);
  }

  @Test
  public void testLoadEmptyLibraryTable() {
    // Test loading a library table with zero entries
    final ByteBuffer buf = ByteBuffer.allocate(1024);

    ResChunk_header.write(
        buf,
        (short) RES_TABLE_LIBRARY_TYPE,
        () -> {
          buf.putInt(0); // count = 0
        },
        () -> {
          // No contents for zero entries
        });

    final ResTable_lib_header header = new ResTable_lib_header(buf, 0);

    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x01, false);
    int result = dynamicRefTable.load(header);

    assertThat(result).isEqualTo(NO_ERROR);
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).isEmpty();
  }

  // Helper method to create a library table buffer with a single entry
  private ByteBuffer createLibraryTableBuffer(int packageId, String packageName) {
    return createLibraryTableBuffer(new int[] {packageId}, new String[] {packageName});
  }

  // Helper method to create a library table buffer with multiple entries
  private ByteBuffer createLibraryTableBuffer(int[] packageIds, String[] packageNames) {
    if (packageIds.length != packageNames.length) {
      throw new IllegalArgumentException("packageIds and packageNames must have the same length");
    }

    final ByteBuffer buf = ByteBuffer.allocate(1024);

    ResChunk_header.write(
        buf,
        (short) RES_TABLE_LIBRARY_TYPE,
        () -> {
          buf.putInt(packageIds.length); // count
        },
        () -> {
          // Contents: array of ResTable_lib_entry
          for (int i = 0; i < packageIds.length; i++) {
            // ResTable_lib_entry.packageId
            buf.putInt(packageIds[i]);

            // ResTable_lib_entry.packageName (128 chars, null-terminated)
            char[] nameChars = new char[128];
            char[] srcChars = packageNames[i].toCharArray();
            System.arraycopy(srcChars, 0, nameChars, 0, Math.min(srcChars.length, 127));
            // Remaining chars are already '\0' from array initialization

            for (char c : nameChars) {
              buf.putChar(c);
            }
          }
        });

    return buf;
  }
}
