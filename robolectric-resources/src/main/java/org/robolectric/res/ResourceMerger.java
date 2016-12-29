package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.List;

public class  ResourceMerger {
  @NotNull
  public static ResourceTable buildResourceTable(AndroidManifest appManifest) {
    List<ResourcePath> allResourcePaths = appManifest.getIncludedResourcePaths();
    PackageResourceIndex resourceIndex = new PackageResourceIndex(appManifest.getPackageName());
    ResourceTable resourceTable = new ResourceTable(resourceIndex);

    ResourceRemapper resourceRemapper = null;
    for (ResourcePath resourcePath : allResourcePaths) {
      if (resourceRemapper == null) {
        resourceRemapper = new ResourceRemapper(resourcePath.getRClass());
      } else {
        resourceRemapper.remapRClass(resourcePath.getRClass());
      }

      if (resourcePath.getRClass() != null) {
        ResourceExtractor.populate(resourceIndex, resourcePath.getRClass());
      }
    }

    for (ResourcePath resourcePath : allResourcePaths) {
      ResourceParser.load(appManifest.getPackageName(), resourcePath, resourceTable);
    }
    return resourceTable;
  }
}
