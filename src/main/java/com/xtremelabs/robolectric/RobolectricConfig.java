package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class RobolectricConfig {
    private File androidManifestFile;
    private List<File> resourcePath;
    private File assetsDirectory;
    private String rClassName;
    private String packageName;
    private String applicationName;
    private boolean manifestIsParsed = false;
    private int sdkVersion;

    /**
     * Creates a Robolectric configuration using default Android files relative to the specified base directory.
     * <p/>
     * The manifest will be baseDir/AndroidManifest.xml, res will be baseDir/res, and assets in baseDir/assets.
     *
     * @param baseDir the base directory of your Android project
     */
    public RobolectricConfig(File baseDir) {
        this(new File(baseDir, "AndroidManifest.xml"), new File(baseDir, "res"), new File(baseDir, "assets"));
    }

    public RobolectricConfig(File androidManifestFile, File resourceDirectory) {
        this(androidManifestFile, resourceDirectory, new File(resourceDirectory.getParent(), "assets"));
    }

    /**
     * Creates a Robolectric configuration using specified locations.
     *
     * @param androidManifestFile location of the AndroidManifest.xml file
     * @param resourceDirectory   location of the res directory
     * @param assetsDirectory     location of the assets directory
     */
    public RobolectricConfig(File androidManifestFile, File resourceDirectory, File assetsDirectory) {
        this(androidManifestFile, Collections.singletonList(resourceDirectory), assetsDirectory);
    }

    public RobolectricConfig(File androidManifestFile, List<File> resourcePath, File assetsDirectory) {
        this.androidManifestFile = androidManifestFile;
        this.resourcePath = Collections.unmodifiableList(resourcePath);
        this.assetsDirectory = assetsDirectory;
    }

    public static RobolectricConfig fromBaseDirWithLibraries(File baseDir) {
        List<File> resources = new ArrayList<File>();
        buildResourcePath(baseDir, resources);
        return new RobolectricConfig(new File(baseDir, "AndroidManifest.xml"), resources, new File(baseDir, "assets"));
    }

    private static void buildResourcePath(File baseDir, List<File> resources) {
        resources.add(new File(baseDir, "res"));

        Properties properties = getProperties(new File(baseDir, "project.properties"));
        String lib = properties == null ? null : properties.getProperty("android.library.reference.1");

        if (lib != null) {
            buildResourcePath(new File(baseDir, lib), resources);
        }
    }

    private static Properties getProperties(File propertiesFile) {
        if (!propertiesFile.exists()) return null;

        Properties properties = new Properties();
        FileInputStream stream;
        try {
            stream = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public String getRClassName() throws Exception {
        parseAndroidManifest();
        return rClassName;
    }

    public void validate() throws FileNotFoundException {
        if (!androidManifestFile.exists() || !androidManifestFile.isFile()) {
            throw new FileNotFoundException(androidManifestFile.getAbsolutePath() + " not found or not a file; it should point to your project's AndroidManifest.xml");
        }

        for (File f : getResourcePath()) {
            if (!f.exists() || !f.isDirectory()) {
                throw new FileNotFoundException(f.getAbsolutePath() + " not found or not a directory; it should point to a res directory");
            }
        }
    }

    private void parseAndroidManifest() {
        if (manifestIsParsed) {
            return;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document manifestDocument = db.parse(androidManifestFile);

            packageName = getTagAttributeText(manifestDocument, "manifest", "package");
            rClassName = packageName + ".R";
            applicationName = getTagAttributeText(manifestDocument, "application", "android:name");
            sdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion", 10);
        } catch (Exception ignored) {
        }
        manifestIsParsed = true;
    }

    private int getTagAttributeIntValue(Document doc, String tag, String attribute, int defaultValue) {
        String sdkVersionString = getTagAttributeText(doc, tag, attribute);
        if (sdkVersionString != null) {
            return Integer.parseInt(sdkVersionString);
        }
        return defaultValue;
    }

    public String getApplicationName() {
        parseAndroidManifest();
        return applicationName;
    }

    public String getPackageName() {
        parseAndroidManifest();
        return packageName;
    }

    public int getSdkVersion() {
        parseAndroidManifest();
        return sdkVersion;
    }

    @Deprecated
    public File getResourceDirectory() {
        return resourcePath.get(0);
    }

    public List<File> getResourcePath() {
        return resourcePath;
    }

    public File getAssetsDirectory() {
        return assetsDirectory;
    }

    private static String getTagAttributeText(Document doc, String tag, String attribute) {
        NodeList elementsByTagName = doc.getElementsByTagName(tag);
        for (int i = 0; i < elementsByTagName.getLength(); ++i) {
            Node item = elementsByTagName.item(i);
            Node namedItem = item.getAttributes().getNamedItem(attribute);
            if (namedItem != null) {
                return namedItem.getTextContent();
            }
        }
        return null;
    }

    private static Application newApplicationInstance(String packageName, String applicationName) {
        Application application;
        try {
            Class<? extends Application> applicationClass =
                    new ClassNameResolver<Application>(packageName, applicationName).resolve();
            application = applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobolectricConfig that = (RobolectricConfig) o;

        if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile) : that.androidManifestFile != null)
            return false;
        if (getAssetsDirectory() != null ? !getAssetsDirectory().equals(that.getAssetsDirectory()) : that.getAssetsDirectory() != null)
            return false;
        if (getResourcePath() != null ? !getResourcePath().equals(that.getResourcePath()) : that.getResourcePath() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
        result = 31 * result + (getResourcePath() != null ? getResourcePath().hashCode() : 0);
        result = 31 * result + (getAssetsDirectory() != null ? getAssetsDirectory().hashCode() : 0);
        return result;
    }
}
