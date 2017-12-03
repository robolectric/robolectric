package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.location.Geocoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit test for {@link ShadowGeocoder}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowGeocoderTest {

  @Test
  public void isPresentReturnsTrueByDefault() {
    assertThat(Geocoder.isPresent()).isTrue();
  }

  @Test
  public void isPresentReturnsFalseWhenOverridden() {
    ShadowGeocoder.setIsPresent(false);

    assertThat(Geocoder.isPresent()).isFalse();
  }

  @Test
  public void resettingShadowRestoresDefaultValueForIsPresent() {
    ShadowGeocoder.setIsPresent(false);
    ShadowGeocoder.reset();
    assertThat(Geocoder.isPresent()).isTrue();
  }
}
