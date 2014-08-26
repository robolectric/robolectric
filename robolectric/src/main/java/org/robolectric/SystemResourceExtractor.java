package org.robolectric;

import org.robolectric.res.ResName;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourcePath;

class SystemResourceExtractor extends ResourceExtractor {
  public SystemResourceExtractor(ResourcePath systemResourcePath) {
    super(systemResourcePath);
  }

  @Override public synchronized ResName getResName(int resourceId) {
    ResName resName = super.getResName(resourceId);

    if (resName == null) {
      // todo: pull in android.internal.R, remove this, and remove the "synchronized" on methods since we should then be immutable...
      if ((resourceId & 0xfff00000) == 0x01000000) {
        new RuntimeException("WARN: couldn't find a name for resource id " + resourceId).printStackTrace(System.out);
        ResName internalResName = new ResName("android.internal", "unknown", resourceId + "");
        resourceNameToId.put(internalResName, resourceId);
        resourceIdToResName.put(resourceId, internalResName);
        return internalResName;
      }
    }

    return resName;
  }

  @Override public synchronized Integer getResourceId(ResName resName) {
    return super.getResourceId(resName);
  }
}
