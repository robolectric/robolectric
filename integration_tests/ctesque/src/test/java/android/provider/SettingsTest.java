package android.provider;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.provider.Settings.Secure.LOCATION_MODE;
import static android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;
import static android.provider.Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
import static com.google.common.truth.Truth.assertThat;

import android.content.ContentResolver;
import android.provider.Settings.Secure;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link Settings} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class SettingsTest {

  private ContentResolver contentResolver;
  private boolean gpsProviderStartState;
  private boolean networkProviderStartState;
  private int locationModeStartState;

  @Before
  public void setUp() {
    contentResolver = InstrumentationRegistry.getTargetContext().getContentResolver();
    gpsProviderStartState = Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER);
    networkProviderStartState = Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER);
    locationModeStartState = Secure.getInt(contentResolver, LOCATION_MODE, -1);
  }

  @After
  public void tearDown() {
    Secure.setLocationProviderEnabled(contentResolver, GPS_PROVIDER, gpsProviderStartState);
    Secure.setLocationProviderEnabled(contentResolver, NETWORK_PROVIDER, networkProviderStartState);
    Secure.putInt(contentResolver, LOCATION_MODE, locationModeStartState);
  }

  @SdkSuppress(maxSdkVersion = JELLY_BEAN_MR2)
  @Config(maxSdk = JELLY_BEAN_MR2)
  @Test
  public void setLocationProviderEnabled() {
    // Verify default values
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();

    Secure.setLocationProviderEnabled(contentResolver, NETWORK_PROVIDER, true);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();

    Secure.setLocationProviderEnabled(contentResolver, GPS_PROVIDER, false);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();

    Secure.setLocationProviderEnabled(contentResolver, NETWORK_PROVIDER, false);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();
  }

  @SdkSuppress(minSdkVersion = LOLLIPOP)
  @Config(minSdk = LOLLIPOP)
  @Test
  public void contentProviders_affectsLocationMode() {
    // Verify default values
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();

    Secure.setLocationProviderEnabled(contentResolver, NETWORK_PROVIDER, true);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_HIGH_ACCURACY);

    Secure.setLocationProviderEnabled(contentResolver, GPS_PROVIDER, false);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_BATTERY_SAVING);

    Secure.setLocationProviderEnabled(contentResolver, NETWORK_PROVIDER, false);

    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1)).isEqualTo(LOCATION_MODE_OFF);
  }

  @SdkSuppress(minSdkVersion = LOLLIPOP)
  @Config(minSdk = LOLLIPOP)
  @Test
  public void locationMode_affectsContentProviders() {
    // Verify the default value
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_SENSORS_ONLY);

    // LOCATION_MODE_OFF should set value and disable both content providers
    assertThat(Secure.putInt(contentResolver, LOCATION_MODE, LOCATION_MODE_OFF)).isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1)).isEqualTo(LOCATION_MODE_OFF);
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();

    // LOCATION_MODE_SENSORS_ONLY should set value and enable GPS_PROVIDER
    assertThat(Secure.putInt(contentResolver, LOCATION_MODE, LOCATION_MODE_SENSORS_ONLY)).isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_SENSORS_ONLY);
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isFalse();

    // LOCATION_MODE_BATTERY_SAVING should set value and enable NETWORK_PROVIDER
    assertThat(Secure.putInt(contentResolver, LOCATION_MODE, LOCATION_MODE_BATTERY_SAVING))
        .isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_BATTERY_SAVING);
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isFalse();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();

    // LOCATION_MODE_HIGH_ACCURACY should set value and enable both providers
    assertThat(Secure.putInt(contentResolver, LOCATION_MODE, LOCATION_MODE_HIGH_ACCURACY)).isTrue();
    assertThat(Secure.getInt(contentResolver, LOCATION_MODE, -1))
        .isEqualTo(LOCATION_MODE_HIGH_ACCURACY);
    assertThat(Secure.isLocationProviderEnabled(contentResolver, GPS_PROVIDER)).isTrue();
    assertThat(Secure.isLocationProviderEnabled(contentResolver, NETWORK_PROVIDER)).isTrue();
  }
}
