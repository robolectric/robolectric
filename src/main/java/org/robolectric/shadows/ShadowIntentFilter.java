package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.IntentFilter;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code IntentFilter} implemented with a {@link java.util.List}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentFilter.class)
public class ShadowIntentFilter {

	List<String> actions = new ArrayList<String>();
    List<String> schemes = new ArrayList<String>();
    List<IntentFilter.AuthorityEntry> authoritites = new ArrayList<IntentFilter.AuthorityEntry>();
    List<String> categories = new ArrayList<String>();
    
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
    
    @Implementation
    public void addCategory( String category ) {
    	categories.add( category );
    }
    
    @Implementation
    public boolean hasCategory( String category ) {
    	return categories.contains( category );
    }
    
    @Implementation
    public Iterator<String> categoriesIterator() {
    	return categories.iterator();
    }
    
    @Implementation
    public String getCategory( int index ) {
    	return categories.get( index );
    }
    
    @Implementation
    public boolean matchCategories(Set<String> categories){
    	for( String category: this.categories ){
    		if( !categories.contains( category ) ){
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override @Implementation
    public boolean equals(Object o) {
        if (o == null) return false;
        o = shadowOf_(o);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowIntentFilter that = (ShadowIntentFilter) o;

        return actions.equals( that.actions ) && categories.equals( that.categories )
        		&& schemes.equals( that.schemes ) && authoritites.equals( that.authoritites );
    }

    @Override @Implementation
    public int hashCode() {
        int result = 13;
        result = 31 * result + actions.hashCode();
        result = 31 * result + categories.hashCode();
        result = 31 * result + schemes.hashCode();
        result = 31 * result + authoritites.hashCode();
        return result;
    }
}
