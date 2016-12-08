package org.robolectric.res;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlContext {
  public static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");

  public final ResourcePath resourcePath;
  private final FsFile xmlFile;

  public XmlContext(ResourcePath resourcePath, FsFile xmlFile) {
    this.resourcePath = resourcePath;
    this.xmlFile = xmlFile;
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
        "resourcePath='" + resourcePath + '\'' +
        ", xmlFile=" + xmlFile +
        '}';
  }
}
