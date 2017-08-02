package org.robolectric.res.android;

/**
 * A specification of the resources defined by a particular type.
 * <p>
 * There should be one of these chunks for each resource type.
 * <p>
 * This structure is followed by an array of integers providing the set of
 * configuration change flags (ResTable_config::CONFIG_*) that have multiple
 * resources for that configuration.  In addition, the high bit is set if that
 * resource has been made public.
 */
class ResTableTypeSpec {
    ResChunkHeader header;

    // The type identifier this chunk is holding.  Type IDs start
    // at 1 (corresponding to the value of the type bits in a
    // resource identifier).  0 is invalid.
    byte id;

    // Must be 0.
    byte res0;
    // Must be 0.
    short res1;

    // Number of uint32_t entry configuration masks that follow.
    int entryCount;

    // Additional flag indicating an entry is public.
    int SPEC_PUBLIC = 0x40000000;
}