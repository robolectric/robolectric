package org.robolectric.res.android;

/**
 * Representation of a ResTable entry.
 *
 * <p>frameworks/base/libs/androidfw/ResourceTypes.cpp (struct ResTable::Entry)
 */
public final class Entry {
  int configDensity; // config->density
  ResTableEntry entry;
  int specFlags;
  int packageHeaderIndex; // package->header->index from Package* package (== block number)
}
