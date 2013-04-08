package org.robolectric;

import org.robolectric.res.ResourcePath;
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

import static android.content.pm.ApplicationInfo.*;

public class AndroidManifest {
    private final File androidManifestFile;
    private final File resDirectory;
    private final File assetsDirectory;
    private String rClassName;
    private String packageName;
    private String processName;
    private String applicationName;
    private boolean manifestIsParsed = false;
    private Integer targetSdkVersion;
    private Integer minSdkVersion;
    private int applicationFlags;
    private final List<ReceiverAndIntentFilter> receivers = new ArrayList<ReceiverAndIntentFilter>();
    private List<AndroidManifest> libraryManifests;

    /**
     * Creates a Robolectric configuration using default Android files relative to the specified base directory.
     * <p/>
     * The manifest will be baseDir/AndroidManifest.xml, res will be baseDir/res, and assets in baseDir/assets.
     *
     * @param baseDir the base directory of your Android project
     */
    public AndroidManifest(final File baseDir) {
        this(new File(baseDir, "AndroidManifest.xml"), new File(baseDir, "res"), new File(baseDir, "assets"));
    }

    public AndroidManifest(final File androidManifestFile, final File resDirectory) {
        this(androidManifestFile, resDirectory, new File(resDirectory.getParent(), "assets"));
    }

    /**
     * Creates a Robolectric configuration using specified locations.
     *
     * @param androidManifestFile location of the AndroidManifest.xml file
     * @param resDirectory        location of the res directory
     * @param assetsDirectory     location of the assets directory
     */
    public AndroidManifest(File androidManifestFile, File resDirectory, File assetsDirectory) {
        this.androidManifestFile = androidManifestFile;
        this.resDirectory = resDirectory;
        this.assetsDirectory = assetsDirectory;
    }

    public String getRClassName() throws Exception {
        parseAndroidManifest();
        return rClassName;
    }

    public Class getRClass() {
        try {
            String rClassName = getRClassName();
            return Class.forName(rClassName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void validate() {
        if (!androidManifestFile.exists() || !androidManifestFile.isFile()) {
            throw new RuntimeException(androidManifestFile.getAbsolutePath() + " not found or not a file; it should point to your project's AndroidManifest.xml");
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
            minSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");
            targetSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion");
            processName = getTagAttributeText(manifestDocument, "application", "android:process");
            if (processName == null) {
                processName = packageName;
            }

            parseApplicationFlags(manifestDocument);
            parseReceivers(manifestDocument, packageName);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        manifestIsParsed = true;
    }

    private void parseReceivers(final Document manifestDocument, String packageName) {
        Node application = manifestDocument.getElementsByTagName("application").item(0);
        if (application == null) {
            return;
        }
        for (Node receiverNode : getChildrenTags(application, "receiver")) {
            Node namedItem = receiverNode.getAttributes().getNamedItem("android:name");
            if (namedItem == null) {
                continue;
            }
            String receiverName = namedItem.getTextContent();
            if (receiverName.startsWith(".")) {
                receiverName = packageName + receiverName;
            }
            for (Node intentFilterNode : getChildrenTags(receiverNode, "intent-filter")) {
                List<String> actions = new ArrayList<String>();
                for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
                    Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
                    if (nameNode != null) {
                        actions.add(nameNode.getTextContent());
                    }
                }
                receivers.add(new ReceiverAndIntentFilter(receiverName, actions));
            }
        }
    }

    private List<Node> getChildrenTags(final Node node, final String tagName) {
        List<Node> children = new ArrayList<Node>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeName().equalsIgnoreCase(tagName)) {
                children.add(childNode);
            }
        }
        return children;
    }

    private void parseApplicationFlags(final Document manifestDocument) {
        applicationFlags = getApplicationFlag(manifestDocument, "android:allowBackup", FLAG_ALLOW_BACKUP);
        applicationFlags += getApplicationFlag(manifestDocument, "android:allowClearUserData", FLAG_ALLOW_CLEAR_USER_DATA);
        applicationFlags += getApplicationFlag(manifestDocument, "android:allowTaskReparenting", FLAG_ALLOW_TASK_REPARENTING);
        applicationFlags += getApplicationFlag(manifestDocument, "android:debuggable", FLAG_DEBUGGABLE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:hasCode", FLAG_HAS_CODE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:killAfterRestore", FLAG_KILL_AFTER_RESTORE);
        applicationFlags += getApplicationFlag(manifestDocument, "android:persistent", FLAG_PERSISTENT);
        applicationFlags += getApplicationFlag(manifestDocument, "android:resizeable", FLAG_RESIZEABLE_FOR_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:restoreAnyVersion", FLAG_RESTORE_ANY_VERSION);
        applicationFlags += getApplicationFlag(manifestDocument, "android:largeScreens", FLAG_SUPPORTS_LARGE_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:normalScreens", FLAG_SUPPORTS_NORMAL_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:anyDensity", FLAG_SUPPORTS_SCREEN_DENSITIES);
        applicationFlags += getApplicationFlag(manifestDocument, "android:smallScreens", FLAG_SUPPORTS_SMALL_SCREENS);
        applicationFlags += getApplicationFlag(manifestDocument, "android:testOnly", FLAG_TEST_ONLY);
        applicationFlags += getApplicationFlag(manifestDocument, "android:vmSafeMode", FLAG_VM_SAFE_MODE);
    }

    private int getApplicationFlag(final Document doc, final String attribute, final int attributeValue) {
        String flagString = getTagAttributeText(doc, "application", attribute);
        return "true".equalsIgnoreCase(flagString) ? attributeValue : 0;
    }

    private Integer getTagAttributeIntValue(final Document doc, final String tag, final String attribute) {
        return getTagAttributeIntValue(doc, tag, attribute, null);
    }

    private Integer getTagAttributeIntValue(final Document doc, final String tag, final String attribute, final Integer defaultValue) {
        String valueString = getTagAttributeText(doc, tag, attribute);
        if (valueString != null) {
            return Integer.parseInt(valueString);
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

    public int getMinSdkVersion() {
        parseAndroidManifest();
        return minSdkVersion == null ? 1 : minSdkVersion;
    }

    public int getTargetSdkVersion() {
        parseAndroidManifest();
        return targetSdkVersion == null ? getMinSdkVersion() : targetSdkVersion;
    }

    public int getApplicationFlags() {
        parseAndroidManifest();
        return applicationFlags;
    }

    public String getProcessName() {
        parseAndroidManifest();
        return processName;
    }

    public ResourcePath getResourcePath() {
        validate();
        return new ResourcePath(getRClass(), resDirectory, assetsDirectory);
    }

    public List<ResourcePath> getIncludedResourcePaths() {
        List<ResourcePath> resourcePaths = new ArrayList<ResourcePath>();
        resourcePaths.add(getResourcePath());
        for (AndroidManifest libraryManifest : getLibraryManifests()) {
            resourcePaths.addAll(libraryManifest.getIncludedResourcePaths());
        }
        return resourcePaths;
    }

    protected void createLibraryManifests() {
        libraryManifests = new ArrayList<AndroidManifest>();
        List<File> libraryBaseDirs = findLibraries();

        for (File libraryBaseDir : libraryBaseDirs) {
            AndroidManifest libraryManifest = createLibraryAndroidManifest(libraryBaseDir);
            libraryManifest.createLibraryManifests();
            libraryManifests.add(libraryManifest);
        }
    }

    protected List<File> findLibraries() {
        File baseDir = getBaseDir();
        List<File> libraryBaseDirs = new ArrayList<File>();

        Properties properties = getProperties(new File(baseDir, "project.properties"));
        if (properties != null) {
            int libRef = 1;
            String lib;
            while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
                File libraryBaseDir = new File(baseDir, lib);
                libraryBaseDirs.add(libraryBaseDir);
                libRef++;
            }
        }
        return libraryBaseDirs;
    }

    protected File getBaseDir() {
        return getResDirectory().getParentFile();
    }

    protected AndroidManifest createLibraryAndroidManifest(File libraryBaseDir) {
        return new AndroidManifest(libraryBaseDir);
    }

    public List<AndroidManifest> getLibraryManifests() {
        if (libraryManifests == null) createLibraryManifests();
        return Collections.unmodifiableList(libraryManifests);
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

    public File getResDirectory() {
        return resDirectory;
    }

    public File getAssetsDirectory() {
        return assetsDirectory;
    }

    public int getReceiverCount() {
        parseAndroidManifest();
        return receivers.size();
    }

    public String getReceiverClassName(final int receiverIndex) {
        parseAndroidManifest();
        return receivers.get(receiverIndex).getBroadcastReceiverClassName();
    }

    public List<String> getReceiverIntentFilterActions(final int receiverIndex) {
        parseAndroidManifest();
        return receivers.get(receiverIndex).getIntentFilterActions();
    }

    private static String getTagAttributeText(final Document doc, final String tag, final String attribute) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidManifest that = (AndroidManifest) o;

        if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile) : that.androidManifestFile != null)
            return false;
        if (assetsDirectory != null ? !assetsDirectory.equals(that.assetsDirectory) : that.assetsDirectory != null)
            return false;
        if (resDirectory != null ? !resDirectory.equals(that.resDirectory) : that.resDirectory != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
        result = 31 * result + (resDirectory != null ? resDirectory.hashCode() : 0);
        result = 31 * result + (assetsDirectory != null ? assetsDirectory.hashCode() : 0);
        return result;
    }

    private static class ReceiverAndIntentFilter {
        private final List<String> intentFilterActions;
        private final String broadcastReceiverClassName;

        public ReceiverAndIntentFilter(final String broadcastReceiverClassName, final List<String> intentFilterActions) {
            this.broadcastReceiverClassName = broadcastReceiverClassName;
            this.intentFilterActions = intentFilterActions;
        }

        public String getBroadcastReceiverClassName() {
            return broadcastReceiverClassName;
        }

        public List<String> getIntentFilterActions() {
            return intentFilterActions;
        }
    }
}
