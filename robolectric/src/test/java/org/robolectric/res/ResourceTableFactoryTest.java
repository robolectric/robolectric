package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.*;

public class ResourceTableFactoryTest {
  private ResourceTable appResourceTable;
  private ResourceTable systemResourceTable;

  @Before
  public void setUp() throws Exception {
    ResourceRemapper resourceRemapper = new ResourceRemapper(testResources().getRClass());
    resourceRemapper.remapRClass(lib1Resources().getRClass());
    resourceRemapper.remapRClass(lib2Resources().getRClass());
    resourceRemapper.remapRClass(lib3Resources().getRClass());


    appResourceTable = ResourceTableFactory.newResourceTable("org.robolectric",
        lib3Resources(),
        lib2Resources(),
        lib1Resources(),
        testResources());

    systemResourceTable = ResourceTableFactory.newResourceTable("android",
        systemResources(),
        systemResources());
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
  public void shouldHandleNull() throws Exception {
    assertThat(appResourceTable.getResourceId(ResName.qualifyResName(AttributeResource.NULL_VALUE, null, null))).isEqualTo(null);
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
