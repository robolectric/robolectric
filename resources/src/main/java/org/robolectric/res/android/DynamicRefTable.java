package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h

import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.UNKNOWN_ERROR;
import static org.robolectric.res.android.ResTable.APP_PACKAGE_ID;
import static org.robolectric.res.android.ResTable.Res_GETPACKAGE;
import static org.robolectric.res.android.ResTable.SYS_PACKAGE_ID;
import static org.robolectric.res.android.Util.ALOGW;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.robolectric.res.android.ResourceTypes.Res_value;

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
    this.mAssignedPackageId = packageId;
    this.mAppAsLib = appAsLib;

    mLookupTable[APP_PACKAGE_ID] = APP_PACKAGE_ID;
    mLookupTable[SYS_PACKAGE_ID] = SYS_PACKAGE_ID;
  }

//  // Loads an unmapped reference table from the package.
//  Errors load(final ResTable_lib_header header) {
//    return null;
//  }

  // Adds mappings from the other DynamicRefTable
  int addMappings(final DynamicRefTable other) {
    if (mAssignedPackageId != other.mAssignedPackageId) {
      return UNKNOWN_ERROR;
    }

//    final int entryCount = other.mEntries.size();
//    for (size_t i = 0; i < entryCount; i++) {
//      ssize_t index = mEntries.indexOfKey(other.mEntries.keyAt(i));
//      if (index < 0) {
//        mEntries.add(other.mEntries.keyAt(i), other.mEntries[i]);
//      } else {
//        if (other.mEntries[i] != mEntries[index]) {
//          return UNKNOWN_ERROR;
//        }
//      }
//    }
    for (Entry<String, Byte> otherEntry : other.mEntries.entrySet()) {
      String key = otherEntry.getKey();
      Byte curValue = mEntries.get(key);
      if (curValue == null) {
        mEntries.put(key, otherEntry.getValue());
      } else {
        if (!Objects.equals(otherEntry.getValue(), curValue)) {
          return UNKNOWN_ERROR;
        }
      }
    }

    // Merge the lookup table. No entry can conflict
    // (value of 0 means not set).
    for (int i = 0; i < 256; i++) {
      if (mLookupTable[i] != other.mLookupTable[i]) {
        if (mLookupTable[i] == 0) {
          mLookupTable[i] = other.mLookupTable[i];
        } else if (other.mLookupTable[i] != 0) {
          return UNKNOWN_ERROR;
        }
      }
    }
    return NO_ERROR;
  }

  // Creates a mapping from build-time package ID to run-time package ID for
  // the given package.
  int addMapping(final String packageName, byte packageId) {
    Byte index = mEntries.get(packageName);
    if (index == null) {
      return UNKNOWN_ERROR;
    }
    mLookupTable[index] = packageId;
    return NO_ERROR;
  }

//  // Performs the actual conversion of build-time resource ID to run-time
//  // resource ID.
  int lookupResourceId(Ref<Integer> resId) {
    int res = resId.get();
    int packageId = Res_GETPACKAGE(res) + 1;

    if (packageId == APP_PACKAGE_ID && !mAppAsLib) {
      // No lookup needs to be done, app package IDs are absolute.
      return NO_ERROR;
    }

    if (packageId == 0 || (packageId == APP_PACKAGE_ID && mAppAsLib)) {
      // The package ID is 0x00. That means that a shared library is accessing
      // its own local resource.
      // Or if app resource is loaded as shared library, the resource which has
      // app package Id is local resources.
      // so we fix up those resources with the calling package ID.
      resId.set((0xFFFFFF & (resId.get())) | (((int) mAssignedPackageId) << 24));
      return NO_ERROR;
    }

    // Do a proper lookup.
    int translatedId = mLookupTable[packageId];
    if (translatedId == 0) {
      ALOGW("DynamicRefTable(0x%02x): No mapping for build-time package ID 0x%02x.",
          mAssignedPackageId, packageId);
      for (int i = 0; i < 256; i++) {
        if (mLookupTable[i] != 0) {
          ALOGW("e[0x%02x] . 0x%02x", i, mLookupTable[i]);
        }
      }
      return UNKNOWN_ERROR;
    }

    resId.set((res & 0x00ffffff) | (((int) translatedId) << 24));
    return NO_ERROR;
  }
//
  int lookupResourceValue(Ref<Res_value> value) {
    byte resolvedType = DataType.REFERENCE.code();
    Res_value inValue = value.get();
    switch (DataType.fromCode(inValue.dataType)) {
      case ATTRIBUTE:
        resolvedType = DataType.ATTRIBUTE.code();
        // fallthrough
      case REFERENCE:
        if (!mAppAsLib) {
          return NO_ERROR;
        }

        // If the package is loaded as shared library, the resource reference
        // also need to be fixed.
        break;
      case DYNAMIC_ATTRIBUTE:
        resolvedType = DataType.ATTRIBUTE.code();
        // fallthrough
      case DYNAMIC_REFERENCE:
        break;
      default:
        return NO_ERROR;
    }

    final Ref<Integer> resIdRef = new Ref<>(inValue.data);
    int err = lookupResourceId(resIdRef);
    value.set(inValue.withData(resIdRef.get()));
    if (err != NO_ERROR) {
      return err;
    }

    value.set(new Res_value(resolvedType, resIdRef.get()));
    return NO_ERROR;
 }

  public Map<String, Byte> entries() {
    return mEntries;
  }

  //
//  final KeyedVector<String16, uint8_t>& entries() final {
//  return mEntries;
//}
//
//  private:
    final byte                   mAssignedPackageId;
  final byte[]                         mLookupTable = new byte[256];
  final Map<String, Byte> mEntries = new HashMap<>();
  boolean                            mAppAsLib;
};
