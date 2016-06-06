package org.robolectric.fakes;

import android.content.Context;
import android.util.AttributeSet;

import com.android.internal.util.XmlUtils;
import com.google.android.collect.Lists;

import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;

import java.util.List;

/**
 * Robolectric implementation of {@link android.util.AttributeSet}.
 */
public class RoboAttributeSet implements AttributeSet {
  private final List<Attribute> attributes;
  private Context context;

  private RoboAttributeSet(List<Attribute> attributes, Context context) {
    this.attributes = attributes;
    this.context = context;
  }

  /**
   * Creates a {@link RoboAttributeSet} as {@link AttributeSet} for the given
   * {@link Context} and {@link Attribute}(s)
   */
  public static AttributeSet create(Context context, Attribute... attrs) {
    List<Attribute> attributesList = Lists.newArrayList(attrs);
    return create(context, attributesList);
  }

  public static AttributeSet create(Context context, List<Attribute> attributesList) {
    return new RoboAttributeSet(attributesList, context);
  }

  @Override
  public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
    return XmlUtils.convertValueToBoolean(this.getAttributeValue(namespace, attribute), defaultValue);
  }

  @Override
  public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
    return XmlUtils.convertValueToInt(this.getAttributeValue(namespace, attribute), defaultValue);
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
    String packageName = Attribute.extractPackageName(namespace);
    return Attribute.findValue(attributes, new ResName(packageName, "attr", attribute).getFullyQualifiedName());
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
    String resName = attributes.get(index).resName.getFullyQualifiedName();
    return context.getResources().getIdentifier(resName, null, context.getPackageName());
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
    String s = this.getAttributeValue(namespace, attribute);
    return s != null?Float.parseFloat(s):defaultValue;
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
    return XmlUtils.convertValueToInt(this.getAttributeValue(namespace, attribute), defaultValue);
  }

  @Override
  public int getAttributeResourceValue(int resourceId, int defaultValue) {
    return XmlUtils.convertValueToInt(this.getAttributeValue(resourceId), defaultValue);
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
    return this.getAttributeResourceValue(null, "style", 0);
  }
}
