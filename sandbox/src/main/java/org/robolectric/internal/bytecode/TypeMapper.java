package org.robolectric.internal.bytecode;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Type;

class TypeMapper {
  private final Map<String, String> classesToRemap;

  public TypeMapper(Map<String, String> classNameToClassNameMap) {
    classesToRemap = convertToSlashes(classNameToClassNameMap);
  }

  private static Map<String, String> convertToSlashes(Map<String, String> map) {
    HashMap<String, String> newMap = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = internalize(entry.getKey());
      String value = internalize(entry.getValue());
      newMap.put(key, value);
      newMap.put("L" + key + ";", "L" + value + ";"); // also the param reference form
    }
    return newMap;
  }

  private static String internalize(String className) {
    return className.replace('.', '/');
  }

  // remap android/Foo to android/Bar
  String mappedTypeName(String internalName) {
    String remappedInternalName = classesToRemap.get(internalName);
    if (remappedInternalName != null) {
      return remappedInternalName;
    } else {
      return internalName;
    }
  }

  Type mappedType(Type type) {
    String internalName = type.getInternalName();
    String remappedInternalName = classesToRemap.get(internalName);
    if (remappedInternalName != null) {
      return Type.getObjectType(remappedInternalName);
    } else {
      return type;
    }
  }

  String remapParams(String desc) {
    StringBuilder buf = new StringBuilder();
    buf.append("(");
    for (Type type : Type.getArgumentTypes(desc)) {
      buf.append(remapParamType(type));
    }
    buf.append(")");
    buf.append(remapParamType(Type.getReturnType(desc)));
    return buf.toString();
  }

  // remap Landroid/Foo; to Landroid/Bar;
  String remapParamType(String desc) {
    return remapParamType(Type.getType(desc));
  }

  private String remapParamType(Type type) {
    String remappedName;
    String internalName;

    switch (type.getSort()) {
      case ARRAY:
        internalName = type.getInternalName();
        int count = 0;
        while (internalName.charAt(count) == '[') count++;

        remappedName = remapParamType(internalName.substring(count));
        if (remappedName != null) {
          return Type.getObjectType(internalName.substring(0, count) + remappedName).getDescriptor();
        }
        break;

      case OBJECT:
        type = mappedType(type);
        break;

      default:
        break;
    }
    return type.getDescriptor();
  }
}
