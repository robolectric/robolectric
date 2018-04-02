package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.*;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
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

//  Errors setTo(final void* entryHeader, int size) {
//    if (reinterpret_cast<uintptr_t>(entryHeader) & 0x03) {
//      ALOGE("idmap: entry header is not word aligned");
//      return UNKNOWN_ERROR;
//    }
//
//    if (size < SIZEOF_SHORT * 4) {
//      ALOGE("idmap: entry header is too small (%u bytes)", (uint32_t) size);
//      return UNKNOWN_ERROR;
//    }
//
//        final short[] header = reinterpret_cast<final short*>(entryHeader);
//        final short targetTypeId = Util.dtohs(header[0]);
//        final short overlayTypeId = Util.dtohs(header[1]);
//    if (targetTypeId == 0 || overlayTypeId == 0 || targetTypeId > 255 || overlayTypeId > 255) {
//      ALOGE("idmap: invalid type map (%u -> %u)", targetTypeId, overlayTypeId);
//      return UNKNOWN_ERROR;
//    }
//
//    short entryCount = Util.dtohs(header[2]);
//    if (size < SIZEOF_INT * (entryCount + 2)) {
//      ALOGE("idmap: too small (%u bytes) for the number of entries (%u)",
//          (uint32_t) size, (uint32_t) entryCount);
//      return UNKNOWN_ERROR;
//    }
//    mData = header;
//    return NO_ERROR;
//  }

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
