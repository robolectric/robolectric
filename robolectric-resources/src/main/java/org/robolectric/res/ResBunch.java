package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ResBunch {
  private final Map<String, ResBundle<TypedResource>> types = new LinkedHashMap<>();

  public void put(String attrType, String name, TypedResource value, XmlLoader.XmlContext xmlContext) {
    ResBundle<TypedResource> bundle = getBundle(attrType);
    bundle.put(attrType, name, value, xmlContext);
  }

  private ResBundle<TypedResource> getBundle(String attrType) {
    ResBundle<TypedResource> bundle = types.get(attrType);
    if (bundle == null) {
      bundle = new ResBundle<>();
      types.put(attrType, bundle);
    }
    return bundle;
  }

  public TypedResource get(@NotNull ResName resName, String qualifiers) {
    ResBundle.Value<TypedResource> value = getValue(resName, qualifiers);
    return value == null ? null : value.getValue();
  }

  public ResBundle.Value<TypedResource> getValue(@NotNull ResName resName, String qualifiers) {
    ResBundle<TypedResource> bundle = getBundle(resName.type);
    return bundle.getValue(resName, qualifiers);
  }

  public int size() {
    int size = 0;
    for (ResBundle<TypedResource> bundle : types.values()) {
      size += bundle.size();
    }
    return size;
  }

  public void makeImmutable() {
    for (ResBundle<TypedResource> bundle : types.values()) {
      bundle.makeImmutable();
    }
  }

  public void mergeLibraryStyle(ResBunch fromResBundle, String packageName) {
    for (Map.Entry<String, ResBundle<TypedResource>> entry : fromResBundle.types.entrySet()) {
      getBundle(entry.getKey()).mergeLibraryStyle(entry.getValue(), packageName);
    }
  }
}
