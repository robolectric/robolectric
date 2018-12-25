package org.robolectric.res;

import java.nio.file.Path;
import org.robolectric.res.android.ResTable_config;

public class XmlContext {
  private final String packageName;
  private final Path xmlFile;
  private final Qualifiers qualifiers;

  public XmlContext(String packageName, Path xmlFile, Qualifiers qualifiers) {
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

  public Path getXmlFile() {
    return xmlFile;
  }

  @Override public String toString() {
    return '{' + packageName + ':' + xmlFile + '}';
  }
}
