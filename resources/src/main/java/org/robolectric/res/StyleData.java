package org.robolectric.res;

import com.google.common.base.Strings;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StyleData implements Style {
  private final String packageName;
  private final String name;
  private final String parent;
  private final Map<ResName, AttributeResource> items = new LinkedHashMap<>();

  public StyleData(String packageName, String name, String parent, List<AttributeResource> attributeResources) {
    this.packageName = packageName;
    this.name = name;
    this.parent = parent;

    for (AttributeResource attributeResource : attributeResources) {
      add(attributeResource.resName, attributeResource);
    }
  }

  public String getName() {
    return name;
  }

  public String getParent() {
    return parent;
  }

  private void add(ResName attrName, AttributeResource attribute) {
    attrName.mustBe("attr");
    items.put(attrName, attribute);
  }

  @Override public AttributeResource getAttrValue(ResName resName) {
    AttributeResource attributeResource = items.get(resName);

    // This hack allows us to look up attributes from downstream dependencies, see comment in
    // org.robolectric.shadows.ShadowThemeTest.obtainTypedArrayFromDependencyLibrary()
    // for an explanation. TODO(jongerrish): Make Robolectric use a more realistic resource merging
    // scheme.
    if (attributeResource == null && !"android".equals(resName.packageName) && !"android".equals(packageName)) {
      attributeResource = items.get(resName.withPackageName(packageName));
      if (attributeResource != null && (!"android".equals(attributeResource.contextPackageName))) {
        attributeResource = new AttributeResource(resName, attributeResource.value, resName.packageName);
      }
    }

    return attributeResource;
  }

  public boolean grep(Pattern pattern) {
    for (ResName resName : items.keySet()) {
      if (pattern.matcher(resName.getFullyQualifiedName()).find()) {
        return true;
      }
    }
    return false;
  }

  public void visit(Visitor visitor) {
    for (AttributeResource attributeResource : items.values()) {
      visitor.visit(attributeResource);
    }
  }

  public interface Visitor {
    void visit(AttributeResource attributeResource);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StyleData)) {
      return false;
    }
    StyleData other = (StyleData) obj;

    return Objects.equals(packageName, other.packageName)
        && Objects.equals(name, other.name)
        && Objects.equals(parent, other.parent)
        && items.size() == other.items.size();
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    hashCode = 31 * hashCode + Strings.nullToEmpty(packageName).hashCode();
    hashCode = 31 * hashCode + Strings.nullToEmpty(name).hashCode();
    hashCode = 31 * hashCode + Strings.nullToEmpty(parent).hashCode();
    hashCode = 31 * hashCode + items.size();
    return hashCode;
  }

  @Override public String toString() {
    return "Style " + packageName + ":" + name;
  }

  public String getPackageName() {
    return packageName;
  }
}
