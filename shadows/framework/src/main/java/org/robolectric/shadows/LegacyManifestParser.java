package org.robolectric.shadows;

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
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.PatternMatcher.PATTERN_LITERAL;
import static android.os.PatternMatcher.PATTERN_PREFIX;
import static android.os.PatternMatcher.PATTERN_SIMPLE_GLOB;
import static java.util.Arrays.asList;

import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.ServiceIntentInfo;
import android.content.pm.PathPermission;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Process;
import android.util.Pair;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.manifest.ActivityData;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.manifest.IntentFilterData;
import org.robolectric.manifest.IntentFilterData.DataAuthority;
import org.robolectric.manifest.PackageItemData;
import org.robolectric.manifest.PathPermissionData;
import org.robolectric.manifest.PermissionGroupItemData;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.manifest.ServiceData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.util.ReflectionHelpers;

/** Creates a {@link PackageInfo} from a {@link AndroidManifest} */
public class LegacyManifestParser {

  private static final List<Pair<String, Integer>> APPLICATION_FLAGS =
      asList(
          Pair.create("android:allowBackup", FLAG_ALLOW_BACKUP),
          Pair.create("android:allowClearUserData", FLAG_ALLOW_CLEAR_USER_DATA),
          Pair.create("android:allowTaskReparenting", FLAG_ALLOW_TASK_REPARENTING),
          Pair.create("android:debuggable", FLAG_DEBUGGABLE),
          Pair.create("android:hasCode", FLAG_HAS_CODE),
          Pair.create("android:killAfterRestore", FLAG_KILL_AFTER_RESTORE),
          Pair.create("android:persistent", FLAG_PERSISTENT),
          Pair.create("android:resizeable", FLAG_RESIZEABLE_FOR_SCREENS),
          Pair.create("android:restoreAnyVersion", FLAG_RESTORE_ANY_VERSION),
          Pair.create("android:largeScreens", FLAG_SUPPORTS_LARGE_SCREENS),
          Pair.create("android:normalScreens", FLAG_SUPPORTS_NORMAL_SCREENS),
          Pair.create("android:anyDensity", FLAG_SUPPORTS_SCREEN_DENSITIES),
          Pair.create("android:smallScreens", FLAG_SUPPORTS_SMALL_SCREENS),
          Pair.create("android:testOnly", FLAG_TEST_ONLY),
          Pair.create("android:vmSafeMode", FLAG_VM_SAFE_MODE));
  private static final List<Pair<String, Integer>> CONFIG_OPTIONS =
      asList(
          Pair.create("mcc", ActivityInfo.CONFIG_MCC),
          Pair.create("mnc", ActivityInfo.CONFIG_MNC),
          Pair.create("locale", ActivityInfo.CONFIG_LOCALE),
          Pair.create("touchscreen", ActivityInfo.CONFIG_TOUCHSCREEN),
          Pair.create("keyboard", ActivityInfo.CONFIG_KEYBOARD),
          Pair.create("keyboardHidden", ActivityInfo.CONFIG_KEYBOARD_HIDDEN),
          Pair.create("navigation", ActivityInfo.CONFIG_NAVIGATION),
          Pair.create("screenLayout", ActivityInfo.CONFIG_SCREEN_LAYOUT),
          Pair.create("fontScale", ActivityInfo.CONFIG_FONT_SCALE),
          Pair.create("uiMode", ActivityInfo.CONFIG_UI_MODE),
          Pair.create("orientation", ActivityInfo.CONFIG_ORIENTATION),
          Pair.create("screenSize", ActivityInfo.CONFIG_SCREEN_SIZE),
          Pair.create("smallestScreenSize", ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE));

  public static Package createPackage(AndroidManifest androidManifest) {

    Package pkg = new Package(androidManifest.getPackageName());

    pkg.mVersionName = androidManifest.getVersionName();
    pkg.mVersionCode = androidManifest.getVersionCode();

    Map<String, PermissionItemData> permissionItemData = androidManifest.getPermissions();
    for (PermissionItemData itemData : permissionItemData.values()) {
      Permission permission = new Permission(pkg, createPermissionInfo(pkg, itemData));
      permission.metaData = permission.info.metaData;
      pkg.permissions.add(permission);
    }

    Map<String, PermissionGroupItemData> permissionGroupItemData =
        androidManifest.getPermissionGroups();
    for (PermissionGroupItemData itemData : permissionGroupItemData.values()) {
      PermissionGroup permissionGroup =
          new PermissionGroup(pkg, createPermissionGroupInfo(pkg, itemData));
      permissionGroup.metaData = permissionGroup.info.metaData;
      pkg.permissionGroups.add(permissionGroup);
    }

    pkg.requestedPermissions.addAll(androidManifest.getUsedPermissions());
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.M) {
      List<Boolean> permissionsRequired =
          ReflectionHelpers.getField(pkg, "requestedPermissionsRequired");
      permissionsRequired.addAll(buildBooleanList(pkg.requestedPermissions.size(), true));
    }

    pkg.applicationInfo.flags = decodeFlags(androidManifest.getApplicationAttributes());
    pkg.applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
    pkg.applicationInfo.packageName = androidManifest.getPackageName();
    pkg.applicationInfo.processName = androidManifest.getProcessName();
    if (!Strings.isNullOrEmpty(androidManifest.getApplicationName())) {
      pkg.applicationInfo.className =
          buildClassName(pkg.applicationInfo.packageName, androidManifest.getApplicationName());
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.N_MR1) {
        pkg.applicationInfo.name = pkg.applicationInfo.className;
      }
    }
    pkg.applicationInfo.metaData = metaDataToBundle(androidManifest.getApplicationMetaData());
    pkg.applicationInfo.uid = Process.myUid();
    if (androidManifest.getThemeRef() != null) {
      pkg.applicationInfo.theme =
          RuntimeEnvironment.getAppResourceTable()
              .getResourceId(
                  ResName.qualifyResName(
                      androidManifest.getThemeRef().replace("@", ""), pkg.packageName, "style"));
    }

    int labelRes = 0;
    if (androidManifest.getLabelRef() != null) {
      String fullyQualifiedName =
          ResName.qualifyResName(androidManifest.getLabelRef(), androidManifest.getPackageName());
      Integer id =
          fullyQualifiedName == null
              ? null
              : RuntimeEnvironment.getAppResourceTable()
                  .getResourceId(new ResName(fullyQualifiedName));
      labelRes = id != null ? id : 0;
    }

    pkg.applicationInfo.labelRes = labelRes;
    String labelRef = androidManifest.getLabelRef();
    if (labelRef != null && !labelRef.startsWith("@")) {
      pkg.applicationInfo.nonLocalizedLabel = labelRef;
    }

    Map<String, ActivityData> activityDatas = androidManifest.getActivityDatas();
    for (ActivityData data : activityDatas.values()) {
      ActivityInfo activityInfo = new ActivityInfo();
      activityInfo.name = buildClassName(pkg.packageName, data.getName());
      activityInfo.packageName = pkg.packageName;
      activityInfo.configChanges = getConfigChanges(data);
      activityInfo.parentActivityName = data.getParentActivityName();
      activityInfo.metaData = metaDataToBundle(data.getMetaData().getValueMap());
      activityInfo.applicationInfo = pkg.applicationInfo;
      activityInfo.targetActivity = data.getTargetActivityName();
      activityInfo.exported = data.isExported();
      activityInfo.permission = data.getPermission();
      activityInfo.enabled = data.isEnabled();
      String themeRef;

      // Based on ShadowActivity
      if (data.getThemeRef() != null) {
        themeRef = data.getThemeRef();
      } else {
        themeRef = androidManifest.getThemeRef();
      }
      if (themeRef != null) {
        activityInfo.theme =
            RuntimeEnvironment.getAppResourceTable()
                .getResourceId(
                    ResName.qualifyResName(themeRef.replace("@", ""), pkg.packageName, "style"));
      }

      if (data.getLabel() != null) {
        activityInfo.labelRes =
            RuntimeEnvironment.getAppResourceTable()
                .getResourceId(
                    ResName.qualifyResName(
                        data.getLabel().replace("@", ""), pkg.packageName, "string"));
        if (activityInfo.labelRes == 0) {
          activityInfo.nonLocalizedLabel = data.getLabel();
        }
      }

      Activity activity = createActivity(pkg, activityInfo);
      for (IntentFilterData intentFilterData : data.getIntentFilters()) {
        ActivityIntentInfo outInfo = new ActivityIntentInfo(activity);
        populateIntentInfo(intentFilterData, outInfo);
        activity.intents.add(outInfo);
      }
      pkg.activities.add(activity);
    }

    for (ContentProviderData data : androidManifest.getContentProviders()) {
      ProviderInfo info = new ProviderInfo();
      populateComponentInfo(info, pkg, data);
      info.authority = data.getAuthorities();

      List<PathPermission> permissions = new ArrayList<>();
      for (PathPermissionData permissionData : data.getPathPermissionDatas()) {
        permissions.add(createPathPermission(permissionData));
      }
      info.pathPermissions = permissions.toArray(new PathPermission[permissions.size()]);
      info.readPermission = data.getReadPermission();
      info.writePermission = data.getWritePermission();
      info.grantUriPermissions = data.getGrantUriPermissions();
      info.enabled = data.isEnabled();
      pkg.providers.add(createProvider(pkg, info));
    }

    for (BroadcastReceiverData data : androidManifest.getBroadcastReceivers()) {
      ActivityInfo info = new ActivityInfo();
      populateComponentInfo(info, pkg, data);
      info.permission = data.getPermission();
      info.exported = data.isExported();
      info.enabled = data.isEnabled();
      Activity receiver = createActivity(pkg, info);
      for (IntentFilterData intentFilterData : data.getIntentFilters()) {
        ActivityIntentInfo outInfo = new ActivityIntentInfo(receiver);
        populateIntentInfo(intentFilterData, outInfo);
        receiver.intents.add(outInfo);
      }
      pkg.receivers.add(receiver);
    }

    for (ServiceData data : androidManifest.getServices()) {
      ServiceInfo info = new ServiceInfo();
      populateComponentInfo(info, pkg, data);
      info.permission = data.getPermission();
      info.exported = data.isExported();
      info.enabled = data.isEnabled();

      Service service = createService(pkg, info);
      for (IntentFilterData intentFilterData : data.getIntentFilters()) {
        ServiceIntentInfo outInfo = new ServiceIntentInfo(service);
        populateIntentInfo(intentFilterData, outInfo);
        service.intents.add(outInfo);
      }
      pkg.services.add(service);
    }

    String codePath = RuntimeEnvironment.getTempDirectory()
        .createIfNotExists(pkg.packageName + "-codePath")
        .toAbsolutePath()
        .toString();
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      pkg.codePath = codePath;
    } else {
      ReflectionHelpers.setField(Package.class, pkg, "mPath", codePath);
    }

    return pkg;
  }

  private static PathPermission createPathPermission(PathPermissionData data) {
    if (!Strings.isNullOrEmpty(data.pathPattern)) {
      return new PathPermission(
          data.pathPattern, PATTERN_SIMPLE_GLOB, data.readPermission, data.writePermission);
    } else if (!Strings.isNullOrEmpty(data.path)) {
      return new PathPermission(
          data.path, PATTERN_LITERAL, data.readPermission, data.writePermission);
    } else if (!Strings.isNullOrEmpty(data.pathPrefix)) {
      return new PathPermission(
          data.pathPrefix, PATTERN_PREFIX, data.readPermission, data.writePermission);
    } else {
      throw new IllegalStateException("Permission without type");
    }
  }

  private static void populateComponentInfo(
      ComponentInfo outInfo, Package owner, PackageItemData itemData) {
    populatePackageItemInfo(outInfo, owner, itemData);
    outInfo.applicationInfo = owner.applicationInfo;
  }

  private static void populatePackageItemInfo(
      PackageItemInfo outInfo, Package owner, PackageItemData itemData) {
    outInfo.name = buildClassName(owner.packageName, itemData.getName());
    outInfo.packageName = owner.packageName;
    outInfo.metaData = metaDataToBundle(itemData.getMetaData().getValueMap());
  }

  private static List<Boolean> buildBooleanList(int size, boolean defaultVal) {
    Boolean[] barray = new Boolean[size];
    Arrays.fill(barray, defaultVal);
    return Arrays.asList(barray);
  }

  private static PackageParser.Provider createProvider(Package pkg, ProviderInfo info) {
    PackageParser.Provider provider =
        ReflectionHelpers.callConstructor(PackageParser.Provider.class);
    populateComponent(pkg, info, provider);
    return provider;
  }

  private static Activity createActivity(Package pkg, ActivityInfo activityInfo) {
    Activity activity = ReflectionHelpers.callConstructor(Activity.class);
    populateComponent(pkg, activityInfo, activity);
    return activity;
  }

  private static Service createService(Package pkg, ServiceInfo info) {
    PackageParser.Service service = ReflectionHelpers.callConstructor(PackageParser.Service.class);
    populateComponent(pkg, info, service);
    return service;
  }

  private static void populateComponent(
      Package pkg, ComponentInfo info, PackageParser.Component component) {
    ReflectionHelpers.setField(component, "info", info);
    ReflectionHelpers.setField(component, "intents", new ArrayList<>());
    ReflectionHelpers.setField(component, "owner", pkg);
    ReflectionHelpers.setField(component, "className", info.name);
  }

  private static void populateIntentInfo(IntentFilterData intentFilterData, IntentInfo outInfo) {
    for (String action : intentFilterData.getActions()) {
      outInfo.addAction(action);
    }
    for (String category : intentFilterData.getCategories()) {
      outInfo.addCategory(category);
    }
    for (DataAuthority dataAuthority : intentFilterData.getAuthorities()) {
      outInfo.addDataAuthority(dataAuthority.getHost(), dataAuthority.getPort());
    }
    for (String mimeType : intentFilterData.getMimeTypes()) {
      try {
        outInfo.addDataType(mimeType);
      } catch (MalformedMimeTypeException e) {
        throw new RuntimeException(e);
      }
    }
    for (String scheme : intentFilterData.getSchemes()) {
      outInfo.addDataScheme(scheme);
    }
    for (String pathPattern : intentFilterData.getPathPatterns()) {
      outInfo.addDataPath(pathPattern, PATTERN_SIMPLE_GLOB);
    }
    for (String pathPattern : intentFilterData.getPathPrefixes()) {
      outInfo.addDataPath(pathPattern, PATTERN_PREFIX);
    }
    for (String pathPattern : intentFilterData.getPaths()) {
      outInfo.addDataPath(pathPattern, PATTERN_LITERAL);
    }
  }

  private static int getConfigChanges(ActivityData activityData) {
    String s = activityData.getConfigChanges();

    int res = 0;

    // quick sanity check.
    if (s == null || "".equals(s)) {
      return res;
    }

    String[] pieces = s.split("\\|", 0);

    for (String s1 : pieces) {
      s1 = s1.trim();

      for (Pair<String, Integer> pair : CONFIG_OPTIONS) {
        if (s1.equals(pair.first)) {
          res |= pair.second;
          break;
        }
      }
    }

    // Matches platform behavior
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
      res |= ActivityInfo.CONFIG_MNC;
      res |= ActivityInfo.CONFIG_MCC;
    }

    return res;
  }

  private static int decodeFlags(Map<String, String> applicationAttributes) {
    int applicationFlags = 0;
    for (Pair<String, Integer> pair : APPLICATION_FLAGS) {
      if ("true".equals(applicationAttributes.get(pair.first))) {
        applicationFlags |= pair.second;
      }
    }
    return applicationFlags;
  }

  private static PermissionInfo createPermissionInfo(Package owner, PermissionItemData itemData) {
    PermissionInfo permissionInfo = new PermissionInfo();
    populatePackageItemInfo(permissionInfo, owner, itemData);

    permissionInfo.group = itemData.getPermissionGroup();
    permissionInfo.protectionLevel = decodeProtectionLevel(itemData.getProtectionLevel());
    permissionInfo.metaData = metaDataToBundle(itemData.getMetaData().getValueMap());

    String descriptionRef = itemData.getDescription();
    if (descriptionRef != null) {
      ResName descResName =
          AttributeResource.getResourceReference(descriptionRef, owner.packageName, "string");
      permissionInfo.descriptionRes =
          RuntimeEnvironment.getAppResourceTable().getResourceId(descResName);
    }

    String labelRefOrString = itemData.getLabel();
    if (labelRefOrString != null) {
      if (AttributeResource.isResourceReference(labelRefOrString)) {
        ResName labelResName =
            AttributeResource.getResourceReference(labelRefOrString, owner.packageName, "string");
        permissionInfo.labelRes =
            RuntimeEnvironment.getAppResourceTable().getResourceId(labelResName);
      } else {
        permissionInfo.nonLocalizedLabel = labelRefOrString;
      }
    }

    return permissionInfo;
  }

  private static PermissionGroupInfo createPermissionGroupInfo(Package owner,
      PermissionGroupItemData itemData) {
    PermissionGroupInfo permissionGroupInfo = new PermissionGroupInfo();
    populatePackageItemInfo(permissionGroupInfo, owner, itemData);

    permissionGroupInfo.metaData = metaDataToBundle(itemData.getMetaData().getValueMap());

    String descriptionRef = itemData.getDescription();
    if (descriptionRef != null) {
      ResName descResName =
          AttributeResource.getResourceReference(descriptionRef, owner.packageName, "string");
      permissionGroupInfo.descriptionRes =
          RuntimeEnvironment.getAppResourceTable().getResourceId(descResName);
    }

    String labelRefOrString = itemData.getLabel();
    if (labelRefOrString != null) {
      if (AttributeResource.isResourceReference(labelRefOrString)) {
        ResName labelResName =
            AttributeResource.getResourceReference(labelRefOrString, owner.packageName, "string");
        permissionGroupInfo.labelRes =
            RuntimeEnvironment.getAppResourceTable().getResourceId(labelResName);
      } else {
        permissionGroupInfo.nonLocalizedLabel = labelRefOrString;
      }
    }

    return permissionGroupInfo;
  }

  private static int decodeProtectionLevel(String protectionLevel) {
    if (protectionLevel == null) {
      return PermissionInfo.PROTECTION_NORMAL;
    }

    int permissions = PermissionInfo.PROTECTION_NORMAL;
    String[] levels = protectionLevel.split("\\|", 0);

    for (String level : levels) {
      switch (level) {
        case "normal":
          permissions |= PermissionInfo.PROTECTION_NORMAL;
          break;
        case "dangerous":
          permissions |= PermissionInfo.PROTECTION_DANGEROUS;
          break;
        case "signature":
          permissions |= PermissionInfo.PROTECTION_SIGNATURE;
          break;
        case "signatureOrSystem":
          permissions |= PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM;
          break;
        case "privileged":
          permissions |= PermissionInfo.PROTECTION_FLAG_PRIVILEGED;
          break;
        case "system":
          permissions |= PermissionInfo.PROTECTION_FLAG_SYSTEM;
          break;
        case "development":
          permissions |= PermissionInfo.PROTECTION_FLAG_DEVELOPMENT;
          break;
        case "appop":
          permissions |= PermissionInfo.PROTECTION_FLAG_APPOP;
          break;
        case "pre23":
          permissions |= PermissionInfo.PROTECTION_FLAG_PRE23;
          break;
        case "installer":
          permissions |= PermissionInfo.PROTECTION_FLAG_INSTALLER;
          break;
        case "verifier":
          permissions |= PermissionInfo.PROTECTION_FLAG_VERIFIER;
          break;
        case "preinstalled":
          permissions |= PermissionInfo.PROTECTION_FLAG_PREINSTALLED;
          break;
        case "setup":
          permissions |= PermissionInfo.PROTECTION_FLAG_SETUP;
          break;
        case "instant":
          permissions |= PermissionInfo.PROTECTION_FLAG_INSTANT;
          break;
        case "runtime":
          permissions |= PermissionInfo.PROTECTION_FLAG_RUNTIME_ONLY;
          break;
        case "oem":
          permissions |= PermissionInfo.PROTECTION_FLAG_OEM;
          break;
        case "vendorPrivileged":
          permissions |= PermissionInfo.PROTECTION_FLAG_VENDOR_PRIVILEGED;
          break;
        case "textClassifier":
          permissions |= PermissionInfo.PROTECTION_FLAG_SYSTEM_TEXT_CLASSIFIER;
          break;
        default:
          throw new IllegalArgumentException("unknown protection level " + protectionLevel);
      }
    }
    return permissions;
  }

  /**
   * Goes through the meta data and puts each value in to a bundle as the correct type.
   *
   * <p>Note that this will convert resource identifiers specified via the value attribute as well.
   *
   * @param meta Meta data to put in to a bundle
   * @return bundle containing the meta data
   */
  private static Bundle metaDataToBundle(Map<String, Object> meta) {
    if (meta.size() == 0) {
      return null;
    }

    Bundle bundle = new Bundle();

    for (Map.Entry<String, Object> entry : meta.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (Boolean.class.isInstance(value)) {
        bundle.putBoolean(key, (Boolean) value);
      } else if (Float.class.isInstance(value)) {
        bundle.putFloat(key, (Float) value);
      } else if (Integer.class.isInstance(value)) {
        bundle.putInt(key, (Integer) value);
      } else {
        bundle.putString(key, value == null ? null : value.toString());
      }
    }
    return bundle;
  }

  private static String buildClassName(String pkg, String cls) {
    if (Strings.isNullOrEmpty(cls)) {
      throw new IllegalArgumentException("Empty class name in package " + pkg);
    }
    char c = cls.charAt(0);
    if (c == '.') {
      return (pkg + cls).intern();
    }
    if (cls.indexOf('.') < 0) {
      StringBuilder b = new StringBuilder(pkg);
      b.append('.');
      b.append(cls);
      return b.toString();
    }
    return cls;
    // TODO: consider reenabling this for stricter platform-complaint checking
    // if (c >= 'a' && c <= 'z') {
    // return cls;
    // }
    // throw new IllegalArgumentException("Bad class name " + cls + " in package " + pkg);
  }
}
