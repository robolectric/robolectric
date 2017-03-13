package org.robolectric.res;

import java.io.Serializable;

public class TypedResource<T> implements Serializable {
  private static final long serialVersionUID = 42L;

  private final T data;
  private final ResType resType;
  private final String qualifiers;
  private final XmlContext xmlContext;

  public TypedResource(T data, ResType resType, XmlContext xmlContext) {
    this.data = data;
    this.resType = resType;
    this.xmlContext = xmlContext;

    String qualifiers = xmlContext.getQualifiers();
    this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
  }

  public T getData() {
    return resType == null ? data : resType.transform(data);
  }

  public T getRawData() {
    return data;
  }

  public ResType getResType() {
    return resType;
  }

  public String getQualifiers() {
    return qualifiers;
  }

  public XmlContext getXmlContext() {
    return xmlContext;
  }

  public String asString() {
    T data = getData();
    return data instanceof String ? (String) data : null;
  }

  public boolean isFile() {
    return false;
  }

  public boolean isReference() {
    Object data = getData();
    if (data instanceof String) {
      String s = (String) data;
      return !s.isEmpty() && s.charAt(0) == '@';
    }
    return false;
  }

  @Override public String toString() {
    return getClass().getSimpleName() + "{" +
        "values=" + data +
        ", resType=" + resType +
        ", xmlContext=" + xmlContext +
        '}';
  }

  public boolean isXml() {
    return false;
  }
}
