package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {

    private Map<Integer, Integer> stateToResource;
    private Map<List<Integer>, Drawable> stateToDrawable;

    public void __constructor__() {
        stateToResource = new HashMap<Integer, Integer>();
        stateToDrawable = new HashMap<List<Integer>, Drawable>();
    }

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
