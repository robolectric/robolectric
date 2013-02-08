package org.robolectric.res;

public abstract class ResourceIndex {
    public abstract Integer getResourceId(ResName resName);

    public String getResourceName(int resourceId) {
        ResName resName = getResName(resourceId);
        return (resName != null) ? resName.getFullyQualifiedName() : null;
    }

    public abstract ResName getResName(int resourceId);
}
