package org.robolectric.shadows;

import android.os.DropBoxManager;
import android.os.DropBoxManager.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Fake dropbox manager that starts with no entries. */
@Implements(value = DropBoxManager.class)
public class ShadowDropBoxManager {
  private final SortedMap<Long, Entry> entries = new TreeMap<>();

  public ShadowDropBoxManager() {
    reset();
  }

  /**
   * Adds entry to the DropboxManager with the flag indicating data is text.
   *
   * <p>The existing {@link DropBoxManager#addData}, {@link DropBoxManager#addFile}, and {@link
   * DropBoxManager#addText} methods in DropBoxManager are not shadowed. This method is a
   * convenience for quickly adding multiple historical entries. The entries can be added in any
   * order since this shadow will sort the entries by the specified timestamp.
   *
   * <p>The flag will be set to {@link DropBoxManager#IS_TEXT} so that {@link
   * DropBoxManager.Entry#getText} can be used.
   *
   * @param tag can be any arbitrary string
   * @param timestamp is an arbitrary timestamp that must be unique from all other entries
   * @param data must not be null
   */
  void addData(String tag, long timestamp, byte[] data) {
    entries.put(timestamp, new DropBoxManager.Entry(tag, timestamp, data, DropBoxManager.IS_TEXT));
  }

  /**
   * Clears all entries.
   */
  public void reset() {
    entries.clear();
  }

  @Implementation
  protected DropBoxManager.Entry getNextEntry(String tag, long millis) {
    for (DropBoxManager.Entry entry : entries.tailMap(millis).values()) {
      if ((tag != null && !entry.getTag().equals(tag)) || entry.getTimeMillis() <= millis) {
        continue;
      }
      return entry;
    }
    return null;
  }
}
