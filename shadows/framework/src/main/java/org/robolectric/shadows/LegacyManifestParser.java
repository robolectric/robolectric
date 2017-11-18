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
import static java.util.Arrays.asList;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.manifest.ActivityData;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;

/**
 * Creates a {@link PackageInfo} from a {@link AndroidManifest}
 */
public class LegacyManifestParser {

  private static final List<Pair<String, Integer>> APPLICATION_FLAGS = asList(
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
      Pair.create("android:vmSafeMode", FLAG_VM_SAFE_MODE)
  );
  private static final List<Pair<String, Integer>> CONFIG_OPTIONS = asList(
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
      Pair.create("smallestScreenSize", ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE)
  );

  public static PackageInfo addManifest(AndroidManifest androidManifest,
      Map<Intent, List<ResolveInfo>> resolveInfoForIntent) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = androidManifest.getPackageName();
    packageInfo.versionName = androidManifest.getVersionName();
    packageInfo.versionCode = androidManifest.getVersionCode();

    Map<String,ActivityData> activityDatas = androidManifest.getActivityDatas();

    Map<String, PermissionItemData> permissionItemData = androidManifest.getPermissions();
    List<PermissionInfo> permissionInfos = new ArrayList<>();
    for (PermissionItemData itemData : permissionItemData.values()) {
      permissionInfos.add(createPermissionInfo(packageInfo.packageName, itemData));
    }
    packageInfo.permissions = permissionInfos.toArray(new PermissionInfo[permissionInfos.size()]);

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.flags = decodeFlags(androidManifest.getApplicationAttributes());
    applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
    applicationInfo.packageName = androidManifest.getPackageName();
    applicationInfo.processName = androidManifest.getProcessName();
    applicationInfo.name = androidManifest.getApplicationName();
    applicationInfo.metaData = metaDataToBundle(androidManifest.getApplicationMetaData());
    applicationInfo.uid = Process.myUid();
    if (androidManifest.getThemeRef() != null) {
      applicationInfo.theme = RuntimeEnvironment.getAppResourceTable().getResourceId(ResName.qualifyResName(androidManifest.getThemeRef().replace("@", ""), packageInfo.packageName, "style"));
    }
    // ShadowPackageManager.setUpPackageStorage(applicationInfo);

    int labelRes = 0;
    if (androidManifest.getLabelRef() != null) {
      String fullyQualifiedName = ResName
          .qualifyResName(androidManifest.getLabelRef(), androidManifest
              .getPackageName());
      Integer id = fullyQualifiedName == null ? null : RuntimeEnvironment.getAppResourceTable().getResourceId(new ResName(fullyQualifiedName));
      labelRes = id != null ? id : 0;
    }

    applicationInfo.labelRes = labelRes;
    String labelRef = androidManifest.getLabelRef();
    if (labelRef != null && !labelRef.startsWith("@")) {
      applicationInfo.nonLocalizedLabel = labelRef;
    }

    packageInfo.applicationInfo = applicationInfo;

    List<ActivityInfo> activityInfos = new ArrayList<>();
    for (ActivityData data : activityDatas.values()) {
      String name = data.getName();
      String activityName = name.startsWith(".") ? androidManifest.getPackageName() + name : name;

      ActivityInfo activityInfo = new ActivityInfo();
      activityInfo.name = activityName;
      activityInfo.packageName = packageInfo.packageName;
      activityInfo.configChanges = getConfigChanges(data);
      activityInfo.parentActivityName = data.getParentActivityName();
      activityInfo.metaData = metaDataToBundle(data.getMetaData().getValueMap());
      activityInfo.applicationInfo = packageInfo.applicationInfo;
      String themeRef;

      // Based on ShadowActivity
      if (data.getThemeRef() != null) {
        themeRef = data.getThemeRef();
      } else {
        themeRef = androidManifest.getThemeRef();
      }
      if (themeRef != null) {
        activityInfo.theme = RuntimeEnvironment.getAppResourceTable().getResourceId(ResName.qualifyResName(themeRef.replace("@", ""), packageInfo.packageName, "style"));
      }

      if (data.getLabel() != null) {
        activityInfo.labelRes = RuntimeEnvironment.getAppResourceTable().getResourceId(ResName.qualifyResName(data.getLabel().replace("@", ""), packageInfo.packageName, "string"));
      }

      activityInfos.add(activityInfo);

      Intent intent = new Intent(activityName);
      List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
      if (infoList1 == null) {
        infoList1 = new ArrayList<>();
        resolveInfoForIntent.put(intent, infoList1);
      }
      List<ResolveInfo> infoList = infoList1;
      ResolveInfo activityResolveInfo = new ResolveInfo();
      activityResolveInfo.activityInfo = activityInfo;
      infoList.add(activityResolveInfo);

    }
    packageInfo.activities = activityInfos.toArray(new ActivityInfo[activityDatas.size()]);

    ContentProviderData[] cpdata = androidManifest.getContentProviders().toArray(new ContentProviderData[]{});
    if (cpdata.length == 0) {
      packageInfo.providers = null;
    } else {
      packageInfo.providers = new ProviderInfo[cpdata.length];
      for (int i = 0; i < cpdata.length; i++) {
        ProviderInfo info = new ProviderInfo();
        info.authority = cpdata[i].getAuthorities(); // todo: support multiple authorities
        info.name = cpdata[i].getClassName();
        info.packageName = androidManifest.getPackageName();
        info.metaData = metaDataToBundle(cpdata[i].getMetaData().getValueMap());
        packageInfo.providers[i] = info;
      }
    }

    // Populate information related to BroadcastReceivers. Broadcast receivers can be queried in two
    // possible ways,
    // 1. PackageManager#getPackageInfo(...),
    // 2. PackageManager#queryBroadcastReceivers(...)
    // The following piece of code will let you enable querying receivers through both the methods.
    List<ActivityInfo> receiverActivityInfos = new ArrayList<>();
    for (int i = 0; i < androidManifest.getBroadcastReceivers().size(); ++i) {
      ActivityInfo activityInfo = new ActivityInfo();
      activityInfo.name = androidManifest.getBroadcastReceivers().get(i).getClassName();
      activityInfo.permission = androidManifest.getBroadcastReceivers().get(i).getPermission();
      receiverActivityInfos.add(activityInfo);

      ResolveInfo resolveInfo = new ResolveInfo();
      resolveInfo.activityInfo = activityInfo;
      IntentFilter filter = new IntentFilter();
      for (String action : androidManifest.getBroadcastReceivers().get(i).getActions()) {
        filter.addAction(action);
      }
      resolveInfo.filter = filter;

      for (String action : androidManifest.getBroadcastReceivers().get(i).getActions()) {
        Intent intent = new Intent(action);
        intent.setPackage(androidManifest.getPackageName());
        List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
        if (infoList1 == null) {
          infoList1 = new ArrayList<>();
          resolveInfoForIntent.put(intent, infoList1);
        }
        List<ResolveInfo> infoList = infoList1;
        infoList.add(resolveInfo);
      }
    }
    packageInfo.receivers = receiverActivityInfos.toArray(new ActivityInfo[0]);

    String[] usedPermissions = androidManifest.getUsedPermissions().toArray(new String[]{});
    if (usedPermissions.length == 0) {
      packageInfo.requestedPermissions = null;
    } else {
      packageInfo.requestedPermissions = usedPermissions;
    }

    return packageInfo;
  }

  private static int getConfigChanges(ActivityData activityData) {
    String s = activityData.getConfigChanges();

    int res = 0;

    //quick sanity check.
    if (s == null || "".equals(s)) {
      return res;
    }

    String[] pieces = s.split("\\|");

    for(String s1 : pieces) {
      s1 = s1.trim();

      for (Pair<String, Integer> pair : CONFIG_OPTIONS) {
        if (s1.equals(pair.first)) {
          res |= pair.second;
          break;
        }
      }
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

  private static PermissionInfo createPermissionInfo(String packageName,
      PermissionItemData permissionItemData) {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.packageName = packageName;
    permissionInfo.name = permissionItemData.getName();
    permissionInfo.group = permissionItemData.getPermissionGroup();
    permissionInfo.protectionLevel = decodeProtectionLevel(permissionItemData.getProtectionLevel());

    String descriptionRef = permissionItemData.getDescription();
    if (descriptionRef != null) {
      ResName descResName = AttributeResource
          .getResourceReference(descriptionRef, packageName, "string");
      permissionInfo.descriptionRes = RuntimeEnvironment.getAppResourceTable().getResourceId(descResName);
    }

    String labelRefOrString = permissionItemData.getLabel();
    if (labelRefOrString != null) {
      if (AttributeResource.isResourceReference(labelRefOrString)) {
        ResName labelResName = AttributeResource.getResourceReference(labelRefOrString, packageName, "string");
        permissionInfo.labelRes = RuntimeEnvironment.getAppResourceTable().getResourceId(labelResName);
      } else {
        permissionInfo.nonLocalizedLabel = labelRefOrString;
      }
    }

    permissionInfo.metaData = metaDataToBundle(permissionItemData.getMetaData().getValueMap());
    return permissionInfo;
  }

  private static int decodeProtectionLevel(String protectionLevel) {
    if (protectionLevel == null) {
      return PermissionInfo.PROTECTION_NORMAL;
    }

    switch (protectionLevel) {
      case "normal":
        return PermissionInfo.PROTECTION_NORMAL;
      case "dangerous":
        return PermissionInfo.PROTECTION_DANGEROUS;
      case "signature":
        return PermissionInfo.PROTECTION_SIGNATURE;
      case "signatureOrSystem":
        return PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM;
      default:
        throw new IllegalArgumentException("unknown protection level " + protectionLevel);
    }
  }

  /**
   * Goes through the meta data and puts each value in to a
   * bundle as the correct type.
   *
   * Note that this will convert resource identifiers specified
   * via the value attribute as well.
   * @param meta Meta data to put in to a bundle
   * @return bundle containing the meta data
   */
  private static Bundle metaDataToBundle(Map<String, Object> meta) {
    if (meta.size() == 0) {
      return null;
    }

    Bundle bundle = new Bundle();

    for (Map.Entry<String,Object> entry : meta.entrySet()) {
      if (Boolean.class.isInstance(entry.getValue())) {
        bundle.putBoolean(entry.getKey(), (Boolean) entry.getValue());
      } else if (Float.class.isInstance(entry.getValue())) {
        bundle.putFloat(entry.getKey(), (Float) entry.getValue());
      } else if (Integer.class.isInstance(entry.getValue())) {
        bundle.putInt(entry.getKey(), (Integer) entry.getValue());
      } else {
        bundle.putString(entry.getKey(), entry.getValue().toString());
      }
    }
    return bundle;
  }
}
