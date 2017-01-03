package org.robolectric.res;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlContext {
  private static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");

  private final String packageName;
  private final FsFile xmlFile;

  public XmlContext(String packageName, FsFile xmlFile) {
    this.packageName = packageName;
    this.xmlFile = xmlFile;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getDirPrefix() {
    String parentDir = xmlFile.getParent().getName();
    return parentDir.split("-")[0];
  }

  public String getQualifiers() {
    FsFile parentDir = xmlFile.getParent();
    if (parentDir == null) {
      return "";
    } else {
      String parentDirName = parentDir.getName();
      Matcher matcher = DIR_QUALIFIER_PATTERN.matcher(parentDirName);
      if (!matcher.find()) throw new IllegalStateException(parentDirName);
      return matcher.group(1);
    }
  }

  public FsFile getXmlFile() {
    return xmlFile;
  }

  @Override public String toString() {
    return "XmlContext{" +
        "packageName='" + packageName + '\'' +
        ", xmlFile=" + xmlFile +
        '}';
  }
}
