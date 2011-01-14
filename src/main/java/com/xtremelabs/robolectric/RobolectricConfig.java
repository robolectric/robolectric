package com.xtremelabs.robolectric;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RobolectricConfig {
    private File androidManifestFile;
    private File resourceDirectory;
    private File assetsDirectory;

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
        this.androidManifestFile = androidManifestFile;
        this.resourceDirectory = resourceDirectory;
        this.assetsDirectory = assetsDirectory;
    }

    public String findRClassName() throws Exception {
        return findResourcePackageName(getAndroidManifestFile());
    }

    public void validate() throws FileNotFoundException {
        if (!getAndroidManifestFile().exists() || !getAndroidManifestFile().isFile()) {
            throw new FileNotFoundException(getAndroidManifestFile().getAbsolutePath() + " not found or not a file; it should point to your project's AndroidManifest.xml");
        }

        if (!getResourceDirectory().exists() || !getResourceDirectory().isDirectory()) {
            throw new FileNotFoundException(getResourceDirectory().getAbsolutePath() + " not found or not a directory; it should point to your project's res directory");
        }
    }

    private String findResourcePackageName(File projectManifestFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(projectManifestFile);

        String projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();

        return projectPackage + ".R";
    }

    public File getAndroidManifestFile() {
        return androidManifestFile;
    }

    public File getResourceDirectory() {
        return resourceDirectory;
    }

    public File getAssetsDirectory() {
        return assetsDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobolectricConfig that = (RobolectricConfig) o;

        if (getAndroidManifestFile() != null ? !getAndroidManifestFile().equals(that.getAndroidManifestFile()) : that.getAndroidManifestFile() != null)
            return false;
        if (getAssetsDirectory() != null ? !getAssetsDirectory().equals(that.getAssetsDirectory()) : that.getAssetsDirectory() != null)
            return false;
        if (getResourceDirectory() != null ? !getResourceDirectory().equals(that.getResourceDirectory()) : that.getResourceDirectory() != null)
            return false;

        return true;
    }


    @Override
    public int hashCode() {
        int result = getAndroidManifestFile() != null ? getAndroidManifestFile().hashCode() : 0;
        result = 31 * result + (getResourceDirectory() != null ? getResourceDirectory().hashCode() : 0);
        result = 31 * result + (getAssetsDirectory() != null ? getAssetsDirectory().hashCode() : 0);
        return result;
    }
}
