package org.robolectric.shadows;

import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import org.robolectric.res.AttrData;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;
import org.robolectric.util.I18nException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.robolectric.Robolectric.shadowOf;

public class RoboAttributeSet implements AttributeSet {
  private static final Set<String> ALREADY_WARNED_ABOUT = new HashSet<String>();

  private final List<Attribute> attributes;
  private final Resources resources;
  private Class<? extends View> viewClass;
  private final ResourceLoader resourceLoader;

  /**
   * Names of attributes to be validated for i18n-safe values.
   */
  private static final ResName strictI18nAttrs[] = {
      new ResName("android:attr/text"),
      new ResName("android:attr/title"),
      new ResName("android:attr/titleCondensed"),
      new ResName("android:attr/summary")
  };

  public RoboAttributeSet(List<Attribute> attributes, Resources resources, Class<? extends View> viewClass) {
    this.attributes = attributes;
    this.resources = resources;
    this.viewClass = viewClass;
    this.resourceLoader = shadowOf(resources).getResourceLoader();
  }

  public RoboAttributeSet put(String fullyQualifiedName, String value, String valuePackage) {
    return put(new Attribute(fullyQualifiedName, value, valuePackage));
  }

  public RoboAttributeSet put(Attribute attribute) {
    attributes.add(attribute);
    return this;
  }

  @Override
  public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
    ResName resName = getAttrResName(namespace, attribute);
    Attribute attr = findByName(resName);
    return (attr != null) ? Boolean.valueOf(attr.value) : defaultValue;
  }

  @Override
  public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
    ResName resName = getAttrResName(namespace, attribute);
    Attribute attr = findByName(resName);
    if (attr == null) return defaultValue;

    String qualifiers = shadowOf(resources.getAssets()).getQualifiers();
    TypedResource<AttrData> typedResource = resourceLoader.getValue(resName, qualifiers);
    if (typedResource == null) {
      System.out.println("WARN: no attr found for " + resName + ", assuming it's an integer...");
      typedResource = new TypedResource<AttrData>(new AttrData(attribute, "integer", null), ResType.INTEGER);
    }

    TypedValue outValue = new TypedValue();
    Converter.convertAndFill(attr, outValue, resourceLoader, qualifiers, typedResource.getData());
    if (outValue.type == TypedValue.TYPE_NULL) {
      return defaultValue;
    }

    return outValue.data;
  }

  @Override
  public int getAttributeCount() {
    return attributes.size();
  }

  @Override
  public String getAttributeName(int index) {
    return attributes.get(index).resName.getFullyQualifiedName();
  }

  @Override
  public String getAttributeValue(String namespace, String attribute) {
    ResName resName = getAttrResName(namespace, attribute);
    Attribute attr = findByName(resName);
    if (attr != null && !attr.isNull()) {
      return attr.qualifiedValue();
    }

    return null;
  }

  @Override
  public String getAttributeValue(int index) {
    if (index > attributes.size()) return null;

    Attribute attr = attributes.get(index);
    if (attr != null && !attr.isNull()) {
      return attr.qualifiedValue();
    }

    return null;
  }

  @Override
  public String getPositionDescription() {
    return "position description from RoboAttributeSet -- implement me!";
  }

  @Override
  public int getAttributeNameResource(int index) {
    ResName resName = attributes.get(index).resName;
    Integer resourceId = resourceLoader.getResourceIndex().getResourceId(resName);
    return resourceId == null ? 0 : resourceId;
  }

  @Override
  public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
    ResName resName = getAttrResName(namespace, attribute);
    Attribute attr = findByName(resName);
    return (attr != null) ? Float.valueOf(attr.value) : defaultValue;
  }

  @Override
  public int getAttributeListValue(int index, String[] options, int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getAttributeBooleanValue(int resourceId, boolean defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
    ResName resName = getAttrResName(namespace, attribute);
    Attribute attr = findByName(resName);
    if (attr == null) return defaultValue;

    Integer resourceId = ResName.getResourceId(resourceLoader.getResourceIndex(), attr.value, attr.contextPackageName);
    return resourceId == null ? defaultValue : resourceId;
  }

  @Override
  public int getAttributeResourceValue(int resourceId, int defaultValue) {
    String attrName = resourceLoader.getResourceIndex().getResourceName(resourceId);
    ResName resName = getAttrResName(null, attrName);
    Attribute attr = findByName(resName);
    if (attr == null) return defaultValue;
    Integer extracted = ResName.getResourceId(resourceLoader.getResourceIndex(), attr.value, attr.contextPackageName);
    return (extracted == null) ? defaultValue : extracted;
  }

  @Override
  public int getAttributeIntValue(int index, int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getAttributeUnsignedIntValue(int index, int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public float getAttributeFloatValue(int index, float defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getIdAttribute() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getClassAttribute() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getIdAttributeResourceValue(int defaultValue) {
    throw new UnsupportedOperationException();
  }

  @Override public int getStyleAttribute() {
    Attribute styleAttribute = Attribute.find(attributes, new ResName("", "attr", "style"));
    if (styleAttribute == null) {
      // Per Android specifications, return 0 if there is no style.
      return 0;
    }
    Integer i = ResName.getResourceId(resourceLoader.getResourceIndex(), styleAttribute.value, styleAttribute.contextPackageName);
    return i != null ? i : 0;
  }


  public void validateStrictI18n() {
    for (ResName key : strictI18nAttrs) {
      Attribute attribute = findByName(key);
      if (attribute != null) {
        if (!attribute.value.startsWith("@string/")) {
          throw new I18nException("View class: " + (viewClass != null ? viewClass.getName() : "") +
              " has attribute: " + key + " with hardcoded value: \"" + attribute.value + "\" and is not i18n-safe.");
        }
      }
    }
  }

  private ResName getAttrResName(String namespace, String attrName) {
    String packageName = Attribute.extractPackageName(namespace);
    return new ResName(packageName, "attr", attrName);
  }

  private Attribute findByName(ResName resName) {
    ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
    Integer resourceId = resourceIndex.getResourceId(resName);
    // canonicalize the attr name if we can, otherwise don't...
    // todo: this is awful; fix it.
    if (resourceId == null) {
      return Attribute.find(attributes, resName);
    } else {
      return Attribute.find(attributes, resourceId, resourceIndex);
    }
  }
}
