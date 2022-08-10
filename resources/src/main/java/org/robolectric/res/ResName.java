package org.robolectric.res;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

@SuppressWarnings("NewApi")
public class ResName {
  public static final String ID_TYPE = "id";

  private static final Pattern FQN_PATTERN = Pattern.compile("^([^:]*):([^/]+)/(.+)$");
  private static final int NAMESPACE = 1;
  private static final int TYPE = 2;
  private static final int NAME = 3;

  public final @Nonnull String packageName;
  public final @Nonnull String type;
  public final @Nonnull String name;

  public final int hashCode;

  public ResName(@Nonnull String packageName, @Nonnull String type, @Nonnull String name) {
    this.packageName = packageName;
    this.type = type.trim();
    this.name = name.trim();

    hashCode = computeHashCode();
  }

  public ResName(@Nonnull String fullyQualifiedName) {
    Matcher matcher = FQN_PATTERN.matcher(fullyQualifiedName.trim());
    if (!matcher.find()) {
      throw new IllegalStateException("\"" + fullyQualifiedName + "\" is not fully qualified");
    }
    packageName = matcher.group(NAMESPACE);
    type = matcher.group(TYPE).trim();
    name = matcher.group(NAME).trim();

    hashCode = computeHashCode();
    if (packageName.equals("xmlns"))
      throw new IllegalStateException("\"" + fullyQualifiedName + "\" unexpected");
  }

  /** Returns the fully qualified resource name if null if the resource could not be qualified. */
  public static String qualifyResourceName(
      @Nonnull String possiblyQualifiedResourceName,
      String defaultPackageName,
      String defaultType) {
    ResName resName = qualifyResName(possiblyQualifiedResourceName, defaultPackageName, defaultType);
    return resName != null ? resName.getFullyQualifiedName() : null;
  }

  public static ResName qualifyResName(@Nonnull String possiblyQualifiedResourceName, ResName defaults) {
    return qualifyResName(possiblyQualifiedResourceName, defaults.packageName, defaults.type);
  }

  public static ResName qualifyResName(@Nonnull String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {
    int indexOfColon = possiblyQualifiedResourceName.indexOf(':');
    int indexOfSlash = possiblyQualifiedResourceName.indexOf('/');
    String type = null;
    String packageName = null;
    String name = possiblyQualifiedResourceName;
    if (indexOfColon > indexOfSlash) {
      if (indexOfSlash > 0) {
        type = possiblyQualifiedResourceName.substring(0, indexOfSlash);
      }
      packageName = possiblyQualifiedResourceName.substring(indexOfSlash + 1, indexOfColon);
      name =  possiblyQualifiedResourceName.substring(indexOfColon + 1);
    } else if (indexOfSlash > indexOfColon) {
      if (indexOfColon > 0) {
        packageName = possiblyQualifiedResourceName.substring(0, indexOfColon);
      }
      type = possiblyQualifiedResourceName.substring(indexOfColon + 1, indexOfSlash);
      name = possiblyQualifiedResourceName.substring(indexOfSlash + 1);
    }

    if ((type == null && defaultType == null) || (packageName == null && defaultPackageName == null)) {
      return null;
    }

    if (packageName == null) {
      packageName = defaultPackageName;
    } else if ("*android".equals(packageName)) {
      packageName = "android";
    }

    return new ResName(packageName, type == null ? defaultType : type, name);
  }

  public static String qualifyResName(String possiblyQualifiedResourceName, String contextPackageName) {
    if (possiblyQualifiedResourceName == null) {
      return null;
    }

    if (AttributeResource.isNull(possiblyQualifiedResourceName)) {
      return null;
    }

    // Was not able to fully qualify the resource name
    String fullyQualifiedResourceName = qualifyResourceName(possiblyQualifiedResourceName, contextPackageName, null);
    if (fullyQualifiedResourceName == null) {
      return null;
    }

    return fullyQualifiedResourceName.replaceAll("[@+]", "");
  }

  public static ResName qualifyFromFilePath(@Nonnull final String packageName, @Nonnull final String filePath) {
    final File file = new File(filePath);
    final String type = file.getParentFile().getName().split("-", 0)[0];
    final String name = Fs.baseNameFor(file.toPath());

    return new ResName(packageName, type, name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResName)) return false;

    ResName resName = (ResName) o;

    if (hashCode() != resName.hashCode()) return false;

    if (!packageName.equals(resName.packageName)) return false;
    if (!type.equals(resName.type)) return false;
    if (!name.equals(resName.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return hashCode;
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
      throw new RuntimeException(
          "expected " + getFullyQualifiedName() + " to be a " + expectedType + ", is a " + type);
    }
  }

  private int computeHashCode() {
    int result = packageName.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
