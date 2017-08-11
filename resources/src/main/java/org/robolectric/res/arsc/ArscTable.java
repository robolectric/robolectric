package org.robolectric.res.arsc;

import static org.robolectric.res.android.Status.BAD_INDEX;
import static org.robolectric.res.android.Status.NO_ERROR;
import static org.robolectric.res.android.Util.dtohl;

import com.google.common.collect.Iterables;
import java.util.List;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.android.DynamicRefTable;
import org.robolectric.res.android.Entry;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.ResTableConfig;
import org.robolectric.res.android.ResTableEntry;
import org.robolectric.res.android.ResTablePackage;
import org.robolectric.res.android.ResTableType;
import org.robolectric.res.android.ResTable_type;
import org.robolectric.res.android.Status;
import org.robolectric.res.android.Util;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.StringPoolChunk;
import org.robolectric.res.arsc.Chunk.TableChunk;

public class ArscTable {

  private TableChunk chunk;

  public ArscTable(TableChunk chunk) {
    this.chunk = chunk;
  }

  // transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp

  private static final int IDMAP_MAGIC             = 0x504D4449;
  private static final int IDMAP_CURRENT_VERSION   = 0x00000001;

  private static final int APP_PACKAGE_ID      = 0x7f;
  private static final int SYS_PACKAGE_ID      = 0x01;

  private static final boolean kDebugStringPoolNoisy = false;
  private static final boolean kDebugXMLNoisy = false;
  private static final boolean kDebugTableNoisy = false;
  private static final boolean kDebugTableGetEntry = false;
  private static final boolean kDebugTableSuperNoisy = false;
  private static final boolean kDebugLoadTableNoisy = false;
  private static final boolean kDebugLoadTableSuperNoisy = false;
  private static final boolean kDebugTableTheme = false;
  private static final boolean kDebugResXMLTree = false;
  private static final boolean kDebugLibNoisy = false;

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
    { }
  
    ~PackageGroup() {
        clearBagCache();
        final int numTypes = types.size();
        for (int i = 0; i < numTypes; i++) {
            final List<ResTableType> typeList = types.get(i);
            final int numInnerTypes = typeList.size();
            for (int j = 0; j < numInnerTypes; j++) {
                if (typeList[j]._package_.owner == owner) {
                    delete typeList[j];
                }
            }
        }
  
        final int N = packages.size();
        for (int i=0; i<N; i++) {
            ResTablePackage pkg = packages[i];
            if (pkg.owner == owner) {
                delete pkg;
            }
        }
    }
  
    /**
     * Clear all cache related data that depends on parameters/configuration.
     * This includes the bag caches and filtered types.
     */
    void clearBagCache() {
        for (int i = 0; i < typeCacheEntries.size(); i++) {
            if (kDebugTableNoisy) {
                printf("type=%zu\n", i);
            }
            final List<ResTableType>& typeList = types[i];
            if (!typeList.isEmpty()) {
                TypeCacheEntry& cacheEntry = typeCacheEntries.editItemAt(i);
  
                // Reset the filtered configurations.
                cacheEntry.filteredConfigs.clear();
  
                bag_set** typeBags = cacheEntry.cachedBags;
                if (kDebugTableNoisy) {
                    printf("typeBags=%p\n", typeBags);
                }
  
                if (typeBags) {
                    final int N = typeList[0].entryCount;
                    if (kDebugTableNoisy) {
                        printf("type.entryCount=%zu\n", N);
                    }
                    for (int j = 0; j < N; j++) {
                        if (typeBags[j] && typeBags[j] != (bag_set*)0xFFFFFFFF) {
                            free(typeBags[j]);
                        }
                    }
                    free(typeBags);
                    cacheEntry.cachedBags = NULL;
                }
            }
        }
    }
  
    long findType16(final String type, int len) {
        final int N = packages.size();
        for (int i = 0; i < N; i++) {
            ssize_t index = packages[i].typeStrings.indexOfString(type, len);
            if (index >= 0) {
                return index + packages[i].typeIdOffset;
            }
        }
        return -1;
    }
  
    final ResTable           owner;
    final String                   name;
    final int                  id;
  
    // This is mainly used to keep track of the loaded packages
    // and to clean them up properly. Accessing resources happens from
    // the 'types' array.
    List<ResTablePackage>                packages;
  
    List<List<ResTableType>>       types;
  
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
  };
  
  private static final Object NULL = null;
  
  // transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
  Status getEntry(
        final PackageGroup packageGroup, int typeIndex, int entryIndex,
        final ResTableConfig config,
        Ref<Entry> outEntryRef)
  {
    final List<ResTable_type> typeList = packageGroup.types[typeIndex];
    if (typeList.isEmpty()) {
      ALOGV("Skipping entry type index 0x%02x because type is NULL!\n", typeIndex);
      return BAD_TYPE;
    }

    ResTable_type bestType = null;
    int bestOffset = ResTableType.NO_ENTRY;
    final ResTablePackage bestPackage = null;
    int specFlags = 0;
    byte actualTypeIndex = (byte) typeIndex;
    ResTableConfig bestConfig;
    memset(&bestConfig, 0, sizeof(bestConfig));

    // Iterate over the Types of each package.
    final int typeCount = typeList.size();
    for (int i = 0; i < typeCount; i++) {
      final ResTable_type typeSpec = typeList.get(i);

      int realEntryIndex = entryIndex;
      int realTypeIndex = typeIndex;
      boolean currentTypeIsOverlay = false;

      // Runtime overlay packages provide a mapping of app resource
      // ID to package resource ID.
      if (typeSpec.idmapEntries.hasEntries()) {
        short overlayEntryIndex;
        if (typeSpec.idmapEntries.lookup(entryIndex, &overlayEntryIndex) != NO_ERROR) {
          // No such mapping exists
          continue;
        }
        realEntryIndex = overlayEntryIndex;
        realTypeIndex = typeSpec.idmapEntries.overlayTypeId() - 1;
        currentTypeIsOverlay = true;
      }

      if (static_cast<int>(realEntryIndex) >= typeSpec.entryCount) {
        ALOGW("For resource 0x%08x, entry index(%d) is beyond type entryCount(%d)",
            Res_MAKEID(packageGroup.id - 1, typeIndex, entryIndex),
            entryIndex, static_cast<int>(typeSpec.entryCount));
        // We should normally abort here, but some legacy apps declare
        // resources in the 'android' package (old bug in AAPT).
        continue;
      }

      // Aggregate all the flags for each package that defines this entry.
      if (typeSpec.typeSpecFlags != NULL) {
        specFlags |= dtohl(typeSpec.typeSpecFlags[realEntryIndex]);
      } else {
        specFlags = -1;
      }

      final List<ResTable_type> candidateConfigs = &typeSpec.configs;

      std::shared_ptr<List<final ResTable_type*>> filteredConfigs;
      if (config && memcmp(&mParams, config, sizeof(mParams)) == 0) {
        // Grab the lock first so we can safely get the current filtered list.
        AutoMutex _lock(mFilteredConfigLock);

        // This configuration is equal to the one we have previously cached for,
        // so use the filtered configs.

        final TypeCacheEntry& cacheEntry = packageGroup.typeCacheEntries[typeIndex];
        if (i < cacheEntry.filteredConfigs.size()) {
          if (cacheEntry.filteredConfigs[i]) {
            // Grab a reference to the shared_ptr so it doesn't get destroyed while
            // going through this list.
            filteredConfigs = cacheEntry.filteredConfigs[i];

            // Use this filtered list.
            candidateConfigs = filteredConfigs.get();
          }
        }
      }

      final int numConfigs = candidateConfigs.size();
      for (int c = 0; c < numConfigs; c++) {
        final ResTable_type thisType = candidateConfigs.itemAt(c);
        if (thisType == NULL) {
          continue;
        }

        ResTableConfig thisConfig;
        thisConfig.copyFromDtoH(thisType.config);

        // Check to make sure this one is valid for the current parameters.
        if (config != NULL && !thisConfig.match(*config)) {
          continue;
        }

        // Check if there is the desired entry in this type.
        final uint32_t* final eindex = reinterpret_cast<final uint32_t*>(
            reinterpret_cast<final uint8_t*>(thisType) + dtohs(thisType.header.headerSize));

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
        actualTypeIndex = realTypeIndex;

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

    if (bestOffset > (dtohl(bestType.header.size)-sizeof(ResTable_entry))) {
      ALOGW("ResTable_entry at 0x%x is beyond type chunk data 0x%x",
          bestOffset, dtohl(bestType.header.size));
      return BAD_TYPE;
    }
    if ((bestOffset & 0x3) != 0) {
      ALOGW("ResTable_entry at 0x%x is not on an integer boundary", bestOffset);
      return BAD_TYPE;
    }

    final ResTable_entry* final entry = reinterpret_cast<final ResTable_entry*>(
      reinterpret_cast<final uint8_t*>(bestType) + bestOffset);
    if (dtohs(entry.size) < sizeof(*entry)) {
    ALOGW("ResTable_entry size 0x%x is too small", dtohs(entry.size));
    return BAD_TYPE;
  }

    if (outEntryRef != null) {
      Entry outEntry = new Entry();
      outEntry.entry = entry;
      outEntry.config = bestConfig;
      outEntry.type = bestType;
      outEntry.specFlags = specFlags;
      outEntry._package_ = bestPackage;
      outEntry.typeStr = StringPoolRef(&bestPackage.typeStrings, actualTypeIndex - bestPackage.typeIdOffset);
      outEntry.keyStr = StringPoolRef(&bestPackage.keyStrings, dtohl(entry.key.index));
      outEntryRef.set(outEntry);
    }
    return NO_ERROR;
  }

  public ResTableEntry getEntry(int resId, int configDensity) {
    List<TypeChunk> types = chunk.getPackageChunk(ResourceIds.getPackageIdentifier(resId))
        .getTypes(ResourceIds.getTypeIdentifier(resId));

    TypeChunk onlyElement = Iterables.getFirst(types, null);
    List<ResTableEntry> entries = onlyElement.getEntries();

    return entries.get(ResourceIds.getEntryIdentifier(resId));
  }

  public String getTypeName(int resId) {
    int typeId = ResourceIds.getTypeIdentifier(resId);
    int packageId = ResourceIds.getPackageIdentifier(resId);
    StringPoolChunk stringPool = chunk.getPackageChunk(packageId).getTypeStringPool();
    return stringPool.getString(typeId - 1); // TT in PPTTEEEE is 1 indexed
  }

  public String getPackageName(int resId) {
    int packageId = ResourceIds.getPackageIdentifier(resId);
    String rawName = chunk.getPackageChunk(packageId).getName();
    int firstNull = rawName.indexOf(0);
    return rawName.substring(0, firstNull);
  }
}

