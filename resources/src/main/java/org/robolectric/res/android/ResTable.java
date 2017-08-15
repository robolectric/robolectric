package org.robolectric.res.android;

import static org.robolectric.res.android.Status.BAD_INDEX;
import static org.robolectric.res.android.Status.BAD_TYPE;
import static org.robolectric.res.android.Status.NO_ERROR;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h
public class ResTable {
  private static final int IDMAP_MAGIC             = 0x504D4449;
  private static final int IDMAP_CURRENT_VERSION   = 0x00000001;

  private static final int APP_PACKAGE_ID      = 0x7f;
  private static final int SYS_PACKAGE_ID      = 0x01;

  private static final boolean kDebugStringPoolNoisy = false;
  private static final boolean kDebugXMLNoisy = false;
  private static final boolean kDebugTableNoisy = false;
  private static final boolean kDebugTableGetEntry = false;
  static final boolean kDebugTableSuperNoisy = false;
  private static final boolean kDebugLoadTableNoisy = false;
  private static final boolean kDebugLoadTableSuperNoisy = false;
  private static final boolean kDebugTableTheme = false;
  private static final boolean kDebugResXMLTree = false;
  private static final boolean kDebugLibNoisy = false;

  private static final Object NULL = null;

  Object               mLock;

  // Mutex that controls access to the list of pre-filtered configurations
  // to check when looking up entries.
  // When iterating over a bag, the mLock mutex is locked. While mLock is locked,
  // we do resource lookups.
  // Mutex is not reentrant, so we must use a different lock than mLock.
  Object               mFilteredConfigLock;

  Status                    mError;

  ResTableConfig             mParams;

  // Array of all resource tables.
  List<Header>             mHeaders;

  // Array of packages in all resource tables.
  List<PackageGroup>       mPackageGroups;

  // Mapping from resource package IDs to indices into the internal
  // package array.
  byte[]                     mPackageMap = new byte[256];

  byte                     mNextPackageId;

  Status getEntry(
      final PackageGroup packageGroup, int typeIndex, int entryIndex,
      final ResTableConfig config,
      Ref<Entry> outEntryRef)
  {
    final List<Type> typeList = packageGroup.types.get(typeIndex);
    if (typeList.isEmpty()) {
      Util.ALOGV("Skipping entry type index 0x%02x because type is NULL!\n", typeIndex);
      return BAD_TYPE;
    }

    ResTableType bestType = null;
    int bestOffset = ResTableType.NO_ENTRY;
    Package bestPackage = null;
    int specFlags = 0;
    byte actualTypeIndex = (byte) typeIndex;
    ResTableConfig bestConfig = null;
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
        Ref<Short> overlayEntryIndex = new Ref<>((short) 0);
        if (typeSpec.idmapEntries.lookup(entryIndex, overlayEntryIndex) != NO_ERROR) {
          // No such mapping exists
          continue;
        }
        realEntryIndex = overlayEntryIndex.get();
        realTypeIndex = typeSpec.idmapEntries.overlayTypeId() - 1;
        currentTypeIsOverlay = true;
      }

      if (((int) realEntryIndex) >= typeSpec.entryCount) {
        Util.ALOGW("For resource 0x%08x, entry index(%d) is beyond type entryCount(%d)",
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

      List<ResTableType> candidateConfigs = typeSpec.configs;

      List<ResTableType> filteredConfigs;
      if (isTruthy(config) && Objects.equals(mParams, config)) {
        // Grab the lock first so we can safely get the current filtered list.
        synchronized (mFilteredConfigLock) {
          // This configuration is equal to the one we have previously cached for,
          // so use the filtered configs.

          final TypeCacheEntry cacheEntry = packageGroup.typeCacheEntries.get(typeIndex);
          if (i < cacheEntry.filteredConfigs.size()) {
            if (isTruthy(cacheEntry.filteredConfigs.get(i))) {
              // Grab a reference to the shared_ptr so it doesn't get destroyed while
              // going through this list.
              filteredConfigs = cacheEntry.filteredConfigs.get(i);

              // Use this filtered list.
              candidateConfigs = filteredConfigs;
            }
          }
        }
      }

      final int numConfigs = candidateConfigs.size();
      for (int c = 0; c < numConfigs; c++) {
        final ResTableType thisType = candidateConfigs.get(c);
        if (thisType == NULL) {
          continue;
        }

        final ResTableConfig thisConfig;
//        thisConfig.copyFromDtoH(thisType.config);
        thisConfig = ResTableConfig.fromDtoH(thisType.config);

        // Check to make sure this one is valid for the current parameters.
        if (config != NULL && !thisConfig.match(config)) {
          continue;
        }

        // Check if there is the desired entry in this type.
//        const uint32_t* const eindex = reinterpret_cast<const uint32_t*>(
//            reinterpret_cast<const uint8_t*>(thisType) + dtohs(thisType->header.headerSize));
        final int[] eindex = thisType.eindex(dtohs(thisType.header.headerSize));

        int thisOffset = dtohl(eindex[realEntryIndex]);
        if (thisOffset == ResTableType.NO_ENTRY) {
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
    int sizeOfResTableEntry = 2 // uint16_t size
        + 2  // uint16_t flags
        + 4; // struct ResStringPool_ref key: uint32_t index
    if (bestOffset > (dtohl(bestType.header.size)- sizeOfResTableEntry)) {
      Util.ALOGW("ResTable_entry at 0x%x is beyond type chunk data 0x%x",
          bestOffset, dtohl(bestType.header.size));
      return BAD_TYPE;
    }
    if ((bestOffset & 0x3) != 0) {
      Util.ALOGW("ResTable_entry at 0x%x is not on an integer boundary", bestOffset);
      return BAD_TYPE;
    }

//    final ResTable_entry* final entry = reinterpret_cast<final ResTable_entry*>(
//      reinterpret_cast<final uint8_t*>(bestType) + bestOffset);
    final ResTableEntry entry = bestType.getEntry(bestOffset);
//    if (dtohs(entry.size) < sizeof(*entry)) {
    int ptrSize = 4;
    if (dtohs(entry.size) < ptrSize) {
    Util.ALOGW("ResTable_entry size 0x%x is too small", dtohs(entry.size));
    return BAD_TYPE;
  }

    if (outEntryRef != null) {
      Entry outEntry = new Entry();
      outEntry.entry = entry;
      outEntry.config = bestConfig;
      outEntry.type = bestType;
      outEntry.specFlags = specFlags;
      outEntry._package_ = bestPackage;
      outEntry.typeStr = new StringPoolRef(bestPackage.typeStrings, actualTypeIndex - bestPackage.typeIdOffset);
      outEntry.keyStr = new StringPoolRef(bestPackage.keyStrings, dtohl(entry.key));
      outEntryRef.set(outEntry);
    }
    return NO_ERROR;
  }

  // A group of objects describing a particular resource package.
  // The first in 'package' is always the root object (from the resource
  // table that defined the package); the ones after are skins on top of it.
  static class PackageGroup
  {
    PackageGroup(
        ResTable _owner, final String _name, int _id,
        boolean appAsLib, boolean _isSystemAsset)
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
    }

//    ~PackageGroup() {
//      clearBagCache();
//      final int numTypes = types.size();
//      for (int i = 0; i < numTypes; i++) {
//        final List<Type> typeList = types.get(i);
//        final int numInnerTypes = typeList.size();
//        for (int j = 0; j < numInnerTypes; j++) {
//          if (typeList.get(j)._package_.owner == owner) {
//            delete typeList[j];
//          }
//        }
//      }
//
//      final int N = packages.size();
//      for (int i=0; i<N; i++) {
//        ResTablePackage pkg = packages[i];
//        if (pkg.owner == owner) {
//          delete pkg;
//        }
//      }
//    }

//    /**
//     * Clear all cache related data that depends on parameters/configuration.
//     * This includes the bag caches and filtered types.
//     */
//    void clearBagCache() {
//      for (int i = 0; i < typeCacheEntries.size(); i++) {
//        if (kDebugTableNoisy) {
//          printf("type=%zu\n", i);
//        }
//        final List<Type> typeList = types.get(i);
//        if (!typeList.isEmpty()) {
//          TypeCacheEntry cacheEntry = typeCacheEntries.editItemAt(i);
//
//          // Reset the filtered configurations.
//          cacheEntry.filteredConfigs.clear();
//
//          bag_set[][] typeBags = cacheEntry.cachedBags;
//          if (kDebugTableNoisy) {
//            printf("typeBags=%p\n", typeBags);
//          }
//
//          if (isTruthy(typeBags)) {
//            final int N = typeList.get(0).entryCount;
//            if (kDebugTableNoisy) {
//              printf("type.entryCount=%zu\n", N);
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
//    }

    private void printf(String message, Object... arguments) {
      System.out.print(String.format(message, arguments));
    }

//    long findType16(final String type, int len) {
//      final int N = packages.size();
//      for (int i = 0; i < N; i++) {
//        ssize_t index = packages[i].typeStrings.indexOfString(type, len);
//        if (index >= 0) {
//          return index + packages[i].typeIdOffset;
//        }
//      }
//      return -1;
//    }

    final ResTable           owner;
    final String                   name;
    final int                  id;

    // This is mainly used to keep track of the loaded packages
    // and to clean them up properly. Accessing resources happens from
    // the 'types' array.
    List<ResTablePackage>                packages;

    List<List<Type>>       types;

    byte                         largestTypeId;

    // Cached objects dependent on the parameters/configuration of this ResTable.
    // Gets cleared whenever the parameters/configuration changes.
    // These are stored here in a parallel structure because the data in `types` may
    // be shared by other ResTable's (framework resources are shared this way).
    ByteBucketArray<TypeCacheEntry> typeCacheEntries;

    // The table mapping dynamic references to resolved references for
    // this package group.
    // TODO: We may be able to support dynamic references in overlays
    // by having these tables in a per-package scope rather than
    // per-package-group.
    DynamicRefTable dynamicRefTable;

    // If the package group comes from a system asset. Used in
    // determining non-system locales.
    final boolean                      isSystemAsset;
  }

  // transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp:3151

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
    Object                           ownedData;
    ResTableHeader          header;
    int                          size;
//    uint8_t*                  dataEnd;
    int                  dataEnd;
    int                          index;
    int                         cookie;

    ResStringPool                   values;
    int[]                       resourceIDMap;
    int                          resourceIDMapSize;
  };

  public static class Entry {
    ResTableConfig config;
    ResTableEntry entry;
    ResTableType type;
    int specFlags;
    Package _package_;

    StringPoolRef typeStr;
    StringPoolRef keyStr;
  }

  // struct ResTable::Type
  public static class Type {

    final Header header;
    final Package _package_;
    public final int entryCount;
    final ResTableTypeSpec typeSpec;
    public final int[] typeSpecFlags;
    public IdmapEntries                    idmapEntries;
    public List<ResTableType> configs;

    Type(final Header _header, final Package _package, int count)
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
//    Package(ResTable* _owner, const Header* _header, const ResTable_package* _package)
//        : owner(_owner), header(_header), package(_package), typeIdOffset(0) {
//    if (dtohs(package->header.headerSize) == sizeof(package)) {
//      // The package structure is the same size as the definition.
//      // This means it contains the typeIdOffset field.
//      typeIdOffset = package->typeIdOffset;
//    }

    public Package(ResTable owner, Header header, ResTablePackage _package) {
      this.owner = owner;
      this.header = header;
      this._package_ = _package;
    }

    final ResTable owner;
    final Header header;
    final ResTablePackage _package_;

    ResStringPool                   typeStrings;
    ResStringPool                   keyStrings;

    int                          typeIdOffset;
  };

  static class bag_set {
    int numAttrs;    // number in array
    int availAttrs;  // total space in array
    int typeSpecFlags;
    // Followed by 'numAttr' bag_entry structures.
  };

  /**
   * Configuration dependent cached data. This must be cleared when the configuration is
   * changed (setParameters).
   */
  static class TypeCacheEntry {
//    TypeCacheEntry() : cachedBags(NULL) {}

    // Computed attribute bags for this type.
//    bag_set** cachedBags;
    bag_set[][] cachedBags;

    // Pre-filtered list of configurations (per asset path) that match the parameters set on this
    // ResTable.
    List<List<ResTableType>> filteredConfigs;
  };


  private int Res_MAKEID(int packageId, int typeId, int entryId) {
    return (((packageId+1)<<24) | (((typeId+1)&0xFF)<<16) | (entryId&0xFFFF));
  }

}
