package com.xtremelabs.robolectric.res;

import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.tester.android.content.pm.StubPackageManager;

import java.util.ArrayList;
import java.util.List;

public class RobolectricPackageManager extends StubPackageManager {
    public PackageInfo packageInfo;
    public ArrayList<PackageInfo> packageList;
    private ContextWrapper contextWrapper;
    private RobolectricConfig config;
    private ApplicationInfo applicationInfo;

    public RobolectricPackageManager(ContextWrapper contextWrapper, RobolectricConfig config) {
        this.contextWrapper = contextWrapper;
        this.config = config;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        ensurePackageInfo();
        return packageInfo;
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {

        if (config.getPackageName().equals(packageName)) {
            if (applicationInfo == null) {
                applicationInfo = new ApplicationInfo();
                applicationInfo.flags = config.getApplicationFlags();
                applicationInfo.targetSdkVersion = config.getSdkVersion();
                applicationInfo.packageName = config.getPackageName();
                applicationInfo.processName = config.getProcessName();
            }
            return applicationInfo;
        }

        throw new NameNotFoundException();
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        ensurePackageInfo();
        if (packageList == null) {
            packageList = new ArrayList<PackageInfo>();
            packageList.add(packageInfo);
        }
        return packageList;
    }

    private void ensurePackageInfo() {
        if (packageInfo == null) {
            packageInfo = new PackageInfo();
            packageInfo.packageName = contextWrapper.getPackageName();
            packageInfo.versionName = "1.0";
        }
    }
}
