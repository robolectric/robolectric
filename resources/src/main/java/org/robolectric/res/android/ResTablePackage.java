package org.robolectric.res.android;

/**
 * A collection of resource data types within a package.  Followed by
 * one or more ResTable_type and ResTable_typeSpec structures containing the
 * entry values for each resource type.
 */
class ResTablePackage {
    ResChunkHeader header;

    // If this is a base package, its ID.  Package IDs start
    // at 1 (corresponding to the value of the package bits in a
    // resource identifier).  0 means this is not a base package.
    int id;

    // Actual name of this package, \0-terminated.
    char[] name = new char[128];

    // Offset to a ResStringPool_header defining the resource
    // type symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    int typeStrings;

    // Last index into typeStrings that is for public use by others.
    int lastPublicType;

    // Offset to a ResStringPool_header defining the resource
    // key symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    int keyStrings;

    // Last index into keyStrings that is for public use by others.
    int lastPublicKey;

    int typeIdOffset;
};
