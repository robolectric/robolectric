package com.xtremelabs.droidsugar.util;

import java.io.File;
import java.io.FileFilter;

public class ResourceLoader {
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;
    public final AttrResourceLoader attrResourceLoader;
    public final ColorResourceLoader colorResourceLoader;

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        File xmlDir = new File(resourceDir, "values");
        
        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.addResourceXmlDir(xmlDir);

        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        colorResourceLoader.addResourceXmlDir(xmlDir);

        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        attrResourceLoader.addResourceXmlDir(xmlDir);
        
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
