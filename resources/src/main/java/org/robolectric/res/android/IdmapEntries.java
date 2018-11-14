package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.*;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceTypes.cpp
public class IdmapEntries {

  public boolean hasEntries() {
    if (mData == null) {
      return false;
    }

    return (Util.dtohs(mData[0]) > 0);
  }

//  int byteSize() {
//    if (mData == null) {
//      return 0;
//    }
//    short entryCount = Util.dtohs(mData[2]);
//    return (SIZEOF_SHORT * 4) + (SIZEOF_INT * static_cast<int>(entryCount));
//  }

  byte targetTypeId() {
    if (mData == null) {
      return 0;
    }
    return (byte) Util.dtohs(mData[0]);
  }

  public byte overlayTypeId() {
    if (mData == null) {
      return 0;
    }
    return (byte) Util.dtohs(mData[1]);
  }

  public int lookup(int entryId, Ref<Short> outEntryId) {
    short entryCount = Util.dtohs(mData[2]);
    short offset = Util.dtohs(mData[3]);

    if (entryId < offset) {
      // The entry is not present in this idmap
      return BAD_INDEX;
    }

    entryId -= offset;

    if (entryId >= entryCount) {
      // The entry is not present in this idmap
      return BAD_INDEX;
    }

    throw new UnsupportedOperationException("todo"); // todo

//    // It is safe to access the type here without checking the size because
//    // we have checked this when it was first loaded.
////        final int[] entries = reinterpret_cast<final uint32_t*>(mData) + 2;
//        final int[] entries = reinterpret_cast<final uint32_t*>(mData) + 2;
//    int mappedEntry = Util.dtohl(entries[entryId]);
//    if (mappedEntry == 0xffffffff) {
//      // This entry is not present in this idmap
//      return BAD_INDEX;
//    }
//        *outEntryId = static_cast<short>(mappedEntry);
//    return NO_ERROR;
  }

  private short[] mData;

}
