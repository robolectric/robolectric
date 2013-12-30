package org.robolectric.res;

public class ContentProviderData {
  private final String className;
  private final String authority;

  public ContentProviderData(String className, String authority) {
    this.className = className;
    this.authority = authority;
  }

  public String getClassName() {
    return className;
  }

  public String getAuthority() {
    return authority;
  }
}
