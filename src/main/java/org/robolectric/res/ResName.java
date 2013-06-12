package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResName {
  private static final Pattern FQN_PATTERN = Pattern.compile("^([^:]*):([^/]+)/(.+)$");
  private static final int NAMESPACE = 1;
  private static final int TYPE = 2;
  private static final int NAME = 3;

  public final @NotNull String packageName;
  public final @NotNull String type;
  public final @NotNull String name;

  public ResName(@NotNull String packageName, @NotNull String type, @NotNull String name) {
    this.packageName = packageName;
    this.type = type;
    this.name = name.indexOf('.') != -1 ? name.replace('.', '_') : name;
  }

  public ResName(@NotNull String fullyQualifiedName) {
    Matcher matcher = FQN_PATTERN.matcher(fullyQualifiedName);
    if (!matcher.find()) {
      throw new IllegalStateException("\"" + fullyQualifiedName + "\" is not fully qualified");
    }
    packageName = matcher.group(NAMESPACE);
    type = matcher.group(TYPE);
    String nameStr = matcher.group(NAME);
    name = nameStr.indexOf('.') != -1 ? nameStr.replace('.', '_') : nameStr;

    if (packageName.equals("xmlns")) throw new IllegalStateException("\"" + fullyQualifiedName + "\" unexpected");
  }

  public static @NotNull String qualifyResourceName(@NotNull String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {
    ResName resName = qualifyResName(possiblyQualifiedResourceName, defaultPackageName, defaultType);
    return resName.getFullyQualifiedName();
  }

  public static @NotNull ResName qualifyResName(@NotNull String possiblyQualifiedResourceName, ResName defaults) {
    return qualifyResName(possiblyQualifiedResourceName, defaults.packageName, defaults.type);
  }

  public static @NotNull ResName qualifyResName(@NotNull String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {
    int indexOfColon = possiblyQualifiedResourceName.indexOf(':');
    int indexOfSlash = possiblyQualifiedResourceName.indexOf('/');
    String packageName = indexOfColon == -1 ? null : possiblyQualifiedResourceName.substring(0, indexOfColon);
    String type = indexOfSlash == -1 ? null : possiblyQualifiedResourceName.substring(indexOfColon == -1 ? 0 : indexOfColon + 1, indexOfSlash);
    int indexBeforeName = indexOfColon > indexOfSlash ? indexOfColon : indexOfSlash;

    return new ResName(packageName == null ? defaultPackageName : packageName,
        type == null ? defaultType : type,
        possiblyQualifiedResourceName.substring(indexBeforeName + 1));
  }

  public static Integer getResourceId(ResourceIndex resourceIndex, String possiblyQualifiedResourceName, String contextPackageName) {
    if (possiblyQualifiedResourceName == null) {
      return null;
    }

    if (possiblyQualifiedResourceName.equals("@null")) {
      return null;
    }

    String fullyQualifiedResourceName = qualifyResourceName(possiblyQualifiedResourceName, contextPackageName, null);

    fullyQualifiedResourceName = fullyQualifiedResourceName.replaceAll("[@+]", "");
    Integer resourceId = resourceIndex.getResourceId(new ResName(fullyQualifiedResourceName));
    // todo warn if resourceId is null
    return resourceId;
  }

  public ResName qualify(String string) {
    return new ResName(qualifyResourceName(string.replace("@", ""), packageName, null));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResName resName = (ResName) o;

    if (!packageName.equals(resName.packageName)) return false;
    if (!type.equals(resName.type)) return false;
    if (!name.equals(resName.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageName.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ResName{" + getFullyQualifiedName() + "}";
  }

  public String getFullyQualifiedName() {
    return packageName + ":" + type + "/" + name;
  }

  public String getNamespaceUri() {
    return "http://schemas.android.com/apk/res/" + packageName;
  }

  public ResName withPackageName(String packageName) {
    if (packageName.equals(this.packageName)) return this;
    return new ResName(packageName, type, name);
  }

  public void mustBe(String expectedType) {
    if (!type.equals(expectedType)) {
      throw new RuntimeException("expected " + getFullyQualifiedName() + " to be a " + expectedType);
    }
  }
}
