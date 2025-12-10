package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.credentials.selection.RequestToken;
import android.os.Build;
import android.os.IBinder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link RequestToken} */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
public final class RequestTokenBuilder {
  private IBinder token;

  private RequestTokenBuilder() {}

  public static RequestTokenBuilder newBuilder() {
    return new RequestTokenBuilder();
  }

  /** Sets the request token. */
  @CanIgnoreReturnValue
  public RequestTokenBuilder setToken(IBinder token) {
    this.token = token;
    return this;
  }

  public RequestToken build() {
    return ReflectionHelpers.callConstructor(
        RequestToken.class, ClassParameter.from(IBinder.class, token));
  }
}
