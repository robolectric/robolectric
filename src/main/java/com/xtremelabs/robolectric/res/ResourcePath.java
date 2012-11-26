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
}
