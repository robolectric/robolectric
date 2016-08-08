package org.robolectric;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.robolectric.util.TestUtil.joinPath;
import static org.robolectric.util.TestUtil.resourceFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourcePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@RunWith(JUnit4.class)
public class ManifestFactoryTest {
  @Test
  public void shouldLoadLibraryManifests() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("manifest", resourceFile("TestAndroidManifest.xml").toString());
    properties.setProperty("libraries", "lib1");
    Config config = Config.Implementation.fromProperties(properties);
    ManifestFactory manifestFactory = new RobolectricTestRunner(ManifestFactoryTest.class).getManifestFactory(config);
    AndroidManifest manifest = manifestFactory.create(manifestFactory.identify(config));

    List<AndroidManifest> libraryManifests = manifest.getLibraryManifests();
    assertEquals(1, libraryManifests.size());
    assertEquals("org.robolectric.lib1", libraryManifests.get(0).getPackageName());
  }

  @Test
  public void shouldLoadAllResourcesForExistingLibraries() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("manifest", resourceFile("TestAndroidManifest.xml").toString());
    properties.setProperty("resourceDir", "res");
    properties.setProperty("assetDir", "assets");
    Config config = Config.Implementation.fromProperties(properties);
    ManifestFactory manifestFactory = new RobolectricTestRunner(ManifestFactoryTest.class).getManifestFactory(config);
    AndroidManifest appManifest = manifestFactory.create(manifestFactory.identify(config));

    // This intentionally loads from the non standard resources/project.properties
    List<String> resourcePaths = stringify(appManifest.getIncludedResourcePaths());
    assertEquals(asList(
        joinPath(".", "src", "test", "resources", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "..", "lib3", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "..", "lib2", "res")),
        resourcePaths);
  }

  private List<String> stringify(Collection<ResourcePath> resourcePaths) {
    List<String> resourcePathBases = new ArrayList<>();
    for (ResourcePath resourcePath : resourcePaths) {
      resourcePathBases.add(resourcePath.getResourceBase().toString());
    }
    return resourcePathBases;
  }
}
