package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.SurfaceControl;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

/** Builder for {@link DragEvent}. */
public class DragEventBuilder {
  private int action;
  private float x;
  private float y;
  @Nullable private Object localState;
  @Nullable private ClipDescription clipDescription;
  @Nullable private ClipData clipData;
  private boolean result;

  private DragEventBuilder() {}

  public static DragEventBuilder newBuilder() {
    return new DragEventBuilder();
  }

  public DragEventBuilder setAction(int action) {
    this.action = action;
    return this;
  }

  public DragEventBuilder setX(float x) {
    this.x = x;
    return this;
  }

  public DragEventBuilder setY(float y) {
    this.y = y;
    return this;
  }

  public DragEventBuilder setLocalState(@Nullable Object localState) {
    this.localState = localState;
    return this;
  }

  public DragEventBuilder setClipDescription(@Nullable ClipDescription clipDescription) {
    this.clipDescription = clipDescription;
    return this;
  }

  public DragEventBuilder setClipData(@Nullable ClipData clipData) {
    this.clipData = clipData;
    return this;
  }

  public DragEventBuilder setResult(boolean result) {
    this.result = result;
    return this;
  }

  public DragEvent build() {
    int api = RuntimeEnvironment.getApiLevel();
    if (api <= M) {
      return reflector(DragEventReflector.class)
          .obtain(action, x, y, localState, clipDescription, clipData, result);
    } else if (api <= R) {
      return reflector(DragEventReflector.class)
          .obtain(action, x, y, localState, clipDescription, clipData, null, result);
    } else if (api <= UPSIDE_DOWN_CAKE) {
      return reflector(DragEventReflector.class)
          .obtain(action, x, y, 0f, 0f, localState, clipDescription, clipData, null, null, result);
    } else if (api <= VANILLA_ICE_CREAM) {
      return reflector(DragEventReflector.class)
          .obtain(
              action, x, y, 0f, 0f, 0, localState, clipDescription, clipData, null, null, result);
    } else if (api <= CINNAMON_BUN) {
      return reflector(DragEventReflector.class)
          .obtain(
              action,
              x,
              y,
              0f,
              0f,
              ShadowDisplay.getDefaultDisplay().getDisplayId(),
              0,
              localState,
              clipDescription,
              clipData,
              null,
              null,
              result);
    } else {
      return reflector(DragEventReflector.class)
          .obtain(
              action,
              x,
              y,
              0f,
              0f,
              ShadowDisplay.getDefaultDisplay().getDisplayId(),
              0,
              0,
              0,
              localState,
              clipDescription,
              clipData,
              null,
              null,
              result);
    }
  }

  @ForType(DragEvent.class)
  interface DragEventReflector {
    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        boolean result);

    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        @WithType("com.android.internal.view.IDragAndDropPermissions") Object permissions,
        boolean result);

    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        float offsetX,
        float offsetY,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        SurfaceControl surfaceControl,
        @WithType("com.android.internal.view.IDragAndDropPermissions") Object permissions,
        boolean result);

    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        float offsetX,
        float offsetY,
        int displayId,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        SurfaceControl surfaceControl,
        @WithType("com.android.internal.view.IDragAndDropPermissions") Object permissions,
        boolean result);

    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        float offsetX,
        float offsetY,
        int displayId,
        int deviceId,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        SurfaceControl surfaceControl,
        @WithType("com.android.internal.view.IDragAndDropPermissions") Object permissions,
        boolean result);

    @Static
    DragEvent obtain(
        int action,
        float x,
        float y,
        float offsetX,
        float offsetY,
        int displayId,
        int deviceId,
        int flag1,
        int flag2,
        Object localState,
        ClipDescription clipDescription,
        ClipData clipData,
        SurfaceControl surfaceControl,
        @WithType("com.android.internal.view.IDragAndDropPermissions") Object permissions,
        boolean result);
  }
}
