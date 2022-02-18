package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.SurfaceControl;
import androidx.annotation.Nullable;
import com.android.internal.view.IDragAndDropPermissions;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
      return ReflectionHelpers.callStaticMethod(
          DragEvent.class,
          "obtain",
          ClassParameter.from(int.class, action),
          ClassParameter.from(float.class, x),
          ClassParameter.from(float.class, y),
          ClassParameter.from(Object.class, localState),
          ClassParameter.from(ClipDescription.class, clipDescription),
          ClassParameter.from(ClipData.class, clipData),
          ClassParameter.from(boolean.class, result));
    } else if (api <= R) {
      return ReflectionHelpers.callStaticMethod(
          DragEvent.class,
          "obtain",
          ClassParameter.from(int.class, action),
          ClassParameter.from(float.class, x),
          ClassParameter.from(float.class, y),
          ClassParameter.from(Object.class, localState),
          ClassParameter.from(ClipDescription.class, clipDescription),
          ClassParameter.from(ClipData.class, clipData),
          ClassParameter.from(IDragAndDropPermissions.class, null),
          ClassParameter.from(boolean.class, result));
    } else {
      return ReflectionHelpers.callStaticMethod(
          DragEvent.class,
          "obtain",
          ClassParameter.from(int.class, action),
          ClassParameter.from(float.class, x),
          ClassParameter.from(float.class, y),
          ClassParameter.from(float.class, 0),
          ClassParameter.from(float.class, 0),
          ClassParameter.from(Object.class, localState),
          ClassParameter.from(ClipDescription.class, clipDescription),
          ClassParameter.from(ClipData.class, clipData),
          ClassParameter.from(SurfaceControl.class, null),
          ClassParameter.from(IDragAndDropPermissions.class, null),
          ClassParameter.from(boolean.class, result));
    }
  }
}
