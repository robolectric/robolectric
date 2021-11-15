package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.Icon.OnDrawableLoadedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import java.util.concurrent.Executor;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Icon.class, minSdk = M)
public class ShadowIcon {

  @Nullable private static Executor executorOverride;

  /** Set the executor where async drawable loading will run. */
  public static void overrideExecutor(Executor executor) {
    executorOverride = executor;
  }

  @RealObject private Icon realIcon;

  @HiddenApi
  @Implementation
  public int getType() {
    return reflector(IconReflector.class, realIcon).getType();
  }

  @HiddenApi
  @Implementation
  public int getResId() {
    return reflector(IconReflector.class, realIcon).getResId();
  }

  @HiddenApi
  @Implementation
  public Bitmap getBitmap() {
    return reflector(IconReflector.class, realIcon).getBitmap();
  }

  @HiddenApi
  @Implementation
  public Uri getUri() {
    return reflector(IconReflector.class, realIcon).getUri();
  }

  @HiddenApi
  @Implementation
  public int getDataLength() {
    return reflector(IconReflector.class, realIcon).getDataLength();
  }

  @HiddenApi
  @Implementation
  public int getDataOffset() {
    return reflector(IconReflector.class, realIcon).getDataOffset();
  }

  @HiddenApi
  @Implementation
  public byte[] getDataBytes() {
    return reflector(IconReflector.class, realIcon).getDataBytes();
  }

  @Implementation
  protected void loadDrawableAsync(Context context, Message andThen) {
    if (executorOverride != null) {
      executorOverride.execute(
          () -> {
            andThen.obj = realIcon.loadDrawable(context);
            andThen.sendToTarget();
          });
    } else {
      reflector(IconReflector.class, realIcon).loadDrawableAsync(context, andThen);
    }
  }

  @Implementation
  protected void loadDrawableAsync(
      Context context, final OnDrawableLoadedListener listener, Handler handler) {
    if (executorOverride != null) {
      executorOverride.execute(
          () -> {
            Drawable result = realIcon.loadDrawable(context);
            handler.post(() -> listener.onDrawableLoaded(result));
          });
    } else {
      reflector(IconReflector.class, realIcon).loadDrawableAsync(context, listener, handler);
    }
  }

  @ForType(Icon.class)
  interface IconReflector {

    @Direct
    int getType();

    @Direct
    int getResId();

    @Direct
    Bitmap getBitmap();

    @Direct
    Uri getUri();

    @Direct
    int getDataLength();

    @Direct
    int getDataOffset();

    @Direct
    byte[] getDataBytes();

    @Direct
    void loadDrawableAsync(Context context, Message andThen);

    @Direct
    void loadDrawableAsync(
        Context context, final OnDrawableLoadedListener listener, Handler handler);
  }
}
