package com.xtremelabs.droidsugar.view;

import java.io.File;

public class ResourceLoader {
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        viewLoader = new ViewLoader(resourceExtractor);
        viewLoader.addResourceXmlDir(new File(resourceDir, "layout"));

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.addResourceXmlDir(new File(resourceDir, "values"));
    }
}
