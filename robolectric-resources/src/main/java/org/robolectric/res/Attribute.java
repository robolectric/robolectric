package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Rather than use {@link org.robolectric.fakes.RoboAttributeSet} and {@link Attribute} please use {@link Robolectric#buildAttributeSet} instead.
 * This class will be removed in the next version of Robolectric.
 */
@Deprecated
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
    return isResourceReference(value);
  }

  public @NotNull ResName getResourceReference() {
    if (!isResourceReference()) throw new RuntimeException("not a resource reference: " + this);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), contextPackageName, "attr");
  }

  public boolean isStyleReference() {
    return isStyleReference(value);
  }

  public ResName getStyleReference() {
    if (!isStyleReference()) throw new RuntimeException("not a style reference: " + this);
    return ResName.qualifyResName(value.substring(1), contextPackageName, "attr");
  }

  public boolean isNull() {
    return AttributeResource.isNull(value);
  }

  public boolean isEmpty() {
    return AttributeResource.isEmpty(value);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "name='" + resName + '\'' +
        ", value='" + value + '\'' +
        ", contextPackageName='" + contextPackageName + '\'' +
        '}';
  }

  public static boolean isResourceReference(String value) {
    return value.startsWith("@") && !isNull(value);
  }

  public static @NotNull ResName getResourceReference(String value, String defPackage, String defType) {
    if (!isResourceReference(value)) throw new IllegalArgumentException("not a resource reference: " + value);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), defPackage, defType);
  }

  public static boolean isStyleReference(String value) {
    return value.startsWith("?");
  }

  public static ResName getStyleReference(String value, String defPackage, String defType) {
    if (!isStyleReference(value)) throw new IllegalArgumentException("not a style reference: " + value);
    return ResName.qualifyResName(value.substring(1), defPackage, defType);
  }

  public static boolean isNull(String value) {
    return "@null".equals(value);
  }
}
