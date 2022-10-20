package org.robolectric.shadows;

import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_CS_VOICE;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_CONDITIONAL;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_NONE;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_UNKNOWN;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.telephony.BarringInfo;
import android.telephony.BarringInfo.BarringServiceInfo;
import android.telephony.CellIdentityLte;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.BarringInfoBuilder.BarringServiceInfoBuilder;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.R)
public final class BarringInfoBuilderTest {

  @Test
  public void buildBarringServiceInfo_noset_fromSdkR() {
    BarringServiceInfo barringServiceInfo = BarringServiceInfoBuilder.newBuilder().build();

    assertThat(barringServiceInfo).isNotNull();
    assertThat(barringServiceInfo.getBarringType()).isEqualTo(BARRING_TYPE_NONE);
    assertThat(barringServiceInfo.isConditionallyBarred()).isFalse();
    assertThat(barringServiceInfo.isBarred()).isFalse();
  }

  @Test
  public void buildBarringInfo_noset_fromSdkR() {
    BarringInfo barringInfo = BarringInfoBuilder.newBuilder().build();
    assertThat(barringInfo).isNotNull();

    BarringServiceInfo barringServiceInfo =
        barringInfo.getBarringServiceInfo(BARRING_SERVICE_TYPE_CS_VOICE);
    assertThat(barringServiceInfo.getBarringType()).isEqualTo(BARRING_TYPE_UNKNOWN);
    assertThat(barringServiceInfo.isConditionallyBarred()).isFalse();
    assertThat(barringServiceInfo.isBarred()).isFalse();
  }

  @Test
  public void buildBarringServiceInfo_fromSdkR() {
    BarringServiceInfo barringServiceInfo =
        BarringServiceInfoBuilder.newBuilder()
            .setBarringType(BARRING_TYPE_CONDITIONAL)
            .setIsConditionallyBarred(true)
            .setConditionalBarringFactor(20)
            .setConditionalBarringTimeSeconds(30)
            .build();

    assertThat(barringServiceInfo).isNotNull();
    assertThat(barringServiceInfo.getBarringType()).isEqualTo(BARRING_TYPE_CONDITIONAL);
    assertThat(barringServiceInfo.isConditionallyBarred()).isTrue();
    assertThat(barringServiceInfo.getConditionalBarringFactor()).isEqualTo(20);
    assertThat(barringServiceInfo.getConditionalBarringTimeSeconds()).isEqualTo(30);
    assertThat(barringServiceInfo.isBarred()).isTrue();
  }

  @Test
  public void buildBarringInfo_fromSdkR() throws Exception {
    BarringServiceInfo barringServiceInfo =
        BarringServiceInfoBuilder.newBuilder()
            .setBarringType(BARRING_TYPE_CONDITIONAL)
            .setIsConditionallyBarred(true)
            .setConditionalBarringFactor(20)
            .setConditionalBarringTimeSeconds(30)
            .build();
    CellIdentityLte cellIdentityLte =
        ReflectionHelpers.callConstructor(
            CellIdentityLte.class,
            ReflectionHelpers.ClassParameter.from(int.class, 310),
            ReflectionHelpers.ClassParameter.from(int.class, 260),
            ReflectionHelpers.ClassParameter.from(int.class, 0),
            ReflectionHelpers.ClassParameter.from(int.class, 0),
            ReflectionHelpers.ClassParameter.from(int.class, 0));
    BarringInfo barringInfo =
        BarringInfoBuilder.newBuilder()
            .setCellIdentity(cellIdentityLte)
            .addBarringServiceInfo(BARRING_SERVICE_TYPE_CS_VOICE, barringServiceInfo)
            .build();

    BarringServiceInfo outBarringServiceInfo =
        barringInfo.getBarringServiceInfo(BARRING_SERVICE_TYPE_CS_VOICE);
    assertThat(outBarringServiceInfo.getBarringType()).isEqualTo(BARRING_TYPE_CONDITIONAL);
    assertThat(outBarringServiceInfo.isConditionallyBarred()).isTrue();
    assertThat(outBarringServiceInfo.getConditionalBarringFactor()).isEqualTo(20);
    assertThat(outBarringServiceInfo.getConditionalBarringTimeSeconds()).isEqualTo(30);
    assertThat(outBarringServiceInfo.isBarred()).isTrue();
  }
}
