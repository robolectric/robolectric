package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResourceTableTest {

  private PackageResourceTable resourceTable;

  @Before
  public void setUp() {
    resourceTable = new ResourceTableFactory().newResourceTable("myPackage");
  }

  @Test
  public void getPackageName_shouldReturnPackageNameOfItsResources() {
    resourceTable.addResource(0x02999999, "type", "name");

    assertThat(resourceTable.getPackageName()).isEqualTo("myPackage");
  }

  @Test
  public void getPackageIdentifier_shouldReturnPackageIdentiferOfItsResources() {
    resourceTable.addResource(0x02999999, "type", "name");

    assertThat(resourceTable.getPackageIdentifier()).isEqualTo(0x02);
  }

  @Test
  public void addResource_shouldPreventMixedPackageIdentifiers() {
    resourceTable.addResource(0x02999999, "type", "name");
    assertThrows(
        IllegalArgumentException.class,
        () -> resourceTable.addResource(0x03999999, "type", "name"));
  }

  @Test
  public void shouldForbidIdClashes() {
    resourceTable.addResource(0x02888888, "type", "name");
    assertThrows(
        IllegalArgumentException.class,
        () -> resourceTable.addResource(0x02999999, "type", "name"));
  }

  @Test
  public void shouldForbidDuplicateNames() {
    resourceTable.addResource(0x02999999, "type", "name");
    assertThrows(
        IllegalArgumentException.class,
        () -> resourceTable.addResource(0x02999999, "type", "anotherName"));
  }
}
