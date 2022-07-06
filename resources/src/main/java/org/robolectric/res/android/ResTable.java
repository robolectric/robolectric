package org.robolectric.res.android;

import static com.google.common.primitives.UnsignedBytes.max;
import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.BAD_VALUE;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_MEMORY;
import static org.robolectric.res.android.Errors.UNKNOWN_ERROR;
import static org.robolectric.res.android.ResourceTypes.RES_STRING_POOL_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_LIBRARY_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_PACKAGE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_SPEC_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_TYPE;
import static org.robolectric.res.android.ResourceTypes.validate_chunk;
import static org.robolectric.res.android.Util.ALOGD;
import static org.robolectric.res.android.Util.ALOGE;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.LOG_FATAL_IF;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.htodl;
import static org.robolectric.res.android.Util.htods;
import static org.robolectric.res.android.Util.isTruthy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResTable_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_header;
import org.robolectric.res.android.ResourceTypes.ResTable_map;
import org.robolectric.res.android.ResourceTypes.ResTable_map_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_package;
import org.robolectric.res.android.ResourceTypes.ResTable_sparseTypeEntry;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.ResTable_typeSpec;
import org.robolectric.res.android.ResourceTypes.Res_value;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h
@SuppressWarnings("NewApi")
public class ResTable {

  private static final int IDMAP_MAGIC             = 0x504D4449;
  private static final int IDMAP_CURRENT_VERSION   = 0x00000001;

  static final int APP_PACKAGE_ID      = 0x7f;
  static final int SYS_PACKAGE_ID      = 0x01;

  static final boolean kDebugStringPoolNoisy = false;
  static final boolean kDebugXMLNoisy = false;
  static final boolean kDebugTableNoisy = false;
  static final boolean kDebugTableGetEntry = false;
  static final boolean kDebugTableSuperNoisy = false;
  static final boolean kDebugLoadTableNoisy = false;
  static final boolean kDebugLoadTableSuperNoisy = false;
  static final boolean kDebugTableTheme = false;
  static final boolean kDebugResXMLTree = false;
  static final boolean kDebugLibNoisy = false;

  private static final Object NULL = null;
  public static final bag_set SENTINEL_BAG_SET = new bag_set(1);

  final Semaphore mLock = new Semaphore(1);

  // Mutex that controls access to the list of pre-filtered configurations
  // to check when looking up entries.
  // When iterating over a bag, the mLock mutex is locked. While mLock is locked,
  // we do resource lookups.
  // Mutex is not reentrant, so we must use a different lock than mLock.
  final Object               mFilteredConfigLock = new Object();

  // type defined in Errors
  int mError;

  ResTable_config mParams;

  // Array of all resource tables.
  final List<Header>             mHeaders = new ArrayList<>();

  // Array of packages in all resource tables.
  final Map<Integer, PackageGroup> mPackageGroups = new HashMap<>();

  // Mapping from resource package IDs to indices into the internal
  // package array.
  final byte[]                     mPackageMap = new byte[256];

  byte                     mNextPackageId;

  static boolean Res_CHECKID(int resid) { return ((resid&0xFFFF0000) != 0);}
  static int Res_GETPACKAGE(int id) {
    return ((id>>24)-1);
  }
  public static int Res_GETTYPE(int id) {
    return (((id>>16)&0xFF)-1);
  }
  static int Res_GETENTRY(int id) {
    return (id&0xFFFF);
  }
  static int Res_MAKEARRAY(int entry) { return (0x02000000 | (entry&0xFFFF)); }
  static boolean Res_INTERNALID(int resid) { return ((resid&0xFFFF0000) != 0 && (resid&0xFF0000) == 0); }

  int getResourcePackageIndex(int resID)
  {
    return Res_GETPACKAGE(resID) + 1;
    //return mPackageMap[Res_GETPACKAGE(resID)+1]-1;
  }

  int getResourcePackageIndexFromPackage(byte packageID) {
    return ((int)mPackageMap[packageID])-1;
  }

  //  Errors add(final Object data, int size, final int cookie, boolean copyData) {
//    return addInternal(data, size, NULL, 0, false, cookie, copyData);
//  }
//
//  Errors add(final Object data, int size, final Object idmapData, int idmapDataSize,
//        final int cookie, boolean copyData, boolean appAsLib) {
//    return addInternal(data, size, idmapData, idmapDataSize, appAsLib, cookie, copyData);
//  }
//
//  Errors add(Asset asset, final int cookie, boolean copyData) {
//    final Object data = asset.getBuffer(true);
//    if (data == NULL) {
//      ALOGW("Unable to get buffer of resource asset file");
//      return UNKNOWN_ERROR;
//    }
//
//    return addInternal(data, static_cast<int>(asset.getLength()), NULL, false, 0, cookie,
//        copyData);
//  }

//  status_t add(Asset* asset, Asset* idmapAsset, const int32_t cookie=-1, bool copyData=false,
//      bool appAsLib=false, bool isSystemAsset=false);
  int add(
      Asset asset, Asset idmapAsset, final int cookie, boolean copyData,
      boolean appAsLib, boolean isSystemAsset) {
    final byte[] data = asset.getBuffer(true);
    if (data == NULL) {
      ALOGW("Unable to get buffer of resource asset file");
      return UNKNOWN_ERROR;
    }

    int idmapSize = 0;
    Object idmapData = NULL;
    if (idmapAsset != NULL) {
      idmapData = idmapAsset.getBuffer(true);
      if (idmapData == NULL) {
        ALOGW("Unable to get buffer of idmap asset file");
        return UNKNOWN_ERROR;
      }
      idmapSize = (int) idmapAsset.getLength();
    }

    return addInternal(data, (int) asset.getLength(),
        idmapData, idmapSize, appAsLib, cookie, copyData, isSystemAsset);
  }

  int add(ResTable src, boolean isSystemAsset)
  {
    mError = src.mError;

    for (int i=0; i < src.mHeaders.size(); i++) {
      mHeaders.add(src.mHeaders.get(i));
    }

    for (PackageGroup srcPg : src.mPackageGroups.values()) {
      PackageGroup pg = new PackageGroup(this, srcPg.name, srcPg.id,
          false /* appAsLib */, isSystemAsset || srcPg.isSystemAsset, srcPg.isDynamic);
      for (int j=0; j<srcPg.packages.size(); j++) {
        pg.packages.add(srcPg.packages.get(j));
      }

      for (Integer typeId : srcPg.types.keySet()) {
        List<Type> typeList = computeIfAbsent(pg.types, typeId, key -> new ArrayList<>());
        typeList.addAll(srcPg.types.get(typeId));
      }
      pg.dynamicRefTable.addMappings(srcPg.dynamicRefTable);
      pg.largestTypeId = max(pg.largestTypeId, srcPg.largestTypeId);
      mPackageGroups.put(pg.id, pg);
    }

//    memcpy(mPackageMap, src->mPackageMap, sizeof(mPackageMap));
    System.arraycopy(src.mPackageMap, 0, mPackageMap, 0, mPackageMap.length);

    return mError;
  }

  int addEmpty(final int cookie) {
    Header header = new Header(this);
    header.index = mHeaders.size();
    header.cookie = cookie;
    header.values.setToEmpty();
    header.ownedData = new byte[ResTable_header.SIZEOF];

    ByteBuffer buf = ByteBuffer.wrap(header.ownedData).order(ByteOrder.LITTLE_ENDIAN);
    ResChunk_header.write(buf, (short) RES_TABLE_TYPE, () -> {}, () -> {});

    ResTable_header resHeader = new ResTable_header(buf, 0);
//    resHeader.header.type = RES_TABLE_TYPE;
//    resHeader.header.headerSize = sizeof(ResTable_header);
//    resHeader.header.size = sizeof(ResTable_header);

    header.header = resHeader;
    mHeaders.add(header);
    return (mError=NO_ERROR);
  }

//  status_t addInternal(const void* data, size_t size, const void* idmapData, size_t idmapDataSize,
//      bool appAsLib, const int32_t cookie, bool copyData, bool isSystemAsset=false);
  int addInternal(byte[] data, int dataSize, final Object idmapData, int idmapDataSize,
      boolean appAsLib, final int cookie, boolean copyData, boolean isSystemAsset)
  {
    if (!isTruthy(data)) {
      return NO_ERROR;
    }

    if (dataSize < ResTable_header.SIZEOF) {
      ALOGE("Invalid data. Size(%d) is smaller than a ResTable_header(%d).",
          (int) dataSize, (int) ResTable_header.SIZEOF);
      return UNKNOWN_ERROR;
    }

    Header header = new Header(this);
    header.index = mHeaders.size();
    header.cookie = cookie;
    if (idmapData != NULL) {
      header.resourceIDMap = new int[idmapDataSize / 4];
      if (header.resourceIDMap == NULL) {
//        delete header;
        return (mError = NO_MEMORY);
      }
//      memcpy(header.resourceIDMap, idmapData, idmapDataSize);
//      header.resourceIDMapSize = idmapDataSize;
    }
    mHeaders.add(header);

    final boolean notDeviceEndian = htods((short) 0xf0) != 0xf0;

    if (kDebugLoadTableNoisy) {
      ALOGV("Adding resources to ResTable: data=%s, size=0x%x, cookie=%d, copy=%d " +
          "idmap=%s\n", data, dataSize, cookie, copyData, idmapData);
    }

    if (copyData || notDeviceEndian) {
      header.ownedData = data; // malloc(dataSize);
      if (header.ownedData == NULL) {
        return (mError=NO_MEMORY);
      }
//      memcpy(header.ownedData, data, dataSize);
      data = header.ownedData;
    }

    ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
//    header->header = (const ResTable_header*)data;
    header.header = new ResTable_header(buf, 0);
    header.size = dtohl(header.header.header.size);
    if (kDebugLoadTableSuperNoisy) {
      ALOGI("Got size 0x%x, again size 0x%x, raw size 0x%x\n", header.size,
          dtohl(header.header.header.size), header.header.header.size);
    }
    if (kDebugLoadTableNoisy) {
      ALOGV("Loading ResTable @%s:\n", header.header);
    }
    if (dtohs(header.header.header.headerSize) > header.size
        || header.size > dataSize) {
      ALOGW("Bad resource table: header size 0x%x or total size 0x%x is larger than data size 0x%x\n",
          (int)dtohs(header.header.header.headerSize),
          (int)header.size, (int)dataSize);
      return (mError=BAD_TYPE);
    }
    if (((dtohs(header.header.header.headerSize)|header.size)&0x3) != 0) {
      ALOGW("Bad resource table: header size 0x%x or total size 0x%x is not on an integer boundary\n",
          (int)dtohs(header.header.header.headerSize),
          (int)header.size);
      return (mError=BAD_TYPE);
    }
//    header->dataEnd = ((const uint8_t*)header->header) + header->size;
    header.dataEnd = header.size;

    // Iterate through all chunks.
    int curPackage = 0;

//    const ResChunk_header* chunk =
//      (const ResChunk_header*)(((const uint8_t*)header->header)
//    + dtohs(header->header->header.headerSize));
    ResChunk_header chunk =
      new ResChunk_header(buf, dtohs(header.header.header.headerSize));
    while (chunk != null && (chunk.myOffset()) <= (header.dataEnd -ResChunk_header.SIZEOF) &&
      (chunk.myOffset()) <= (header.dataEnd -dtohl(chunk.size))) {
    int err = validate_chunk(chunk, ResChunk_header.SIZEOF, header.dataEnd, "ResTable");
    if (err != NO_ERROR) {
      return (mError=err);
    }
    if (kDebugTableNoisy) {
      ALOGV("Chunk: type=0x%x, headerSize=0x%x, size=0x%x, pos=%s\n",
          dtohs(chunk.type), dtohs(chunk.headerSize), dtohl(chunk.size),
          (Object)((chunk.myOffset()) - (header.header.myOffset())));
    }
    final int csize = dtohl(chunk.size);
    final int ctype = dtohs(chunk.type);
    if (ctype == RES_STRING_POOL_TYPE) {
      if (header.values.getError() != NO_ERROR) {
        // Only use the first string chunk; ignore any others that
        // may appear.
        err = header.values.setTo(chunk.myBuf(), chunk.myOffset(), csize, false);
        if (err != NO_ERROR) {
          return (mError=err);
        }
      } else {
        ALOGW("Multiple string chunks found in resource table.");
      }
    } else if (ctype == RES_TABLE_PACKAGE_TYPE) {
      if (curPackage >= dtohl(header.header.packageCount)) {
        ALOGW("More package chunks were found than the %d declared in the header.",
            dtohl(header.header.packageCount));
        return (mError=BAD_TYPE);
      }

      if (parsePackage(
          new ResTable_package(chunk.myBuf(), chunk.myOffset()), header, appAsLib, isSystemAsset) != NO_ERROR) {
        return mError;
      }
      curPackage++;
    } else {
      ALOGW("Unknown chunk type 0x%x in table at 0x%x.\n",
          ctype,
          (chunk.myOffset()) - (header.header.myOffset()));
    }
    chunk = chunk.myOffset() + csize < header.dataEnd
        ? new ResChunk_header(chunk.myBuf(), chunk.myOffset() + csize)
        : null;
  }

    if (curPackage < dtohl(header.header.packageCount)) {
      ALOGW("Fewer package chunks (%d) were found than the %d declared in the header.",
          (int)curPackage, dtohl(header.header.packageCount));
      return (mError=BAD_TYPE);
    }
    mError = header.values.getError();
    if (mError != NO_ERROR) {
      ALOGW("No string values found in resource table!");
    }

    if (kDebugTableNoisy) {
      ALOGV("Returning from add with mError=%d\n", mError);
    }
    return mError;
  }

  public final int getResource(int resID, Ref<Res_value> outValue, boolean mayBeBag, int density,
      final Ref<Integer> outSpecFlags, Ref<ResTable_config> outConfig)
  {
    if (mError != NO_ERROR) {
      return mError;
    }
    final int p = getResourcePackageIndex(resID);
    final int t = Res_GETTYPE(resID);
    final int e = Res_GETENTRY(resID);
    if (p < 0) {
      if (Res_GETPACKAGE(resID)+1 == 0) {
        ALOGW("No package identifier when getting value for resource number 0x%08x", resID);
      } else {
        ALOGW("No known package when getting value for resource number 0x%08x", resID);
      }
      return BAD_INDEX;
    }

    if (t < 0) {
      ALOGW("No type identifier when getting value for resource number 0x%08x", resID);
      return BAD_INDEX;
    }
    final PackageGroup grp = mPackageGroups.get(p);
    if (grp == NULL) {
      ALOGW("Bad identifier when getting value for resource number 0x%08x", resID);
      return BAD_INDEX;
    }
    // Allow overriding density
    ResTable_config desiredConfig = mParams;
    if (density > 0) {
      desiredConfig.density = density;
    }
    Entry entry = new Entry();
    int err = getEntry(grp, t, e, desiredConfig, entry);
    if (err != NO_ERROR) {
      // Only log the failure when we're not running on the host as
      // part of a tool. The caller will do its own logging.
      return err;
    }

    if ((entry.entry.flags & ResTable_entry.FLAG_COMPLEX) != 0) {
      if (!mayBeBag) {
        ALOGW("Requesting resource 0x%08x failed because it is complex\n", resID);
      }
      return BAD_VALUE;
    }

//    const Res_value* value = reinterpret_cast<const Res_value*>(
//      reinterpret_cast<const uint8_t*>(entry.entry) + entry.entry->size);
    Res_value value = new Res_value(entry.entry.myBuf(), entry.entry.myOffset() + entry.entry.size);

//    outValue.size = dtohs(value.size);
//    outValue.res0 = value.res0;
//    outValue.dataType = value.dataType;
//    outValue.data = dtohl(value.data);
    outValue.set(value);

    // The reference may be pointing to a resource in a shared library. These
    // references have build-time generated package IDs. These ids may not match
    // the actual package IDs of the corresponding packages in this ResTable.
    // We need to fix the package ID based on a mapping.
    if (grp.dynamicRefTable.lookupResourceValue(outValue) != NO_ERROR) {
      ALOGW("Failed to resolve referenced package: 0x%08x", outValue.get().data);
      return BAD_VALUE;
    }

//    if (kDebugTableNoisy) {
//      size_t len;
//      printf("Found value: pkg=0x%x, type=%d, str=%s, int=%d\n",
//          entry.package.header.index,
//          outValue.dataType,
//          outValue.dataType == Res_value::TYPE_STRING ?
//              String8(entry.package.header.values.stringAt(outValue.data, &len)).string() :
//      "",
//          outValue.data);
//    }

    if (outSpecFlags != null) {
        outSpecFlags.set(entry.specFlags);
    }
    if (outConfig != null) {
        outConfig.set(entry.config);
    }
    return entry._package_.header.index;
  }

  public final int resolveReference(Ref<Res_value> value, int blockIndex,
      final Ref<Integer> outLastRef) {
    return resolveReference(value, blockIndex, outLastRef, null, null);
  }

  public final int resolveReference(Ref<Res_value> value, int blockIndex,
      final Ref<Integer> outLastRef, Ref<Integer> inoutTypeSpecFlags) {
    return resolveReference(value, blockIndex, outLastRef, inoutTypeSpecFlags, null);
  }

  public final int resolveReference(Ref<Res_value> value, int blockIndex,
      final Ref<Integer> outLastRef, Ref<Integer> inoutTypeSpecFlags,
      final Ref<ResTable_config> outConfig)
  {
    int count=0;
    while (blockIndex >= 0 && value.get().dataType == DataType.REFERENCE.code()
        && value.get().data != 0 && count < 20) {
      if (outLastRef != null) {
        outLastRef.set(value.get().data);
      }
      final Ref<Integer> newFlags = new Ref<>(0);
      final int newIndex = getResource(value.get().data, value, true, 0,
          newFlags, outConfig);
      if (newIndex == BAD_INDEX) {
        return BAD_INDEX;
      }
      if (kDebugTableTheme) {
        ALOGI("Resolving reference 0x%x: newIndex=%d, type=0x%x, data=0x%x\n",
            value.get().data, (int)newIndex, (int)value.get().dataType, value.get().data);
      }
      //printf("Getting reference 0x%08x: newIndex=%d\n", value.data, newIndex);
      if (inoutTypeSpecFlags != null) {
        inoutTypeSpecFlags.set(inoutTypeSpecFlags.get() | newFlags.get());
      }
      if (newIndex < 0) {
        // This can fail if the resource being referenced is a style...
        // in this case, just return the reference, and expect the
        // caller to deal with.
        return blockIndex;
      }
      blockIndex = newIndex;
      count++;
    }
    return blockIndex;
  }

  private interface Compare {
    boolean compare(ResTable_sparseTypeEntry a, ResTable_sparseTypeEntry b);
  }

  ResTable_sparseTypeEntry lower_bound(ResTable_sparseTypeEntry first, ResTable_sparseTypeEntry last,
                                       ResTable_sparseTypeEntry value,
                                       Compare comparator) {
    int count = (last.myOffset() - first.myOffset()) / ResTable_sparseTypeEntry.SIZEOF;
    int itOffset;
    int step;
    while (count > 0) {
      itOffset = first.myOffset();
      step = count / 2;
      itOffset += step * ResTable_sparseTypeEntry.SIZEOF;
      if (comparator.compare(new ResTable_sparseTypeEntry(first.myBuf(), itOffset), value)) {
        itOffset += ResTable_sparseTypeEntry.SIZEOF;
        first = new ResTable_sparseTypeEntry(first.myBuf(), itOffset);
      } else {
        count = step;
      }
    }
    return first;
  }


  private int getEntry(
      final PackageGroup packageGroup, int typeIndex, int entryIndex,
      final ResTable_config config,
      Entry outEntry)
  {
    final List<Type> typeList = getOrDefault(packageGroup.types, typeIndex, Collections.emptyList());
    if (typeList.isEmpty()) {
      ALOGV("Skipping entry type index 0x%02x because type is NULL!\n", typeIndex);
      return BAD_TYPE;
    }

    ResTable_type bestType = null;
    int bestOffset = ResTable_type.NO_ENTRY;
    Package bestPackage = null;
    int specFlags = 0;
    byte actualTypeIndex = (byte) typeIndex;
    ResTable_config bestConfig = null;
//    memset(&bestConfig, 0, sizeof(bestConfig));

    // Iterate over the Types of each package.
    final int typeCount = typeList.size();
    for (int i = 0; i < typeCount; i++) {
      final Type typeSpec = typeList.get(i);

      int realEntryIndex = entryIndex;
      int realTypeIndex = typeIndex;
      boolean currentTypeIsOverlay = false;

      // Runtime overlay packages provide a mapping of app resource
      // ID to package resource ID.
      if (typeSpec.idmapEntries.hasEntries()) {
        final Ref<Short> overlayEntryIndex = new Ref<>((short) 0);
        if (typeSpec.idmapEntries.lookup(entryIndex, overlayEntryIndex) != NO_ERROR) {
          // No such mapping exists
          continue;
        }
        realEntryIndex = overlayEntryIndex.get();
        realTypeIndex = typeSpec.idmapEntries.overlayTypeId() - 1;
        currentTypeIsOverlay = true;
      }

      // Check that the entry idx is within range of the declared entry count (ResTable_typeSpec).
      // Particular types (ResTable_type) may be encoded with sparse entries, and so their
      // entryCount do not need to match.
      if (((int) realEntryIndex) >= typeSpec.entryCount) {
        ALOGW("For resource 0x%08x, entry index(%d) is beyond type entryCount(%d)",
            Res_MAKEID(packageGroup.id - 1, typeIndex, entryIndex),
            entryIndex, ((int) typeSpec.entryCount));
        // We should normally abort here, but some legacy apps declare
        // resources in the 'android' package (old bug in AAPT).
        continue;
      }

      // Aggregate all the flags for each package that defines this entry.
      if (typeSpec.typeSpecFlags != null) {
        specFlags |= dtohl(typeSpec.typeSpecFlags[realEntryIndex]);
      } else {
        specFlags = -1;
      }

      List<ResTable_type> candidateConfigs = typeSpec.configs;

//      List<ResTable_type> filteredConfigs;
//      if (isTruthy(config) && Objects.equals(mParams, config)) {
//        // Grab the lock first so we can safely get the current filtered list.
//        synchronized (mFilteredConfigLock) {
//          // This configuration is equal to the one we have previously cached for,
//          // so use the filtered configs.
//
//          final TypeCacheEntry cacheEntry = packageGroup.typeCacheEntries.get(typeIndex);
//          if (i < cacheEntry.filteredConfigs.size()) {
//            if (isTruthy(cacheEntry.filteredConfigs.get(i))) {
//              // Grab a reference to the shared_ptr so it doesn't get destroyed while
//              // going through this list.
//              filteredConfigs = cacheEntry.filteredConfigs.get(i);
//
//              // Use this filtered list.
//              candidateConfigs = filteredConfigs;
//            }
//          }
//        }
//      }

      final int numConfigs = candidateConfigs.size();
      for (int c = 0; c < numConfigs; c++) {
        final ResTable_type thisType = candidateConfigs.get(c);
        if (thisType == NULL) {
          continue;
        }

        final ResTable_config thisConfig;
//        thisConfig.copyFromDtoH(thisType.config);
        thisConfig = ResTable_config.fromDtoH(thisType.config);

        // Check to make sure this one is valid for the current parameters.
        if (config != NULL && !thisConfig.match(config)) {
          continue;
        }

        // const uint32_t* const eindex = reinterpret_cast<const uint32_t*>(
        // reinterpret_cast<const uint8_t*>(thisType) + dtohs(thisType->header.headerSize));

        final int eindex = thisType.myOffset() + dtohs(thisType.header.headerSize);

        int thisOffset;

        // Check if there is the desired entry in this type.
        if (isTruthy(thisType.flags & ResTable_type.FLAG_SPARSE)) {
          // This is encoded as a sparse map, so perform a binary search.
          final ByteBuffer buf = thisType.myBuf();
          ResTable_sparseTypeEntry sparseIndices = new ResTable_sparseTypeEntry(buf, eindex);
          ResTable_sparseTypeEntry result = lower_bound(
              sparseIndices,
              new ResTable_sparseTypeEntry(buf, sparseIndices.myOffset() + dtohl(thisType.entryCount)),
              new ResTable_sparseTypeEntry(buf, realEntryIndex),
              (a, b) -> dtohs(a.idxOrOffset) < dtohs(b.idxOrOffset));
//          if (result == sparseIndices + dtohl(thisType.entryCount)
//              || dtohs(result.idx) != realEntryIndex) {
          if (result.myOffset() == sparseIndices.myOffset() + dtohl(thisType.entryCount)
              || dtohs(result.idxOrOffset) != realEntryIndex) {
            // No entry found.
            continue;
          }
          // Extract the offset from the entry. Each offset must be a multiple of 4
          // so we store it as the real offset divided by 4.
//          thisOffset = dtohs(result->offset) * 4u;
          thisOffset = dtohs(result.idxOrOffset) * 4;
        } else {
          if (realEntryIndex >= dtohl(thisType.entryCount)) {
            // Entry does not exist.
            continue;
          }
//          thisOffset = dtohl(eindex[realEntryIndex]);
          thisOffset = thisType.entryOffset(realEntryIndex);
        }

        if (thisOffset == ResTable_type.NO_ENTRY) {
          // There is no entry for this index and configuration.
          continue;
        }

        if (bestType != NULL) {
          // Check if this one is less specific than the last found.  If so,
          // we will skip it.  We check starting with things we most care
          // about to those we least care about.
          if (!thisConfig.isBetterThan(bestConfig, config)) {
            if (!currentTypeIsOverlay || thisConfig.compare(bestConfig) != 0) {
              continue;
            }
          }
        }

        bestType = thisType;
        bestOffset = thisOffset;
        bestConfig = thisConfig;
        bestPackage = typeSpec._package_;
        actualTypeIndex = (byte) realTypeIndex;

        // If no config was specified, any type will do, so skip
        if (config == NULL) {
          break;
        }
      }
    }

    if (bestType == NULL) {
      return BAD_INDEX;
    }

    bestOffset += dtohl(bestType.entriesStart);

//    if (bestOffset > (dtohl(bestType->header.size)-sizeof(ResTable_entry))) {
    if (bestOffset > (dtohl(bestType.header.size)- ResTable_entry.SIZEOF)) {
      ALOGW("ResTable_entry at 0x%x is beyond type chunk data 0x%x",
          bestOffset, dtohl(bestType.header.size));
      return BAD_TYPE;
    }
    if ((bestOffset & 0x3) != 0) {
      ALOGW("ResTable_entry at 0x%x is not on an integer boundary", bestOffset);
      return BAD_TYPE;
    }

//    const ResTable_entry* const entry = reinterpret_cast<const ResTable_entry*>(
//      reinterpret_cast<const uint8_t*>(bestType) + bestOffset);
    final ResTable_entry entry = new ResTable_entry(bestType.myBuf(),
        bestType.myOffset() + bestOffset);
    if (dtohs(entry.size) < ResTable_entry.SIZEOF) {
      ALOGW("ResTable_entry size 0x%x is too small", dtohs(entry.size));
      return BAD_TYPE;
    }
    
    if (outEntry != null) {
      outEntry.entry = entry;
      outEntry.config = bestConfig;
      outEntry.type = bestType;
      outEntry.specFlags = specFlags;
      outEntry._package_ = bestPackage;
      outEntry.typeStr = new StringPoolRef(bestPackage.typeStrings, actualTypeIndex - bestPackage.typeIdOffset);
      outEntry.keyStr = new StringPoolRef(bestPackage.keyStrings, dtohl(entry.key.index));
    }
    return NO_ERROR;
  }

  int parsePackage(ResTable_package pkg,
                                Header header, boolean appAsLib, boolean isSystemAsset)
  {
    int base = pkg.myOffset();
    int err = validate_chunk(pkg.header, ResTable_package.SIZEOF - 4 /*sizeof(pkg.typeIdOffset)*/,
      header.dataEnd, "ResTable_package");
    if (err != NO_ERROR) {
      return (mError=err);
    }

    final int pkgSize = dtohl(pkg.header.size);

    if (dtohl(pkg.typeStrings) >= pkgSize) {
      ALOGW("ResTable_package type strings at 0x%x are past chunk size 0x%x.",
          dtohl(pkg.typeStrings), pkgSize);
      return (mError=BAD_TYPE);
    }
    if ((dtohl(pkg.typeStrings)&0x3) != 0) {
      ALOGW("ResTable_package type strings at 0x%x is not on an integer boundary.",
          dtohl(pkg.typeStrings));
      return (mError=BAD_TYPE);
    }
    if (dtohl(pkg.keyStrings) >= pkgSize) {
      ALOGW("ResTable_package key strings at 0x%x are past chunk size 0x%x.",
          dtohl(pkg.keyStrings), pkgSize);
      return (mError=BAD_TYPE);
    }
    if ((dtohl(pkg.keyStrings)&0x3) != 0) {
      ALOGW("ResTable_package key strings at 0x%x is not on an integer boundary.",
          dtohl(pkg.keyStrings));
      return (mError=BAD_TYPE);
    }

    int id = dtohl(pkg.id);
    final Map<Byte, IdmapEntries> idmapEntries = new HashMap<>();

    if (header.resourceIDMap != NULL) {
//      byte targetPackageId = 0;
//      int err = parseIdmap(header.resourceIDMap, header.resourceIDMapSize, &targetPackageId, &idmapEntries);
//      if (err != NO_ERROR) {
//        ALOGW("Overlay is broken");
//        return (mError=err);
//      }
//      id = targetPackageId;
    }

    boolean isDynamic = false;
    if (id >= 256) {
//      LOG_ALWAYS_FATAL("Package id out of range");
      throw new IllegalStateException("Package id out of range");
//      return NO_ERROR;
    } else if (id == 0 || (id == 0x7f && appAsLib) || isSystemAsset) {
      // This is a library or a system asset, so assign an ID
      id = mNextPackageId++;
      isDynamic = true;
    }

    PackageGroup group = null;
    Package _package = new Package(this, header, pkg);
    if (_package == NULL) {
    return (mError=NO_MEMORY);
  }

//    err = package->typeStrings.setTo(base+dtohl(pkg->typeStrings),
//      header->dataEnd-(base+dtohl(pkg->typeStrings)));
    err = _package.typeStrings.setTo(pkg.myBuf(), base+dtohl(pkg.typeStrings),
      header.dataEnd -(base+dtohl(pkg.typeStrings)), false);
    if (err != NO_ERROR) {
//      delete group;
//      delete _package;
      return (mError=err);
    }

//    err = package->keyStrings.setTo(base+dtohl(pkg->keyStrings),
//      header->dataEnd-(base+dtohl(pkg->keyStrings)));
    err = _package.keyStrings.setTo(pkg.myBuf(), base+dtohl(pkg.keyStrings),
      header.dataEnd -(base+dtohl(pkg.keyStrings)), false);
    if (err != NO_ERROR) {
//      delete group;
//      delete _package;
      return (mError=err);
    }

    int idx = mPackageMap[id];
    if (idx == 0) {
      idx = mPackageGroups.size() + 1;

//      char[] tmpName = new char[pkg.name.length /*sizeof(pkg.name)/sizeof(pkg.name[0])*/];
//      strcpy16_dtoh(tmpName, pkg.name, sizeof(pkg.name)/sizeof(pkg.name[0]));
      group = new PackageGroup(this, new String(pkg.name), id, appAsLib, isSystemAsset, isDynamic);
      if (group == NULL) {
//        delete _package;
        return (mError=NO_MEMORY);
      }

      mPackageGroups.put(group.id, group);
//      if (err < NO_ERROR) {
//        return (mError=err);
//      }

      mPackageMap[id] = (byte) idx;

      // Find all packages that reference this package
//      int N = mPackageGroups.size();
//      for (int i = 0; i < N; i++) {
      for (PackageGroup packageGroup : mPackageGroups.values()) {
        packageGroup.dynamicRefTable.addMapping(
            group.name, (byte) group.id);
      }
    } else {
      group = mPackageGroups.get(idx - 1);
      if (group == NULL) {
        return (mError=UNKNOWN_ERROR);
      }
    }

    group.packages.add(_package);
//    if (err < NO_ERROR) {
//      return (mError=err);
//    }

    // Iterate through all chunks.
    ResChunk_header chunk =
      new ResChunk_header(pkg.myBuf(), pkg.myOffset() + dtohs(pkg.header.headerSize));
//      const uint8_t* endPos = ((const uint8_t*)pkg) + dtohs(pkg->header.size);
    final int endPos = (pkg.myOffset()) + pkg.header.size;
//    while (((const uint8_t*)chunk) <= (endPos-sizeof(ResChunk_header)) &&
//      ((const uint8_t*)chunk) <= (endPos-dtohl(chunk->size))) {
    while (chunk != null && (chunk.myOffset()) <= (endPos-ResChunk_header.SIZEOF) &&
      (chunk.myOffset()) <= (endPos-dtohl(chunk.size))) {
    if (kDebugTableNoisy) {
      ALOGV("PackageChunk: type=0x%x, headerSize=0x%x, size=0x%x, pos=%s\n",
          dtohs(chunk.type), dtohs(chunk.headerSize), dtohl(chunk.size),
          ((chunk.myOffset()) - (header.header.myOffset())));
    }
        final int csize = dtohl(chunk.size);
        final short ctype = dtohs(chunk.type);
    if (ctype == RES_TABLE_TYPE_SPEC_TYPE) {
            final ResTable_typeSpec typeSpec = new ResTable_typeSpec(chunk.myBuf(), chunk.myOffset());
      err = validate_chunk(typeSpec.header, ResTable_typeSpec.SIZEOF,
      endPos, "ResTable_typeSpec");
      if (err != NO_ERROR) {
        return (mError=err);
      }

            final int typeSpecSize = dtohl(typeSpec.header.size);
            final int newEntryCount = dtohl(typeSpec.entryCount);

      if (kDebugLoadTableNoisy) {
        ALOGI("TypeSpec off %s: type=0x%x, headerSize=0x%x, size=%s\n",
            (base-chunk.myOffset()),
        dtohs(typeSpec.header.type),
            dtohs(typeSpec.header.headerSize),
            typeSpecSize);
      }
      // look for block overrun or int overflow when multiplying by 4
      if ((dtohl(typeSpec.entryCount) > (Integer.MAX_VALUE/4 /*sizeof(int)*/)
          || dtohs(typeSpec.header.headerSize)+(4 /*sizeof(int)*/*newEntryCount)
          > typeSpecSize)) {
        ALOGW("ResTable_typeSpec entry index to %s extends beyond chunk end %s.",
            (dtohs(typeSpec.header.headerSize) + (4 /*sizeof(int)*/*newEntryCount)),
            typeSpecSize);
        return (mError=BAD_TYPE);
      }

      if (typeSpec.id == 0) {
        ALOGW("ResTable_type has an id of 0.");
        return (mError=BAD_TYPE);
      }

      if (newEntryCount > 0) {
        boolean addToType = true;
        byte typeIndex = (byte) (typeSpec.id - 1);
        IdmapEntries idmapEntry = idmapEntries.get(typeSpec.id);
        if (idmapEntry != null) {
          typeIndex = (byte) (idmapEntry.targetTypeId() - 1);
        } else if (header.resourceIDMap != NULL) {
          // This is an overlay, but the types in this overlay are not
          // overlaying anything according to the idmap. We can skip these
          // as they will otherwise conflict with the other resources in the package
          // without a mapping.
          addToType = false;
        }

        if (addToType) {
          List<Type> typeList = computeIfAbsent(group.types, (int) typeIndex, k -> new ArrayList<>());
          if (!typeList.isEmpty()) {
            final Type existingType = typeList.get(0);
            if (existingType.entryCount != newEntryCount && idmapEntry == null) {
              ALOGW("ResTable_typeSpec entry count inconsistent: given %d, previously %d",
                  (int) newEntryCount, (int) existingType.entryCount);
              // We should normally abort here, but some legacy apps declare
              // resources in the 'android' package (old bug in AAPT).
            }
          }

          Type t = new Type(header, _package, newEntryCount);
          t.typeSpec = typeSpec;
          t.typeSpecFlags = typeSpec.getSpecFlags();
          if (idmapEntry != null) {
            t.idmapEntries = idmapEntry;
          }
          typeList.add(t);
          group.largestTypeId = max(group.largestTypeId, typeSpec.id);
        }
      } else {
        ALOGV("Skipping empty ResTable_typeSpec for type %d", typeSpec.id);
      }

    } else if (ctype == RES_TABLE_TYPE_TYPE) {
            ResTable_type type = new ResTable_type(chunk.myBuf(), chunk.myOffset());
      err = validate_chunk(type.header, ResTable_type.SIZEOF_WITHOUT_CONFIG/*-sizeof(ResTable_config)*/+4,
          endPos, "ResTable_type");
      if (err != NO_ERROR) {
        return (mError=err);
      }

            final int typeSize = dtohl(type.header.size);
            final int newEntryCount = dtohl(type.entryCount);

      if (kDebugLoadTableNoisy) {
        System.out.println(String.format("Type off 0x%x: type=0x%x, headerSize=0x%x, size=%d\n",
            base-chunk.myOffset(),
        dtohs(type.header.type),
            dtohs(type.header.headerSize),
            typeSize));
      }
      if (dtohs(type.header.headerSize)+(4/*sizeof(int)*/*newEntryCount) > typeSize) {
        ALOGW("ResTable_type entry index to %s extends beyond chunk end 0x%x.",
            (dtohs(type.header.headerSize) + (4/*sizeof(int)*/*newEntryCount)),
            typeSize);
        return (mError=BAD_TYPE);
      }

      if (newEntryCount != 0
          && dtohl(type.entriesStart) > (typeSize- ResTable_entry.SIZEOF)) {
        ALOGW("ResTable_type entriesStart at 0x%x extends beyond chunk end 0x%x.",
            dtohl(type.entriesStart), typeSize);
        return (mError=BAD_TYPE);
      }

      if (type.id == 0) {
        ALOGW("ResTable_type has an id of 0.");
        return (mError=BAD_TYPE);
      }

      if (newEntryCount > 0) {
        boolean addToType = true;
        byte typeIndex = (byte) (type.id - 1);
        IdmapEntries idmapEntry = idmapEntries.get(type.id);
        if (idmapEntry != null) {
          typeIndex = (byte) (idmapEntry.targetTypeId() - 1);
        } else if (header.resourceIDMap != NULL) {
          // This is an overlay, but the types in this overlay are not
          // overlaying anything according to the idmap. We can skip these
          // as they will otherwise conflict with the other resources in the package
          // without a mapping.
          addToType = false;
        }

        if (addToType) {
          List<Type> typeList = getOrDefault(group.types, (int) typeIndex, Collections.emptyList());
          if (typeList.isEmpty()) {
            ALOGE("No TypeSpec for type %d", type.id);
            return (mError = BAD_TYPE);
          }

          Type t = typeList.get(typeList.size() - 1);
          if (newEntryCount != t.entryCount) {
            ALOGE("ResTable_type entry count inconsistent: given %d, previously %d",
                (int) newEntryCount, (int) t.entryCount);
            return (mError = BAD_TYPE);
          }

          if (t._package_ != _package) {
            ALOGE("No TypeSpec for type %d", type.id);
            return (mError = BAD_TYPE);
          }

          t.configs.add(type);

          if (kDebugTableGetEntry) {
            ResTable_config thisConfig = ResTable_config.fromDtoH(type.config);
            ALOGI("Adding config to type %d: %s\n", type.id,
                thisConfig.toString());
          }
        }
      } else {
        ALOGV("Skipping empty ResTable_type for type %d", type.id);
      }

    } else if (ctype == RES_TABLE_LIBRARY_TYPE) {
      if (group.dynamicRefTable.entries().isEmpty()) {
        throw new UnsupportedOperationException("libraries not supported yet");
//       const ResTable_lib_header* lib = (const ResTable_lib_header*) chunk;
//       status_t err = validate_chunk(&lib->header, sizeof(*lib),
//       endPos, "ResTable_lib_header");
//       if (err != NO_ERROR) {
//         return (mError=err);
//       }
//
//       err = group->dynamicRefTable.load(lib);
//       if (err != NO_ERROR) {
//          return (mError=err);
//        }
//
//        // Fill in the reference table with the entries we already know about.
//        size_t N = mPackageGroups.size();
//        for (size_t i = 0; i < N; i++) {
//          group.dynamicRefTable.addMapping(mPackageGroups[i].name, mPackageGroups[i].id);
//        }
      } else {
        ALOGW("Found multiple library tables, ignoring...");
      }
    } else {
      err = validate_chunk(chunk, ResChunk_header.SIZEOF,
          endPos, "ResTable_package:unknown");
      if (err != NO_ERROR) {
        return (mError=err);
      }
    }
      chunk = chunk.myOffset() + csize < endPos ? new ResChunk_header(chunk.myBuf(), chunk.myOffset() + csize) : null;
  }

    return NO_ERROR;
  }

  public int getTableCookie(int index) {
    return mHeaders.get(index).cookie;
  }

  void setParameters(ResTable_config params)
  {
//    AutoMutex _lock(mLock);
//    AutoMutex _lock2(mFilteredConfigLock);
    synchronized (mLock) {
      synchronized (mFilteredConfigLock) {
        if (kDebugTableGetEntry) {
          ALOGI("Setting parameters: %s\n", params.toString());
        }
        mParams = params;
        for (PackageGroup packageGroup : mPackageGroups.values()) {
          if (kDebugTableNoisy) {
            ALOGI("CLEARING BAGS FOR GROUP 0x%x!", packageGroup.id);
          }
          packageGroup.clearBagCache();

          // Find which configurations match the set of parameters. This allows for a much
          // faster lookup in getEntry() if the set of values is narrowed down.
          //for (int t = 0; t < packageGroup.types.size(); t++) {
            //if (packageGroup.types.get(t).isEmpty()) {
            //   continue;
            // }
            //
            // List<Type> typeList = packageGroup.types.get(t);
        for (List<Type> typeList : packageGroup.types.values()) {
          if (typeList.isEmpty()) {
               continue;
            }

          // Retrieve the cache entry for this type.
            //TypeCacheEntry cacheEntry = packageGroup.typeCacheEntries.editItemAt(t);

            for (int ts = 0; ts < typeList.size(); ts++) {
              Type type = typeList.get(ts);

//              std::shared_ptr<Vector<const ResTable_type*>> newFilteredConfigs =
//                  std::make_shared<Vector<const ResTable_type*>>();
              List<ResTable_type> newFilteredConfigs = new ArrayList<>();

              for (int ti = 0; ti < type.configs.size(); ti++) {
                ResTable_config config = ResTable_config.fromDtoH(type.configs.get(ti).config);

                if (config.match(mParams)) {
                  newFilteredConfigs.add(type.configs.get(ti));
                }
              }

              if (kDebugTableNoisy) {
                ALOGD("Updating pkg=0x%x type=0x%x with 0x%x filtered configs",
                    packageGroup.id, ts, newFilteredConfigs.size());
              }

              // todo: implement cache
//              cacheEntry.filteredConfigs.add(newFilteredConfigs);
            }
          }
        }
      }
    }
  }

  ResTable_config getParameters()
  {
//    mLock.lock();
    synchronized (mLock) {
      return mParams;
    }
//    mLock.unlock();
  }

  private static final Map<String, Integer> sInternalNameToIdMap = new HashMap<>();
  static {
    sInternalNameToIdMap.put("^type", ResTable_map.ATTR_TYPE);
    sInternalNameToIdMap.put("^l10n", ResTable_map.ATTR_L10N);
    sInternalNameToIdMap.put("^min" , ResTable_map.ATTR_MIN);
    sInternalNameToIdMap.put("^max", ResTable_map.ATTR_MAX);
    sInternalNameToIdMap.put("^other", ResTable_map.ATTR_OTHER);
    sInternalNameToIdMap.put("^zero", ResTable_map.ATTR_ZERO);
    sInternalNameToIdMap.put("^one", ResTable_map.ATTR_ONE);
    sInternalNameToIdMap.put("^two", ResTable_map.ATTR_TWO);
    sInternalNameToIdMap.put("^few", ResTable_map.ATTR_FEW);
    sInternalNameToIdMap.put("^many", ResTable_map.ATTR_MANY);
  }

  public int identifierForName(String name, String type, String packageName) {
    return identifierForName(name, type, packageName, null);
  }

  public int identifierForName(String nameString, String type, String packageName,
      final Ref<Integer> outTypeSpecFlags) {
//    if (kDebugTableSuperNoisy) {
//      printf("Identifier for name: error=%d\n", mError);
//    }
//    // Check for internal resource identifier as the very first thing, so
//    // that we will always find them even when there are no resources.
    if (nameString.startsWith("^")) {
      if (sInternalNameToIdMap.containsKey(nameString)) {
        if (outTypeSpecFlags != null) {
          outTypeSpecFlags.set(ResTable_typeSpec.SPEC_PUBLIC);
        }
        return sInternalNameToIdMap.get(nameString);
      }
      if (nameString.length() > 7)
        if (nameString.substring(1, 6).equals("index_")) {
          int index = Integer.getInteger(nameString.substring(7));
          if (Res_CHECKID(index)) {
            ALOGW("Array resource index: %d is too large.",
                index);
            return 0;
          }
          if (outTypeSpecFlags != null) {
            outTypeSpecFlags.set(ResTable_typeSpec.SPEC_PUBLIC);
          }
          return  Res_MAKEARRAY(index);
        }

      return 0;
    }

    if (mError != NO_ERROR) {
      return 0;
    }


    // Figure out the package and type we are looking in...
    // TODO(BC): The following code block was a best effort attempt to directly transliterate
    // C++ code which uses pointer artihmetic. Consider replacing with simpler logic

    boolean fakePublic = false;
    char[] name = nameString.toCharArray();
    int packageEnd = -1;
    int typeEnd = -1;
    int nameEnd = name.length;
    int pIndex = 0;
    while (pIndex < nameEnd) {
      char p = name[pIndex];
      if (p == ':') packageEnd = pIndex;
      else if (p == '/') typeEnd = pIndex;
      pIndex++;
    }
    int nameIndex = 0;
    if (name[nameIndex] == '@') {
      nameIndex++;
      if (name[nameIndex] == '*') {
        fakePublic = true;
        nameIndex++;
    }
  }
    if (nameIndex >= nameEnd) {
      return 0;
    }
    if (packageEnd != -1) {
        packageName = nameString.substring(nameIndex, packageEnd);
        nameIndex = packageEnd+1;
    } else if (packageName == null) {
      return 0;
    }
    if (typeEnd != -1) {
      type = nameString.substring(nameIndex, typeEnd);
      nameIndex = typeEnd+1;
    } else if (type == null) {
      return 0;
    }
    if (nameIndex >= nameEnd) {
      return 0;
    }
    nameString = nameString.substring(nameIndex, nameEnd);

//    nameLen = nameEnd-name;
//    if (kDebugTableNoisy) {
//      printf("Looking for identifier: type=%s, name=%s, package=%s\n",
//          String8(type, typeLen).string(),
//          String8(name, nameLen).string(),
//          String8(package, packageLen).string());
//    }
    final String attr = "attr";
    final String attrPrivate = "^attr-private";
    for (PackageGroup group : mPackageGroups.values()) {
      if (!Objects.equals(packageName.trim(), group.name.trim())) {
        if (kDebugTableNoisy) {
           System.out.println(String.format("Skipping package group: %s\n", group.name));
        }
        continue;
      }
      for (Package pkg : group.packages) {
        String targetType = type;

        do {
          int ti = pkg.typeStrings.indexOfString(targetType);
          if (ti < 0) {
            continue;
          }
          ti += pkg.typeIdOffset;
          int identifier = findEntry(group, ti, nameString, outTypeSpecFlags);
          if (identifier != 0) {
            if (fakePublic && outTypeSpecFlags != null) {
                        outTypeSpecFlags.set(outTypeSpecFlags.get() | ResTable_typeSpec.SPEC_PUBLIC);
            }
            return identifier;
          }
        } while (attr.compareTo(targetType) == 0
            && ((targetType = attrPrivate) != null)
            );
      }
      break;
    }
    return 0;
  }

  int findEntry(PackageGroup group, int typeIndex, String name, Ref<Integer> outTypeSpecFlags) {
    List<Type> typeList = getOrDefault(group.types, typeIndex, Collections.emptyList());
    for (Type type : typeList) {
      int ei = type._package_.keyStrings.indexOfString(name);
      if (ei < 0) {
        continue;
      }
      for (ResTable_type resTableType : type.configs) {
        int entryIndex = resTableType.findEntryByResName(ei);
        if (entryIndex >= 0) {
          int resId = Res_MAKEID(group.id - 1, typeIndex, entryIndex);
          if (outTypeSpecFlags != null) {
            Entry result = new Entry();
            if (getEntry(group, typeIndex, entryIndex, null, result) != NO_ERROR) {
              ALOGW("Failed to find spec flags for 0x%08x", resId);
              return 0;
            }
            outTypeSpecFlags.set(result.specFlags);
          }
          return resId;
        }
      }
    }
    return 0;
  }

//bool ResTable::expandResourceRef(const char16_t* refStr, size_t refLen,
//                                 String16* outPackage,
//                                 String16* outType,
//                                 String16* outName,
//                                 const String16* defType,
//                                 const String16* defPackage,
//                                 const char** outErrorMsg,
//                                 bool* outPublicOnly)
//{
//    const char16_t* packageEnd = NULL;
//    const char16_t* typeEnd = NULL;
//    const char16_t* p = refStr;
//    const char16_t* const end = p + refLen;
//    while (p < end) {
//        if (*p == ':') packageEnd = p;
//        else if (*p == '/') {
//            typeEnd = p;
//            break;
//        }
//        p++;
//    }
//    p = refStr;
//    if (*p == '@') p++;
//
//    if (outPublicOnly != NULL) {
//        *outPublicOnly = true;
//    }
//    if (*p == '*') {
//        p++;
//        if (outPublicOnly != NULL) {
//            *outPublicOnly = false;
//        }
//    }
//
//    if (packageEnd) {
//        *outPackage = String16(p, packageEnd-p);
//        p = packageEnd+1;
//    } else {
//        if (!defPackage) {
//            if (outErrorMsg) {
//                *outErrorMsg = "No resource package specified";
//            }
//            return false;
//        }
//        *outPackage = *defPackage;
//    }
//    if (typeEnd) {
//        *outType = String16(p, typeEnd-p);
//        p = typeEnd+1;
//    } else {
//        if (!defType) {
//            if (outErrorMsg) {
//                *outErrorMsg = "No resource type specified";
//            }
//            return false;
//        }
//        *outType = *defType;
//    }
//    *outName = String16(p, end-p);
//    if(**outPackage == 0) {
//        if(outErrorMsg) {
//            *outErrorMsg = "Resource package cannot be an empty string";
//        }
//        return false;
//    }
//    if(**outType == 0) {
//        if(outErrorMsg) {
//            *outErrorMsg = "Resource type cannot be an empty string";
//        }
//        return false;
//    }
//    if(**outName == 0) {
//        if(outErrorMsg) {
//            *outErrorMsg = "Resource id cannot be an empty string";
//        }
//        return false;
//    }
//    return true;
//}
//
//static uint32_t get_hex(char c, bool* outError)
//{
//    if (c >= '0' && c <= '9') {
//        return c - '0';
//    } else if (c >= 'a' && c <= 'f') {
//        return c - 'a' + 0xa;
//    } else if (c >= 'A' && c <= 'F') {
//        return c - 'A' + 0xa;
//    }
//    *outError = true;
//    return 0;
//}
//
//struct unit_entry
//{
//    const char* name;
//    size_t len;
//    uint8_t type;
//    uint32_t unit;
//    float scale;
//};
//
//static const unit_entry unitNames[] = {
//    { "px", strlen("px"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_PX, 1.0f },
//    { "dip", strlen("dip"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_DIP, 1.0f },
//    { "dp", strlen("dp"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_DIP, 1.0f },
//    { "sp", strlen("sp"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_SP, 1.0f },
//    { "pt", strlen("pt"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_PT, 1.0f },
//    { "in", strlen("in"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_IN, 1.0f },
//    { "mm", strlen("mm"), Res_value::TYPE_DIMENSION, Res_value::COMPLEX_UNIT_MM, 1.0f },
//    { "%", strlen("%"), Res_value::TYPE_FRACTION, Res_value::COMPLEX_UNIT_FRACTION, 1.0f/100 },
//    { "%s", strlen("%s"), Res_value::TYPE_FRACTION, Res_value::COMPLEX_UNIT_FRACTION_PARENT, 1.0f/100 },
//    { NULL, 0, 0, 0, 0 }
//};
//
//static bool parse_unit(const char* str, Res_value* outValue,
//                       float* outScale, const char** outEnd)
//{
//    const char* end = str;
//    while (*end != 0 && !isspace((unsigned char)*end)) {
//        end++;
//    }
//    const size_t len = end-str;
//
//    const char* realEnd = end;
//    while (*realEnd != 0 && isspace((unsigned char)*realEnd)) {
//        realEnd++;
//    }
//    if (*realEnd != 0) {
//        return false;
//    }
//
//    const unit_entry* cur = unitNames;
//    while (cur->name) {
//        if (len == cur->len && strncmp(cur->name, str, len) == 0) {
//            outValue->dataType = cur->type;
//            outValue->data = cur->unit << Res_value::COMPLEX_UNIT_SHIFT;
//            *outScale = cur->scale;
//            *outEnd = end;
//            //printf("Found unit %s for %s\n", cur->name, str);
//            return true;
//        }
//        cur++;
//    }
//
//    return false;
//}
//
//bool U16StringToInt(const char16_t* s, size_t len, Res_value* outValue)
//{
//    while (len > 0 && isspace16(*s)) {
//        s++;
//        len--;
//    }
//
//    if (len <= 0) {
//        return false;
//    }
//
//    size_t i = 0;
//    int64_t val = 0;
//    bool neg = false;
//
//    if (*s == '-') {
//        neg = true;
//        i++;
//    }
//
//    if (s[i] < '0' || s[i] > '9') {
//        return false;
//    }
//
//    static_assert(std::is_same<uint32_t, Res_value::data_type>::value,
//                  "Res_value::data_type has changed. The range checks in this "
//                  "function are no longer correct.");
//
//    // Decimal or hex?
//    bool isHex;
//    if (len > 1 && s[i] == '0' && s[i+1] == 'x') {
//        isHex = true;
//        i += 2;
//
//        if (neg) {
//            return false;
//        }
//
//        if (i == len) {
//            // Just u"0x"
//            return false;
//        }
//
//        bool error = false;
//        while (i < len && !error) {
//            val = (val*16) + get_hex(s[i], &error);
//            i++;
//
//            if (val > std::numeric_limits<uint32_t>::max()) {
//                return false;
//            }
//        }
//        if (error) {
//            return false;
//        }
//    } else {
//        isHex = false;
//        while (i < len) {
//            if (s[i] < '0' || s[i] > '9') {
//                return false;
//            }
//            val = (val*10) + s[i]-'0';
//            i++;
//
//            if ((neg && -val < std::numeric_limits<int32_t>::min()) ||
//                (!neg && val > std::numeric_limits<int32_t>::max())) {
//                return false;
//            }
//        }
//    }
//
//    if (neg) val = -val;
//
//    while (i < len && isspace16(s[i])) {
//        i++;
//    }
//
//    if (i != len) {
//        return false;
//    }
//
//    if (outValue) {
//        outValue->dataType =
//            isHex ? outValue->TYPE_INT_HEX : outValue->TYPE_INT_DEC;
//        outValue->data = static_cast<Res_value::data_type>(val);
//    }
//    return true;
//}
//
//bool ResTable::stringToInt(const char16_t* s, size_t len, Res_value* outValue)
//{
//    return U16StringToInt(s, len, outValue);
//}
//
//bool ResTable::stringToFloat(const char16_t* s, size_t len, Res_value* outValue)
//{
//    while (len > 0 && isspace16(*s)) {
//        s++;
//        len--;
//    }
//
//    if (len <= 0) {
//        return false;
//    }
//
//    char buf[128];
//    int i=0;
//    while (len > 0 && *s != 0 && i < 126) {
//        if (*s > 255) {
//            return false;
//        }
//        buf[i++] = *s++;
//        len--;
//    }
//
//    if (len > 0) {
//        return false;
//    }
//    if ((buf[0] < '0' || buf[0] > '9') && buf[0] != '.' && buf[0] != '-' && buf[0] != '+') {
//        return false;
//    }
//
//    buf[i] = 0;
//    const char* end;
//    float f = strtof(buf, (char**)&end);
//
//    if (*end != 0 && !isspace((unsigned char)*end)) {
//        // Might be a unit...
//        float scale;
//        if (parse_unit(end, outValue, &scale, &end)) {
//            f *= scale;
//            const bool neg = f < 0;
//            if (neg) f = -f;
//            uint64_t bits = (uint64_t)(f*(1<<23)+.5f);
//            uint32_t radix;
//            uint32_t shift;
//            if ((bits&0x7fffff) == 0) {
//                // Always use 23p0 if there is no fraction, just to make
//                // things easier to read.
//                radix = Res_value::COMPLEX_RADIX_23p0;
//                shift = 23;
//            } else if ((bits&0xffffffffff800000LL) == 0) {
//                // Magnitude is zero -- can fit in 0 bits of precision.
//                radix = Res_value::COMPLEX_RADIX_0p23;
//                shift = 0;
//            } else if ((bits&0xffffffff80000000LL) == 0) {
//                // Magnitude can fit in 8 bits of precision.
//                radix = Res_value::COMPLEX_RADIX_8p15;
//                shift = 8;
//            } else if ((bits&0xffffff8000000000LL) == 0) {
//                // Magnitude can fit in 16 bits of precision.
//                radix = Res_value::COMPLEX_RADIX_16p7;
//                shift = 16;
//            } else {
//                // Magnitude needs entire range, so no fractional part.
//                radix = Res_value::COMPLEX_RADIX_23p0;
//                shift = 23;
//            }
//            int32_t mantissa = (int32_t)(
//                (bits>>shift) & Res_value::COMPLEX_MANTISSA_MASK);
//            if (neg) {
//                mantissa = (-mantissa) & Res_value::COMPLEX_MANTISSA_MASK;
//            }
//            outValue->data |=
//                (radix<<Res_value::COMPLEX_RADIX_SHIFT)
//                | (mantissa<<Res_value::COMPLEX_MANTISSA_SHIFT);
//            //printf("Input value: %f 0x%016Lx, mult: %f, radix: %d, shift: %d, final: 0x%08x\n",
//            //       f * (neg ? -1 : 1), bits, f*(1<<23),
//            //       radix, shift, outValue->data);
//            return true;
//        }
//        return false;
//    }
//
//    while (*end != 0 && isspace((unsigned char)*end)) {
//        end++;
//    }
//
//    if (*end == 0) {
//        if (outValue) {
//            outValue->dataType = outValue->TYPE_FLOAT;
//            *(float*)(&outValue->data) = f;
//            return true;
//        }
//    }
//
//    return false;
//}
//
//bool ResTable::stringToValue(Res_value* outValue, String16* outString,
//                             const char16_t* s, size_t len,
//                             bool preserveSpaces, bool coerceType,
//                             uint32_t attrID,
//                             const String16* defType,
//                             const String16* defPackage,
//                             Accessor* accessor,
//                             void* accessorCookie,
//                             uint32_t attrType,
//                             bool enforcePrivate) const
//{
//    bool localizationSetting = accessor != NULL && accessor->getLocalizationSetting();
//    const char* errorMsg = NULL;
//
//    outValue->size = sizeof(Res_value);
//    outValue->res0 = 0;
//
//    // First strip leading/trailing whitespace.  Do this before handling
//    // escapes, so they can be used to force whitespace into the string.
//    if (!preserveSpaces) {
//        while (len > 0 && isspace16(*s)) {
//            s++;
//            len--;
//        }
//        while (len > 0 && isspace16(s[len-1])) {
//            len--;
//        }
//        // If the string ends with '\', then we keep the space after it.
//        if (len > 0 && s[len-1] == '\\' && s[len] != 0) {
//            len++;
//        }
//    }
//
//    //printf("Value for: %s\n", String8(s, len).string());
//
//    uint32_t l10nReq = ResTable_map::L10N_NOT_REQUIRED;
//    uint32_t attrMin = 0x80000000, attrMax = 0x7fffffff;
//    bool fromAccessor = false;
//    if (attrID != 0 && !Res_INTERNALID(attrID)) {
//        const ssize_t p = getResourcePackageIndex(attrID);
//        const bag_entry* bag;
//        ssize_t cnt = p >= 0 ? lockBag(attrID, &bag) : -1;
//        //printf("For attr 0x%08x got bag of %d\n", attrID, cnt);
//        if (cnt >= 0) {
//            while (cnt > 0) {
//                //printf("Entry 0x%08x = 0x%08x\n", bag->map.name.ident, bag->map.value.data);
//                switch (bag->map.name.ident) {
//                case ResTable_map::ATTR_TYPE:
//                    attrType = bag->map.value.data;
//                    break;
//                case ResTable_map::ATTR_MIN:
//                    attrMin = bag->map.value.data;
//                    break;
//                case ResTable_map::ATTR_MAX:
//                    attrMax = bag->map.value.data;
//                    break;
//                case ResTable_map::ATTR_L10N:
//                    l10nReq = bag->map.value.data;
//                    break;
//                }
//                bag++;
//                cnt--;
//            }
//            unlockBag(bag);
//        } else if (accessor && accessor->getAttributeType(attrID, &attrType)) {
//            fromAccessor = true;
//            if (attrType == ResTable_map::TYPE_ENUM
//                    || attrType == ResTable_map::TYPE_FLAGS
//                    || attrType == ResTable_map::TYPE_INTEGER) {
//                accessor->getAttributeMin(attrID, &attrMin);
//                accessor->getAttributeMax(attrID, &attrMax);
//            }
//            if (localizationSetting) {
//                l10nReq = accessor->getAttributeL10N(attrID);
//            }
//        }
//    }
//
//    const bool canStringCoerce =
//        coerceType && (attrType&ResTable_map::TYPE_STRING) != 0;
//
//    if (*s == '@') {
//        outValue->dataType = outValue->TYPE_REFERENCE;
//
//        // Note: we don't check attrType here because the reference can
//        // be to any other type; we just need to count on the client making
//        // sure the referenced type is correct.
//
//        //printf("Looking up ref: %s\n", String8(s, len).string());
//
//        // It's a reference!
//        if (len == 5 && s[1]=='n' && s[2]=='u' && s[3]=='l' && s[4]=='l') {
//            // Special case @null as undefined. This will be converted by
//            // AssetManager to TYPE_NULL with data DATA_NULL_UNDEFINED.
//            outValue->data = 0;
//            return true;
//        } else if (len == 6 && s[1]=='e' && s[2]=='m' && s[3]=='p' && s[4]=='t' && s[5]=='y') {
//            // Special case @empty as explicitly defined empty value.
//            outValue->dataType = Res_value::TYPE_NULL;
//            outValue->data = Res_value::DATA_NULL_EMPTY;
//            return true;
//        } else {
//            bool createIfNotFound = false;
//            const char16_t* resourceRefName;
//            int resourceNameLen;
//            if (len > 2 && s[1] == '+') {
//                createIfNotFound = true;
//                resourceRefName = s + 2;
//                resourceNameLen = len - 2;
//            } else if (len > 2 && s[1] == '*') {
//                enforcePrivate = false;
//                resourceRefName = s + 2;
//                resourceNameLen = len - 2;
//            } else {
//                createIfNotFound = false;
//                resourceRefName = s + 1;
//                resourceNameLen = len - 1;
//            }
//            String16 package, type, name;
//            if (!expandResourceRef(resourceRefName,resourceNameLen, &package, &type, &name,
//                                   defType, defPackage, &errorMsg)) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, errorMsg);
//                }
//                return false;
//            }
//
//            uint32_t specFlags = 0;
//            uint32_t rid = identifierForName(name.string(), name.size(), type.string(),
//                    type.size(), package.string(), package.size(), &specFlags);
//            if (rid != 0) {
//                if (enforcePrivate) {
//                    if (accessor == NULL || accessor->getAssetsPackage() != package) {
//                        if ((specFlags&ResTable_typeSpec::SPEC_PUBLIC) == 0) {
//                            if (accessor != NULL) {
//                                accessor->reportError(accessorCookie, "Resource is not public.");
//                            }
//                            return false;
//                        }
//                    }
//                }
//
//                if (accessor) {
//                    rid = Res_MAKEID(
//                        accessor->getRemappedPackage(Res_GETPACKAGE(rid)),
//                        Res_GETTYPE(rid), Res_GETENTRY(rid));
//                    if (kDebugTableNoisy) {
//                        ALOGI("Incl %s:%s/%s: 0x%08x\n",
//                                String8(package).string(), String8(type).string(),
//                                String8(name).string(), rid);
//                    }
//                }
//
//                uint32_t packageId = Res_GETPACKAGE(rid) + 1;
//                if (packageId != APP_PACKAGE_ID && packageId != SYS_PACKAGE_ID) {
//                    outValue->dataType = Res_value::TYPE_DYNAMIC_REFERENCE;
//                }
//                outValue->data = rid;
//                return true;
//            }
//
//            if (accessor) {
//                uint32_t rid = accessor->getCustomResourceWithCreation(package, type, name,
//                                                                       createIfNotFound);
//                if (rid != 0) {
//                    if (kDebugTableNoisy) {
//                        ALOGI("Pckg %s:%s/%s: 0x%08x\n",
//                                String8(package).string(), String8(type).string(),
//                                String8(name).string(), rid);
//                    }
//                    uint32_t packageId = Res_GETPACKAGE(rid) + 1;
//                    if (packageId == 0x00) {
//                        outValue->data = rid;
//                        outValue->dataType = Res_value::TYPE_DYNAMIC_REFERENCE;
//                        return true;
//                    } else if (packageId == APP_PACKAGE_ID || packageId == SYS_PACKAGE_ID) {
//                        // We accept packageId's generated as 0x01 in order to support
//                        // building the android system resources
//                        outValue->data = rid;
//                        return true;
//                    }
//                }
//            }
//        }
//
//        if (accessor != NULL) {
//            accessor->reportError(accessorCookie, "No resource found that matches the given name");
//        }
//        return false;
//    }
//
//    // if we got to here, and localization is required and it's not a reference,
//    // complain and bail.
//    if (l10nReq == ResTable_map::L10N_SUGGESTED) {
//        if (localizationSetting) {
//            if (accessor != NULL) {
//                accessor->reportError(accessorCookie, "This attribute must be localized.");
//            }
//        }
//    }
//
//    if (*s == '#') {
//        // It's a color!  Convert to an integer of the form 0xaarrggbb.
//        uint32_t color = 0;
//        bool error = false;
//        if (len == 4) {
//            outValue->dataType = outValue->TYPE_INT_COLOR_RGB4;
//            color |= 0xFF000000;
//            color |= get_hex(s[1], &error) << 20;
//            color |= get_hex(s[1], &error) << 16;
//            color |= get_hex(s[2], &error) << 12;
//            color |= get_hex(s[2], &error) << 8;
//            color |= get_hex(s[3], &error) << 4;
//            color |= get_hex(s[3], &error);
//        } else if (len == 5) {
//            outValue->dataType = outValue->TYPE_INT_COLOR_ARGB4;
//            color |= get_hex(s[1], &error) << 28;
//            color |= get_hex(s[1], &error) << 24;
//            color |= get_hex(s[2], &error) << 20;
//            color |= get_hex(s[2], &error) << 16;
//            color |= get_hex(s[3], &error) << 12;
//            color |= get_hex(s[3], &error) << 8;
//            color |= get_hex(s[4], &error) << 4;
//            color |= get_hex(s[4], &error);
//        } else if (len == 7) {
//            outValue->dataType = outValue->TYPE_INT_COLOR_RGB8;
//            color |= 0xFF000000;
//            color |= get_hex(s[1], &error) << 20;
//            color |= get_hex(s[2], &error) << 16;
//            color |= get_hex(s[3], &error) << 12;
//            color |= get_hex(s[4], &error) << 8;
//            color |= get_hex(s[5], &error) << 4;
//            color |= get_hex(s[6], &error);
//        } else if (len == 9) {
//            outValue->dataType = outValue->TYPE_INT_COLOR_ARGB8;
//            color |= get_hex(s[1], &error) << 28;
//            color |= get_hex(s[2], &error) << 24;
//            color |= get_hex(s[3], &error) << 20;
//            color |= get_hex(s[4], &error) << 16;
//            color |= get_hex(s[5], &error) << 12;
//            color |= get_hex(s[6], &error) << 8;
//            color |= get_hex(s[7], &error) << 4;
//            color |= get_hex(s[8], &error);
//        } else {
//            error = true;
//        }
//        if (!error) {
//            if ((attrType&ResTable_map::TYPE_COLOR) == 0) {
//                if (!canStringCoerce) {
//                    if (accessor != NULL) {
//                        accessor->reportError(accessorCookie,
//                                "Color types not allowed");
//                    }
//                    return false;
//                }
//            } else {
//                outValue->data = color;
//                //printf("Color input=%s, output=0x%x\n", String8(s, len).string(), color);
//                return true;
//            }
//        } else {
//            if ((attrType&ResTable_map::TYPE_COLOR) != 0) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Color value not valid --"
//                            " must be #rgb, #argb, #rrggbb, or #aarrggbb");
//                }
//                #if 0
//                fprintf(stderr, "%s: Color ID %s value %s is not valid\n",
//                        "Resource File", //(const char*)in->getPrintableSource(),
//                        String8(*curTag).string(),
//                        String8(s, len).string());
//                #endif
//                return false;
//            }
//        }
//    }
//
//    if (*s == '?') {
//        outValue->dataType = outValue->TYPE_ATTRIBUTE;
//
//        // Note: we don't check attrType here because the reference can
//        // be to any other type; we just need to count on the client making
//        // sure the referenced type is correct.
//
//        //printf("Looking up attr: %s\n", String8(s, len).string());
//
//        static const String16 attr16("attr");
//        String16 package, type, name;
//        if (!expandResourceRef(s+1, len-1, &package, &type, &name,
//                               &attr16, defPackage, &errorMsg)) {
//            if (accessor != NULL) {
//                accessor->reportError(accessorCookie, errorMsg);
//            }
//            return false;
//        }
//
//        //printf("Pkg: %s, Type: %s, Name: %s\n",
//        //       String8(package).string(), String8(type).string(),
//        //       String8(name).string());
//        uint32_t specFlags = 0;
//        uint32_t rid =
//            identifierForName(name.string(), name.size(),
//                              type.string(), type.size(),
//                              package.string(), package.size(), &specFlags);
//        if (rid != 0) {
//            if (enforcePrivate) {
//                if ((specFlags&ResTable_typeSpec::SPEC_PUBLIC) == 0) {
//                    if (accessor != NULL) {
//                        accessor->reportError(accessorCookie, "Attribute is not public.");
//                    }
//                    return false;
//                }
//            }
//
//            if (accessor) {
//                rid = Res_MAKEID(
//                    accessor->getRemappedPackage(Res_GETPACKAGE(rid)),
//                    Res_GETTYPE(rid), Res_GETENTRY(rid));
//            }
//
//            uint32_t packageId = Res_GETPACKAGE(rid) + 1;
//            if (packageId != APP_PACKAGE_ID && packageId != SYS_PACKAGE_ID) {
//                outValue->dataType = Res_value::TYPE_DYNAMIC_ATTRIBUTE;
//            }
//            outValue->data = rid;
//            return true;
//        }
//
//        if (accessor) {
//            uint32_t rid = accessor->getCustomResource(package, type, name);
//            if (rid != 0) {
//                uint32_t packageId = Res_GETPACKAGE(rid) + 1;
//                if (packageId == 0x00) {
//                    outValue->data = rid;
//                    outValue->dataType = Res_value::TYPE_DYNAMIC_ATTRIBUTE;
//                    return true;
//                } else if (packageId == APP_PACKAGE_ID || packageId == SYS_PACKAGE_ID) {
//                    // We accept packageId's generated as 0x01 in order to support
//                    // building the android system resources
//                    outValue->data = rid;
//                    return true;
//                }
//            }
//        }
//
//        if (accessor != NULL) {
//            accessor->reportError(accessorCookie, "No resource found that matches the given name");
//        }
//        return false;
//    }
//
//    if (stringToInt(s, len, outValue)) {
//        if ((attrType&ResTable_map::TYPE_INTEGER) == 0) {
//            // If this type does not allow integers, but does allow floats,
//            // fall through on this error case because the float type should
//            // be able to accept any integer value.
//            if (!canStringCoerce && (attrType&ResTable_map::TYPE_FLOAT) == 0) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Integer types not allowed");
//                }
//                return false;
//            }
//        } else {
//            if (((int32_t)outValue->data) < ((int32_t)attrMin)
//                    || ((int32_t)outValue->data) > ((int32_t)attrMax)) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Integer value out of range");
//                }
//                return false;
//            }
//            return true;
//        }
//    }
//
//    if (stringToFloat(s, len, outValue)) {
//        if (outValue->dataType == Res_value::TYPE_DIMENSION) {
//            if ((attrType&ResTable_map::TYPE_DIMENSION) != 0) {
//                return true;
//            }
//            if (!canStringCoerce) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Dimension types not allowed");
//                }
//                return false;
//            }
//        } else if (outValue->dataType == Res_value::TYPE_FRACTION) {
//            if ((attrType&ResTable_map::TYPE_FRACTION) != 0) {
//                return true;
//            }
//            if (!canStringCoerce) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Fraction types not allowed");
//                }
//                return false;
//            }
//        } else if ((attrType&ResTable_map::TYPE_FLOAT) == 0) {
//            if (!canStringCoerce) {
//                if (accessor != NULL) {
//                    accessor->reportError(accessorCookie, "Float types not allowed");
//                }
//                return false;
//            }
//        } else {
//            return true;
//        }
//    }
//
//    if (len == 4) {
//        if ((s[0] == 't' || s[0] == 'T') &&
//            (s[1] == 'r' || s[1] == 'R') &&
//            (s[2] == 'u' || s[2] == 'U') &&
//            (s[3] == 'e' || s[3] == 'E')) {
//            if ((attrType&ResTable_map::TYPE_BOOLEAN) == 0) {
//                if (!canStringCoerce) {
//                    if (accessor != NULL) {
//                        accessor->reportError(accessorCookie, "Boolean types not allowed");
//                    }
//                    return false;
//                }
//            } else {
//                outValue->dataType = outValue->TYPE_INT_BOOLEAN;
//                outValue->data = (uint32_t)-1;
//                return true;
//            }
//        }
//    }
//
//    if (len == 5) {
//        if ((s[0] == 'f' || s[0] == 'F') &&
//            (s[1] == 'a' || s[1] == 'A') &&
//            (s[2] == 'l' || s[2] == 'L') &&
//            (s[3] == 's' || s[3] == 'S') &&
//            (s[4] == 'e' || s[4] == 'E')) {
//            if ((attrType&ResTable_map::TYPE_BOOLEAN) == 0) {
//                if (!canStringCoerce) {
//                    if (accessor != NULL) {
//                        accessor->reportError(accessorCookie, "Boolean types not allowed");
//                    }
//                    return false;
//                }
//            } else {
//                outValue->dataType = outValue->TYPE_INT_BOOLEAN;
//                outValue->data = 0;
//                return true;
//            }
//        }
//    }
//
//    if ((attrType&ResTable_map::TYPE_ENUM) != 0) {
//        const ssize_t p = getResourcePackageIndex(attrID);
//        const bag_entry* bag;
//        ssize_t cnt = p >= 0 ? lockBag(attrID, &bag) : -1;
//        //printf("Got %d for enum\n", cnt);
//        if (cnt >= 0) {
//            resource_name rname;
//            while (cnt > 0) {
//                if (!Res_INTERNALID(bag->map.name.ident)) {
//                    //printf("Trying attr #%08x\n", bag->map.name.ident);
//                    if (getResourceName(bag->map.name.ident, false, &rname)) {
//                        #if 0
//                        printf("Matching %s against %s (0x%08x)\n",
//                               String8(s, len).string(),
//                               String8(rname.name, rname.nameLen).string(),
//                               bag->map.name.ident);
//                        #endif
//                        if (strzcmp16(s, len, rname.name, rname.nameLen) == 0) {
//                            outValue->dataType = bag->map.value.dataType;
//                            outValue->data = bag->map.value.data;
//                            unlockBag(bag);
//                            return true;
//                        }
//                    }
//
//                }
//                bag++;
//                cnt--;
//            }
//            unlockBag(bag);
//        }
//
//        if (fromAccessor) {
//            if (accessor->getAttributeEnum(attrID, s, len, outValue)) {
//                return true;
//            }
//        }
//    }
//
//    if ((attrType&ResTable_map::TYPE_FLAGS) != 0) {
//        const ssize_t p = getResourcePackageIndex(attrID);
//        const bag_entry* bag;
//        ssize_t cnt = p >= 0 ? lockBag(attrID, &bag) : -1;
//        //printf("Got %d for flags\n", cnt);
//        if (cnt >= 0) {
//            bool failed = false;
//            resource_name rname;
//            outValue->dataType = Res_value::TYPE_INT_HEX;
//            outValue->data = 0;
//            const char16_t* end = s + len;
//            const char16_t* pos = s;
//            while (pos < end && !failed) {
//                const char16_t* start = pos;
//                pos++;
//                while (pos < end && *pos != '|') {
//                    pos++;
//                }
//                //printf("Looking for: %s\n", String8(start, pos-start).string());
//                const bag_entry* bagi = bag;
//                ssize_t i;
//                for (i=0; i<cnt; i++, bagi++) {
//                    if (!Res_INTERNALID(bagi->map.name.ident)) {
//                        //printf("Trying attr #%08x\n", bagi->map.name.ident);
//                        if (getResourceName(bagi->map.name.ident, false, &rname)) {
//                            #if 0
//                            printf("Matching %s against %s (0x%08x)\n",
//                                   String8(start,pos-start).string(),
//                                   String8(rname.name, rname.nameLen).string(),
//                                   bagi->map.name.ident);
//                            #endif
//                            if (strzcmp16(start, pos-start, rname.name, rname.nameLen) == 0) {
//                                outValue->data |= bagi->map.value.data;
//                                break;
//                            }
//                        }
//                    }
//                }
//                if (i >= cnt) {
//                    // Didn't find this flag identifier.
//                    failed = true;
//                }
//                if (pos < end) {
//                    pos++;
//                }
//            }
//            unlockBag(bag);
//            if (!failed) {
//                //printf("Final flag value: 0x%lx\n", outValue->data);
//                return true;
//            }
//        }
//
//
//        if (fromAccessor) {
//            if (accessor->getAttributeFlags(attrID, s, len, outValue)) {
//                //printf("Final flag value: 0x%lx\n", outValue->data);
//                return true;
//            }
//        }
//    }
//
//    if ((attrType&ResTable_map::TYPE_STRING) == 0) {
//        if (accessor != NULL) {
//            accessor->reportError(accessorCookie, "String types not allowed");
//        }
//        return false;
//    }
//
//    // Generic string handling...
//    outValue->dataType = outValue->TYPE_STRING;
//    if (outString) {
//        bool failed = collectString(outString, s, len, preserveSpaces, &errorMsg);
//        if (accessor != NULL) {
//            accessor->reportError(accessorCookie, errorMsg);
//        }
//        return failed;
//    }
//
//    return true;
//}
//
//bool ResTable::collectString(String16* outString,
//                             const char16_t* s, size_t len,
//                             bool preserveSpaces,
//                             const char** outErrorMsg,
//                             bool append)
//{
//    String16 tmp;
//
//    char quoted = 0;
//    const char16_t* p = s;
//    while (p < (s+len)) {
//        while (p < (s+len)) {
//            const char16_t c = *p;
//            if (c == '\\') {
//                break;
//            }
//            if (!preserveSpaces) {
//                if (quoted == 0 && isspace16(c)
//                    && (c != ' ' || isspace16(*(p+1)))) {
//                    break;
//                }
//                if (c == '"' && (quoted == 0 || quoted == '"')) {
//                    break;
//                }
//                if (c == '\'' && (quoted == 0 || quoted == '\'')) {
//                    /*
//                     * In practice, when people write ' instead of \'
//                     * in a string, they are doing it by accident
//                     * instead of really meaning to use ' as a quoting
//                     * character.  Warn them so they don't lose it.
//                     */
//                    if (outErrorMsg) {
//                        *outErrorMsg = "Apostrophe not preceded by \\";
//                    }
//                    return false;
//                }
//            }
//            p++;
//        }
//        if (p < (s+len)) {
//            if (p > s) {
//                tmp.append(String16(s, p-s));
//            }
//            if (!preserveSpaces && (*p == '"' || *p == '\'')) {
//                if (quoted == 0) {
//                    quoted = *p;
//                } else {
//                    quoted = 0;
//                }
//                p++;
//            } else if (!preserveSpaces && isspace16(*p)) {
//                // Space outside of a quote -- consume all spaces and
//                // leave a single plain space char.
//                tmp.append(String16(" "));
//                p++;
//                while (p < (s+len) && isspace16(*p)) {
//                    p++;
//                }
//            } else if (*p == '\\') {
//                p++;
//                if (p < (s+len)) {
//                    switch (*p) {
//                    case 't':
//                        tmp.append(String16("\t"));
//                        break;
//                    case 'n':
//                        tmp.append(String16("\n"));
//                        break;
//                    case '#':
//                        tmp.append(String16("#"));
//                        break;
//                    case '@':
//                        tmp.append(String16("@"));
//                        break;
//                    case '?':
//                        tmp.append(String16("?"));
//                        break;
//                    case '"':
//                        tmp.append(String16("\""));
//                        break;
//                    case '\'':
//                        tmp.append(String16("'"));
//                        break;
//                    case '\\':
//                        tmp.append(String16("\\"));
//                        break;
//                    case 'u':
//                    {
//                        char16_t chr = 0;
//                        int i = 0;
//                        while (i < 4 && p[1] != 0) {
//                            p++;
//                            i++;
//                            int c;
//                            if (*p >= '0' && *p <= '9') {
//                                c = *p - '0';
//                            } else if (*p >= 'a' && *p <= 'f') {
//                                c = *p - 'a' + 10;
//                            } else if (*p >= 'A' && *p <= 'F') {
//                                c = *p - 'A' + 10;
//                            } else {
//                                if (outErrorMsg) {
//                                    *outErrorMsg = "Bad character in \\u unicode escape sequence";
//                                }
//                                return false;
//                            }
//                            chr = (chr<<4) | c;
//                        }
//                        tmp.append(String16(&chr, 1));
//                    } break;
//                    default:
//                        // ignore unknown escape chars.
//                        break;
//                    }
//                    p++;
//                }
//            }
//            len -= (p-s);
//            s = p;
//        }
//    }
//
//    if (tmp.size() != 0) {
//        if (len > 0) {
//            tmp.append(String16(s, len));
//        }
//        if (append) {
//            outString->append(tmp);
//        } else {
//            outString->setTo(tmp);
//        }
//    } else {
//        if (append) {
//            outString->append(String16(s, len));
//        } else {
//            outString->setTo(s, len);
//        }
//    }
//
//    return true;
//}

  public int getBasePackageCount()
  {
    if (mError != NO_ERROR) {
      return 0;
    }
    return mPackageGroups.size();
  }

  public String getBasePackageName(int idx)
  {
    if (mError != NO_ERROR) {
      return null;
    }
    LOG_FATAL_IF(idx >= mPackageGroups.size(),
        "Requested package index %d past package count %d",
        (int)idx, (int)mPackageGroups.size());
    return mPackageGroups.get(keyFor(idx)).name;
  }

  public int getBasePackageId(int idx)
  {
    if (mError != NO_ERROR) {
      return 0;
    }
    LOG_FATAL_IF(idx >= mPackageGroups.size(),
        "Requested package index %d past package count %d",
        (int)idx, (int)mPackageGroups.size());
    return mPackageGroups.get(keyFor(idx)).id;
  }

  int getLastTypeIdForPackage(int idx)
  {
    if (mError != NO_ERROR) {
      return 0;
    }
    LOG_FATAL_IF(idx >= mPackageGroups.size(),
        "Requested package index %d past package count %d",
        (int)idx, (int)mPackageGroups.size());
    PackageGroup group = mPackageGroups.get(keyFor(idx));
    return group.largestTypeId;
  }

  int keyFor(int idx) {
    ArrayList<Integer> keys = new ArrayList<>(mPackageGroups.keySet());
    Collections.sort(keys);
    return keys.get(idx);
  }

  public int getTableCount() {
    return mHeaders.size();
  }

  public ResStringPool getTableStringBlock(int index) {
    return mHeaders.get(index).values;
  }

  public DynamicRefTable getDynamicRefTableForCookie(int cookie) {
    for (PackageGroup pg : mPackageGroups.values()) {
      int M = pg.packages.size();
      for (int j = 0; j < M; j++) {
        if (pg.packages.get(j).header.cookie == cookie) {
          return pg.dynamicRefTable;
        }
      }
    }
    return null;
  }

  public boolean getResourceName(int resID, boolean allowUtf8, ResourceName outName) {
    if (mError != NO_ERROR) {
      return false;
    }

    final int p = getResourcePackageIndex(resID);
    final int t = Res_GETTYPE(resID);
    final int e = Res_GETENTRY(resID);

    if (p < 0) {
      if (Res_GETPACKAGE(resID)+1 == 0) {
        ALOGW("No package identifier when getting name for resource number 0x%08x", resID);
      }
      return false;
    }
    if (t < 0) {
      ALOGW("No type identifier when getting name for resource number 0x%08x", resID);
      return false;
    }

    final PackageGroup grp = mPackageGroups.get(p);
    if (grp == NULL) {
      ALOGW("Bad identifier when getting name for resource number 0x%08x", resID);
      return false;
    }

    Entry entry = new Entry();
    int err = getEntry(grp, t, e, null, entry);
    if (err != NO_ERROR) {
      return false;
    }

    outName.packageName = grp.name;
    outName.type = entry.typeStr.string();
    if (outName.type == null) {
      return false;
    }
    outName.name = entry.keyStr.string();
    if (outName.name == null) {
      return false;
    }

    return true;
  }

  String getResourceName(int resId) {
    ResourceName outName = new ResourceName();
    if (getResourceName(resId, true, outName)) {
      return outName.toString();
    }
    throw new IllegalArgumentException("Unknown resource id " + resId);
  }

  // A group of objects describing a particular resource package.
  // The first in 'package' is always the root object (from the resource
  // table that defined the package); the ones after are skins on top of it.
  // from ResourceTypes.cpp struct ResTable::PackageGroup
  public static class PackageGroup
  {
    public PackageGroup(
        ResTable _owner, final String _name, int _id,
        boolean appAsLib, boolean _isSystemAsset, boolean _isDynamic)
//        : owner(_owner)
//        , name(_name)
//        , id(_id)
//        , largestTypeId(0)
//        , dynamicRefTable(static_cast<uint8_t>(_id), appAsLib)
//        , isSystemAsset(_isSystemAsset)
    {
      this.owner = _owner;
      this.name = _name;
      this.id = _id;
      this.dynamicRefTable = new DynamicRefTable((byte) _id, appAsLib);
      this.isSystemAsset = _isSystemAsset;
      this.isDynamic = _isDynamic;
    }

//    ~PackageGroup() {
//      clearBagCache();
//      final int numTypes = types.size();
//      for (int i = 0; i < numTypes; i++) {
//        final List<DataType> typeList = types.get(i);
//        final int numInnerTypes = typeList.size();
//        for (int j = 0; j < numInnerTypes; j++) {
//          if (typeList.get(j)._package_.owner == owner) {
//            delete typeList[j];
//          }
//        }
//        typeList.clear();
//      }
//
//      final int N = packages.size();
//      for (int i=0; i<N; i++) {
//        ResTable_package pkg = packages[i];
//        if (pkg.owner == owner) {
//          delete pkg;
//        }
//      }
//    }

    /**
     * Clear all cache related data that depends on parameters/configuration.
     * This includes the bag caches and filtered types.
     */
    void clearBagCache() {
//      for (int i = 0; i < typeCacheEntries.size(); i++) {
//        if (kDebugTableNoisy) {
//          printf("type=0x%x\n", i);
//        }
//        final List<DataType> typeList = types.get(i);
//        if (!typeList.isEmpty()) {
//          TypeCacheEntry cacheEntry = typeCacheEntries.editItemAt(i);
//
//          // Reset the filtered configurations.
//          cacheEntry.filteredConfigs.clear();
//
//          bag_set[][] typeBags = cacheEntry.cachedBags;
//          if (kDebugTableNoisy) {
//            printf("typeBags=%s\n", typeBags);
//          }
//
//          if (isTruthy(typeBags)) {
//            final int N = typeList.get(0).entryCount;
//            if (kDebugTableNoisy) {
//              printf("type.entryCount=0x%x\n", N);
//            }
//            for (int j = 0; j < N; j++) {
//              if (typeBags[j] && typeBags[j] != (bag_set *) 0xFFFFFFFF){
//                free(typeBags[j]);
//              }
//            }
//            free(typeBags);
//            cacheEntry.cachedBags = NULL;
//          }
//        }
//      }
    }

    private void printf(String message, Object... arguments) {
      System.out.print(String.format(message, arguments));
    }

//    long findType16(final String type, int len) {
//      final int N = packages.size();
//      for (int i = 0; i < N; i++) {
//        sint index = packages[i].typeStrings.indexOfString(type, len);
//        if (index >= 0) {
//          return index + packages[i].typeIdOffset;
//        }
//      }
//      return -1;
//    }

    final ResTable owner;
    final String name;
    final int id;

    // This is mainly used to keep track of the loaded packages
    // and to clean them up properly. Accessing resources happens from
    // the 'types' array.
    List<Package> packages = new ArrayList<>();

    public final Map<Integer, List<Type>> types = new HashMap<>();

    byte largestTypeId;

    // Cached objects dependent on the parameters/configuration of this ResTable.
    // Gets cleared whenever the parameters/configuration changes.
    // These are stored here in a parallel structure because the data in `types` may
    // be shared by other ResTable's (framework resources are shared this way).
    ByteBucketArray<TypeCacheEntry> typeCacheEntries =
        new ByteBucketArray<TypeCacheEntry>(new TypeCacheEntry()) {
          @Override
          TypeCacheEntry newInstance() {
            return new TypeCacheEntry();
          }
        };

    // The table mapping dynamic references to resolved references for
    // this package group.
    // TODO: We may be able to support dynamic references in overlays
    // by having these tables in a per-package scope rather than
    // per-package-group.
    DynamicRefTable dynamicRefTable;

    // If the package group comes from a system asset. Used in
    // determining non-system locales.
    final boolean isSystemAsset;
    final boolean isDynamic;
  }

  // --------------------------------------------------------------------
// --------------------------------------------------------------------
// --------------------------------------------------------------------

//  struct ResTable::Header
  public static class Header
  {
//    Header(ResTable* _owner) : owner(_owner), ownedData(NULL), header(NULL),
//      resourceIDMap(NULL), resourceIDMapSize(0) { }

    public Header(ResTable owner) {
      this.owner = owner;
    }

//    ~Header()
//    {
//      free(resourceIDMap);
//    }

    ResTable            owner;
    byte[]                           ownedData;
    ResTable_header header;
    int                          size;
    int                  dataEnd;
    int                          index;
    int                         cookie;

    ResStringPool                   values = new ResStringPool();
    int[]                       resourceIDMap;
    int                          resourceIDMapSize;
  };

  public static class Entry {
    ResTable_config config;
    ResTable_entry entry;
    ResTable_type type;
    int specFlags;
    Package _package_;

    StringPoolRef typeStr;
    StringPoolRef keyStr;
  }

  // struct ResTable::DataType
  public static class Type {

    final Header header;
    final Package _package_;
    public final int entryCount;
    public ResTable_typeSpec typeSpec;
    public int[] typeSpecFlags;
    public IdmapEntries idmapEntries = new IdmapEntries();
    public List<ResTable_type> configs;

    public Type(final Header _header, final Package _package, int count)
  //        : header(_header), package(_package), entryCount(count),
  //  typeSpec(NULL), typeSpecFlags(NULL) { }
    {
      this.header = _header;
      _package_ = _package;
      this.entryCount = count;
      this.typeSpec = null;
      this.typeSpecFlags = null;
      this.configs = new ArrayList<>();
    }
  }

//  struct ResTable::Package
  public static class Package {
//    Package(ResTable* _owner, final Header* _header, final ResTable_package* _package)
//        : owner(_owner), header(_header), package(_package), typeIdOffset(0) {
//    if (dtohs(package.header.headerSize) == sizeof(package)) {
//      // The package structure is the same size as the definition.
//      // This means it contains the typeIdOffset field.
//      typeIdOffset = package.typeIdOffset;
//    }

    public Package(ResTable owner, Header header, ResTable_package _package) {
      this.owner = owner;
      this.header = header;
      this._package_ = _package;
    }

    final ResTable owner;
    final Header header;
    final ResTable_package _package_;

    ResStringPool typeStrings = new ResStringPool();
    ResStringPool keyStrings = new ResStringPool();

    int typeIdOffset;
  };

  public static class bag_entry {
    public int stringBlock;
    public ResTable_map map = new ResTable_map();
  }

  public void lock() {
    try {
      mLock.acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void unlock() {
    mLock.release();
  }

  public int lockBag(int resID, Ref<bag_entry[]> outBag) {
    lock();

    int err = getBagLocked(resID, outBag, null);
    if (err < NO_ERROR) {
      //printf("*** get failed!  unlocking\n");
      mLock.release();
    }
    return err;
  }

  public int getBagLocked(int resID, Ref<bag_entry[]> outBag, Ref<Integer> outTypeSpecFlags) {
    if (mError != NO_ERROR) {
      return mError;
    }

    final int p = getResourcePackageIndex(resID);
    final int t = Res_GETTYPE(resID);
    final int e = Res_GETENTRY(resID);

    if (p < 0) {
      ALOGW("Invalid package identifier when getting bag for resource number 0x%08x", resID);
      return BAD_INDEX;
    }
    if (t < 0) {
      ALOGW("No type identifier when getting bag for resource number 0x%08x", resID);
      return BAD_INDEX;
    }

    //printf("Get bag: id=0x%08x, p=%d, t=%d\n", resID, p, t);
    PackageGroup grp = mPackageGroups.get(p);
    if (grp == NULL) {
      ALOGW("Bad identifier when getting bag for resource number 0x%08x", resID);
      return BAD_INDEX;
    }

    final List<Type> typeConfigs = getOrDefault(grp.types, t, Collections.emptyList());
    if (typeConfigs.isEmpty()) {
      ALOGW("Type identifier 0x%x does not exist.", t+1);
      return BAD_INDEX;
    }

    final int NENTRY = typeConfigs.get(0).entryCount;
    if (e >= (int)NENTRY) {
      ALOGW("Entry identifier 0x%x is larger than entry count 0x%x",
          e, (int)typeConfigs.get(0).entryCount);
      return BAD_INDEX;
    }

    // First see if we've already computed this bag...
    TypeCacheEntry cacheEntry = grp.typeCacheEntries.editItemAt(t);
    bag_set[] typeSet = cacheEntry.cachedBags;
    // todo cache
//    if (isTruthy(typeSet)) {
//      bag_set set = typeSet[e];
//      if (isTruthy(set)) {
//        if (set != (bag_set) 0xFFFFFFFF){
//        if (set != SENTINEL_BAG_SET){
//          if (outTypeSpecFlags != NULL) {
//                    outTypeSpecFlags.set(set.typeSpecFlags);
//          }
//          outBag.set((bag_entry *) (set + 1);
//          if (kDebugTableSuperNoisy) {
//            ALOGI("Found existing bag for: 0x%x\n", resID);
//          }
//          return set.numAttrs;
//        }
//        ALOGW("Attempt to retrieve bag 0x%08x which is invalid or in a cycle.",
//            resID);
//        return BAD_INDEX;
//      }
//    }
//
    // Bag not found, we need to compute it!
    if (!isTruthy(typeSet)) {
      typeSet = new bag_set[NENTRY]; // (bag_set**)calloc(NENTRY, sizeof(bag_set*));
      //cacheEntry.cachedBags = typeSet;
    }
//
//    // Mark that we are currently working on this one.
//    typeSet[e] = (bag_set*)0xFFFFFFFF;
//    typeSet[e] = SENTINEL_BAG_SET;

    if (kDebugTableNoisy) {
      ALOGI("Building bag: %x\n", resID);
    }

    // Now collect all bag attributes
    Entry entry = new Entry();
    int err = getEntry(grp, t, e, mParams, entry);
    if (err != NO_ERROR) {
      return err;
    }
    final short entrySize = dtohs(entry.entry.size);
//    const uint32_t parent = entrySize >= sizeof(ResTable_map_entry)
//        ? dtohl(((const ResTable_map_entry*)entry.entry)->parent.ident) : 0;
//    const uint32_t count = entrySize >= sizeof(ResTable_map_entry)
//        ? dtohl(((const ResTable_map_entry*)entry.entry)->count) : 0;
    ResTable_map_entry mapEntry = entrySize >= ResTable_map_entry.BASE_SIZEOF ?
        new ResTable_map_entry(entry.entry.myBuf(), entry.entry.myOffset()) : null;
    final int parent = mapEntry != null ? dtohl(mapEntry.parent.ident) : 0;
    final int count = mapEntry != null ? dtohl(mapEntry.count) : 0;

    int N = count;

    if (kDebugTableNoisy) {
      ALOGI("Found map: size=%x parent=%x count=%d\n", entrySize, parent, count);

      // If this map inherits from another, we need to start
      // with its parent's values.  Otherwise start out empty.
      ALOGI("Creating new bag, entrySize=0x%08x, parent=0x%08x\n", entrySize, parent);
    }

    // This is what we are building.
    bag_set set;

    if (isTruthy(parent)) {
      final Ref<Integer> resolvedParent = new Ref<>(parent);

      // Bags encode a parent reference without using the standard
      // Res_value structure. That means we must always try to
      // resolve a parent reference in case it is actually a
      // TYPE_DYNAMIC_REFERENCE.
      err = grp.dynamicRefTable.lookupResourceId(resolvedParent);
      if (err != NO_ERROR) {
        ALOGE("Failed resolving bag parent id 0x%08x", parent);
        return UNKNOWN_ERROR;
      }

      final Ref<bag_entry[]> parentBag = new Ref<>(null);
      final Ref<Integer> parentTypeSpecFlags = new Ref<>(0);
      final int NP = getBagLocked(resolvedParent.get(), parentBag, parentTypeSpecFlags);
      final int NT = ((NP >= 0) ? NP : 0) + N;
      set = new bag_set(NT);
      if (NP > 0) {
        set.copyFrom(parentBag.get(), NP);
        set.numAttrs = NP;
        if (kDebugTableNoisy) {
          ALOGI("Initialized new bag with %d inherited attributes.\n", NP);
        }
      } else {
        if (kDebugTableNoisy) {
          ALOGI("Initialized new bag with no inherited attributes.\n");
        }
        set.numAttrs = 0;
      }
      set.availAttrs = NT;
      set.typeSpecFlags = parentTypeSpecFlags.get();
    } else {
      set = new bag_set(N);
      set.numAttrs = 0;
      set.availAttrs = N;
      set.typeSpecFlags = 0;
    }

    set.typeSpecFlags |= entry.specFlags;

    // Now merge in the new attributes...
//    int curOff = (reinterpret_cast<uintptr_t>(entry.entry) - reinterpret_cast<uintptr_t>(entry.type))
//        + dtohs(entry.entry.size);
    int curOff = entry.entry.myOffset() - entry.type.myOffset() + entry.entry.size;
    ResTable_map map;
//    bag_entry* entries = (bag_entry*)(set+1);
    bag_entry[] entries = set.bag_entries;
    int curEntry = 0;
    int pos = 0;
    if (kDebugTableNoisy) {
      ALOGI("Starting with set %s, entries=%s, avail=0x%x\n", set, entries, set.availAttrs);
    }
    while (pos < count) {
      if (kDebugTableNoisy) {
//        ALOGI("Now at %s\n", curOff);
        ALOGI("Now at %s\n", curEntry);
      }

      if (curOff > (dtohl(entry.type.header.size)- ResTable_map.SIZEOF)) {
        ALOGW("ResTable_map at %d is beyond type chunk data %d",
            (int)curOff, dtohl(entry.type.header.size));
        return BAD_TYPE;
      }
//      map = (const ResTable_map*)(((const uint8_t*)entry.type) + curOff);
      map = new ResTable_map(entry.type.myBuf(), entry.type.myOffset() + curOff);
      N++;

      final Ref<Integer> newName = new Ref<>(htodl(map.name.ident));
      if (!Res_INTERNALID(newName.get())) {
        // Attributes don't have a resource id as the name. They specify
        // other data, which would be wrong to change via a lookup.
        if (grp.dynamicRefTable.lookupResourceId(newName) != NO_ERROR) {
          ALOGE("Failed resolving ResTable_map name at %d with ident 0x%08x",
              (int) curEntry, (int) newName.get());
          return UNKNOWN_ERROR;
        }
      }

      boolean isInside;
      int oldName = 0;
      while ((isInside=(curEntry < set.numAttrs))
          && (oldName=entries[curEntry].map.name.ident) < newName.get()) {
        if (kDebugTableNoisy) {
          ALOGI("#0x%x: Keeping existing attribute: 0x%08x\n",
              curEntry, entries[curEntry].map.name.ident);
        }
        curEntry++;
      }

      if ((!isInside) || oldName != newName.get()) {
        // This is a new attribute...  figure out what to do with it.
        if (set.numAttrs >= set.availAttrs) {
          // Need to alloc more memory...
                final int newAvail = set.availAttrs+N;
//          set = (bag_set[])realloc(set,
//              sizeof(bag_set)
//                  + sizeof(bag_entry)*newAvail);
          set.resizeBagEntries(newAvail);
          set.availAttrs = newAvail;
//          entries = (bag_entry*)(set+1);
          entries = set.bag_entries;
          if (kDebugTableNoisy) {
            ALOGI("Reallocated set %s, entries=%s, avail=0x%x\n",
                set, entries, set.availAttrs);
          }
        }
        if (isInside) {
          // Going in the middle, need to make space.
//          memmove(entries+curEntry+1, entries+curEntry,
//              sizeof(bag_entry)*(set.numAttrs-curEntry));
          System.arraycopy(entries, curEntry, entries, curEntry + 1, set.numAttrs - curEntry);
          entries[curEntry] = null;
          set.numAttrs++;
        }
        if (kDebugTableNoisy) {
          ALOGI("#0x%x: Inserting new attribute: 0x%08x\n", curEntry, newName.get());
        }
      } else {
        if (kDebugTableNoisy) {
          ALOGI("#0x%x: Replacing existing attribute: 0x%08x\n", curEntry, oldName);
        }
      }

      bag_entry cur = entries[curEntry];
      if (cur == null) {
        cur = entries[curEntry] = new bag_entry();
      }

      cur.stringBlock = entry._package_.header.index;
      cur.map.name.ident = newName.get();
//      cur->map.value.copyFrom_dtoh(map->value);
      cur.map.value = map.value;
      final Ref<Res_value> valueRef = new Ref<>(cur.map.value);
      err = grp.dynamicRefTable.lookupResourceValue(valueRef);
      cur.map.value = map.value = valueRef.get();
      if (err != NO_ERROR) {
        ALOGE("Reference item(0x%08x) in bag could not be resolved.", cur.map.value.data);
        return UNKNOWN_ERROR;
      }

      if (kDebugTableNoisy) {
        ALOGI("Setting entry #0x%x %s: block=%d, name=0x%08d, type=%d, data=0x%08x\n",
            curEntry, cur, cur.stringBlock, cur.map.name.ident,
            cur.map.value.dataType, cur.map.value.data);
      }

      // On to the next!
      curEntry++;
      pos++;
      final int size = dtohs(map.value.size);
//      curOff += size + sizeof(*map)-sizeof(map->value);
      curOff += size + ResTable_map.SIZEOF-Res_value.SIZEOF;
    };

    if (curEntry > set.numAttrs) {
      set.numAttrs = curEntry;
    }

    // And this is it...
    typeSet[e] = set;
    if (isTruthy(set)) {
      if (outTypeSpecFlags != NULL) {
        outTypeSpecFlags.set(set.typeSpecFlags);
      }
      outBag.set(set.bag_entries);
      if (kDebugTableNoisy) {
        ALOGI("Returning 0x%x attrs\n", set.numAttrs);
      }
      return set.numAttrs;
    }
    return BAD_INDEX;
  }

  public void unlockBag(Ref<bag_entry[]> bag) {
    unlock();
  }

  static class bag_set {
    int numAttrs;    // number in array
    int availAttrs;  // total space in array
    int typeSpecFlags;
    // Followed by 'numAttr' bag_entry structures.

    bag_entry[] bag_entries;

    public bag_set(int entryCount) {
      bag_entries = new bag_entry[entryCount];
    }

    public void copyFrom(bag_entry[] parentBag, int count) {
      for (int i = 0; i < count; i++) {
        bag_entries[i] = parentBag[i];
      }
    }

    public void resizeBagEntries(int newEntryCount) {
      bag_entry[] newEntries = new bag_entry[newEntryCount];
      System.arraycopy(bag_entries, 0, newEntries, 0, Math.min(bag_entries.length, newEntryCount));
      bag_entries = newEntries;
    }
  };

  /**
   * Configuration dependent cached data. This must be cleared when the configuration is
   * changed (setParameters).
   */
  static class TypeCacheEntry {
//    TypeCacheEntry() : cachedBags(NULL) {}

    // Computed attribute bags for this type.
//    bag_set** cachedBags;
    bag_set[] cachedBags;

    // Pre-filtered list of configurations (per asset path) that match the parameters set on this
    // ResTable.
    List<List<ResTable_type>> filteredConfigs;
  };


  private int Res_MAKEID(int packageId, int typeId, int entryId) {
    return (((packageId+1)<<24) | (((typeId+1)&0xFF)<<16) | (entryId&0xFFFF));
  }

  // struct resource_name
  public static class ResourceName {
    public String packageName;
    public String type;
    public String name;

    @Override
    public String toString() {
      return packageName.trim() + '@' + type + ':' + name;
    }
  }

  private interface Function<K, V> {
    V apply(K key);
  }

  static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> vFunction) {
    V v = map.get(key);
    if (v == null) {
      v = vFunction.apply(key);
      map.put(key, v);
    }
    return v;
  }

  static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
    V v;
    return (((v = map.get(key)) != null) || map.containsKey(key)) ? v : defaultValue;
  }
}
