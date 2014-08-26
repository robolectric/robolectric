package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class RawResourceLoaderTest {

  private ResourceExtractor resourceIndex;
  private ResBundle<FsFile> rawResourceFiles;

  @Before public void setUp() throws Exception {
    resourceIndex = new ResourceExtractor(testResources());
    rawResourceFiles = new ResBundle<FsFile>();
    RawResourceLoader rawResourceLoader = new RawResourceLoader(TEST_RESOURCE_PATH);
    rawResourceLoader.loadTo(rawResourceFiles);
  }

  @Test
  public void shouldReturnRawResourcesWithExtensions() throws Exception {
    FsFile f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_resource), "");
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.rawDir.join("raw_resource.txt"));
  }

  @Test
  public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
    FsFile f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_no_ext), "");
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.rawDir.join("raw_no_ext"));
  }
}
