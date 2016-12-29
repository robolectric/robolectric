package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.*;

public class ResourceExtractorTest {
  private ResourceIndex resourceIndex;

  @Before
  public void setUp() throws Exception {
    ResourceRemapper resourceRemapper = new ResourceRemapper(testResources().getRClass());
    resourceRemapper.remapRClass(lib1Resources().getRClass());
    resourceRemapper.remapRClass(lib2Resources().getRClass());
    resourceRemapper.remapRClass(lib3Resources().getRClass());

    PackageResourceIndex systemResourceIndex = new PackageResourceIndex("android");

    PackageResourceIndex appResourceIndex = new PackageResourceIndex("org.robolectric");
    ResourceExtractor.populate(appResourceIndex,
        lib3Resources().getRClass(),
        lib2Resources().getRClass(),
        lib1Resources().getRClass(),
        testResources().getRClass());

    ResourceExtractor.populate(systemResourceIndex, systemResources().getRClass(), systemResources().getInternalRClass());
    resourceIndex = new RoutingResourceIndex(systemResourceIndex, appResourceIndex);
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "id/textStyle", R.class.getPackage().getName())).isEqualTo(R.id.textStyle);
    assertThat(ResName.getResourceId(resourceIndex, "styleable/TitleBar_textStyle", R.class.getPackage().getName())).isEqualTo(0);
  }

  @Test
  public void shouldPrefixResourcesWithPackageContext() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "id/text1", "android")).isEqualTo(android.R.id.text1);
    assertThat(ResName.getResourceId(resourceIndex, "id/text1", R.class.getPackage().getName())).isEqualTo(R.id.text1);
  }

  @Test
  public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "android:id/text1", "android")).isEqualTo(android.R.id.text1);
  }

  @Test
  public void shouldHandleNull() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "android")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "anything")).isEqualTo(null);
  }

  @Test
  public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
    assertThat(resourceIndex.getResName(android.R.id.text1).getFullyQualifiedName()).isEqualTo("android:id/text1");
    assertThat(resourceIndex.getResName(R.id.burritos).getFullyQualifiedName()).isEqualTo("org.robolectric:id/burritos");
  }

  @Test
  public void shouldNotResolveLibraryResourceName() throws Exception {
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib1", "string", "in_all_libs"))).isEqualTo(0);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib2", "string", "in_all_libs"))).isEqualTo(0);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib3", "string", "in_all_libs"))).isEqualTo(0);
  }
}
