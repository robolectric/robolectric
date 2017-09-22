package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.lib1Resources;
import static org.robolectric.util.TestUtil.lib2Resources;
import static org.robolectric.util.TestUtil.lib3Resources;
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
  private ResourceTableFactory resourceTableFactory;

  @Before
  public void setUp() throws Exception {
    ResourceRemapper resourceRemapper = new ResourceRemapper(testResources().getRClass());
    resourceRemapper.remapRClass(lib1Resources().getRClass());
    resourceRemapper.remapRClass(lib2Resources().getRClass());
    resourceRemapper.remapRClass(lib3Resources().getRClass());


    resourceTableFactory = new ResourceTableFactory();
    appResourceTable = resourceTableFactory.newResourceTable("org.robolectric",
        lib3Resources(),
        lib2Resources(),
        lib1Resources(),
        testResources());

    systemResourceTable = resourceTableFactory.newFrameworkResourceTable(systemResources());
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric:id/textStyle"))).isEqualTo(R.id.textStyle);
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

  @Test
  public void shouldNotResolveLibraryResourceName() throws Exception {
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric.lib1", "string", "in_all_libs"))).isEqualTo(0);
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric.lib2", "string", "in_all_libs"))).isEqualTo(0);
    assertThat(appResourceTable.getResourceId(new ResName("org.robolectric.lib3", "string", "in_all_libs"))).isEqualTo(0);
  }
}
