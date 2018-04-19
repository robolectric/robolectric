package org.robolectric.shadows;

import android.media.MediaRouter;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MediaRouter.class)
public class ShadowMediaRouter {

  private final List<MyCallbackInfo> callbacks = new ArrayList<>();

  @Implementation
  public int getRouteCount() {
    return 0;
  }

  @Implementation
  public void addCallback(int types, MediaRouter.Callback cb, int flags) {
    MyCallbackInfo info;
    int index = findCallbackInfo(cb);
    if (index >= 0) {
      info = callbacks.get(index);
      info.type |= types;
      info.flags |= flags;
    } else {
      info = new MyCallbackInfo(cb, types, flags);
      callbacks.add(info);
    }
  }

  @Implementation
  public void removeCallback(MediaRouter.Callback cb) {
    int index = findCallbackInfo(cb);
    if (index >= 0) {
      callbacks.remove(index);
    }
  }

  public List<MediaRouter.Callback> getRegisteredCallbacks() {
    final ArrayList<MediaRouter.Callback> callbacks = new ArrayList<>(this.callbacks.size());
    for (final MyCallbackInfo callback : this.callbacks) {
      callbacks.add(callback.cb);
    }

    return callbacks;
  }

  private int findCallbackInfo(MediaRouter.Callback cb) {
    final int count = callbacks.size();
    for (int i = 0; i < count; i++) {
      final MyCallbackInfo info = callbacks.get(i);
      if (info.cb == cb) {
        return i;
      }
    }
    return -1;
  }

  private static class MyCallbackInfo {
    int type;
    int flags;
    final MediaRouter.Callback cb;

    MyCallbackInfo(MediaRouter.Callback cb, int type, int flags) {
      this.cb = cb;
      this.type = type;
      this.flags = flags;
    }
  }
}
