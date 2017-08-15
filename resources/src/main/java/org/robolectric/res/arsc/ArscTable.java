package org.robolectric.res.arsc;

import com.google.common.collect.Iterables;
import java.util.List;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.ResTableEntry;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.StringPoolChunk;
import org.robolectric.res.arsc.Chunk.TableChunk;

public class ArscTable {

  private TableChunk chunk;

  public ArscTable(TableChunk chunk) {
    this.chunk = chunk;
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

