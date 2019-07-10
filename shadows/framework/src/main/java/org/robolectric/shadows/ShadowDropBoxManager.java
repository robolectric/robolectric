package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;

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
   * <p>The existing {@link DropBoxManager#addData} and {@link DropBoxManager#addFile} methods in
   * DropBoxManager are not shadowed (and do not work), but {@link DropBoxManager#addText} is. This
   * method is a convenience for quickly adding multiple historical entries. The entries can be
   * added in any order since this shadow will sort the entries by the specified timestamp.
   *
   * <p>The flag will be set to {@link DropBoxManager#IS_TEXT} so that {@link
   * DropBoxManager.Entry#getText} can be used.
   *
   * @param tag can be any arbitrary string
   * @param timestamp a unique timestamp for the entry, relative to {@link
   *     System#currentTimeMillis()}
   * @param data must not be null
   */
  public void addData(String tag, long wallTimestamp, byte[] data) {
    if (entries.containsKey(wallTimestamp)) {
      throw new AssertionError("Cannot add multiple entries with the exact same timestamp.");
    }
    entries.put(
        wallTimestamp, new DropBoxManager.Entry(tag, wallTimestamp, data, DropBoxManager.IS_TEXT));
  }

  /**
   * Adds a text entry to dropbox with the current timestamp using UTF-8 encoding.
   *
   * <p>If adding multiple entries, it is required to ensure they have unique timestamps by bumping
   * the wall-clock time, using {@link android.os.SystemClock} or similar.
   */
  @Implementation
  protected void addText(String tag, String data) {
    // NOTE: Need to use ShadowSystemClock for current time, because this doesn't run in the
    // ClassLoader that customizes System.currentTimeMillis.
    addData(tag, ShadowSystem.currentTimeMillis(), data.getBytes(UTF_8));
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
