package org.robolectric.shadows;

import android.content.IntentFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.robolectric.Robolectric.shadowOf_;

/**
 * Shadow of {@code IntentFilter} implemented with a {@link java.util.List}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentFilter.class)
public class ShadowIntentFilter {
  @Implementation
  public static IntentFilter create(String action, String dataType) {
    try {
      return new IntentFilter(action, dataType);
    } catch (IntentFilter.MalformedMimeTypeException e) {
      throw new RuntimeException("Bad MIME type", e);
    }
  }

  List<String> actions = new ArrayList<String>();
  List<String> schemes = new ArrayList<String>();
  List<IntentFilter.AuthorityEntry> authoritites = new ArrayList<IntentFilter.AuthorityEntry>();
  List<String> categories = new ArrayList<String>();

  public void __constructor__(String action) {
    actions.add(action);
  }

  public void __constructor__(String action, String dataType) {
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
  public boolean hasAction(String action) {
    return actions.contains(action);
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
  public boolean hasDataScheme(String scheme) {
    return schemes.contains(scheme);
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
  public String matchCategories(Set<String> categories){
    if (categories == null) {
      return null;
    }

    Iterator<String> it = categories.iterator();

    if (this.categories == null) {
      return it.hasNext() ? it.next() : null;
    }

    while (it.hasNext()) {
      final String category = it.next();
      if (!this.categories.contains(category)) {
        return category;
      }
    }

    return null;
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

  @Implements(IntentFilter.AuthorityEntry.class)
  public static class ShadowAuthorityEntry {
    private String host;
    private int port;

    public void __constructor__(String host, String port) {
      this.host = host;
      if (port == null) {
        this.port = -1;
      } else {
        this.port = Integer.parseInt(port);
      }
    }

    @Implementation
    public String getHost() {
      return host;
    }

    @Implementation
    public int getPort() {
      return port;
    }
  }
}
