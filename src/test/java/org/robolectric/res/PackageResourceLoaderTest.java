package org.robolectric.res;

import org.junit.Test;
import static org.robolectric.util.TestUtil.*;
import static org.fest.assertions.api.Assertions.*;

public class PackageResourceLoaderTest {

  @Test
  public void shouldLoadResourcesFromGradleOutputDirectories() {
    PackageResourceLoader loader = new PackageResourceLoader(gradleAppResources());
    TypedResource value = loader.getValue(new ResName("org.robolectric.gradleapp", "string", "from_gradle_output"), "");
    assertThat(value).describedAs("String from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("string example taken from gradle output directory");
  }

  @Test
  public void shouldLoadDimenResourcesFromGradleOutputDirectoriesDefinedByDimenTag() {
    PackageResourceLoader loader = new PackageResourceLoader(gradleAppResources());
    TypedResource value = loader.getValue(new ResName("org.robolectric.gradleapp", "dimen", "example_dimen"), "");
    assertThat(value).describedAs("Dimen from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("8dp");
  }

  @Test
  public void shouldLoadDimenResourcesFromGradleOutputDirectoriesDefinedByItemTag() {
    PackageResourceLoader loader = new PackageResourceLoader(gradleAppResources());
    TypedResource value = loader.getValue(new ResName("org.robolectric.gradleapp", "dimen", "example_item_dimen"), "");
    assertThat(value).describedAs("Item dimen from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("3.14");
  }

  @Test
  public void shouldLoadStringResourcesFromGradleOutputDirectoriesDefinedByItemTag() {
    PackageResourceLoader loader = new PackageResourceLoader(gradleAppResources());
    TypedResource value = loader.getValue(new ResName("org.robolectric.gradleapp", "string", "item_from_gradle_output"), "");
    assertThat(value).describedAs("Item string from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("3.14");
  }
}
