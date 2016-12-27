package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class PackageResourceIndexTest {

  @Test
  public void getPackageName_shouldReturnPackageNameOfItsResources() {
    PackageResourceIndex resourceIndex = new PackageResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");

    assertThat(resourceIndex.getPackageName()).isEqualTo("myPackage");
  }

  @Test
  public void getPackageIdentifier_shouldReturnPackageIdentiferOfItsResources() {
    PackageResourceIndex resourceIndex = new PackageResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");

    assertThat(resourceIndex.getPackageIdentifier()).isEqualTo(0x02);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addResource_shouldPreventMixedPackageIdentifiers() {
    PackageResourceIndex resourceIndex = new PackageResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");
    resourceIndex.addResource(0x03999999, "type", "name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidIdClashes() {
    PackageResourceIndex resourceIndex = new PackageResourceIndex("myPackage");
    resourceIndex.addResource(0x02888888, "type", "name");
    resourceIndex.addResource(0x02999999, "type", "name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidDuplicateNames() {
    PackageResourceIndex resourceIndex = new PackageResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");
    resourceIndex.addResource(0x02999999, "type", "anotherName");
  }
}
