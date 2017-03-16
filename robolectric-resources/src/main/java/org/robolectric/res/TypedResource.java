package org.robolectric.res;

public final class TypedResource<T> {
  private final T data;
  private final ResType resType;
  private final String qualifiers;
  private final XmlContext xmlContext;
  private final boolean isFile;

  public TypedResource(FsFile data, ResType resType, XmlContext xmlContext) {
    this((T) data, resType, xmlContext, true);
  }

  public TypedResource(T data, ResType resType, XmlContext xmlContext) {
    this(data, resType, xmlContext, false);
  }

  public TypedResource(T data, ResType resType, XmlContext xmlContext, boolean isFile) {
    this.data = data;
    this.resType = resType;
    this.xmlContext = xmlContext;
    this.isFile = isFile;

    String qualifiers = xmlContext.getQualifiers();
    this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
  }

  public T getData() {
    if (isFile()) {
      return (T) getFsFile().getPath();
    }

    return data;
  }

  public FsFile getFsFile() {
    if (!isFile()) {
      throw new IllegalStateException("not a file: " + data);
    }
    return (FsFile) data;
  }

  public boolean isXml() {
    return isFile() && getFsFile().getName().endsWith("xml");
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

    return data instanceof String
        ? (String) data
        : null;
  }

  public boolean isFile() {
    return isFile;
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
}
