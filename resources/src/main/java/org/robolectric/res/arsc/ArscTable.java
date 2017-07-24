package org.robolectric.res.arsc;

import java.nio.charset.Charset;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.arsc.Chunk.PackageChunk;
import org.robolectric.res.arsc.Chunk.StringPoolChunk;
import org.robolectric.res.arsc.Chunk.TableChunk;

/**
 * Created by jongerrish on 7/13/17.
 */
public class ArscTable {

  private TableChunk chunk;

  public ArscTable(TableChunk chunk) {
    this.chunk = chunk;
  }

  public String getString(int resId) {
    return null;
  }

  public String getTypeName(int resId) {
    int typeId = ResourceIds.getTypeIdentifier(resId);
    StringPoolChunk stringPool = getMainPackageChunk().getTypeStringPool();
    return stringPool.getString(typeId - 1); // TT in PPTTEEEE is 1 indexed
  }

  public String getKeyName(int resId) {
    int keyId = ResourceIds.getEntryIdentifier(resId);
    StringPoolChunk stringPool = getMainPackageChunk().getKeyStringPool();
    return stringPool.getString(keyId); // EEEE in PPTTEEEE is 0 indexed
  }

  public String getPackageName() {
    String rawName = getMainPackageChunk().getName();

    int firstNull = rawName.indexOf(0);

    return rawName.substring(0, firstNull);
  }

  private PackageChunk getMainPackageChunk() {
    return chunk.getPackageChunks().get(0);
  }
}
