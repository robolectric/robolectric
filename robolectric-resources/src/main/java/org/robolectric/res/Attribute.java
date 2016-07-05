package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

public class Attribute {
  public static final String ANDROID_RES_NS_PREFIX = "http://schemas.android.com/apk/res/";
  public static final String RES_AUTO_NS_URI = "http://schemas.android.com/apk/res-auto";

  public final @NotNull ResName resName;
  public final @NotNull String value;
  public final @NotNull String contextPackageName;

  public Attribute(@NotNull ResName resName, @NotNull String value, @NotNull String contextPackageName) {
    if (!resName.type.equals("attr")) throw new IllegalStateException("\"" + resName.getFullyQualifiedName() + "\" unexpected");

    this.resName = resName;
    this.value = value;
    this.contextPackageName = contextPackageName;
  }

  public String qualifiedValue() {
    if (isResourceReference()) return "@" + getResourceReference().getFullyQualifiedName();
    if (isStyleReference()) return "?" + getStyleReference().getFullyQualifiedName();
    else return value;
  }

  public boolean isResourceReference() {
    return value.startsWith("@") && !isNull();
  }

  public @NotNull ResName getResourceReference() {
    if (!isResourceReference()) throw new RuntimeException("not a resource reference: " + this);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), contextPackageName, "attr");
  }

  public boolean isStyleReference() {
    return value.startsWith("?");
  }

  public ResName getStyleReference() {
    if (!isStyleReference()) throw new RuntimeException("not a style reference: " + this);
    return ResName.qualifyResName(value.substring(1), contextPackageName, "attr");
  }

  public boolean isNull() {
    return "@null".equals(value);
  }

  public boolean isEmpty() {
    return "@empty".equals(value);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "name='" + resName + '\'' +
        ", value='" + value + '\'' +
        ", contextPackageName='" + contextPackageName + '\'' +
        '}';
  }
}
