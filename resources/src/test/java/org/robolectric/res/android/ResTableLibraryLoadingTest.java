package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.Errors.NO_ERROR;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResTableLibraryLoadingTest {

  @Test
  public void testResTableIntegrationWithDynamicRefTable() {
    // Test that we can create a ResTable with library support
    ResTable resTable = new ResTable();

    // Add an empty resource table - this tests basic integration
    int result = resTable.addEmpty(1);
    assertThat(result).isEqualTo(NO_ERROR);

    // Verify the table was created successfully
    assertThat(resTable.getTableCount()).isEqualTo(1);
  }

  @Test
  public void testResTableIntegrationWithDynamicRefTableInMemory() {
    ResTable resTable = new ResTable();
    int result = resTable.addEmpty(1);
    assertThat(result).isEqualTo(NO_ERROR);

    // After adding an empty table, we should have a table count of 1
    assertThat(resTable.getTableCount()).isEqualTo(1);

    // The empty resource table doesn't create package groups until there's actual content,
    // so getDynamicRefTableForCookie may return null for empty tables
    resTable.getDynamicRefTableForCookie(1);
    // This may be null for empty tables, which is expected behavior
  }

  @Test
  public void testResTableWithMultiplePackageGroups() {
    ResTable resTable1 = new ResTable();
    ResTable resTable2 = new ResTable();

    // Create two separate resource tables
    int result1 = resTable1.addEmpty(1);
    int result2 = resTable2.addEmpty(2);

    assertThat(result1).isEqualTo(NO_ERROR);
    assertThat(result2).isEqualTo(NO_ERROR);

    // Test merging one ResTable into another
    int mergeResult = resTable1.add(resTable2, false);
    assertThat(mergeResult).isEqualTo(NO_ERROR);

    // After merging, resTable1 should have tables from both
    assertThat(resTable1.getTableCount()).isEqualTo(2);
  }

  @Test
  public void testDynamicRefTableDirectIntegration() {
    // Test direct creation and usage of DynamicRefTable (the class our code modifies)
    DynamicRefTable dynamicRefTable = new DynamicRefTable((byte) 0x7f, false);

    // Verify initial state
    assertThat(dynamicRefTable.entries()).isEmpty();

    // First, we need to load a package into the dynamic ref table before we can add mappings
    // Let's create another DynamicRefTable and add a mapping to it, then test addMappings
    DynamicRefTable otherTable = new DynamicRefTable((byte) 0x7f, false);

    // Add a mapping directly to mEntries to simulate loading from a library table
    otherTable.entries().put("com.example.package", (byte) 0x02);

    // Now test that we can merge mappings from one table to another
    int result = dynamicRefTable.addMappings(otherTable);
    assertThat(result).isEqualTo(NO_ERROR);

    // Verify the mapping was added
    Map<String, Byte> entries = dynamicRefTable.entries();
    assertThat(entries).containsEntry("com.example.package", (byte) 0x02);
  }
}
