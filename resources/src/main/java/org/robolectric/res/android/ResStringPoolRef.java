package org.robolectric.res.android;

/**
 * Reference to a string in a string pool.
 */
class ResStringPoolRef {
    // Index into the string pool table (uint32_t-offset from the indices
    // immediately after ResStringPool_header) at which to find the location
    // of the string data in the pool.
    int index;

    ResStringPoolRef(int index) {
        this.index = index;
    }
};
