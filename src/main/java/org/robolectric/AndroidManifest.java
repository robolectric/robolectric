package org.robolectric;

import android.app.Activity;
import org.robolectric.res.ActivityData;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.ApplicationInfo.FLAG_HAS_CODE;
import static android.content.pm.ApplicationInfo.FLAG_KILL_AFTER_RESTORE;
import static android.content.pm.ApplicationInfo.FLAG_PERSISTENT;
import static android.content.pm.ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_RESTORE_ANY_VERSION;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_TEST_ONLY;
import static android.content.pm.ApplicationInfo.FLAG_VM_SAFE_MODE;

public class AndroidManifest {
  private final FsFile androidManifestFile;
  private final FsFile resDirectory;
  private final FsFile assetsDirectory;
  private boolean manifestIsParsed = false;

  private String applicationName;
  private String rClassName;
  private String packageName;
  private String processName;
  private String themeRef;
  private Integer targetSdkVersion;
  private Integer minSdkVersion;
  private int versionCode;
  private String versionName;
  private int applicationFlags;
  private final List<ReceiverAndIntentFilter> receivers = new ArrayList<ReceiverAndIntentFilter>();
  private final Map<String, ActivityData> activityDatas = new LinkedHashMap<String, ActivityData>();
  private final Map<String, String> applicationMetaData = new LinkedHashMap<String, String>();
  private List<AndroidManifest> libraryManifests;

  /**
   * Creates a Robolectric configuration using default Android files relative to the specified base directory.
   * <p/>
   * The manifest will be baseDir/AndroidManifest.xml, res will be baseDir/res, and assets in baseDir/assets.
   *
   * @param baseDir the base directory of your Android project
   * @deprecated Use {@link #AndroidManifest(org.robolectric.res.FsFile, org.robolectric.res.FsFile, org.robolectric.res.FsFile)} instead.}
   */
  public AndroidManifest(final File baseDir) {
    this(Fs.newFile(baseDir));
  }

  public AndroidManifest(final FsFile androidManifestFile, final FsFile resDirectory) {
    this(androidManifestFile, resDirectory, resDirectory.getParent().join("assets"));
  }

  /**
   * @deprecated Use {@link #AndroidManifest(org.robolectric.res.FsFile, org.robolectric.res.FsFile, org.robolectric.res.FsFile)} instead.}
   */
  public AndroidManifest(final FsFile baseDir) {
    this(baseDir.join("AndroidManifest.xml"), baseDir.join("res"), baseDir.join("assets"));
  }

  /**
   * Creates a Robolectric configuration using specified locations.
   *
   * @param androidManifestFile location of the AndroidManifest.xml file
   * @param resDirectory        location of the res directory
   * @param assetsDirectory     location of the assets directory
   */
  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory) {
    this.androidManifestFile = androidManifestFile;
    this.resDirectory = resDirectory;
    this.assetsDirectory = assetsDirectory;
  }

  public String getThemeRef(Class<? extends Activity> activityClass) {
    ActivityData activityData = getActivityData(activityClass.getName());
    String themeRef = activityData != null ? activityData.getThemeRef() : null;
    if (themeRef == null) {
      themeRef = getThemeRef();
    }
    return themeRef;
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
        return null;
    }
  }

  public void validate() {
    if (!androidManifestFile.exists() || !androidManifestFile.isFile()) {
      throw new RuntimeException(androidManifestFile + " not found or not a file; it should point to your project's AndroidManifest.xml");
    }
  }

  void parseAndroidManifest() {
    if (manifestIsParsed) {
      return;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream inputStream = androidManifestFile.getInputStream();
      Document manifestDocument = db.parse(inputStream);
      inputStream.close();

      packageName = getTagAttributeText(manifestDocument, "manifest", "package");
      versionCode = getTagAttributeIntValue(manifestDocument, "manifest", "android:versionCode", 0);
      versionName = getTagAttributeText(manifestDocument, "manifest", "android:versionName");
      rClassName = packageName + ".R";
      applicationName = getTagAttributeText(manifestDocument, "application", "android:name");
      minSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");
      targetSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion");
      processName = getTagAttributeText(manifestDocument, "application", "android:process");
      if (processName == null) {
        processName = packageName;
      }

      themeRef = getTagAttributeText(manifestDocument, "application", "android:theme");

      parseApplicationFlags(manifestDocument);
      parseReceivers(manifestDocument);
      parseActivities(manifestDocument);
      parseApplicationMetaData(manifestDocument);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    manifestIsParsed = true;
  }

  private void parseReceivers(final Document manifestDocument) {
    Node application = manifestDocument.getElementsByTagName("application").item(0);
    if (application == null) return;

    for (Node receiverNode : getChildrenTags(application, "receiver")) {
      Node namedItem = receiverNode.getAttributes().getNamedItem("android:name");
      if (namedItem == null) continue;

      String receiverName = resolveClassRef(namedItem.getTextContent());
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

  private void parseActivities(final Document manifestDocument) {
    Node application = manifestDocument.getElementsByTagName("application").item(0);
    if (application == null) return;

    for (Node activityNode : getChildrenTags(application, "activity")) {
      NamedNodeMap attributes = activityNode.getAttributes();
      Node nameAttr = attributes.getNamedItem("android:name");
      Node themeAttr = attributes.getNamedItem("android:theme");
      if (nameAttr == null) continue;
      String activityName = nameAttr.getNodeValue();
      activityDatas.put(activityName,
          new ActivityData(activityName,
              themeAttr == null ? null : resolveClassRef(themeAttr.getNodeValue())
          ));
    }
  }

  private void parseApplicationMetaData(final Document manifestDocument) {
    Node application = manifestDocument.getElementsByTagName("application").item(0);
    if (application == null) return;
    
    for (Node metaNode : getChildrenTags(application, "meta-data")) {
      NamedNodeMap attributes = metaNode.getAttributes();
      Node nameAttr = attributes.getNamedItem("android:name");
      Node valueAttr = attributes.getNamedItem("android:value");
      // TODO: support android:resource attribute
      if (valueAttr == null) { continue; }
      applicationMetaData.put(nameAttr.getNodeValue(), valueAttr.getNodeValue());
    }
  }
  
  private String resolveClassRef(String maybePartialClassName) {
    return (maybePartialClassName.startsWith(".")) ? packageName + maybePartialClassName : maybePartialClassName;
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

  public int getVersionCode() {
    return versionCode;
  }

  public String getVersionName() {
    return versionName;
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

  public Map<String, String> getApplicationMetaData() {
    parseAndroidManifest();
    return applicationMetaData;
  }
  
  public ResourcePath getResourcePath() {
    validate();
    return new ResourcePath(getRClass(), getPackageName(), resDirectory, assetsDirectory);
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
    List<FsFile> libraryBaseDirs = findLibraries();

    for (FsFile libraryBaseDir : libraryBaseDirs) {
      AndroidManifest libraryManifest = createLibraryAndroidManifest(libraryBaseDir);
      libraryManifest.createLibraryManifests();
      libraryManifests.add(libraryManifest);
    }
  }

  protected List<FsFile> findLibraries() {
    FsFile baseDir = getBaseDir();
    List<FsFile> libraryBaseDirs = new ArrayList<FsFile>();

    Properties properties = getProperties(baseDir.join("project.properties"));
    // get the project.properties overrides and apply them (if any)
    Properties overrideProperties = getProperties(baseDir.join("test-project.properties"));
    if (overrideProperties!=null) properties.putAll(overrideProperties);
    if (properties != null) {
      int libRef = 1;
      String lib;
      while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
        FsFile libraryBaseDir = baseDir.join(lib);
        if (libraryBaseDir.exists()) {
          libraryBaseDirs.add(libraryBaseDir);
        }

        libRef++;
      }
    }
    return libraryBaseDirs;
  }

  protected FsFile getBaseDir() {
    return getResDirectory().getParent();
  }

  protected AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir) {
    return new AndroidManifest(libraryBaseDir);
  }

  public List<AndroidManifest> getLibraryManifests() {
    if (libraryManifests == null) createLibraryManifests();
    return Collections.unmodifiableList(libraryManifests);
  }

  private static Properties getProperties(FsFile propertiesFile) {
    if (!propertiesFile.exists()) return null;

    Properties properties = new Properties();
    InputStream stream;
    try {
      stream = propertiesFile.getInputStream();
    } catch (IOException e) {
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

  public FsFile getResDirectory() {
    return resDirectory;
  }

  public FsFile getAssetsDirectory() {
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

  public ActivityData getActivityData(String activityClassName) {
    return activityDatas.get(activityClassName);
  }

  public String getThemeRef() {
    return themeRef;
  }

  public Map<String, ActivityData> getActivityDatas() {
    return activityDatas;
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
