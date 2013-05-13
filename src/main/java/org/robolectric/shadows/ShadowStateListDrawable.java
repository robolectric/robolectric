package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.StateSet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {

  private Map<Integer, Integer> stateToResource = new HashMap<Integer, Integer>();
  private Map<List<Integer>, Drawable> stateToDrawable = new HashMap<List<Integer>, Drawable>();

  public void addState(int stateId, int resId) {
    stateToResource.put(stateId, resId);
  }

  public int getResourceIdForState(int stateId) {
    return stateToResource.get(stateId);
  }

  @Implementation
  public void addState(int[] stateSet, Drawable drawable) {
    stateToDrawable.put(createStateList(stateSet), drawable);
  }

  @Implementation
  public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
    // todo 2.0-cleanup
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
    List<Integer> stateList = new ArrayList<Integer>();
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
