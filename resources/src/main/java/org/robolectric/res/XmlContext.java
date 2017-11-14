package org.robolectric.res;

import org.robolectric.res.android.ResTable_config;

public class XmlContext {
  private final String packageName;
  private final FsFile xmlFile;
  private final Qualifiers qualifiers;

  public XmlContext(String packageName, FsFile xmlFile, Qualifiers qualifiers) {
    this.packageName = packageName;
    this.xmlFile = xmlFile;
    this.qualifiers = qualifiers;
  }

  public String getPackageName() {
    return packageName;
  }

  public ResTable_config getConfig() {
    return qualifiers.getConfig();
  }

  public Qualifiers getQualifiers() {
    return qualifiers;
  }

  public FsFile getXmlFile() {
    return xmlFile;
  }

  @Override public String toString() {
    return '{' + packageName + ':' + xmlFile + '}';
  }
}
