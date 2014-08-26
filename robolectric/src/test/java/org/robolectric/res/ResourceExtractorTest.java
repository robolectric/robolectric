package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.*;

public class ResourceExtractorTest {
  private ResourceIndex resourceIndex;

  @Before
  public void setUp() throws Exception {
    resourceIndex = new MergedResourceIndex(
        new ResourceExtractor(testResources()),
        new ResourceExtractor(systemResources()));
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "id/textStyle", R.class.getPackage().getName())).isEqualTo(R.id.textStyle);
    assertThat(ResName.getResourceId(resourceIndex, "styleable/TitleBar_textStyle", R.class.getPackage().getName())).isNull();
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
    assertThat(ResName.getResourceId(resourceIndex, "@null", "")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, "@null", "android")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, "@null", "anything")).isEqualTo(null);
  }

  @Test
  public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
    assertThat(resourceIndex.getResName(android.R.id.text1).getFullyQualifiedName()).isEqualTo("android:id/text1");
    assertThat(resourceIndex.getResName(R.id.burritos).getFullyQualifiedName()).isEqualTo("org.robolectric:id/burritos");
  }

  @Test
  public void shouldResolveEquivalentResNames() throws Exception {
    OverlayResourceIndex overlayResourceIndex = new OverlayResourceIndex(
        "org.robolectric",
        new ResourceExtractor(testResources()),
        new ResourceExtractor(lib1Resources()),
        new ResourceExtractor(lib2Resources()),
        new ResourceExtractor(lib3Resources()));
    resourceIndex = new MergedResourceIndex(overlayResourceIndex, new ResourceExtractor(systemResources()));

    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib1", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib2", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib3", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
  }
}
