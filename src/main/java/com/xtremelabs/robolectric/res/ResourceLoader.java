package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ResourceLoader {
    private final ResourceExtractor resourceExtractor;
    private ViewLoader viewLoader;
    private final StringResourceLoader stringResourceLoader;
    private final StringArrayResourceLoader stringArrayResourceLoader;
    private final AttrResourceLoader attrResourceLoader;
    private final ColorResourceLoader colorResourceLoader;
    private final RawResourceLoader rawResourceLoader;
    private boolean isInitialized = false;

    // TODO: get these value from the xml resources instead [xw 20101011]
    public final Map<Integer, Integer> dimensions = new HashMap<Integer, Integer>();
    private File resourceDir;

    public ResourceLoader(Class rClass, File resourceDir) throws Exception {
        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(rClass);

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        rawResourceLoader = new RawResourceLoader(resourceExtractor, resourceDir);

        this.resourceDir = resourceDir;
    }

    private void init() {
        if (isInitialized) {
            return;
        }

        try {
            if (resourceDir != null) {
                DocumentLoader stringResourcesDocumentLoader = new DocumentLoader(stringResourceLoader);
                File valuesResourceDir = new File(resourceDir, "values");
                stringResourcesDocumentLoader.loadResourceXmlDir(valuesResourceDir);

                DocumentLoader resourcesDocumentLoader = new DocumentLoader(stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
                resourcesDocumentLoader.loadResourceXmlDir(valuesResourceDir);

                viewLoader = new ViewLoader(resourceExtractor, attrResourceLoader);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isInitialized = true;
    }

    boolean isLayoutDirectory(String path) {
        return path.contains(File.separator + "layout");
    }

    /*
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
        ResourceLoader resourceLoader = shadowOf(context.getApplicationContext()).getResourceLoader();
        resourceLoader.init();
        return resourceLoader;
    }

    public String getNameForId(int viewId) {
        init();
        return resourceExtractor.getResourceName(viewId);
    }

    public View inflateView(Context context, int resource, ViewGroup viewGroup) {
        init();
        return viewLoader.inflateView(context, resource, viewGroup);
    }

    public int getColorValue(int id) {
        init();
        return colorResourceLoader.getValue(id);
    }

    public String getStringValue(int id) {
        init();
        return stringResourceLoader.getValue(id);
    }

    public InputStream getRawValue(int id) {
        init();
        return rawResourceLoader.getValue(id);
    }

    public String[] getStringArrayValue(int id) {
        init();
        return stringArrayResourceLoader.getArrayValue(id);
    }
}
