package com.xtremelabs.droidsugar.util;

import java.io.File;
import java.io.FileFilter;

public class ResourceLoader {
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;
    public final AttrResourceLoader attrResourceLoader;

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.addResourceXmlDir(new File(resourceDir, "values"));

        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        attrResourceLoader.addResourceXmlDir(new File(resourceDir, "values"));
        
        viewLoader = new ViewLoader(resourceExtractor, stringResourceLoader, attrResourceLoader);
        File[] layoutDirs = resourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().contains("/layout");
            }
        });
        for (File layoutDir : layoutDirs) {
            viewLoader.addResourceXmlDir(layoutDir);
        }
    }
}
