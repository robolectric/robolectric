package org.robolectric.integrationtests.playservices;

import static com.google.common.truth.Truth.assertThat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(instrumentedPackages = "com.google.android.gms.common.api")
public class ApiExceptionTest {
  /**
   * The ApiException constructor bundled with play-services-basement has a constructor that does
   * not conform to what Robolectric expects. It is likely due to either desugaring or proguarding.
   * Ensure that attempting to instantiate it does not cause a VerifyError
   */
  @Test
  public void testApiException() {
    ApiException apiException = new ApiException(new Status(CommonStatusCodes.ERROR, ""));
    assertThat(apiException).isNotNull();
  }
}
