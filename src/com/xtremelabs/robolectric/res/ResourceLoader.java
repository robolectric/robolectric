package com.xtremelabs.robolectric.res;

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

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        DocumentLoader resourcesDocumentLoader = new DocumentLoader(stringResourceLoader, stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
        resourcesDocumentLoader.loadResourceXmlDir(new File(resourceDir, "values"));

        viewLoader = new ViewLoader(resourceExtractor, stringResourceLoader, attrResourceLoader);
        DocumentLoader viewDocumentLoader = new DocumentLoader(viewLoader);
        File[] layoutDirs = resourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().contains("/layout");
            }
        });
        viewDocumentLoader.loadResourceXmlDirs(layoutDirs);
    }

    /**
     * For tests only...
     */
    protected ResourceLoader(StringResourceLoader stringResourceLoader, StringArrayResourceLoader stringArrayResourceLoader, ColorResourceLoader colorResourceLoader, AttrResourceLoader attrResourceLoader, ViewLoader viewLoader) {
        this.stringResourceLoader = stringResourceLoader;
        this.stringArrayResourceLoader = stringArrayResourceLoader;
        this.colorResourceLoader = colorResourceLoader;
        this.attrResourceLoader = attrResourceLoader;
        this.viewLoader = viewLoader;
    }
}
