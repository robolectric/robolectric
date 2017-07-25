package org.robolectric.res.arsc;

import com.google.common.collect.Iterables;
import java.util.List;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.android.ResTableEntry;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.StringPoolChunk;
import org.robolectric.res.arsc.Chunk.TableChunk;

public class ArscTable {

  private TableChunk chunk;

  public ArscTable(TableChunk chunk) {
    this.chunk = chunk;
  }

  //status_t ResTable::getEntry(
//        const PackageGroup* packageGroup, int typeIndex, int entryIndex,
//        const ResTable_config* config,
//        Entry* outEntry) const
//{
//    const TypeList& typeList = packageGroup->types[typeIndex];
//    if (typeList.isEmpty()) {
//        ALOGV("Skipping entry type index 0x%02x because type is NULL!\n", typeIndex);
//        return BAD_TYPE;
//    }
//
//    const ResTable_type* bestType = NULL;
//    uint32_t bestOffset = ResTable_type::NO_ENTRY;
//    const Package* bestPackage = NULL;
//    uint32_t specFlags = 0;
//    uint8_t actualTypeIndex = typeIndex;
//    ResTable_config bestConfig;
//    memset(&bestConfig, 0, sizeof(bestConfig));
//
//    // Iterate over the Types of each package.
//    const size_t typeCount = typeList.size();
//    for (size_t i = 0; i < typeCount; i++) {
//        const Type* const typeSpec = typeList[i];
//
//        int realEntryIndex = entryIndex;
//        int realTypeIndex = typeIndex;
//        bool currentTypeIsOverlay = false;
//
//        // Runtime overlay packages provide a mapping of app resource
//        // ID to package resource ID.
//        if (typeSpec->idmapEntries.hasEntries()) {
//            uint16_t overlayEntryIndex;
//            if (typeSpec->idmapEntries.lookup(entryIndex, &overlayEntryIndex) != NO_ERROR) {
//                // No such mapping exists
//                continue;
//            }
//            realEntryIndex = overlayEntryIndex;
//            realTypeIndex = typeSpec->idmapEntries.overlayTypeId() - 1;
//            currentTypeIsOverlay = true;
//        }
//
//        if (static_cast<size_t>(realEntryIndex) >= typeSpec->entryCount) {
//            ALOGW("For resource 0x%08x, entry index(%d) is beyond type entryCount(%d)",
//                    Res_MAKEID(packageGroup->id - 1, typeIndex, entryIndex),
//                    entryIndex, static_cast<int>(typeSpec->entryCount));
//            // We should normally abort here, but some legacy apps declare
//            // resources in the 'android' package (old bug in AAPT).
//            continue;
//        }
//
//        // Aggregate all the flags for each package that defines this entry.
//        if (typeSpec->typeSpecFlags != NULL) {
//            specFlags |= dtohl(typeSpec->typeSpecFlags[realEntryIndex]);
//        } else {
//            specFlags = -1;
//        }
//
//        const Vector<const ResTable_type*>* candidateConfigs = &typeSpec->configs;
//
//        std::shared_ptr<Vector<const ResTable_type*>> filteredConfigs;
//        if (config && memcmp(&mParams, config, sizeof(mParams)) == 0) {
//            // Grab the lock first so we can safely get the current filtered list.
//            AutoMutex _lock(mFilteredConfigLock);
//
//            // This configuration is equal to the one we have previously cached for,
//            // so use the filtered configs.
//
//            const TypeCacheEntry& cacheEntry = packageGroup->typeCacheEntries[typeIndex];
//            if (i < cacheEntry.filteredConfigs.size()) {
//                if (cacheEntry.filteredConfigs[i]) {
//                    // Grab a reference to the shared_ptr so it doesn't get destroyed while
//                    // going through this list.
//                    filteredConfigs = cacheEntry.filteredConfigs[i];
//
//                    // Use this filtered list.
//                    candidateConfigs = filteredConfigs.get();
//                }
//            }
//        }
//
//        const size_t numConfigs = candidateConfigs->size();
//        for (size_t c = 0; c < numConfigs; c++) {
//            const ResTable_type* const thisType = candidateConfigs->itemAt(c);
//            if (thisType == NULL) {
//                continue;
//            }
//
//            ResTable_config thisConfig;
//            thisConfig.copyFromDtoH(thisType->config);
//
//            // Check to make sure this one is valid for the current parameters.
//            if (config != NULL && !thisConfig.match(*config)) {
//                continue;
//            }
//
//            // Check if there is the desired entry in this type.
//            const uint32_t* const eindex = reinterpret_cast<const uint32_t*>(
//                    reinterpret_cast<const uint8_t*>(thisType) + dtohs(thisType->header.headerSize));
//
//            uint32_t thisOffset = dtohl(eindex[realEntryIndex]);
//            if (thisOffset == ResTable_type::NO_ENTRY) {
//                // There is no entry for this index and configuration.
//                continue;
//            }
//
//            if (bestType != NULL) {
//                // Check if this one is less specific than the last found.  If so,
//                // we will skip it.  We check starting with things we most care
//                // about to those we least care about.
//                if (!thisConfig.isBetterThan(bestConfig, config)) {
//                    if (!currentTypeIsOverlay || thisConfig.compare(bestConfig) != 0) {
//                        continue;
//                    }
//                }
//            }
//
//            bestType = thisType;
//            bestOffset = thisOffset;
//            bestConfig = thisConfig;
//            bestPackage = typeSpec->package;
//            actualTypeIndex = realTypeIndex;
//
//            // If no config was specified, any type will do, so skip
//            if (config == NULL) {
//                break;
//            }
//        }
//    }
//
//    if (bestType == NULL) {
//        return BAD_INDEX;
//    }
//
//    bestOffset += dtohl(bestType->entriesStart);
//
//    if (bestOffset > (dtohl(bestType->header.size)-sizeof(ResTable_entry))) {
//        ALOGW("ResTable_entry at 0x%x is beyond type chunk data 0x%x",
//                bestOffset, dtohl(bestType->header.size));
//        return BAD_TYPE;
//    }
//    if ((bestOffset & 0x3) != 0) {
//        ALOGW("ResTable_entry at 0x%x is not on an integer boundary", bestOffset);
//        return BAD_TYPE;
//    }
//
//    const ResTable_entry* const entry = reinterpret_cast<const ResTable_entry*>(
//            reinterpret_cast<const uint8_t*>(bestType) + bestOffset);
//    if (dtohs(entry->size) < sizeof(*entry)) {
//        ALOGW("ResTable_entry size 0x%x is too small", dtohs(entry->size));
//        return BAD_TYPE;
//    }
//
//    if (outEntry != NULL) {
//        outEntry->entry = entry;
//        outEntry->config = bestConfig;
//        outEntry->type = bestType;
//        outEntry->specFlags = specFlags;
//        outEntry->package = bestPackage;
//        outEntry->typeStr = StringPoolRef(&bestPackage->typeStrings, actualTypeIndex - bestPackage->typeIdOffset);
//        outEntry->keyStr = StringPoolRef(&bestPackage->keyStrings, dtohl(entry->key.index));
//    }
//    return NO_ERROR;

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

