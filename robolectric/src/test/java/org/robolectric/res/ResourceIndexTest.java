package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ResourceIndexTest {

  @Test
  public void getPackageName_shouldReturnPackageNameOfItsResources() {
    ResourceIndex resourceIndex = new ResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");

    assertThat(resourceIndex.getPackageName()).isEqualTo("myPackage");
  }

  @Test
  public void getPackageIdentifier_shouldReturnPackageIdentiferOfItsResources() {
    ResourceIndex resourceIndex = new ResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");

    assertThat(resourceIndex.getPackageIdentifier()).isEqualTo(0x02);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addResource_shouldPreventMixedPackageIdentifiers() {
    ResourceIndex resourceIndex = new ResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");
    resourceIndex.addResource(0x03999999, "type", "name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidIdClashes() {
    ResourceIndex resourceIndex = new ResourceIndex("myPackage");
    resourceIndex.addResource(0x02888888, "type", "name");
    resourceIndex.addResource(0x02999999, "type", "name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidDuplicateNames() {
    ResourceIndex resourceIndex = new ResourceIndex("myPackage");
    resourceIndex.addResource(0x02999999, "type", "name");
    resourceIndex.addResource(0x02999999, "type", "anotherName");
  }
}
