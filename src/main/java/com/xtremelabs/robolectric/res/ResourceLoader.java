package com.xtremelabs.robolectric.res;

import android.R;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.util.PropertiesHelper;

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
    private static final FileFilter MENU_DIR_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isMenuDirectory(file.getPath());
        }
    };
    private static final FileFilter LAYOUT_DIR_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isLayoutDirectory(file.getPath());
        }
    };

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
                viewLoader = new ViewLoader(resourceExtractor, attrResourceLoader);
                menuLoader = new MenuLoader(resourceExtractor, attrResourceLoader);

                File systemResourceDir = getSystemResourceDir(getPathToAndroidResources());
                File localValueResourceDir = getValueResourceDir(resourceDir);
                File systemValueResourceDir = getValueResourceDir(systemResourceDir);

                loadStringResources(localValueResourceDir, systemValueResourceDir);
                loadValueResources(localValueResourceDir, systemValueResourceDir);
                loadViewResources(systemResourceDir, resourceDir);
                loadMenuResources(resourceDir);
            } else {
                viewLoader = null;
                menuLoader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isInitialized = true;
    }

    private File getSystemResourceDir(String pathToAndroidResources) {
        return pathToAndroidResources != null ? new File(pathToAndroidResources) : null;
    }

    private void loadStringResources(File localResourceDir, File systemValueResourceDir) throws Exception {
        DocumentLoader stringResourceDocumentLoader = new DocumentLoader(this.stringResourceLoader);
        loadValueResourcesFromDirs(stringResourceDocumentLoader, localResourceDir, systemValueResourceDir);
    }

    private void loadValueResources(File localResourceDir, File systemValueResourceDir) throws Exception {
        DocumentLoader valueResourceLoader = new DocumentLoader(stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
        loadValueResourcesFromDirs(valueResourceLoader, localResourceDir, systemValueResourceDir);
    }

    private void loadViewResources(File systemResourceDir, File xmlResourceDir) throws Exception {
        DocumentLoader viewDocumentLoader = new DocumentLoader(viewLoader);
        loadLayoutResourceXmlSubDirs(viewDocumentLoader, xmlResourceDir);
        loadLayoutResourceXmlSubDirs(viewDocumentLoader, systemResourceDir);
    }

    private void loadMenuResources(File xmlResourceDir) throws Exception {
        DocumentLoader menuDocumentLoader = new DocumentLoader(menuLoader);
        loadMenuResourceXmlDirs(menuDocumentLoader, xmlResourceDir);
    }

    private void loadLayoutResourceXmlSubDirs(DocumentLoader layoutDocumentLoader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null) {
            layoutDocumentLoader.loadResourceXmlDirs(xmlResourceDir.listFiles(LAYOUT_DIR_FILE_FILTER));
        }
    }

    private void loadMenuResourceXmlDirs(DocumentLoader menuDocumentLoader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null) {
            menuDocumentLoader.loadResourceXmlDirs(xmlResourceDir.listFiles(MENU_DIR_FILE_FILTER));
        }
    }

    private void loadValueResourcesFromDirs(DocumentLoader documentLoader, File localValueResourceDir, File systemValueResourceDir) throws Exception {
        loadValueResourcesFromDir(documentLoader, localValueResourceDir);
        loadSystemResourceXmlDir(documentLoader, systemValueResourceDir);
    }

    private void loadValueResourcesFromDir(DocumentLoader documentloader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null) {
            documentloader.loadResourceXmlDir(xmlResourceDir);
        }
    }

    private void loadSystemResourceXmlDir(DocumentLoader documentLoader, File stringResourceDir) throws Exception {
        if (stringResourceDir != null) {
            documentLoader.loadSystemResourceXmlDir(stringResourceDir);
        }
    }

    private File getValueResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "values") : null;
    }

    private String getPathToAndroidResources() {
        String resourcePath;
        if ((resourcePath = getAndroidResourcePathFromLocalProperties()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemEnvironment()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathByExecingWhichAndroid()) != null) {
            return resourcePath;
        }

        System.out.println("WARNING: Unable to find path to Android SDK");
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
                PropertiesHelper.doSubstitutions(localProperties);
                String sdkPath = localProperties.getProperty("sdk.dir");
                if (sdkPath != null) {
                    return getResourcePathFromSdkPath(sdkPath);
                }
            } catch (IOException e) {
                // fine, we'll try something else
            }
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

    private String getAndroidResourcePathByExecingWhichAndroid() {
        // Hand tested
        // Should always work from the command line. Often fails in IDEs because they don't pass the full PATH in the environment
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "android"});
            String sdkPath = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (sdkPath != null && sdkPath.endsWith("tools/android")) {
                return getResourcePathFromSdkPath(sdkPath.substring(0, sdkPath.indexOf("tools/android")));
            }
        } catch (IOException e) {
            // fine we'll try something else
        }
        return null;
    }

    private String getResourcePathFromSdkPath(String sdkPath) {
        File androidResourcePath = new File(sdkPath, getAndroidResourceSubPath());
        return androidResourcePath.exists() ? androidResourcePath.toString() : null;
    }

    private String getAndroidResourceSubPath() {
        // TODO: Use the targetSDKVersion from the Android Manifest instead of a hard-coded "9"
        return "platforms/android-9/data/res";
    }

    static boolean isLayoutDirectory(String path) {
        return path.contains(File.separator + "layout");
    }

    static boolean isMenuDirectory(String path) {
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
        init();
        menuLoader.inflateMenu(context, resource, root);
    }

    public File getAssetsBase() {
        return assetsDir;
    }

    public ResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }
}
