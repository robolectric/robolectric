package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.TestUtil.testResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.R;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.util.TestUtil;

@RunWith(JUnit4.class)
public class RawResourceLoaderTest {

  private PackageResourceTable resourceTable;

  @Before
  public void setUp() throws Exception {
    resourceTable = new ResourceTableFactory().newResourceTable("packageName", testResources());
    RawResourceLoader rawResourceLoader = new RawResourceLoader(TestUtil.testResources());
    rawResourceLoader.loadTo(resourceTable);
  }

  @Test
  public void shouldReturnRawResourcesWithExtensions() throws Exception {
    String f = (String) resourceTable.getValue(R.raw.raw_resource, new ResTable_config()).getData();
    assertThat(f).isEqualTo(TestUtil.testResources().getResourceBase().join("raw").join("raw_resource.txt").getPath());
  }

  @Test
  public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
    String f = (String) resourceTable.getValue(R.raw.raw_no_ext, new ResTable_config()).getData();
    assertThat(f).isEqualTo(TestUtil.testResources().getResourceBase().join("raw").join("raw_no_ext").getPath());
  }
}
