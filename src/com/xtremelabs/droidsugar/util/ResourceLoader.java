package com.xtremelabs.droidsugar.util;

import java.io.File;
import java.io.FileFilter;

public class ResourceLoader {
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        viewLoader = new ViewLoader(resourceExtractor);
        File[] layoutDirs = resourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().contains("/layout");
            }
        });
        for (File layoutDir : layoutDirs) {
            viewLoader.addResourceXmlDir(layoutDir);
        }

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.addResourceXmlDir(new File(resourceDir, "values"));
    }
}
