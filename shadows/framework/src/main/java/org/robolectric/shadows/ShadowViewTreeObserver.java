package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.ViewTreeObserver;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ViewTreeObserver}. */
@Implements(ViewTreeObserver.class)
public class ShadowViewTreeObserver {
  @RealObject private ViewTreeObserver realViewTreeObserver;
  List<ViewTreeObserver.OnGlobalLayoutListener> onGlobalLayoutListeners = new ArrayList<>();

  @Implementation
  public void addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener listener) {
    reflector(ViewTreeObserverReflector.class, realViewTreeObserver)
        .addOnGlobalLayoutListener(listener);
    onGlobalLayoutListeners.add(listener);
  }

  @Implementation
  public void removeOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener victim) {
    reflector(ViewTreeObserverReflector.class, realViewTreeObserver)
        .removeOnGlobalLayoutListener(victim);
    onGlobalLayoutListeners.remove(victim);
  }

  /**
   * Gets the current list of {@link ViewTreeObserver.OnGlobalLayoutListener} currently registered
   * in the {@link ViewTreeObserver}.
   */
  public ImmutableList<ViewTreeObserver.OnGlobalLayoutListener> getOnGlobalLayoutListeners() {
    return ImmutableList.copyOf(onGlobalLayoutListeners);
  }

  @ForType(ViewTreeObserver.class)
  interface ViewTreeObserverReflector {
    @Direct
    void addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener listener);

    @Direct
    void removeOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener victim);
  }
}
