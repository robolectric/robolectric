package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {

  @RealObject StateListDrawable realStateListDrawable;

  private final Map<List<Integer>, Drawable> stateToDrawable = new HashMap<>();

  @Implementation
  protected void addState(int[] stateSet, Drawable drawable) {
    stateToDrawable.put(createStateList(stateSet), drawable);
    reflector(StateListDrawableReflector.class, realStateListDrawable).addState(stateSet, drawable);
  }

  /**
   * Non Android accessor to retrieve drawable added for a specific state.
   *
   * @param stateSet Int array describing the state
   * @return Drawable added via {@link #addState(int[], android.graphics.drawable.Drawable)}
   */
  public Drawable getDrawableForState(int[] stateSet) {
    return stateToDrawable.get(createStateList(stateSet));
  }

  private List<Integer> createStateList(int[] stateSet) {
    List<Integer> stateList = new ArrayList<>();
    if (stateSet == StateSet.WILD_CARD) {
      stateList.add(-1);
    } else {
      for (int state : stateSet) {
        stateList.add(state);
      }
    }

    return stateList;
  }

  @ForType(StateListDrawable.class)
  interface StateListDrawableReflector {
    @Direct
    void addState(int[] stateSet, Drawable drawable);
  }
}
