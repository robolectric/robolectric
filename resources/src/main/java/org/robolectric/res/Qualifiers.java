package org.robolectric.res;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;

/**
 * Android qualifiers as defined by
 * https://developer.android.com/guide/topics/resources/providing-resources.html
 */
@SuppressWarnings("NewApi")
public class Qualifiers {
  private static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-([^/]*))?/?$");

  private final String qualifiers;
  private final ResTable_config config;

  public static Qualifiers parse(String qualifiers) {
    return parse(qualifiers, true);
  }

  public static Qualifiers parse(String qualifiers, boolean applyVersionForCompat) {
    final ResTable_config config = new ResTable_config();
    if (!qualifiers.isEmpty()
        && !ConfigDescription.parse(qualifiers, config, applyVersionForCompat)) {
      throw new IllegalArgumentException(
          "failed to parse qualifiers '"
              + qualifiers
              + "'. See"
              + " https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules"
              + " for expected format.");
    }

    return new Qualifiers(qualifiers, config);
  }

  protected Qualifiers(String qualifiers, ResTable_config config) {
    this.qualifiers = qualifiers;
    this.config = config;
  }

  public ResTable_config getConfig() {
    return config;
  }

  @Override
  public String toString() {
    return qualifiers;
  }

  public static Qualifiers fromParentDir(Path parentDir) {
    if (parentDir == null) {
      return parse("");
    } else {
      String parentDirName = parentDir.getFileName().toString();
      Matcher matcher = DIR_QUALIFIER_PATTERN.matcher(parentDirName);
      if (!matcher.find()) {
        throw new IllegalStateException(parentDirName);
      }
      String qualifiers = matcher.group(1);
      return parse(qualifiers != null ? qualifiers : "");
    }
  }
}
