package org.robolectric.res.android;

import java.util.List;

/**
 * Extended form of a ResTable_entry for map entries, defining a parent map resource from which to
 * inherit values.
 */
public final class ResTableMapEntry extends ResTableEntry {
  // Resource identifier of the parent mapping, or 0 if there is none.
  // This is always treated as a TYPE_DYNAMIC_REFERENCE.
  int parentIdent; // parent->ident
  // Number of name/value pairs that follow for FLAG_COMPLEX.
  int count;

  List<ResTableMap> array;

  ResTableMapEntry(List<ResTableMap> array) {
    this(array, 0);
  }

  ResTableMapEntry(List<ResTableMap> array, int parent) {
    this.flags = FLAG_COMPLEX;
    this.array = array;
    count = array.size();
    parentIdent = parent;
  }
}