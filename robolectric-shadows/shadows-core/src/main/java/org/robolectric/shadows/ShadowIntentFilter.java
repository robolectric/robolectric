package org.robolectric.shadows;

import android.content.IntentFilter;
import android.net.Uri;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Shadow for {@link android.content.IntentFilter}.
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

  List<String> actions = new ArrayList<>();
  List<String> schemes = new ArrayList<>();
  List<String> types = new ArrayList<>();
  List<IntentFilter.AuthorityEntry> authoritites = new ArrayList<>();
  List<String> categories = new ArrayList<>();
  int priority;

  public void __constructor__(String action) {
    actions.add(action);
  }

  public void __constructor__(String action, String dataType) {
    actions.add(action);
  }

  public void __constructor__(IntentFilter filter) {
    ShadowIntentFilter shadow = Shadows.shadowOf(filter);
    actions = new ArrayList<>(shadow.actions);
    schemes = new ArrayList<>(shadow.schemes);
    types = new ArrayList<>(shadow.types);
    authoritites = new ArrayList<>(shadow.authoritites);
    categories = new ArrayList<>(shadow.categories);
    priority = shadow.priority;
  }

  @Implementation
  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Implementation
  public int getPriority() {
    return priority;
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
  public void addDataType(String type) throws IntentFilter.MalformedMimeTypeException {
    final int slashpos = type.indexOf('/');
    final int typelen = type.length();
    if (slashpos > 0 && typelen >= slashpos+2) {
      if (typelen == slashpos+2 && type.charAt(slashpos+1) == '*') {
        String str = type.substring(0, slashpos);
        if (!types.contains(str)) {
          types.add(str.intern());
        }
      } else {
        if (!types.contains(type)) {
          types.add(type.intern());
        }
      }
      return;
    }
    throw new IntentFilter.MalformedMimeTypeException(type);
  }

  @Implementation
  public String getDataType(int index) {
    return types.get(index);
  }

  @Implementation
  public boolean hasDataType(String type) {
    final List<String> t = types;
    if (type == null) {
      return false;
    }
    if (t.contains(type)) {
      return true;
    }
    // Deal with an Intent wanting to match every type in the IntentFilter.
    final int typeLength = type.length();
    if (typeLength == 3 && type.equals("*/*")) {
      return !t.isEmpty();
    }
    // Deal with this IntentFilter wanting to match every Intent type.
    if (t.contains("*")) {
      return true;
    }
    final int slashpos = type.indexOf('/');
    if (slashpos > 0) {
      if (t.contains(type.substring(0, slashpos))) {
        return true;
      }
      if (typeLength == slashpos + 2 && type.charAt(slashpos + 1) == '*') {
        // Need to look through all types for one that matches
        // our base...
        final int numTypes = t.size();
        for (int i = 0; i < numTypes; i++) {
          final String v = t.get(i);
          if (type.regionMatches(0, v, 0, slashpos + 1)) {
            return true;
          }
        }
      }
    }
    return false;
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
    return categories.get(index);
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

  @Implementation
  public final int matchData(String type, String scheme, Uri data) {
    boolean schemeMatch = false;

    if (types.isEmpty() && schemes.isEmpty()) {
      if (type == null && data == null) {
        return IntentFilter.MATCH_CATEGORY_EMPTY + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
      } else {
        return IntentFilter.NO_MATCH_DATA;
      }
    }

    if (schemes.isEmpty()) {
      if (hasDataType(type)) {
        return IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
      } else {
        return IntentFilter.NO_MATCH_TYPE;
      }
    } else {
      if (hasDataScheme(scheme)) {
        if (!authoritites.isEmpty()) {
          if (matchDataAuthority(data) == IntentFilter.NO_MATCH_DATA) {
            return IntentFilter.NO_MATCH_DATA;
          }
        }
        schemeMatch = true;
      } else {
        return IntentFilter.NO_MATCH_DATA;
      }

      if (types.isEmpty()) {
        if (type != null) {
          return IntentFilter.NO_MATCH_TYPE;
        }
      } else {
        if (hasDataType(type)) {
          return IntentFilter.MATCH_CATEGORY_TYPE + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
        } else {
          return IntentFilter.NO_MATCH_TYPE;
        }
      }
    }
    return schemeMatch ? IntentFilter.MATCH_CATEGORY_SCHEME + IntentFilter.MATCH_ADJUSTMENT_NORMAL :
        IntentFilter.NO_MATCH_DATA;
  }

  @Implementation
  public final int matchDataAuthority(Uri data) {
    for (IntentFilter.AuthorityEntry entry : authoritites) {
      if (entry.getHost().equals(data.getHost())) {
        if (entry.getPort() != -1) {
          if (entry.getPort() == data.getPort()){
            return IntentFilter.MATCH_CATEGORY_HOST + IntentFilter.MATCH_CATEGORY_PORT
                + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
          }
        } else {
          return IntentFilter.MATCH_CATEGORY_HOST + IntentFilter.MATCH_ADJUSTMENT_NORMAL;
        }
      }
    }
    return IntentFilter.NO_MATCH_DATA;
  }

//  @Override @Implementation
//  public boolean equals(Object o) {
//    if (o == null) return false;
//    o = ShadowExtractor.extract(o);
//    if (o == null) return false;
//    if (this == o) return true;
//    if (getClass() != o.getClass()) return false;
//
//    ShadowIntentFilter that = (ShadowIntentFilter) o;
//
//    return actions.equals( that.actions ) && categories.equals( that.categories )
//        && schemes.equals( that.schemes ) && authoritites.equals( that.authoritites )
//        && types.equals( that.types );
//  }

  @Override @Implementation
  public int hashCode() {
    int result = 13;
    result = 31 * result + actions.hashCode();
    result = 31 * result + categories.hashCode();
    result = 31 * result + schemes.hashCode();
    result = 31 * result + authoritites.hashCode();
    result = 31 * result + types.hashCode();
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
