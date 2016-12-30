package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.List;

public class  ResourceMerger {
  @NotNull
  public static PackageResourceTable buildResourceTable(AndroidManifest appManifest) {
    List<ResourcePath> allResourcePaths = appManifest.getIncludedResourcePaths();
    ResourceRemapper resourceRemapper = null;
    for (ResourcePath resourcePath : allResourcePaths) {
      if (resourceRemapper == null) {
        resourceRemapper = new ResourceRemapper(resourcePath.getRClass());
      } else {
        if (resourcePath.getRClass() != null) {
          resourceRemapper.remapRClass(resourcePath.getRClass());
        }
      }
    }

    PackageResourceTable resourceTable = ResourceTableFactory.newResourceTable(appManifest.getPackageName(),
        allResourcePaths.toArray(new ResourcePath[allResourcePaths.size()]));

    for (ResourcePath resourcePath : allResourcePaths) {
      ResourceParser.load(appManifest.getPackageName(), resourcePath, resourceTable);
    }
    return resourceTable;
  }
}
