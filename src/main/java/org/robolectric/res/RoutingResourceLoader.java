package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoutingResourceLoader implements ResourceLoader {
  private final Map<String, ResourceLoader> resourceLoaders;
  private final ResourceIndex resourceIndex;

  public RoutingResourceLoader(Map<String, ResourceLoader> resourceLoaders) {
    this.resourceLoaders = resourceLoaders;

    Set<ResourceIndex> resourceIndexes = new HashSet<ResourceIndex>();
    for (ResourceLoader resourceLoader : resourceLoaders.values()) {
      resourceIndexes.add(resourceLoader.getResourceIndex());
    }
    resourceIndex = new MergedResourceIndex(resourceIndexes.toArray(new ResourceIndex[resourceIndexes.size()]));
  }

  @Override
  public String getNameForId(int id) {
    return pickFor(id).getNameForId(id);
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return pickFor(resName).getValue(resName, qualifiers);
  }

  @Override
  public Plural getPlural(ResName resName, int quantity, String qualifiers) {
    return pickFor(resName).getPlural(resName, quantity, qualifiers);
  }

  @Override
  public Document getXml(ResName resName, String qualifiers) {
    return pickFor(resName).getXml(resName, "");
  }

  @Override
  public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
    return pickFor(resName).getDrawableNode(resName, qualifiers);
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    return pickFor(resName).getRawValue(resName);
  }

  @Override
  public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
    return pickFor(resName).getPreferenceNode(resName, qualifiers);
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public MenuNode getMenuNode(ResName resName, String qualifiers) {
    return pickFor(resName).getMenuNode(resName, qualifiers);
  }

  @Override public boolean providesFor(String namespace) {
    return whichProvidesFor(namespace) != null;
  }

  private ResourceLoader pickFor(int id) {
    ResName resName = resourceIndex.getResName(id);
    return pickFor(resName);
  }

  private ResourceLoader pickFor(ResName resName) {
    if (resName == null) return new NullResourceLoader();
    return pickFor(resName.packageName);
  }

  private ResourceLoader pickFor(String namespace) {
    if (namespace.equals("android.internal")) {
      return new NullResourceLoader();
    }
    ResourceLoader resourceLoader = resourceLoaders.get(namespace);
    if (resourceLoader == null) {
      resourceLoader = whichProvidesFor(namespace);
      if (resourceLoader != null) return resourceLoader;
      throw new RuntimeException("no ResourceLoader found for " + namespace);
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

  private static class NullResourceLoader extends XResourceLoader {
    public NullResourceLoader() {
      super(null);
    }

    @Override void doInitialize() {
    }

    @Override
    public String getNameForId(int id) {
      return null;
    }

    @Override public boolean providesFor(String namespace) {
      return true;
    }
  }
}
