package org.robolectric.res;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks resource ids and generates new unique values.
 */
public class ResourceIdGenerator {

  private final Map<String, TypeTracker> typeInfo = new HashMap<>();
  private int packageIdentifier;

  private static class TypeTracker {
    private int typeIdentifier;
    private int currentMaxEntry;

    TypeTracker(int typeIdentifier) {
      this.typeIdentifier = typeIdentifier;
    }

    void record(int entryIdentifier) {
      currentMaxEntry = Math.max(currentMaxEntry, entryIdentifier);
    }

    public int getFreeIdentifier() {
      return ++currentMaxEntry;
    }

    public int getTypeIdentifier() {
      return typeIdentifier;
    }
  }

  ResourceIdGenerator(int packageIdentifier) {
    this.packageIdentifier = packageIdentifier;
  }

  public void record(int resId, String type, String name) {
    TypeTracker typeTracker = typeInfo.get(type);
    if (typeTracker == null) {
      typeTracker = new TypeTracker(ResourceIds.getTypeIdentifier(resId));
      typeInfo.put(type, typeTracker);
    }
    typeTracker.record(ResourceIds.getEntryIdentifier(resId));
  }

  public int generate(String type, String name) {
    TypeTracker typeTracker = typeInfo.get(type);
    if (typeTracker == null) {
      typeTracker = new TypeTracker(getNextFreeTypeIdentifier());
      typeInfo.put(type, typeTracker);
    }
    return ResourceIds.makeIdentifer(packageIdentifier, typeTracker.getTypeIdentifier(), typeTracker.getFreeIdentifier());
  }

  private int getNextFreeTypeIdentifier() {
    int result = 0;
    for (TypeTracker typeTracker : typeInfo.values()) {
      result = Math.max(result, typeTracker.getTypeIdentifier());
    }
    return ++result;
  }
}
