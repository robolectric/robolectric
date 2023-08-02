package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Geocoder.GeocodeListener;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit test for {@link ShadowGeocoder}. */
@RunWith(AndroidJUnit4.class)
public class ShadowGeocoderTest {

  private Geocoder geocoder;
  private List<Address> decodedAddresses;

  @Before
  public void setUp() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    geocoder = new Geocoder(context);
  }

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
  public void getFromLocationReturnsAnEmptyArrayByDefault() throws IOException {
    assertThat(geocoder.getFromLocation(90.0,90.0,1)).hasSize(0);
  }

  @Test
  public void getFromLocationReturnsTheOverwrittenListLimitingByMaxResults() throws IOException {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list = Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);

    List<Address> result = geocoder.getFromLocation(90.0, 90.0, 1);
    assertThat(result).hasSize(1);

    result = geocoder.getFromLocation(90.0, 90.0, 2);
    assertThat(result).hasSize(2);

    result = geocoder.getFromLocation(90.0, 90.0, 3);
    assertThat(result).hasSize(2);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocationSetsListenerWithTheOverwrittenListLimitingByMaxResults() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list =
        Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);

    GeocodeListener geocodeListener = addresses -> decodedAddresses = addresses;

    geocoder.getFromLocation(90.0, 90.0, 1, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0));

    geocoder.getFromLocation(90.0, 90.0, 2, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();

    geocoder.getFromLocation(90.0, 90.0, 3, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();
  }

  @Test
  public void getFromLocation_throwsExceptionForInvalidLatitude() throws IOException {
    try {
      geocoder.getFromLocation(91.0, 90.0, 1);
      fail("IllegalArgumentException not thrown");
    } catch (IllegalArgumentException thrown) {
      assertThat(thrown).hasMessageThat().contains(Double.toString(91.0));
    }
  }

  @Test
  public void getFromLocation_throwsExceptionForInvalidLongitude() throws IOException {
    try {
      geocoder.getFromLocation(15.0, -211.0, 1);
      fail("IllegalArgumentException not thrown");
    } catch (IllegalArgumentException thrown) {
      assertThat(thrown).hasMessageThat().contains(Double.toString(-211.0));
    }
  }

  @Test
  public void resettingShadowRestoresDefaultValueForIsPresent() {
    ShadowGeocoder.setIsPresent(false);
    ShadowGeocoder.reset();
    assertThat(Geocoder.isPresent()).isTrue();
  }
}
