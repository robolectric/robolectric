package org.robolectric.res.android;

/**
 * From ResChunk_header androidfw/include/androidfw/ResourceTypes.h
 */
public class ResChunkHeader {
    // Type identifier for this chunk.  The meaning of this value depends
    // on the containing chunk.
    public short type;

    // Size of the chunk header (in bytes).  Adding this value to
    // the address of the chunk allows you to find its associated data
    // (if any).
    public short headerSize;

    // Total size of this chunk (in bytes).  This is the chunkSize plus
    // the size of any data associated with the chunk.  Adding this value
    // to the chunk allows you to completely skip its contents (including
    // any child chunks).  If this value is the same as chunkSize, there is
    // no data associated with the chunk.
    public int size;

}
