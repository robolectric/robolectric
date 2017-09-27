package org.robolectric.res.android;

import org.robolectric.res.android.ResTable.Entry;
import org.robolectric.res.android.ResTable.Package;

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
  /** If set, this is a complex entry, holding a set of name/value
   mappings.  It is followed by an array of ResTable_map structures.
   */
  public static final int FLAG_COMPLEX = 0x0001;
  /** If set, this resource has been declared public, so libraries
   * are allowed to reference it.
   */
  public static final int FLAG_PUBLIC = 0x0002;
  /** If set, this is a weak resource and may be overriden by strong
   resources of the same name/type. This is only useful during
   linking with other resource tables.
   */
  public static final int FLAG_WEAK = 0x0004;

  public final int flags;

  /** Number of bytes in this structure. */
  public final short size;

  /** Reference into ResTable_package::keyStrings identifying this entry. */
  public final ResStringPoolRef key;

  /** Reference to ResValue. Only set if FLAG_COMPLEX is -not- set. */
  public final ResValue value;

  public ResTableEntry(short size, int flags, ResStringPoolRef key, ResValue value) {
    this.size = size;
    this.key = key;
    this.flags = flags;
    this.value = value;
  }
}
