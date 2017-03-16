package org.robolectric.res;

public interface PackageResourceTable extends ResourceTable {
  String getPackageName();

  int getPackageIdentifier();
}
