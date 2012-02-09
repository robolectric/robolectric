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
    List<String> schemes = new ArrayList<String>();
    List<IntentFilter.AuthorityEntry> authoritites = new ArrayList<IntentFilter.AuthorityEntry>();

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
    public int countActions() {
        return actions.size();
    }

    @Implementation
    public Iterator<String> actionsIterator() {
        return actions.iterator();
    }

    @Implementation
    public boolean matchAction(String action) {
        return actions.contains(action);
    }

    @Implementation
    public void addDataAuthority(String host, String port) {
        authoritites.add(new IntentFilter.AuthorityEntry(host, port));
    }

    @Implementation
    public final  IntentFilter.AuthorityEntry getDataAuthority(int index) {
        return authoritites.get(index);
    }

    @Implementation
    public void addDataScheme(String scheme) {
        schemes.add(scheme);
    }

    @Implementation
    public String getDataScheme(int index) {
        return schemes.get(index);
    }
}
