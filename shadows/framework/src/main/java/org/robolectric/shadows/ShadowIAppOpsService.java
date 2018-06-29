package org.robolectric.shadows;

import android.os.IBinder;
import com.android.internal.app.IAppOpsService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

public class ShadowIAppOpsService {

  @Implements(value = IAppOpsService.Stub.class, isInAndroidSdk = false)
  public static class ShadowStub {

    @Implementation
    public static IAppOpsService asInterface(IBinder obj) {
      return ReflectionHelpers.createNullProxy(IAppOpsService.class);
    }
  }
}

