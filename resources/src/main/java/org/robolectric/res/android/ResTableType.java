package org.robolectric.res.android;

/**
 * A collection of resource entries for a particular resource data
 * type.
 * <p>
 * If the flag FLAG_SPARSE is not set in `flags`, then this struct is
 * followed by an array of uint32_t defining the resource
 * values, corresponding to the array of type strings in the
 * ResTable_package::typeStrings string block. Each of these hold an
 * index from entriesStart; a value of NO_ENTRY means that entry is
 * not defined.
 * <p>
 * If the flag FLAG_SPARSE is set in `flags`, then this struct is followed
 * by an array of ResTable_sparseTypeEntry defining only the entries that
 * have values for this type. Each entry is sorted by their entry ID such
 * that a binary search can be performed over the entries. The ID and offset
 * are encoded in a uint32_t. See ResTabe_sparseTypeEntry.
 * <p>
 * There may be multiple of these chunks for a particular resource type,
 * supply different configuration variations for the resource values of
 * that type.
 * <p>
 * It would be nice to have an additional ordered index of entries, so
 * we can do a binary search if trying to find a resource by string name.
 *
 * ResTable_type from androidfw/include/androidfw/ResourceTypes.h
 */
public class ResTableType {
    public static final int NO_ENTRY = 0xFFFFFFFF;

    // If set, the entry is sparse, and encodes both the entry ID and offset into each entry,
    // and a binary search is used to find the key. Only available on platforms >= O.
    // Mark any types that use this with a v26 qualifier to prevent runtime issues on older
    // platforms.
    static final int FLAG_SPARSE = 0x01;

    public ResChunkHeader header;

    // The type identifier this chunk is holding.  Type IDs start
    // at 1 (corresponding to the value of the type bits in a
    // resource identifier).  0 is invalid.
    public byte id;

    public byte flags;

    // Must be 0.
    public short reserved;

    // Number of uint32_t entry indices that follow.
    public int entryCount;

    // Offset from header where ResTable_entry data starts.
    public int entriesStart;

    // Configuration this collection of entries is designed for. This must always be last.
    public ResTableConfig config;
};