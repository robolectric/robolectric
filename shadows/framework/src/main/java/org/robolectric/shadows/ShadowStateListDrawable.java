package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {
  private final Map<Integer, Integer> stateToResource = new HashMap<>();
  private final Map<List<Integer>, Drawable> stateToDrawable = new HashMap<>();

  public void addState(int stateId, int resId) {
    stateToResource.put(stateId, resId);
  }

  public int getResourceIdForState(int stateId) {
    return stateToResource.get(stateId);
  }

  @Implementation
  protected void addState(int[] stateSet, Drawable drawable) {
    stateToDrawable.put(createStateList(stateSet), drawable);
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
}
