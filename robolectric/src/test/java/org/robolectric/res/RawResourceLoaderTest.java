package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;
import static org.robolectric.util.TestUtil.testResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.R;

@RunWith(JUnit4.class)
public class RawResourceLoaderTest {

  private PackageResourceTable resourceTable;

  @Before
  public void setUp() throws Exception {
    resourceTable = new ResourceTableFactory().newResourceTable("packageName", testResources());
    RawResourceLoader rawResourceLoader = new RawResourceLoader(TEST_RESOURCE_PATH);
    rawResourceLoader.loadTo(resourceTable);
  }

  @Test
  public void shouldReturnRawResourcesWithExtensions() throws Exception {
    String f = (String) resourceTable.getValue(R.raw.raw_resource, "").getData();
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.getResourceBase().join("raw").join("raw_resource.txt").getPath());
  }

  @Test
  public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
    String f = (String) resourceTable.getValue(R.raw.raw_no_ext, "").getData();
    assertThat(f).isEqualTo(TEST_RESOURCE_PATH.getResourceBase().join("raw").join("raw_no_ext").getPath());
  }
}
