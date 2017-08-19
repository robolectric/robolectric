package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h

/**
 * Holds the shared library ID table. Shared libraries are assigned package IDs at
 * build time, but they may be loaded in a different order, so we need to maintain
 * a mapping of build-time package ID to run-time assigned package ID.
 *
 * Dynamic references are not currently supported in overlays. Only the base package
 * may have dynamic references.
 */
public class DynamicRefTable
{
  DynamicRefTable(byte packageId, boolean appAsLib) {
    
  }

//  // Loads an unmapped reference table from the package.
//  Errors load(final ResTable_lib_header header) {
//    return null;
//  }
//
//  // Adds mappings from the other DynamicRefTable
//  Errors addMappings(final DynamicRefTable& other) {
//
//  }
//
//  // Creates a mapping from build-time package ID to run-time package ID for
//  // the given package.
//  Errors addMapping(final String16& packageName, byte packageId) {
//
//  }
//
//  // Performs the actual conversion of build-time resource ID to run-time
//  // resource ID.
  int lookupResourceId(int resId) {
    // TODO: is this correct?
    return Errors.NO_ERROR;
  }
//
  int lookupResourceValue(ResValue value) {
    // TODO: is this correct?
    return Errors.NO_ERROR;
 }
//
//  final KeyedVector<String16, uint8_t>& entries() final {
//  return mEntries;
//}
//
//  private:
//    final uint8_t                   mAssignedPackageId;
//  uint8_t                         mLookupTable[256];
//  KeyedVector<String16, uint8_t>  mEntries;
//  bool                            mAppAsLib;
};
