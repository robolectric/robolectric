package org.robolectric.manifest;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for an Android App Manifest, which represents information about one's App to an Android system.
 * @see <a href="https://developer.android.com/guide/topics/manifest/manifest-intro.html">Android App Manifest</a>
 */
public class AndroidManifest {
  private final FsFile androidManifestFile;
  private final FsFile resDirectory;
  private final FsFile assetsDirectory;
  private final String overridePackageName;

  private boolean manifestIsParsed;

  private Node applicationNode;
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
  private final List<ContentProviderData> providers = new ArrayList<>();
  private final List<BroadcastReceiverData> receivers = new ArrayList<>();
  private final Map<String, ServiceData> serviceDatas = new LinkedHashMap<>();
  private final Map<String, ActivityData> activityDatas = new LinkedHashMap<>();
  private final List<String> usedPermissions = new ArrayList<>();
  private final Map<String, String> applicationAttributes = new HashMap<>();
  private MetaData applicationMetaData;
  private List<AndroidManifest> libraryManifests = new ArrayList<>();

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
  public AndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory, String overridePackageName) {
    this.androidManifestFile = androidManifestFile;
    this.resDirectory = resDirectory;
    this.assetsDirectory = assetsDirectory;
    this.overridePackageName = overridePackageName;

    this.packageName = overridePackageName;
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

        applicationNode = findApplicationNode(manifestDocument);
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

          parseReceivers();
          parseServices();
          parseActivities();
          parseApplicationMetaData();
          parseContentProviders();
        }

        minSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");
        targetSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:targetSdkVersion");
        maxSdkVersion = getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:maxSdkVersion");
        if (processName == null) {
          processName = packageName;
        }

        parseUsedPermissions(manifestDocument);
        parsePermissions(manifestDocument);
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
    } else {
      rClassName = (packageName != null && !packageName.equals("")) ? packageName + ".R" : null;

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
      permissions.put(name,
          new PermissionItemData(
              name,
              getAttributeValue(permissionNode, "android:label"),
              getAttributeValue(permissionNode, "android:description"),
              getAttributeValue(permissionNode, "android:permissionGroup"),
              getAttributeValue(permissionNode, "android:protectionLevel"),
              metaData
          ));
    }
  }

  private void parseContentProviders() {
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

      providers.add(new ContentProviderData(resolveClassRef(name),
              metaData,
              authorities,
              getAttributeValue(contentProviderNode, "android:readPermission"),
              getAttributeValue(contentProviderNode, "android:writePermission"),
              pathPermissionDatas));
    }
  }

  private @Nullable String getAttributeValue(Node parentNode, String attributeName) {
    Node attributeNode = parentNode.getAttributes().getNamedItem(attributeName);
    return attributeNode == null ? null : attributeNode.getTextContent();
  }

  private void parseReceivers() {
    for (Node receiverNode : getChildrenTags(applicationNode, "receiver")) {
      Node namedItem = receiverNode.getAttributes().getNamedItem("android:name");
      if (namedItem == null) continue;

      String receiverName = resolveClassRef(namedItem.getTextContent());
      MetaData metaData = new MetaData(getChildrenTags(receiverNode, "meta-data"));

      BroadcastReceiverData receiver = new BroadcastReceiverData(receiverName, metaData);
      List<Node> intentFilters = getChildrenTags(receiverNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            receiver.addAction(nameNode.getTextContent());
          }
        }
      }
      
      Node permissionItem = receiverNode.getAttributes().getNamedItem("android:permission");
      if (permissionItem != null) {
        receiver.setPermission(permissionItem.getTextContent());
      }
      
      receivers.add(receiver);
    }
  }

  private void parseServices() {
    for (Node serviceNode : getChildrenTags(applicationNode, "service")) {
      Node namedItem = serviceNode.getAttributes().getNamedItem("android:name");
      if (namedItem == null) continue;

      String serviceName = resolveClassRef(namedItem.getTextContent());
      MetaData metaData = new MetaData(getChildrenTags(serviceNode, "meta-data"));

      ServiceData service = new ServiceData(serviceName, metaData);
      List<Node> intentFilters = getChildrenTags(serviceNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            service.addAction(nameNode.getTextContent());
          }
        }
      }
      
      Node permissionItem = serviceNode.getAttributes().getNamedItem("android:permission");
      if (permissionItem != null) {
        service.setPermission(permissionItem.getTextContent());
      }
      serviceDatas.put(serviceName, service);
    }
  }

  private void parseActivities() {
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
    final NamedNodeMap attributes = activityNode.getAttributes();
    final int attrCount = attributes.getLength();
    final List<IntentFilterData> intentFilterData = parseIntentFilters(activityNode);
    final MetaData metaData = new MetaData(getChildrenTags(activityNode, "meta-data"));
    final HashMap<String, String> activityAttrs = new HashMap<>(attrCount);
    for(int i = 0; i < attrCount; i++) {
      Node attr = attributes.item(i);
      String v = attr.getNodeValue();
      if( v != null) {
        activityAttrs.put(attr.getNodeName(), v);
      }
    }

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
   * Allows RobolectricPackageManager to provide
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
  }

  private void parseApplicationMetaData() {
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

  @Deprecated
  public void setPackageName(String packageName) {
    this.packageName = packageName;
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

  public int getMinSdkVersion() {
    parseAndroidManifest();
    return minSdkVersion == null ? 1 : minSdkVersion;
  }

  public int getTargetSdkVersion() {
    parseAndroidManifest();
    return targetSdkVersion == null ? getMinSdkVersion() : targetSdkVersion;
  }

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

  public void setLibraryManifests(List<AndroidManifest> libraryManifests) {
    Preconditions.checkNotNull(libraryManifests);
    this.libraryManifests = libraryManifests;
  }

  public List<AndroidManifest> getLibraryManifests() {
    assert(libraryManifests != null);
    return Collections.unmodifiableList(libraryManifests);
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AndroidManifest that = (AndroidManifest) o;

    if (androidManifestFile != null ? !androidManifestFile.equals(that.androidManifestFile) : that.androidManifestFile != null)
      return false;
    if (assetsDirectory != null ? !assetsDirectory.equals(that.assetsDirectory) : that.assetsDirectory != null)
      return false;
    if (resDirectory != null ? !resDirectory.equals(that.resDirectory) : that.resDirectory != null) return false;
    if (overridePackageName != null ? !overridePackageName.equals(that.overridePackageName) : that.overridePackageName != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = androidManifestFile != null ? androidManifestFile.hashCode() : 0;
    result = 31 * result + (resDirectory != null ? resDirectory.hashCode() : 0);
    result = 31 * result + (assetsDirectory != null ? assetsDirectory.hashCode() : 0);
    result = 31 * result + (overridePackageName != null ? overridePackageName.hashCode() : 0);
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
}
