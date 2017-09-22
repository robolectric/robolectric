package org.robolectric.gradleapp;

public class BuildConfig {
  // The build system creates a BuildConfig for each flavor/build type combination,
  // in a different intermediate directory in the build tree.
  // APPLICATION_ID is unique for each combination. The R class is always found
  // in the same package as BuildConfig.
  public static final String APPLICATION_ID = "org.robolectric.gradleapp.demo";

  public static final String BUILD_TYPE = "type1";
  public static final String FLAVOR = "flavor1";
}
