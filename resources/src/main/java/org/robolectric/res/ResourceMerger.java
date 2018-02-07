package org.robolectric.res;

import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.manifest.AndroidManifest;

public class ResourceMerger {
  @Nonnull
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
