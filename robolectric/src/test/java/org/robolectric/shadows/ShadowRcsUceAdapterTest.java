package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.telephony.ims.ImsManager;
import android.telephony.ims.RcsUceAdapter;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowRcsUceAdapter}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = R)
public class ShadowRcsUceAdapterTest {
  private static final int SUBSCRIPTION_ID = 0;
  private static final int OTHER_SUBSCRIPTION_ID = 1;

  private ImsManager imsManager;

  @Before
  public void setUp() {
    imsManager =
        (ImsManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.TELEPHONY_IMS_SERVICE);
  }

  @Test
  public void
      setUceSettingEnabledForSubscriptionId_uceSettingEnabledTrue_overridesDesiredSubscriptionId()
          throws Exception {
    ShadowRcsUceAdapter.setUceSettingEnabledForSubscriptionId(
        SUBSCRIPTION_ID, /* uceSettingEnabled= */ true);

    RcsUceAdapter rcsUceAdapter = imsManager.getImsRcsManager(SUBSCRIPTION_ID).getUceAdapter();
    assertThat(rcsUceAdapter.isUceSettingEnabled()).isTrue();
  }

  @Test
  public void setUceSettingEnabledForSubscriptionId_shouldNotOverridesOtherSubIds()
      throws Exception {
    ShadowRcsUceAdapter.setUceSettingEnabledForSubscriptionId(
        SUBSCRIPTION_ID, /* uceSettingEnabled= */ true);

    RcsUceAdapter rcsUceAdapter =
        imsManager.getImsRcsManager(OTHER_SUBSCRIPTION_ID).getUceAdapter();
    assertThat(rcsUceAdapter.isUceSettingEnabled()).isFalse();
  }

  @Test
  public void setUceSettingEnabledForSubscriptionId_withMultipleSubIds_overridesAllSubIds()
      throws Exception {
    ShadowRcsUceAdapter.setUceSettingEnabledForSubscriptionId(
        SUBSCRIPTION_ID, /* uceSettingEnabled= */ true);
    ShadowRcsUceAdapter.setUceSettingEnabledForSubscriptionId(
        OTHER_SUBSCRIPTION_ID, /* uceSettingEnabled= */ true);

    RcsUceAdapter rcsUceAdapter = imsManager.getImsRcsManager(SUBSCRIPTION_ID).getUceAdapter();
    assertThat(rcsUceAdapter.isUceSettingEnabled()).isTrue();

    rcsUceAdapter = imsManager.getImsRcsManager(OTHER_SUBSCRIPTION_ID).getUceAdapter();
    assertThat(rcsUceAdapter.isUceSettingEnabled()).isTrue();
  }
}
