package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.R;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(JUnit4.class)
public class RawResourceLoaderTest {

  private ResourceExtractor resourceIndex;
  private ResBundle<FsFile> rawResourceFiles;

  @Before
  public void setUp() throws Exception {
    resourceIndex = new ResourceExtractor(testResources());
    rawResourceFiles = new ResBundle<>();
    RawResourceLoader rawResourceLoader = new RawResourceLoader(TEST_RESOURCE_PATH);
    rawResourceLoader.loadTo(rawResourceFiles);
  }

  @Test
  public void shouldReturnRawResourcesWithExtensions() throws Exception {
    FsFile f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_resource), "");
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.resourceBase.join("raw").join("raw_resource.txt"));
  }

  @Test
  public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
    FsFile f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_no_ext), "");
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.resourceBase.join("raw").join("raw_no_ext"));
  }
}
