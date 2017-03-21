package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.ArrayList;
import java.util.List;

public class ResourceMerger {
  @NotNull
  public PackageResourceTable buildResourceTable(AndroidManifest appManifest) {
    ResourceRemapper resourceRemapper = new ResourceRemapper(appManifest.getRClass());

    ResourcePath appResourcePath = appManifest.getResourcePath();
    List<ResourcePath> allResourcePaths = appManifest.getIncludedResourcePaths();
    for (ResourcePath resourcePath : allResourcePaths) {
      if (!resourcePath.equals(appResourcePath) && resourcePath.getRClass() != null) {
        resourceRemapper.remapRClass(resourcePath.getRClass());
      }
    }

    return new ResourceTableFactory().newResourceTable(appManifest.getPackageName(),
        allResourcePaths.toArray(new ResourcePath[allResourcePaths.size()]));
  }
}
