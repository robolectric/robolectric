package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import java.util.Arrays;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {

  @RealObject StateListDrawable realStateListDrawable;

  /** Preserves resource IDs when Android clones a selector's constant state. */
  @Filter(order = Order.AFTER)
  protected void __constructor__(
      @ClassName("android.graphics.drawable.StateListDrawable$StateListState") Object state,
      Resources resources) {
    if (state != null) {
      DrawableContainerState originalState = (DrawableContainerState) state;
      copyResourceId(
          reflector(DrawableContainerStateReflector.class, originalState).getOwner(),
          realStateListDrawable);
      copyChildResourceIds(
          originalState,
          reflector(StateListDrawableReflector.class, realStateListDrawable).getStateListState());
    }
  }

  /** Runs Android mutation while retaining resource IDs on recreated children. */
  @Implementation
  protected Drawable mutate() {
    StateListDrawableReflector stateList =
        reflector(StateListDrawableReflector.class, realStateListDrawable);
    DrawableContainerState originalState = stateList.getStateListState();
    Drawable result = stateList.mutate();
    DrawableContainerState state = stateList.getStateListState();
    if (originalState != null && originalState != state) {
      copyChildResourceIds(originalState, state);
    }
    return result;
  }

  /**
   * Returns the drawable associated with an exact state set.
   *
   * @param stateSet Int array describing the state
   * @return Drawable associated with the state, or {@code null} if none matches
   */
  public Drawable getDrawableForState(int[] stateSet) {
    for (int i = realStateListDrawable.getStateCount() - 1; i >= 0; i--) {
      if (Arrays.equals(stateSet, realStateListDrawable.getStateSet(i))) {
        return realStateListDrawable.getStateDrawable(i);
      }
    }
    return null;
  }

  private static void copyChildResourceIds(
      DrawableContainerState original, DrawableContainerState copy) {
    for (int i = 0; i < original.getChildCount(); i++) {
      copyResourceId(original.getChild(i), copy.getChild(i));
    }
  }

  private static void copyResourceId(Drawable original, Drawable copy) {
    Object originalShadow = Shadow.extract(original);
    Object copyShadow = Shadow.extract(copy);
    if (originalShadow instanceof ShadowDrawable && copyShadow instanceof ShadowDrawable) {
      ((ShadowDrawable) copyShadow).createdFromResId =
          ((ShadowDrawable) originalShadow).createdFromResId;
    }
  }

  @ForType(StateListDrawable.class)
  interface StateListDrawableReflector {
    @Accessor("mStateListState")
    DrawableContainerState getStateListState();

    @Direct
    Drawable mutate();
  }

  @ForType(DrawableContainerState.class)
  interface DrawableContainerStateReflector {
    @Accessor("mOwner")
    DrawableContainer getOwner();
  }
}
