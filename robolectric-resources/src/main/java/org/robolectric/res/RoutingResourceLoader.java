package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.res.builder.XmlBlock;

public class RoutingResourceLoader extends ResourceLoader {
  private final Map<String, ResourceLoader> resourceLoaders;
  private final ResourceIndex resourceIndex;

  public RoutingResourceLoader(Map<String, ResourceLoader> resourceLoaders) {
    this.resourceLoaders = resourceLoaders;

    Set<ResourceIndex> resourceIndexes = new HashSet<>();
    for (ResourceLoader resourceLoader : resourceLoaders.values()) {
      resourceIndexes.add(resourceLoader.getResourceIndex());
    }
    resourceIndex = new MergedResourceIndex(resourceIndexes.toArray(new ResourceIndex[resourceIndexes.size()]));
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return pickFor(resName).getValue(resName, qualifiers);
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return pickFor(resName).getXml(resName, qualifiers);
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    return pickFor(resName).getRawValue(resName);
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override public boolean providesFor(String namespace) {
    return whichProvidesFor(namespace) != null;
  }

  @Override
  public void receive(Visitor visitor) {
    for (ResourceLoader resourceLoader : resourceLoaders.values()) {
      resourceLoader.receive(visitor);
    }
  }

  private ResourceLoader pickFor(int id) {
    ResName resName = resourceIndex.getResName(id);
    return pickFor(resName);
  }

  private ResourceLoader pickFor(ResName resName) {
    if (resName == null) return new EmptyResourceLoader();
    return pickFor(resName.packageName);
  }

  private ResourceLoader pickFor(String namespace) {
    if (namespace.equals("android.internal")) {
      return new EmptyResourceLoader();
    }
    ResourceLoader resourceLoader = resourceLoaders.get(namespace);
    if (resourceLoader == null) {
      resourceLoader = whichProvidesFor(namespace);
      return (resourceLoader != null) ? resourceLoader : new EmptyResourceLoader();
    }
    return resourceLoader;
  }

  private ResourceLoader whichProvidesFor(String namespace) {
    for (ResourceLoader loader : resourceLoaders.values()) {
      if (loader.providesFor(namespace)) {
        return loader;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "RoutingResourceLoader{" + resourceLoaders + "}";
  }
}
