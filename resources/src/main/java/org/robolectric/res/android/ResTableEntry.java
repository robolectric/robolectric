package org.robolectric.res.android;

/**
 * This is the beginning of information about an entry in the resource table. It holds the reference
 * to the name of this entry, and is immediately followed by one of:
 *
 * <ul>
 *   <li>A Res_value structure, if FLAG_COMPLEX is -not- set.
 *   <li>An array of ResTable_map structures, if FLAG_COMPLEX is set. These supply a set of
 *       name/value mappings of data.
 * </ul>
 *
 * <p>frameworks/base/include/androidfw/ResourceTypes.h (struct ResTable_entry)
 */
public class ResTableEntry {
  public static final int FLAG_COMPLEX = 0x0001;
  public static final int FLAG_PUBLIC = 0x0002;
  public static final int FLAG_WEAK = 0x0004;

  public final short size;
  public final int key;
  public final int flags;
  public final ResValue value;

  public ResTableEntry(short size, int flags, int key, ResValue value) {
    this.size = size;
    this.key = key;
    this.flags = flags;
    this.value = value;
  }
}
