package com.xtremelabs.robolectric.res;

import java.io.File;

public class ResourcePath {
    public final Class<?> rClass;
    public final File resourceBase;
    public final File assetsDir;

    public ResourcePath(Class<?> rClass, File resourceBase, File assetsDir) {
        this.rClass = rClass;
        this.resourceBase = resourceBase;
        this.assetsDir = assetsDir;
    }

    public String getPackageName() {
        return rClass.getPackage().getName();
    }

    @Override
    public String toString() {
        return "ResourcePath{package=" + getPackageName() + ", path=" + resourceBase + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourcePath that = (ResourcePath) o;

        if (assetsDir != null ? !assetsDir.equals(that.assetsDir) : that.assetsDir != null) return false;
        if (!rClass.equals(that.rClass)) return false;
        if (!resourceBase.equals(that.resourceBase)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rClass.hashCode();
        result = 31 * result + resourceBase.hashCode();
        result = 31 * result + (assetsDir != null ? assetsDir.hashCode() : 0);
        return result;
    }
}
