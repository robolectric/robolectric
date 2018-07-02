package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.os.IBinder;
import com.android.internal.app.IAppOpsService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

public class ShadowIAppOpsService {

  @Implements(value = IAppOpsService.Stub.class, isInAndroidSdk = false)
  public static class ShadowStub {

    @Implementation(minSdk = JELLY_BEAN_MR2)
    public static IAppOpsService asInterface(IBinder obj) {
      return ReflectionHelpers.createNullProxy(IAppOpsService.class);
    }
  }
}

