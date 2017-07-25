package org.robolectric.res.arsc;

import java.nio.charset.Charset;
import java.util.List;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.arsc.Chunk.PackageChunk;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk.Entry;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk.Entry.Value;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk.SimpleEntry;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeSpecChunk;
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
    int packageId = ResourceIds.getPackageIdentifier(resId);
    int typeId = ResourceIds.getTypeIdentifier(resId);
    TypeSpecChunk typeSpec = chunk.getPackageChunk(packageId).getTypeSpec(typeId);
    List<TypeChunk> types = chunk.getPackageChunk(packageId).getTypes(typeId);

    for (TypeChunk type : types) {
      for (Entry entry : type.getEntries()) {
        SimpleEntry simpleEntry = (SimpleEntry)entry;
        if (simpleEntry != null) {
          Value value = simpleEntry.getValue();
          int data = value.getData();
          System.out.println(chunk.getValuesStringPool().getString(data));
        }
      }
    }
    return null;
  }

  public String getTypeName(int resId) {
    int typeId = ResourceIds.getTypeIdentifier(resId);
    int packageId = ResourceIds.getPackageIdentifier(resId);
    StringPoolChunk stringPool = chunk.getPackageChunk(packageId).getTypeStringPool();
    return stringPool.getString(typeId - 1); // TT in PPTTEEEE is 1 indexed
  }

  public String getKeyName(int resId) {
    int keyId = ResourceIds.getEntryIdentifier(resId);
    int packageId = ResourceIds.getPackageIdentifier(resId);
    PackageChunk packageChunk = chunk.getPackageChunk(packageId);
    StringPoolChunk stringPool = packageChunk.getKeyStringPool();

    return stringPool.getString(keyId); // EEEE in PPTTEEEE is 0 indexed
  }

  public String getPackageName(int resId) {
    int packageId = ResourceIds.getPackageIdentifier(resId);
    String rawName = chunk.getPackageChunk(packageId).getName();
    int firstNull = rawName.indexOf(0);
    return rawName.substring(0, firstNull);
  }

  public int getInt(int resId) {
    int packageId = ResourceIds.getPackageIdentifier(resId);
    int typeId = ResourceIds.getTypeIdentifier(resId);
    TypeSpecChunk typeSpec = chunk.getPackageChunk(packageId).getTypeSpec(typeId);
    List<TypeChunk> types = chunk.getPackageChunk(packageId).getTypes(typeId);

    for (TypeChunk type : types) {
      for (Entry entry : type.getEntries()) {
        SimpleEntry simpleEntry = (SimpleEntry)entry;
        Value value = simpleEntry.getValue();
        return value.getData();
      }
    }
    return -1;
  }
}
