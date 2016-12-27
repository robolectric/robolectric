package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class RoutingResourceIndexTest {

  @Mock private PackageResourceIndex systemPackageIndex;
  private RoutingResourceIndex resourceIndex;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(systemPackageIndex.getPackageIdentifier()).thenReturn(0x01);
    when(systemPackageIndex.getPackageName()).thenReturn("android");
    when(systemPackageIndex.getResName(0x01223333)).thenReturn(new ResName("android", "type", "name"));
    when(systemPackageIndex.getResourceId(new ResName("android", "type", "name"))).thenReturn(0x01223333);
    resourceIndex = new RoutingResourceIndex(systemPackageIndex);
  }

  @Test
  public void shouldRouteToCorrectIndexForIds() {
    assertThat(resourceIndex.getResName(0x01223333).getFullyQualifiedName()).isEqualTo("android:type/name");
  }

  @Test
  public void shouldRouteToCorrectIndexForResName() {
    assertThat(resourceIndex.getResourceId(new ResName("android", "type", "name"))).isEqualTo(0x01223333);
  }

  @Test
  public void shouldHandleUnknownPackages() {
    assertThat(resourceIndex.getResourceId(new ResName("unknown", "type", "name"))).isEqualTo(0);
    assertThat(resourceIndex.getResName(0x09223333)).isNull();
  }
}
