package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.provider.CallLog;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link ShadowCallLogCalls} */
@RunWith(AndroidJUnit4.class)
public final class ShadowCallLogCallsTest {
  private static final String TEST_LAST_CALL = "test last call";

  @Test
  public void getLastOutgoingCall_default() {
    assertThat(CallLog.Calls.getLastOutgoingCall(ApplicationProvider.getApplicationContext()))
        .isNull();
  }

  @Test
  public void getLastOutgoingCall_withValue() {
    ShadowCallLogCalls.setLastOutgoingCall(TEST_LAST_CALL);
    assertThat(CallLog.Calls.getLastOutgoingCall(ApplicationProvider.getApplicationContext()))
        .isEqualTo(TEST_LAST_CALL);
  }
}
