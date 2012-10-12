package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ApplicationInfo.*;
import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;

public class RobolectricConfig {
    private final File androidManifestFile;
    private final File resourceDirectory;
    private final File assetsDirectory;
    private String rClassName;
    private String packageName;
    private String processName;
    private String applicationName;
    private boolean manifestIsParsed = false;
    private int sdkVersion;
    private int minSdkVersion;
    private boolean sdkVersionSpecified = true;
    private boolean minSdkVersionSpecified = true;
    private int applicationFlags;
    private final List<ReceiverAndIntentFilter> receivers = new ArrayList<ReceiverAndIntentFilter>();
    private boolean strictI18n = false;
    private String valuesResQualifiers="";
    private String oldValuesResQualifier="";

    /**
     * Creates a Robolectric configuration using default Android files relative to the specified base directory.
     * <p/>
     * The manifest will be baseDir/AndroidManifest.xml, res will be baseDir/res, and assets in baseDir/assets.
     *
     * @param baseDir the base directory of your Android project
     */
    public RobolectricConfig(final File baseDir) {
        this(new File(baseDir, "AndroidManifest.xml"), new File(baseDir, "res"), new File(baseDir, "assets"));
    }

    public RobolectricConfig(final File androidManifestFile, final File resourceDirectory) {
        this(androidManifestFile, resourceDirectory, new File(resourceDirectory.getParent(), "assets"));
    }

    /**
     * Creates a Robolectric configuration using specified locations.
     *
     * @param androidManifestFile location of the AndroidManifest.xml file
     * @param resourceDirectory   location of the res directory
     * @param assetsDirectory     location of the assets directory
     */
    public RobolectricConfig(final File androidManifestFile, final File resourceDirectory, final File assetsDirectory) {
        this.androidManifestFile = androidManifestFile;
        this.resourceDirectory = resourceDirectory;
        this.assetsDirectory = assetsDirectory;
    }

    public String getRClassName() throws Exception {
        parseAndroidManifest();
        return rClassName;
    }

    public void validate() throws FileNotFoundException {
        if (!androidManifestFile.exists() || !androidManifestFile.isFile()) {
            throw new FileNotFoundException(androidManifestFile.getAbsolutePath() + " not found or not a file; it should point to your project's AndroidManifest.xml");
        }

        if (!getResourceDirectory().exists() || !getResourceDirectory().isDirectory()) {
            throw new FileNotFoundException(getResourceDirectory().getAbsolutePath() + " not found or not a directory; it should point to your project's res directory");
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
            Integer minSdkVer = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");
            Integer sdkVer = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion");
            if (minSdkVer == null) {
                minSdkVersion = DEFAULT_SDK_VERSION;
                minSdkVersionSpecified = false;
            } else {
                minSdkVersion = minSdkVer;
            }
            if (sdkVer == null) {
                sdkVersion = DEFAULT_SDK_VERSION;
                sdkVersionSpecified = false;
            } else {
                sdkVersion = sdkVer;
            }

            processName = getTagAttributeText(manifestDocument, "application", "android:process");
            if (processName == null) {
            	processName = packageName;
            }

            parseApplicationFlags(manifestDocument);
            parseReceivers(manifestDocument, packageName);
        } catch (Exception ignored) {
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
		return minSdkVersion;
	}

    public int getSdkVersion() {
        parseAndroidManifest();
        return sdkVersion;
    }

    public int getApplicationFlags() {
    	parseAndroidManifest();
    	return applicationFlags;
    }
    
    public String getProcessName() {
		parseAndroidManifest();
		return processName;
	}
    
    public File getResourceDirectory() {
        return resourceDirectory;
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

    public boolean getStrictI18n() {
    	return strictI18n;
    }
    
    public void setStrictI18n(boolean strict) {
    	strictI18n = strict;
    }

    public void setValuesResQualifiers( String qualifiers ){
    	this.oldValuesResQualifier = this.valuesResQualifiers;
    	this.valuesResQualifiers = qualifiers;
    }
    
    public String getValuesResQualifiers() {
    	return valuesResQualifiers;
    }
    
    public boolean isValuesResQualifiersChanged() {
    	return !valuesResQualifiers.equals( oldValuesResQualifier );
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
    
    private static Application newApplicationInstance(final String packageName, final String applicationName) {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RobolectricConfig that = (RobolectricConfig) o;

        if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile) : that.androidManifestFile != null) {
            return false;
        }
        if (getAssetsDirectory() != null ? !getAssetsDirectory().equals(that.getAssetsDirectory()) : that.getAssetsDirectory() != null) {
            return false;
        }
        if (getResourceDirectory() != null ? !getResourceDirectory().equals(that.getResourceDirectory()) : that.getResourceDirectory() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
        result = 31 * result + (getResourceDirectory() != null ? getResourceDirectory().hashCode() : 0);
        result = 31 * result + (getAssetsDirectory() != null ? getAssetsDirectory().hashCode() : 0);
        return result;
    }
    
    public int getRealSdkVersion() {
        parseAndroidManifest();
        if (sdkVersionSpecified) {
            return sdkVersion;
        }
        if (minSdkVersionSpecified) {
            return minSdkVersion;
        }
        return sdkVersion;
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
