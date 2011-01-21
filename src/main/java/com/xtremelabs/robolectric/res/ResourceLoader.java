package com.xtremelabs.robolectric.res;

import android.R;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ResourceLoader {
    private File resourceDir;
    private File assetsDir;

    private final ResourceExtractor resourceExtractor;
    private ViewLoader viewLoader;
    private MenuLoader menuLoader;
    private final StringResourceLoader stringResourceLoader;
    private final StringArrayResourceLoader stringArrayResourceLoader;
    private final AttrResourceLoader attrResourceLoader;
    private final ColorResourceLoader colorResourceLoader;
    private final RawResourceLoader rawResourceLoader;
    private boolean isInitialized = false;

    // TODO: get these value from the xml resources instead [xw 20101011]
    public final Map<Integer, Integer> dimensions = new HashMap<Integer, Integer>();

    public ResourceLoader(Class rClass, File resourceDir, File assetsDir) throws Exception {
        this.assetsDir = assetsDir;
        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(rClass);
        resourceExtractor.addSystemRClass(R.class);

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
                String resourcePath = getPathToAndroidResources();
                File systemResourceDir = new File(resourcePath);

                DocumentLoader stringResourcesDocumentLoader = new DocumentLoader(stringResourceLoader);
                File valuesResourceDir = new File(resourceDir, "values");
                File systemValuesResourceDir = new File(systemResourceDir, "values");
                stringResourcesDocumentLoader.loadLocalResourceXmlDir(valuesResourceDir);
                stringResourcesDocumentLoader.loadSystemResourceXmlDir(systemValuesResourceDir);

                DocumentLoader resourcesDocumentLoader = new DocumentLoader(stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
                resourcesDocumentLoader.loadLocalResourceXmlDir(valuesResourceDir);
                resourcesDocumentLoader.loadSystemResourceXmlDir(systemValuesResourceDir);

                viewLoader = new ViewLoader(resourceExtractor, attrResourceLoader);
                DocumentLoader viewDocumentLoader = new DocumentLoader(viewLoader);
                File[] layoutDirs = resourceDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return isLayoutDirectory(file.getPath());
                    }
                });
                viewDocumentLoader.loadLocalResourceXmlDirs(layoutDirs);

                layoutDirs = systemResourceDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return isLayoutDirectory(file.getPath());
                    }
                });
                viewDocumentLoader.loadLocalResourceXmlDirs(layoutDirs);

                menuLoader = new MenuLoader(resourceExtractor);
                DocumentLoader menuDocumentLoader = new DocumentLoader(menuLoader);
                File[] menuDirs = resourceDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return isMenuDirectory(file.getPath());
                    }
                });
                menuDocumentLoader.loadLocalResourceXmlDirs(menuDirs);
            } else {
                viewLoader = null;
                menuLoader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isInitialized = true;
    }

    private String getPathToAndroidResources() {
        String resourcePath;
        if ((resourcePath = getAndroidResourcePathFromRClass()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromLocalProperties()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemEnvironment()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathByExecingWhichAndroid()) != null) {
            return resourcePath;
        }

        throw new RuntimeException("Unable to find path to Android SDK");
    }

    private String getAndroidResourcePathFromRClass() {
        // Cribbed from known-working code from palfrey
        // ToDo: Is this still a valid strategy?
        String resourcePath = R.class.getResource("/res/layout").toString();
        if (resourcePath.startsWith("jar:file:") && resourcePath.indexOf("android.jar!")!=-1) {
            return resourcePath.substring("jar:file:".length(), resourcePath.indexOf("android.jar!")) + "data/res";
        }
        return null;
    }

    private String getAndroidResourcePathFromSystemEnvironment() {
        // Hand tested
        String resourcePath = System.getenv().get("ANDROID_HOME");
        if (resourcePath != null) {
            return new File(resourcePath, getAndroidResourceSubPath()).toString();
        }
        return null;
    }

    private String getAndroidResourcePathFromLocalProperties() {
        // Hand tested
        // This is the path most often taken by IntelliJ
        File rootDir = resourceDir.getParentFile();
        String localPropertiesFileName = "local.properties";
        File localPropertiesFile = new File(rootDir, localPropertiesFileName);
        if (!localPropertiesFile.exists()) {
            localPropertiesFile = new File(localPropertiesFileName);
        }
        if (localPropertiesFile.exists()) {
            Properties localProperties = new Properties();
            try {
                localProperties.load(new FileInputStream(localPropertiesFile));
                String resourcePath = localProperties.getProperty("sdk.dir");
                if (resourcePath != null) {
                    return new File(resourcePath, getAndroidResourceSubPath()).toString();
                }
            } catch (IOException e) {
                // fine, we'll try something else
            }
        }
        return null;
    }

    private String getAndroidResourcePathByExecingWhichAndroid() {
        // Hand tested
        // Should always work from the command line. Often fails in IDEs because they don't pass the full PATH in the environment
        try {
            Process process = Runtime.getRuntime().exec(new String[] {"which", "android"});
            String resourcePath = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (resourcePath != null && resourcePath.endsWith("tools/android")) {
                return new File(resourcePath.substring(0, resourcePath.indexOf("tools/android")), getAndroidResourceSubPath()).toString();
            }
        } catch (IOException e) {
            // fine we'll try something else
        }
        return null;
    }

    private String getAndroidResourceSubPath() {
        // TODO: Use the targetSDKVersion from the Android Manifest instead of a hard-coded "9"
        return "platforms/android-9/data/res";
    }

    boolean isLayoutDirectory(String path) {
        return path.contains(File.separator + "layout");
    }

    boolean isMenuDirectory(String path) {
        return path.contains(File.separator + "menu");
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

    public void inflateMenu(Context context, int resource, Menu root) {
        menuLoader.inflateMenu(context, resource, root);
    }

    public File getAssetsBase() {
        return assetsDir;
    }
}
