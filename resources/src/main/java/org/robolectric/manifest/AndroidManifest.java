package org.robolectric.manifest;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
public class AndroidManifest {
  final FsFile androidManifestFile;
  final FsFile resDirectory;
  final FsFile assetsDirectory;
  final String overridePackageName;

  boolean manifestIsParsed;

  String applicationName;
  String applicationLabel;
  String rClassName;
  String packageName;
  String processName;
  String themeRef;
  String labelRef;
  Integer minSdkVersion;
  Integer targetSdkVersion;
  Integer maxSdkVersion;
  int versionCode;
  String versionName;
  final Map<String, PermissionItemData> permissions = new HashMap<>();
  final List<ContentProviderData> providers = new ArrayList<>();
  final List<BroadcastReceiverData> receivers = new ArrayList<>();
  final Map<String, ServiceData> serviceDatas = new LinkedHashMap<>();
  final Map<String, ActivityData> activityDatas = new LinkedHashMap<>();
  final List<String> usedPermissions = new ArrayList<>();
  final Map<String, String> applicationAttributes = new HashMap<>();
  MetaData applicationMetaData;
  List<AndroidManifest> libraryManifests = new ArrayList<>();
  AndroidManifestParser parser;

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

  public void setParser(AndroidManifestParser parser) {
    this.parser = parser;
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
      parser.parse(this);
    } else {
      rClassName = (packageName != null && !packageName.equals("")) ? packageName + ".R" : null;

      if (androidManifestFile != null) {
        System.err.println("No such manifest file: " + androidManifestFile);
      }
    }

    manifestIsParsed = true;
  }

  boolean packageNameIsOverridden() {
    return overridePackageName != null && !overridePackageName.isEmpty();
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
    parseAndroidManifest();
    return serviceDatas.get(serviceClassName);
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
      if (receiver.getClassName().equals(className)) {
        return receiver;
      }
    }
    return null;
  }
}
