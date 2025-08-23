package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.graphics.Region;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link AccessibilityWindowInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
public class AccessibilityWindowInfoBuilderTest {

  @Test
  public void build_noValuesSet_returnsDefaultWindowInfo() {
    AccessibilityWindowInfo windowInfo = AccessibilityWindowInfoBuilder.newBuilder().build();

    assertThat(windowInfo).isNotNull();
    Rect bounds = new Rect();
    windowInfo.getBoundsInScreen(bounds);
    assertThat(bounds).isEqualTo(new Rect());
  }

  @Config(maxSdk = Q)
  @Test
  public void setBoundsInScreen_preR_setsCorrectBounds() {
    Rect bounds = new Rect(1, 2, 3, 4);
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder().setBoundsInScreen(bounds).build();

    Rect actualBounds = new Rect();
    windowInfo.getBoundsInScreen(actualBounds);

    assertThat(actualBounds).isEqualTo(bounds);
  }

  @Config(minSdk = R)
  @Test
  public void setRegionInScreen_atLeastR_setsCorrectRegion() {
    Region region = new Region(new Rect(0, 0, 100, 100));
    region.union(new Rect(100, 100, 200, 200));
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder().setRegionInScreen(region).build();

    Region outRegion = new Region();
    windowInfo.getRegionInScreen(outRegion);

    assertThat(outRegion).isEqualTo(region);
  }

  @Config(minSdk = R)
  @Test
  public void setBoundsAndRegion_atLeastR_regionTakesPrecedence() {
    Rect bounds = new Rect(1, 2, 3, 4);
    Region region = new Region(new Rect(0, 0, 100, 100));
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder()
            .setBoundsInScreen(bounds)
            .setRegionInScreen(region)
            .build();

    Region outRegion = new Region();
    windowInfo.getRegionInScreen(outRegion);

    // The explicitly set region should take precedence.
    assertThat(outRegion).isEqualTo(region);

    // The bounds should be derived from the region.
    Rect actualBounds = new Rect();
    windowInfo.getBoundsInScreen(actualBounds);
    assertThat(actualBounds).isEqualTo(region.getBounds());
  }

  @Test
  public void setId_setsCorrectId() {
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder().setId(123).build();
    assertThat(windowInfo.getId()).isEqualTo(123);
  }

  @Config(minSdk = N)
  @Test
  public void setTitle_setsCorrectTitle() {
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder().setTitle("My Window").build();
    assertThat(windowInfo.getTitle().toString()).isEqualTo("My Window");
  }

  @Test
  public void setFocused_setsCorrectFocusedState() {
    AccessibilityWindowInfo windowInfo =
        AccessibilityWindowInfoBuilder.newBuilder().setFocused(true).build();
    assertThat(windowInfo.isFocused()).isTrue();
  }
}
