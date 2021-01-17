package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.content.Context;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link org.robolectric.shadows.ShadowPhoneAccount}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class PhoneAccountBuilderTest {
  private PhoneAccount phoneAccount;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();

    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(new ComponentName(context.getPackageName(), "CLASS"), "id");

    phoneAccount = new PhoneAccountBuilder(phoneAccountHandle, "phoneAccount").build();
  }

  @Test
  public void enabled() {
    assertThat(phoneAccount.isEnabled()).isFalse();

    phoneAccount = new PhoneAccountBuilder(phoneAccount).setIsEnabled(true).build();

    assertThat(phoneAccount.isEnabled()).isTrue();
  }
}
