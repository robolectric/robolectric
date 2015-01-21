package org.robolectric.shadows;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(GooglePlayServicesUtil.class)
public class ShadowGooglePlayServicesUtil {
  private static int availabilityCode = ConnectionResult.SERVICE_MISSING;

  @Implementation
  public static int isGooglePlayServicesAvailable(Context context) {
    return availabilityCode;
  }

  public static void setIsGooglePlayServicesAvailable(int availabilityCode) {
    ShadowGooglePlayServicesUtil.availabilityCode = availabilityCode;
  }
}
