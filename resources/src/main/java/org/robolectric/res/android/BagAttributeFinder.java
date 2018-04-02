package org.robolectric.res.android;

import org.robolectric.res.android.ResTable.bag_entry;

public class BagAttributeFinder {

  private final bag_entry[] bag_entries;
  private final int bagEndIndex;

  public BagAttributeFinder(bag_entry[] bag_entries, int bagEndIndex) {
    this.bag_entries = bag_entries;
    this.bagEndIndex = bagEndIndex;
  }

  public bag_entry find(int curIdent) {
    for (int curIndex = bagEndIndex - 1; curIndex >= 0; curIndex--) {
      if (bag_entries[curIndex].map.name.ident == curIdent) {
        return bag_entries[curIndex];
      }
    }
    return null;
  }
}
