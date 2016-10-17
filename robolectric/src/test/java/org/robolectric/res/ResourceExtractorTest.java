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
    resourceIndex = new MergedResourceIndex(
        new ResourceExtractor(testResources()),
        new ResourceExtractor(systemResources()));
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(getResourceId("id/textStyle", R.class.getPackage().getName())).isEqualTo(R.id.textStyle);
    assertThat(getResourceId("styleable/TitleBar_textStyle", R.class.getPackage().getName())).isNull();
  }

  @Test
  public void shouldPrefixResourcesWithPackageContext() throws Exception {
    assertThat(getResourceId("id/text1", "android")).isEqualTo(android.R.id.text1);
    assertThat(getResourceId("id/text1", R.class.getPackage().getName())).isEqualTo(R.id.text1);
  }

  @Test
  public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
    assertThat(getResourceId("android:id/text1", "android")).isEqualTo(android.R.id.text1);
  }

  @Test
  public void shouldHandleNull() throws Exception {
    assertThat(getResourceId(AttributeResource.NULL_VALUE, "")).isEqualTo(null);
    assertThat(getResourceId(AttributeResource.NULL_VALUE, "android")).isEqualTo(null);
    assertThat(getResourceId(AttributeResource.NULL_VALUE, "anything")).isEqualTo(null);
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

  //////////////////////

  public Integer getResourceId(String possiblyQualifiedResourceName, String contextPackageName) {
    ResName resName = ResName.qualifyPossiblyNullResName(possiblyQualifiedResourceName, contextPackageName);
    if (resName == null) return null;
    return resourceIndex.getResourceId(resName);
  }
}
