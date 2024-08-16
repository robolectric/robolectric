package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowGeocoderTTest {
  private static final String GEOCODER_ERROR_MESSAGE = "Failed to get location";
  private Geocoder geocoder;
  private List<Address> decodedAddresses;
  private String errorMessage = "";

  @Before
  public void setUp() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    geocoder = new Geocoder(context);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocationSetsListenerWithTheOverwrittenListLimitingByMaxResults() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list =
        Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);

    Geocoder.GeocodeListener geocodeListener = addresses -> decodedAddresses = addresses;

    geocoder.getFromLocation(90.0, 90.0, 1, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0));

    geocoder.getFromLocation(90.0, 90.0, 2, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();

    geocoder.getFromLocation(90.0, 90.0, 3, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocation_onError() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list =
        Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);
    shadowGeocoder.setErrorMessage(GEOCODER_ERROR_MESSAGE);

    Geocoder.GeocodeListener geocodeListener =
        new Geocoder.GeocodeListener() {
          @Override
          public void onGeocode(List<Address> list) {
            decodedAddresses = list;
          }

          @Override
          public void onError(@Nullable String message) {
            errorMessage = message;
          }
        };

    geocoder.getFromLocation(90.0, 90.0, 1, geocodeListener);
    assertThat(decodedAddresses).isNull();
    assertThat(errorMessage).isEqualTo(GEOCODER_ERROR_MESSAGE);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocationNameSetsListenerWithTheOverwrittenListLimitingByMaxResults() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list =
        Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);

    Geocoder.GeocodeListener geocodeListener = addresses -> decodedAddresses = addresses;

    geocoder.getFromLocationName("test", 1, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0));

    geocoder.getFromLocationName("test", 2, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();

    geocoder.getFromLocationName("test", 3, geocodeListener);
    assertThat(decodedAddresses).containsExactly(list.get(0), list.get(1)).inOrder();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocationNameSetsListenerWithoutOverwrittenList() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    Geocoder.GeocodeListener geocodeListener = addresses -> decodedAddresses = addresses;
    shadowGeocoder.getFromLocationName("test", 1, geocodeListener);

    assertThat(decodedAddresses).hasSize(0);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void getFromLocationName_onError() {
    ShadowGeocoder shadowGeocoder = shadowOf(geocoder);

    List<Address> list =
        Arrays.asList(new Address(Locale.getDefault()), new Address(Locale.CANADA));
    shadowGeocoder.setFromLocation(list);
    shadowGeocoder.setErrorMessage(GEOCODER_ERROR_MESSAGE);

    Geocoder.GeocodeListener geocodeListener =
        new Geocoder.GeocodeListener() {
          @Override
          public void onGeocode(List<Address> list) {
            decodedAddresses = list;
          }

          @Override
          public void onError(@Nullable String message) {
            errorMessage = message;
          }
        };

    geocoder.getFromLocationName("test", 1, geocodeListener);
    assertThat(decodedAddresses).isNull();
    assertThat(errorMessage).isEqualTo(GEOCODER_ERROR_MESSAGE);
  }
}
