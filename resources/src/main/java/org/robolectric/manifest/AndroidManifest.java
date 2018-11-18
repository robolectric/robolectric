package org.robolectric.manifest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.robolectric.UsesSdk;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A wrapper for an Android App Manifest, which represents information about one's App to an Android system.
 * @see <a href="https://developer.android.com/guide/topics/manifest/manifest-intro.html">Android App Manifest</a>
 */
public class AndroidManifest implements UsesSdk {
  private final FsFile androidManifestFile;
  private final FsFile resDirectory;
  private final FsFile assetsDirectory;
  private final String overridePackageName;
  private final List<AndroidManifest> libraryManifests;
  private final FsFile apkFile;

  private boolean manifestIsParsed;

  private String applicationName;
  private String applicationLabel;
  private String rClassName;
  private String packageName;
  private String processName;
  private String themeRef;
  private String labelRef;
  private Integer minSdkVersion;
  private Integer targetSdkVersion;
  private Integer maxSdkVersion;
  private int versionCode;
  private String versionName;
  private final Map<String, PermissionItemData> permissions = new HashMap<>();
  private final Map<String, PermissionGroupItemData> permissionGroups = new HashMap<>();
  private final List<ContentProviderData> providers = new ArrayList<>();
  private final List<BroadcastReceiverData> receivers = new ArrayList<>();
  private final Map<String, ServiceData> serviceDatas = new LinkedHashMap<>();
  private final Map<String, ActivityData> activityDatas = new LinkedHashMap<>();
  private final List<String> usedPermissions = new ArrayList<>();
  private final Map<String, String> applicationAttributes = new HashMap<>();
  private MetaData applicationMetaData;

  private Boolean supportsBinaryResourcesMode;

  /**
   * Creates a Robolectric configuration using specified locations.
   *
   * @param androidManifestFile Location of the AndroidManifest.xml file.
   * @param resDirectory        Location of the res directory.
   * @param assetsDirectory     Location of the assets directory.
   */
  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory) {
    this(androidManifestFile, resDirectory, assetsDirectory, null);
  }

  /**
   * Creates a Robolectric configuration using specified values.
   *
   * @param androidManifestFile Location of the AndroidManifest.xml file.
   * @param resDirectory        Location of the res directory.
   * @param assetsDirectory     Location of the assets directory.
   * @param overridePackageName Application package name.
   */
  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory,
      String overridePackageName) {
    this(androidManifestFile, resDirectory, assetsDirectory, Collections.emptyList(), overridePackageName);
  }

  /**
   * Creates a Robolectric configuration using specified values.
   *
   * @param androidManifestFile Location of the AndroidManifest.xml file.
   * @param resDirectory        Location of the res directory.
   * @param assetsDirectory     Location of the assets directory.
   * @param libraryManifests    List of dependency library manifests.
   * @param overridePackageName Application package name.
   */
  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory,
      @Nonnull List<AndroidManifest> libraryManifests, String overridePackageName) {
    this(
        androidManifestFile,
        resDirectory,
        assetsDirectory,
        libraryManifests,
        overridePackageName,
        null);
  }

  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory,
      @Nonnull List<AndroidManifest> libraryManifests, String overridePackageName, FsFile apkFile) {
    this.androidManifestFile = androidManifestFile;
    this.resDirectory = resDirectory;
    this.assetsDirectory = assetsDirectory;
    this.overridePackageName = overridePackageName;
    this.libraryManifests = libraryManifests;

    this.packageName = overridePackageName;
    this.apkFile = apkFile;
  }

  public String getThemeRef(String activityClassName) {
    ActivityData activityData = getActivityData(activityClassName);
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

  @SuppressWarnings("CatchAndPrintStackTrace")
  void parseAndroidManifest() {
    if (manifestIsParsed) {
      return;
    }

    if (androidManifestFile != null && androidManifestFile.exists()) {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();
        InputStream inputStream = androidManifestFile.getInputStream();
        Document manifestDocument = db.parse(inputStream);
        inputStream.close();

        if (!packageNameIsOverridden()) {
          packageName = getTagAttributeText(manifestDocument, "manifest", "package");
        }

        versionCode = getTagAttributeIntValue(manifestDocument, "manifest", "android:versionCode", 0);
        versionName = getTagAttributeText(manifestDocument, "manifest", "android:versionName");
        rClassName = packageName + ".R";

        Node applicationNode = findApplicationNode(manifestDocument);
        if (applicationNode != null) {
          NamedNodeMap attributes = applicationNode.getAttributes();
          int attrCount = attributes.getLength();
          for (int i = 0; i < attrCount; i++) {
            Node attr = attributes.item(i);
            applicationAttributes.put(attr.getNodeName(), attr.getTextContent());
          }

          applicationName = applicationAttributes.get("android:name");
          applicationLabel = applicationAttributes.get("android:label");
          processName = applicationAttributes.get("android:process");
          themeRef = applicationAttributes.get("android:theme");
          labelRef = applicationAttributes.get("android:label");

          parseReceivers(applicationNode);
          parseServices(applicationNode);
          parseActivities(applicationNode);
          parseApplicationMetaData(applicationNode);
          parseContentProviders(applicationNode);
        }

        minSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");

        String targetSdkText =
            getTagAttributeText(manifestDocument, "uses-sdk", "android:targetSdkVersion");
        if (targetSdkText != null) {
          // Support Android O Preview. This can be removed once Android O is officially launched.
          targetSdkVersion = targetSdkText.equals("O") ? 26 : Integer.parseInt(targetSdkText);
        }

        maxSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:maxSdkVersion");
        if (processName == null) {
          processName = packageName;
        }

        parseUsedPermissions(manifestDocument);
        parsePermissions(manifestDocument);
        parsePermissionGroups(manifestDocument);
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
    } else {
      if (androidManifestFile != null) {
        System.out.println("WARNING: No manifest file found at " + androidManifestFile.getPath() + ".");
        System.out.println("Falling back to the Android OS resources only.");
        System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
      }

      if (packageName == null || packageName.equals("")) {
        packageName = "org.robolectric.default";
      }

      rClassName = packageName + ".R";

      if (androidManifestFile != null) {
        System.err.println("No such manifest file: " + androidManifestFile);
      }
    }

    manifestIsParsed = true;
  }

  private boolean packageNameIsOverridden() {
    return overridePackageName != null && !overridePackageName.isEmpty();
  }

  private void parseUsedPermissions(Document manifestDocument) {
    NodeList elementsByTagName = manifestDocument.getElementsByTagName("uses-permission");
    int length = elementsByTagName.getLength();
    for (int i = 0; i < length; i++) {
      Node node = elementsByTagName.item(i).getAttributes().getNamedItem("android:name");
      usedPermissions.add(node.getNodeValue());
    }
  }

  private void parsePermissions(final Document manifestDocument) {
    NodeList elementsByTagName = manifestDocument.getElementsByTagName("permission");

    for (int i = 0; i < elementsByTagName.getLength(); i++) {
      Node permissionNode = elementsByTagName.item(i);
      final MetaData metaData = new MetaData(getChildrenTags(permissionNode, "meta-data"));
      String name = getAttributeValue(permissionNode, "android:name");
      permissions.put(
          name,
          new PermissionItemData(
              name,
              getAttributeValue(permissionNode, "android:label"),
              getAttributeValue(permissionNode, "android:description"),
              getAttributeValue(permissionNode, "android:permissionGroup"),
              getAttributeValue(permissionNode, "android:protectionLevel"),
              metaData));
    }
  }

  private void parsePermissionGroups(final Document manifestDocument) {
    NodeList elementsByTagName = manifestDocument.getElementsByTagName("permission-group");

    for (int i = 0; i < elementsByTagName.getLength(); i++) {
      Node permissionGroupNode = elementsByTagName.item(i);
      final MetaData metaData = new MetaData(getChildrenTags(permissionGroupNode, "meta-data"));
      String name = getAttributeValue(permissionGroupNode, "android:name");
      permissionGroups.put(
          name,
          new PermissionGroupItemData(
              name,
              getAttributeValue(permissionGroupNode, "android:label"),
              getAttributeValue(permissionGroupNode, "android:description"),
              metaData));
    }
  }

  private void parseContentProviders(Node applicationNode) {
    for (Node contentProviderNode : getChildrenTags(applicationNode, "provider")) {
      String name = getAttributeValue(contentProviderNode, "android:name");
      String authorities = getAttributeValue(contentProviderNode, "android:authorities");
      MetaData metaData = new MetaData(getChildrenTags(contentProviderNode, "meta-data"));

      List<PathPermissionData> pathPermissionDatas = new ArrayList<>();
      for (Node node : getChildrenTags(contentProviderNode, "path-permission")) {
        pathPermissionDatas.add(new PathPermissionData(
                getAttributeValue(node, "android:path"),
                getAttributeValue(node, "android:pathPrefix"),
                getAttributeValue(node, "android:pathPattern"),
                getAttributeValue(node, "android:readPermission"),
                getAttributeValue(node, "android:writePermission")
        ));
      }

      providers.add(
          new ContentProviderData(
              resolveClassRef(name),
              metaData,
              authorities,
              parseNodeAttributes(contentProviderNode),
              pathPermissionDatas));
    }
  }

  private @Nullable String getAttributeValue(Node parentNode, String attributeName) {
    Node attributeNode = parentNode.getAttributes().getNamedItem(attributeName);
    return attributeNode == null ? null : attributeNode.getTextContent();
  }

  private static HashMap<String, String> parseNodeAttributes(Node node) {
    final NamedNodeMap attributes = node.getAttributes();
    final int attrCount = attributes.getLength();
    final HashMap<String, String> receiverAttrs = new HashMap<>(attributes.getLength());
    for (int i = 0; i < attrCount; i++) {
      Node attribute = attributes.item(i);
      String value = attribute.getNodeValue();
      if (value != null) {
        receiverAttrs.put(attribute.getNodeName(), value);
      }
    }
    return receiverAttrs;
  }

  private void parseReceivers(Node applicationNode) {
    for (Node receiverNode : getChildrenTags(applicationNode, "receiver")) {
      final HashMap<String, String> receiverAttrs = parseNodeAttributes(receiverNode);

      String receiverName = resolveClassRef(receiverAttrs.get("android:name"));
      receiverAttrs.put("android:name", receiverName);

      MetaData metaData = new MetaData(getChildrenTags(receiverNode, "meta-data"));

      final List<IntentFilterData> intentFilterData = parseIntentFilters(receiverNode);
      BroadcastReceiverData receiver =
          new BroadcastReceiverData(receiverAttrs, metaData, intentFilterData);
      List<Node> intentFilters = getChildrenTags(receiverNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            receiver.addAction(nameNode.getTextContent());
          }
        }
      }

      receivers.add(receiver);
    }
  }

  private void parseServices(Node applicationNode) {
    for (Node serviceNode : getChildrenTags(applicationNode, "service")) {
      final HashMap<String, String> serviceAttrs = parseNodeAttributes(serviceNode);

      String serviceName = resolveClassRef(serviceAttrs.get("android:name"));
      serviceAttrs.put("android:name", serviceName);

      MetaData metaData = new MetaData(getChildrenTags(serviceNode, "meta-data"));

      final List<IntentFilterData> intentFilterData = parseIntentFilters(serviceNode);
      ServiceData service = new ServiceData(serviceAttrs, metaData, intentFilterData);
      List<Node> intentFilters = getChildrenTags(serviceNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            service.addAction(nameNode.getTextContent());
          }
        }
      }

      serviceDatas.put(serviceName, service);
    }
  }

  private void parseActivities(Node applicationNode) {
    for (Node activityNode : getChildrenTags(applicationNode, "activity")) {
      parseActivity(activityNode, false);
    }

    for (Node activityNode : getChildrenTags(applicationNode, "activity-alias")) {
      parseActivity(activityNode, true);
    }
  }

  private Node findApplicationNode(Document manifestDocument) {
    NodeList applicationNodes = manifestDocument.getElementsByTagName("application");
    if (applicationNodes.getLength() > 1) {
      throw new RuntimeException("found " + applicationNodes.getLength() + " application elements");
    }
    return applicationNodes.item(0);
  }

  private void parseActivity(Node activityNode, boolean isAlias) {
    final List<IntentFilterData> intentFilterData = parseIntentFilters(activityNode);
    final MetaData metaData = new MetaData(getChildrenTags(activityNode, "meta-data"));
    final HashMap<String, String> activityAttrs = parseNodeAttributes(activityNode);

    String activityName = resolveClassRef(activityAttrs.get(ActivityData.getNameAttr("android")));
    if (activityName == null) {
      return;
    }
    ActivityData targetActivity = null;
    if (isAlias) {
      String targetName = resolveClassRef(activityAttrs.get(ActivityData.getTargetAttr("android")));
      if (activityName == null) {
        return;
      }
      // The target activity should have been parsed already so if it exists we should find it in
      // activityDatas.
      targetActivity = activityDatas.get(targetName);
      activityAttrs.put(ActivityData.getTargetAttr("android"), targetName);
    }
    activityAttrs.put(ActivityData.getNameAttr("android"), activityName);
    activityDatas.put(activityName, new ActivityData("android", activityAttrs, intentFilterData, targetActivity, metaData));
  }

  private List<IntentFilterData> parseIntentFilters(final Node activityNode) {
    ArrayList<IntentFilterData> intentFilterDatas = new ArrayList<>();
    for (Node n : getChildrenTags(activityNode, "intent-filter")) {
      ArrayList<String> actionNames = new ArrayList<>();
      ArrayList<String> categories = new ArrayList<>();
      //should only be one action.
      for (Node action : getChildrenTags(n, "action")) {
        NamedNodeMap attributes = action.getAttributes();
        Node actionNameNode = attributes.getNamedItem("android:name");
        if (actionNameNode != null) {
          actionNames.add(actionNameNode.getNodeValue());
        }
      }
      for (Node category : getChildrenTags(n, "category")) {
        NamedNodeMap attributes = category.getAttributes();
        Node categoryNameNode = attributes.getNamedItem("android:name");
        if (categoryNameNode != null) {
          categories.add(categoryNameNode.getNodeValue());
        }
      }
      IntentFilterData intentFilterData = new IntentFilterData(actionNames, categories);
      intentFilterData = parseIntentFilterData(n, intentFilterData);
      intentFilterDatas.add(intentFilterData);
    }

    return intentFilterDatas;
  }

  private IntentFilterData parseIntentFilterData(final Node intentFilterNode, IntentFilterData intentFilterData) {
    for (Node n : getChildrenTags(intentFilterNode, "data")) {
      NamedNodeMap attributes = n.getAttributes();
      String host = null;
      String port = null;

      Node schemeNode = attributes.getNamedItem("android:scheme");
      if (schemeNode != null) {
        intentFilterData.addScheme(schemeNode.getNodeValue());
      }

      Node hostNode = attributes.getNamedItem("android:host");
      if (hostNode != null) {
        host = hostNode.getNodeValue();
      }

      Node portNode = attributes.getNamedItem("android:port");
      if (portNode != null) {
        port = portNode.getNodeValue();
      }
      intentFilterData.addAuthority(host, port);

      Node pathNode = attributes.getNamedItem("android:path");
      if (pathNode != null) {
        intentFilterData.addPath(pathNode.getNodeValue());
      }

      Node pathPatternNode = attributes.getNamedItem("android:pathPattern");
      if (pathPatternNode != null) {
        intentFilterData.addPathPattern(pathPatternNode.getNodeValue());
      }

      Node pathPrefixNode = attributes.getNamedItem("android:pathPrefix");
      if (pathPrefixNode != null) {
        intentFilterData.addPathPrefix(pathPrefixNode.getNodeValue());
      }

      Node mimeTypeNode = attributes.getNamedItem("android:mimeType");
      if (mimeTypeNode != null) {
        intentFilterData.addMimeType(mimeTypeNode.getNodeValue());
      }
    }
    return intentFilterData;
  }

  /***
   * Allows ShadowPackageManager to provide
   * a resource index for initialising the resource attributes in all the metadata elements
   * @param resourceTable used for getting resource IDs from string identifiers
   */
  public void initMetaData(ResourceTable resourceTable) throws RoboNotFoundException {
    if (!packageNameIsOverridden()) {
      // packageName needs to be resolved
      parseAndroidManifest();
    }

    if (applicationMetaData != null) {
      applicationMetaData.init(resourceTable, packageName);
    }
    for (PackageItemData receiver : receivers) {
      receiver.getMetaData().init(resourceTable, packageName);
    }
    for (ServiceData service : serviceDatas.values()) {
      service.getMetaData().init(resourceTable, packageName);
    }
    for (ContentProviderData providerData : providers) {
      providerData.getMetaData().init(resourceTable, packageName);
    }
  }

  private void parseApplicationMetaData(Node applicationNode) {
    applicationMetaData = new MetaData(getChildrenTags(applicationNode, "meta-data"));
  }

  private String resolveClassRef(String maybePartialClassName) {
    return (maybePartialClassName.startsWith(".")) ? packageName + maybePartialClassName : maybePartialClassName;
  }

  private List<Node> getChildrenTags(final Node node, final String tagName) {
    List<Node> children = new ArrayList<>();
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (childNode.getNodeName().equalsIgnoreCase(tagName)) {
        children.add(childNode);
      }
    }
    return children;
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

  public String getActivityLabel(String activityClassName) {
    parseAndroidManifest();
    ActivityData data = getActivityData(activityClassName);
    return (data != null && data.getLabel() != null) ? data.getLabel() : applicationLabel;
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

  public String getLabelRef() {
    return labelRef;
  }

  /**
   * Returns the minimum Android SDK version that this package expects to be runnable on, as
   * specified in the manifest.
   *
   * <p>Note that if `targetSdkVersion` isn't set, this value changes the behavior of some Android
   * code (notably {@link android.content.SharedPreferences}) to emulate old bugs.
   *
   * @return the minimum SDK version, or Jelly Bean (16) by default
   */
  @Override
  public int getMinSdkVersion() {
    parseAndroidManifest();
    return minSdkVersion == null ? 16 : minSdkVersion;
  }

  /**
   * Returns the Android SDK version that this package prefers to be run on, as specified in the
   * manifest.
   *
   * <p>Note that this value changes the behavior of some Android code (notably {@link
   * android.content.SharedPreferences}) to emulate old bugs.
   *
   * @return the minimum SDK version, or Jelly Bean (16) by default
   */
  @Override
  public int getTargetSdkVersion() {
    parseAndroidManifest();
    return targetSdkVersion == null ? getMinSdkVersion() : targetSdkVersion;
  }

  @Override
  public Integer getMaxSdkVersion() {
    parseAndroidManifest();
    return maxSdkVersion;
  }

  public Map<String, String> getApplicationAttributes() {
    parseAndroidManifest();
    return applicationAttributes;
  }

  public String getProcessName() {
    parseAndroidManifest();
    return processName;
  }

  public Map<String, Object> getApplicationMetaData() {
    parseAndroidManifest();
    if (applicationMetaData == null) {
      applicationMetaData = new MetaData(Collections.<Node>emptyList());
    }
    return applicationMetaData.getValueMap();
  }

  public ResourcePath getResourcePath() {
    return new ResourcePath(getRClass(), resDirectory, assetsDirectory);
  }

  public List<ResourcePath> getIncludedResourcePaths() {
    Collection<ResourcePath> resourcePaths = new LinkedHashSet<>(); // Needs stable ordering and no duplicates
    resourcePaths.add(getResourcePath());
    for (AndroidManifest libraryManifest : getLibraryManifests()) {
      resourcePaths.addAll(libraryManifest.getIncludedResourcePaths());
    }
    return new ArrayList<>(resourcePaths);
  }

  public List<ContentProviderData> getContentProviders() {
    parseAndroidManifest();
    return providers;
  }

  public List<AndroidManifest> getLibraryManifests() {
    assert(libraryManifests != null);
    return Collections.unmodifiableList(libraryManifests);
  }

  /**
   * Returns all transitively reachable manifests, including this one, in order and without
   * duplicates.
   */
  public List<AndroidManifest> getAllManifests() {
    Set<AndroidManifest> seenManifests = new HashSet<>();
    List<AndroidManifest> uniqueManifests = new ArrayList<>();
    addTransitiveManifests(seenManifests, uniqueManifests);
    return uniqueManifests;
  }

  private void addTransitiveManifests(Set<AndroidManifest> unique, List<AndroidManifest> list) {
    if (unique.add(this)) {
      list.add(this);
      for (AndroidManifest androidManifest : getLibraryManifests()) {
        androidManifest.addTransitiveManifests(unique, list);
      }
    }
  }

  public FsFile getResDirectory() {
    return resDirectory;
  }

  public FsFile getAssetsDirectory() {
    return assetsDirectory;
  }

  public FsFile getAndroidManifestFile() {
    return androidManifestFile;
  }

  public List<BroadcastReceiverData> getBroadcastReceivers() {
    parseAndroidManifest();
    return receivers;
  }

  public List<ServiceData> getServices() {
    parseAndroidManifest();
    return new ArrayList<>(serviceDatas.values());
  }

  public ServiceData getServiceData(String serviceClassName) {
    parseAndroidManifest();
    return serviceDatas.get(serviceClassName);
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AndroidManifest that = (AndroidManifest) o;

    if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile)
        : that.androidManifestFile != null) {
      return false;
    }
    if (resDirectory != null ? !resDirectory.equals(that.resDirectory)
        : that.resDirectory != null) {
      return false;
    }
    if (assetsDirectory != null ? !assetsDirectory.equals(that.assetsDirectory)
        : that.assetsDirectory != null) {
      return false;
    }
    if (overridePackageName != null ? !overridePackageName.equals(that.overridePackageName)
        : that.overridePackageName != null) {
      return false;
    }
    if (libraryManifests != null ? !libraryManifests.equals(that.libraryManifests)
        : that.libraryManifests != null) {
      return false;
    }
    return apkFile != null ? apkFile.equals(that.apkFile) : that.apkFile == null;
  }

  @Override
  public int hashCode() {
    int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
    result = 31 * result + (resDirectory != null ? resDirectory.hashCode() : 0);
    result = 31 * result + (assetsDirectory != null ? assetsDirectory.hashCode() : 0);
    result = 31 * result + (overridePackageName != null ? overridePackageName.hashCode() : 0);
    result = 31 * result + (libraryManifests != null ? libraryManifests.hashCode() : 0);
    result = 31 * result + (apkFile != null ? apkFile.hashCode() : 0);
    return result;
  }

  public ActivityData getActivityData(String activityClassName) {
    parseAndroidManifest();
    return activityDatas.get(activityClassName);
  }

  public String getThemeRef() {
    return themeRef;
  }

  public Map<String, ActivityData> getActivityDatas() {
    parseAndroidManifest();
    return activityDatas;
  }

  public List<String> getUsedPermissions() {
    parseAndroidManifest();
    return usedPermissions;
  }

  public Map<String, PermissionItemData> getPermissions() {
    parseAndroidManifest();
    return permissions;
  }

  public Map<String, PermissionGroupItemData> getPermissionGroups() {
    parseAndroidManifest();
    return permissionGroups;
  }

  /**
   * Returns data for the broadcast receiver with the provided name from this manifest. If no
   * receiver with the class name can be found, returns null.
   *
   * @param className the fully resolved class name of the receiver
   * @return data for the receiver or null if it cannot be found
   */
  public @Nullable BroadcastReceiverData getBroadcastReceiver(String className) {
    parseAndroidManifest();
    for (BroadcastReceiverData receiver : receivers) {
      if (receiver.getName().equals(className)) {
        return receiver;
      }
    }
    return null;
  }

  public FsFile getApkFile() {
    return apkFile;
  }

  /** @deprecated Do not use. */
  @Deprecated
  public boolean supportsLegacyResourcesMode() {
    return true;
  }

  /** @deprecated Do not use. */
  @Deprecated
  synchronized public boolean supportsBinaryResourcesMode() {
    if (supportsBinaryResourcesMode == null) {
      supportsBinaryResourcesMode = apkFile != null && apkFile.exists();
    }
    return supportsBinaryResourcesMode;
  }
}
