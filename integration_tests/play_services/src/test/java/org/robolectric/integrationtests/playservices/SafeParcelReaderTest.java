package org.robolectric.integrationtests.playservices;

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(instrumentedPackages = "com.google.android.gms.common.internal.safeparcel")
public class SafeParcelReaderTest {
  @Test
  public void parseException_canBeInstantiated() {
    Parcel parcel = Parcel.obtain();
    parcel.writeInt(42);

    SafeParcelReader.ParseException exception = new SafeParcelReader.ParseException("boom", parcel);

    assertThat(exception).hasMessageThat().contains("boom");
    assertThat(exception).hasMessageThat().contains("Parcel:");
  }
}
