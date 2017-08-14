package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import android.content.res.Resources;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

@RunWith(TestRunners.SelfTest.class)
public class ResourceMergerTest {
  private final Resources resources = RuntimeEnvironment.application.getResources();

  @Test
  public void shouldFetchResourcesFromMergedLibraries() throws Exception {
    assertEquals("from main", resources.getText(org.robolectric.R.string.only_in_main));
    assertEquals("from lib1", resources.getText(org.robolectric.R.string.only_in_lib1));
    assertEquals("from lib2", resources.getText(org.robolectric.R.string.only_in_lib2));
    assertEquals("from lib3", resources.getText(org.robolectric.R.string.only_in_lib3));
  }

  @Test
  public void shouldFetchResourcesAccordingToLibraryPrecedence() throws Exception {
    // main includes lib1 and lib2; lib1 includes lib3
    assertEquals("from main", resources.getText(org.robolectric.R.string.in_all_libs));
    assertEquals("from lib3", resources.getText(org.robolectric.R.string.in_lib2_and_lib3));
    assertEquals("from lib1", resources.getText(org.robolectric.R.string.in_lib1_and_lib3));
    assertEquals("from main", resources.getText(org.robolectric.R.string.in_main_and_lib1));
  }

  @Test
  public void sameIdentifiersFromLibraryRClassesShouldReturnSameValues() throws Exception {
    assertThat(resources.getText(org.robolectric.R.string.in_all_libs))
        .isEqualTo(resources.getText(org.robolectric.lib1.R.string.in_all_libs));
  }

  @Test
  @Config(libraries="lib1")
  public void libraryConfigShouldOverrideProjectProperties() throws Exception {
    AndroidManifest manifest = Shadows.shadowOf(RuntimeEnvironment.application).getAppManifest();
    List<AndroidManifest> libraryManifests = manifest.getLibraryManifests();
    assertEquals(1, libraryManifests.size());
    assertEquals("org.robolectric.lib1", libraryManifests.get(0).getPackageName());
  }
}
