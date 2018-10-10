package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.TestUtil.systemResources;
import static org.robolectric.util.TestUtil.testResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.R;

@RunWith(JUnit4.class)
public class ResourceTableFactoryTest {
  private ResourceTable appResourceTable;
  private ResourceTable systemResourceTable;

  @Before
  public void setUp() throws Exception {
    ResourceTableFactory resourceTableFactory = new ResourceTableFactory();
    appResourceTable = resourceTableFactory.newResourceTable("org.robolectric",
        testResources());

    systemResourceTable = resourceTableFactory.newFrameworkResourceTable(systemResources());
  }

  @Test
  public void shouldHandleMipmapReferences() {
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric:mipmap/mipmap_reference"))).isEqualTo(R.mipmap.mipmap_reference);
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric:id/burritos"))).isEqualTo(R.id.burritos);
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric:styleable/TitleBar_textStyle"))).isEqualTo(0);
  }

  @Test
  public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
    assertThat(systemResourceTable.getResourceId(new ResName("android:id/text1"))).isEqualTo(android.R.id.text1);
  }

  @Test
  public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
    assertThat(systemResourceTable.getResName(android.R.id.text1).getFullyQualifiedName()).isEqualTo("android:id/text1");
    assertThat(appResourceTable.getResName(R.id.burritos).getFullyQualifiedName()).isEqualTo("org.robolectric:id/burritos");
  }
}
