package org.robolectric.res;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.util.Logger;

public class XmlContext {
  private static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");
  private static final Pattern VERSION_QUALIFIER_PATTERN = Pattern.compile("v(\\d+)$");

  private final String packageName;
  private final FsFile xmlFile;
  private final String qualifiers;
  private final ResTable_config config;

  public XmlContext(String packageName, FsFile xmlFile) {
    this.packageName = packageName;
    this.xmlFile = xmlFile;
    config = new ResTable_config();
    this.qualifiers = determineQualifiers();
    if (!qualifiers.isEmpty() && !new ConfigDescription().parse(qualifiers, config)) {
      Logger.warn("failed to parse %s", qualifiers);
      Matcher matcher = VERSION_QUALIFIER_PATTERN.matcher(qualifiers);
      if (matcher.find()) {
        config.sdkVersion = Integer.parseInt(matcher.group(1));
      }
    }
  }

  public String getPackageName() {
    return packageName;
  }

  public ResTable_config getConfig() {
    return config;
  }

  public String getQualifiers() {
    return qualifiers;
  }

  private String determineQualifiers() {
    FsFile parentDir = xmlFile.getParent();
    if (parentDir == null) {
      return "";
    } else {
      String parentDirName = parentDir.getName();
      Matcher matcher = DIR_QUALIFIER_PATTERN.matcher(parentDirName);
      if (!matcher.find()) throw new IllegalStateException(parentDirName);
      String qualifiers = matcher.group(1);
      return qualifiers != null ? qualifiers : "";
    }
  }

  public FsFile getXmlFile() {
    return xmlFile;
  }

  @Override public String toString() {
    return '{' + packageName + ':' + xmlFile + '}';
  }

  public XmlContext withLineNumber(final int lineNumber) {
    return new XmlContext(packageName, xmlFile) {
      @Override
      public String toString() {
        return '{' + packageName + ':' + xmlFile + ':' + lineNumber + '}';
      }
    };
  }
}
