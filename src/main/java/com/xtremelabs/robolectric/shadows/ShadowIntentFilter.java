package com.xtremelabs.robolectric.shadows;

import android.content.IntentFilter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Shadow of {@code IntentFilter} implemented with a {@link java.util.List}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentFilter.class)
public class ShadowIntentFilter {
    List<String> actions = new ArrayList<String>();

    public void __constructor__(String action) {
        actions.add(action);
    }

    @Implementation
    public void addAction(String action) {
        actions.add(action);
    }

    @Implementation
    public String getAction(int index) {
        return actions.get(index);
    }

    @Implementation
    public Iterator<String> actionsIterator() {
        return actions.iterator();
    }

    @Implementation
    public boolean matchAction(String action) {
        return actions.contains(action);
    }
}
