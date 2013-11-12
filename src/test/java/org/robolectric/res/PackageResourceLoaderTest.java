package org.robolectric.res;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.gradleAppResources;

public class PackageResourceLoaderTest {

  @Test
  public void shouldLoadResourcesFromGradleOutputDirectories() {
    PackageResourceLoader loader = new PackageResourceLoader(gradleAppResources());
    TypedResource value = loader.getValue(new ResName("org.robolectric.gradleapp", "string", "from_gradle_output"), "");
    assertThat(value).describedAs("String from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("string example taken from gradle output directory");
  }

}
