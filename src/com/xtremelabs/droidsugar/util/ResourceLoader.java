package com.xtremelabs.droidsugar.util;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

public class ResourceLoader {
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;
    public final StringArrayResourceLoader stringArrayResourceLoader;
    public final AttrResourceLoader attrResourceLoader;
    public final ColorResourceLoader colorResourceLoader;

    // todo: get these value from the xml resources instead [xw 20101011]
    public final Map<Integer, Integer> dimensions = new HashMap<Integer, Integer>();

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        File xmlDir = new File(resourceDir, "values");

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.addResourceXmlDir(xmlDir);

        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor);
        stringArrayResourceLoader.addResourceXmlDir(xmlDir);

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
