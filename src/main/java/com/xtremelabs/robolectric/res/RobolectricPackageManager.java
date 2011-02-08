package com.xtremelabs.robolectric.res;

import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import com.xtremelabs.robolectric.tester.android.content.pm.StubPackageManager;

import java.util.ArrayList;
import java.util.List;

public class RobolectricPackageManager extends StubPackageManager {
    public PackageInfo packageInfo;
    public ArrayList<PackageInfo> packageList;
    private ContextWrapper contextWrapper;

    public RobolectricPackageManager(ContextWrapper contextWrapper) {
        this.contextWrapper = contextWrapper;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        ensurePackageInfo();
        return packageInfo;
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
