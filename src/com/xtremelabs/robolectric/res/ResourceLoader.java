package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class ResourceLoader {
    private final ResourceExtractor resourceExtractor;
    public final ViewLoader viewLoader;
    public final StringResourceLoader stringResourceLoader;
    public final StringArrayResourceLoader stringArrayResourceLoader;
    public final AttrResourceLoader attrResourceLoader;
    public final ColorResourceLoader colorResourceLoader;
    public final RawResourceLoader rawResourceLoader;

    // todo: get these value from the xml resources instead [xw 20101011]
    public final Map<Integer, Integer> dimensions = new HashMap<Integer, Integer>();

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        rawResourceLoader = new RawResourceLoader(resourceExtractor, resourceDir);

        if (resourceDir != null) {
            DocumentLoader resourcesDocumentLoader = new DocumentLoader(stringResourceLoader, stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
            resourcesDocumentLoader.loadResourceXmlDir(new File(resourceDir, "values"));

            viewLoader = new ViewLoader(resourceExtractor, stringResourceLoader, attrResourceLoader);
            DocumentLoader viewDocumentLoader = new DocumentLoader(viewLoader);
            File[] layoutDirs = resourceDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return isLayoutDirectory(file.getPath());
                }
            });
            viewDocumentLoader.loadResourceXmlDirs(layoutDirs);
        } else {
            viewLoader = null;
        }
    }

    boolean isLayoutDirectory(String path) {
        return path.contains(File.separator + "layout");
    }

    /**
     * For tests only...
     */
    protected ResourceLoader(StringResourceLoader stringResourceLoader) {
        resourceExtractor = new ResourceExtractor();
        this.stringResourceLoader = stringResourceLoader;
        viewLoader = null;
        stringArrayResourceLoader = null;
        attrResourceLoader = null;
        colorResourceLoader = null;
        rawResourceLoader = null;
    }

    public static ResourceLoader getFrom(Context context) {
        return shadowOf(context.getApplicationContext()).getResourceLoader();
    }

    public String getNameForId(int viewId) {
        return resourceExtractor.getResourceName(viewId);
    }
}
