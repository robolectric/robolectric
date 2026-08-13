package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.location.ContextHubTransaction;
import android.os.Build.VERSION_CODES;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ContextHubTransaction}. */
@Implements(value = ContextHubTransaction.class, minSdk = VERSION_CODES.P, isInAndroidSdk = false)
public class ShadowContextHubTransaction<T> {
  @RealObject private ContextHubTransaction<T> realTransaction;
  private static boolean shouldThrowTimeout = false;

  /** Sets whether {@link ContextHubTransaction#waitForResponse} should throw a TimeoutException. */
  public static void setShouldThrowTimeout(boolean shouldThrow) {
    shouldThrowTimeout = shouldThrow;
  }

  @Resetter
  public static void reset() {
    shouldThrowTimeout = false;
  }

  @SuppressWarnings("unchecked")
  @Implementation
  protected ContextHubTransaction.Response<T> waitForResponse(long timeout, TimeUnit unit)
      throws InterruptedException, TimeoutException {
    if (shouldThrowTimeout) {
      throw new TimeoutException("Timed out while waiting for transaction");
    }
    return (ContextHubTransaction.Response<T>)
        reflector(ContextHubTransactionReflector.class, realTransaction)
            .waitForResponse(timeout, unit);
  }

  @ForType(ContextHubTransaction.class)
  interface ContextHubTransactionReflector {
    @Direct
    ContextHubTransaction.Response<?> waitForResponse(long timeout, TimeUnit unit);
  }
}
