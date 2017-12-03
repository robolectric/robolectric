package org.robolectric.internal.bytecode;

enum RoboType {
  VOID(Void.TYPE),
  BOOLEAN(Boolean.TYPE),
  BYTE(Byte.TYPE),
  CHAR(Character.TYPE),
  SHORT(Short.TYPE),
  INT(Integer.TYPE),
  LONG(Long.TYPE),
  FLOAT(Float.TYPE),
  DOUBLE(Double.TYPE),
  OBJECT(null);

  RoboType(Class type) {
    this.type = type;
  }

  private final Class type;

  public static Class findPrimitiveClass(String name) {
    for (RoboType type : RoboType.values()) {
      if (type.type != null && type.type.getName().equals(name)) {
        return type.type;
      }
    }
    return null;
  }
}
