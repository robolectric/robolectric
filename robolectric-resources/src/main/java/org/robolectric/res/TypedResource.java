package org.robolectric.res;

public class TypedResource<T> {
  private final T data;
  private final ResType resType;

  public TypedResource(T data, ResType resType) {
    this.data = data;
    this.resType = resType;
  }

  public ResType getResType() {
    return resType;
  }

  public T getData() {
    return data;
  }

  public String asString() {
    return ((String) getData());
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
        "data=" + data +
        ", resType=" + resType +
        '}';
  }
}
