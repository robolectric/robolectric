package org.robolectric.util;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_5;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.robolectric.util.Reflector.WithType;

class ReflectorClassWriter extends ClassWriter {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final Type CLASS_TYPE = Type.getType(Class.class);
  private static final Type METHOD_TYPE = Type.getType(Method.class);
  private static final org.objectweb.asm.commons.Method CLASS$GET_DECLARED_METHOD =
      findMethod(Class.class, "getDeclaredMethod", new Class[]{String.class, Class[].class});
  private static final org.objectweb.asm.commons.Method METHOD$SET_ACCESSIBLE =
      findMethod(Method.class, "setAccessible", new Class[]{boolean.class});
  private static final org.objectweb.asm.commons.Method METHOD$INVOKE =
      findMethod(Method.class, "invoke", new Class[]{Object.class, Object[].class});
  private static final org.objectweb.asm.commons.Method OBJECT_INIT
      = new org.objectweb.asm.commons.Method("<init>", Type.VOID_TYPE, new Type[0]);

  private static final String TARGET_FIELD = "__target__";

  private static org.objectweb.asm.commons.Method findMethod(
      Class<?> clazz, String methodName, Class[] paramTypes) {
    try {
      return asmMethod(clazz.getMethod(methodName, paramTypes));
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  private final Class iClass;
  private final Type iType;
  private final Type reflectorType;
  private final Type targetType;

  ReflectorClassWriter(Class iClass, Class targetClass, String reflectorName) {
    super(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

    this.iClass = iClass;
    iType = Type.getType(iClass);
    reflectorType = asType(reflectorName);
    targetType = Type.getType(targetClass);
  }

  void write() {
    int accessModifiers =
        iClass.getModifiers()
            & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
    visit(V1_5, accessModifiers | ACC_SUPER | ACC_FINAL, reflectorType.getInternalName(),
        null, OBJECT_TYPE.getInternalName(), new String[]{iType.getInternalName()});

    writeTargetField();

    writeConstructor();

    int methodNumber = 0;
    for (Method method : iClass.getMethods()) {
      if (method.isDefault()) continue;

      String methodFieldName = "method" + (methodNumber++);

      // write field to hold method reference...
      visitField(ACC_PRIVATE | ACC_STATIC,
          methodFieldName, METHOD_TYPE.getDescriptor(), null, null);

      new ReflectorMethodWriter(method, asmMethod(method), methodFieldName).write();
    }

    visitEnd();
  }

  private void writeTargetField() {
    visitField(ACC_PRIVATE, TARGET_FIELD, targetType.getDescriptor(), null, null);
  }

  private void writeConstructor() {
    org.objectweb.asm.commons.Method initMethod =
        new org.objectweb.asm.commons.Method(
            "<init>", Type.VOID_TYPE, new Type[]{targetType});

    GeneratorAdapter init = new GeneratorAdapter(
        ACC_PUBLIC, initMethod, null, null, this);
    init.loadThis();
    init.invokeConstructor(OBJECT_TYPE, OBJECT_INIT);

    init.loadThis();
    init.loadArg(0);
    init.putField(reflectorType, TARGET_FIELD, targetType);

    init.returnValue();
    init.endMethod();
  }

  private class ReflectorMethodWriter extends GeneratorAdapter {

    private final Method iMethod;
    private final String methodFieldName;
    private final Type[] targetParamTypes;

    ReflectorMethodWriter(Method method, org.objectweb.asm.commons.Method asmMethod,
        String methodFieldName) {
      super(
          Opcodes.ASM6,
          ReflectorClassWriter.this.visitMethod(
              Opcodes.ACC_PUBLIC,
              asmMethod.getName(),
              asmMethod.getDescriptor(),
              null,
              ReflectorClassWriter.getInternalNames(method.getExceptionTypes())),
          Opcodes.ACC_PUBLIC,
          asmMethod.getName(),
          asmMethod.getDescriptor());
      this.iMethod = method;
      this.methodFieldName = methodFieldName;
      this.targetParamTypes = resolveParamTypes(iMethod);
    }

    void write() {
      loadOriginalMethodRef(iMethod, methodFieldName);

      loadThis();
      getField(reflectorType, TARGET_FIELD, targetType);
      loadArgArray();
      invokeVirtual(METHOD_TYPE, METHOD$INVOKE);

      if (iMethod.getReturnType().isPrimitive()) {
        unbox(Type.getType(iMethod.getReturnType()));
      } else {
        checkCast(Type.getType(iMethod.getReturnType()));
      }
      returnValue();

      endMethod();
    }

    private void loadOriginalMethodRef(Method method, String methodFieldName) {
      // if (methodN == null) {
      //   methodN = targetClass.getDeclaredMethod(name, paramTypes);
      //   methodN.setAccessible(true);
      // }
      // -> load method reference

      getStatic(reflectorType, methodFieldName, METHOD_TYPE);
      dup();
      Label haveMethodRef = newLabel();
      ifNonNull(haveMethodRef);
      pop();

      // targetClass.getDeclaredMethod(name, paramTypes);
      push(targetType);
      push(method.getName());
      Type[] paramTypes = targetParamTypes;
      push(paramTypes.length);
      newArray(CLASS_TYPE);
      for (int i = 0; i < paramTypes.length; i++) {
        dup();
        push(i);
        push(paramTypes[i]);
        arrayStore(CLASS_TYPE);
      }
      invokeVirtual(CLASS_TYPE, CLASS$GET_DECLARED_METHOD);

      // method.setAccessible(true);
      dup();
      push(true);
      invokeVirtual(METHOD_TYPE, METHOD$SET_ACCESSIBLE);

      // methodN = method;
      dup();
      putStatic(reflectorType, methodFieldName, METHOD_TYPE);
      mark(haveMethodRef);
    }

    private Type[] resolveParamTypes(Method iMethod) {
      Class<?>[] iParamTypes = iMethod.getParameterTypes();
      Annotation[][] paramAnnotations = iMethod.getParameterAnnotations();

      Type[] targetParamTypes = new Type[iParamTypes.length];
      for (int i = 0; i < iParamTypes.length; i++) {
        Class<?> paramType = findWithType(paramAnnotations[i]);
        if (paramType == null) {
          paramType = iParamTypes[i];
        }
        targetParamTypes[i] = Type.getType(paramType);
      }
      return targetParamTypes;
    }

    private Class<?> findWithType(Annotation[] paramAnnotation) {
      for (Annotation annotation : paramAnnotation) {
        if (annotation instanceof WithType) {
          String withTypeName = ((WithType) annotation).value();
          try {
            return Class.forName(withTypeName, true, iClass.getClassLoader());
          } catch (ClassNotFoundException e1) {
            // it's okay, ignore
          }
        }
      }
      return null;
    }
  }

  private static String[] getInternalNames(final Class[] types) {
    if (types == null) {
      return null;
    }
    String[] names = new String[types.length];
    for (int i = 0; i < names.length; ++i) {
      names[i] = Type.getType(types[i]).getInternalName();
    }
    return names;
  }

  private Type asType(String reflectorName) {
    return Type.getType("L" + reflectorName.replace('.', '/') + ";");
  }

  private static org.objectweb.asm.commons.Method asmMethod(Method method) {
    return org.objectweb.asm.commons.Method.getMethod(method);
  }
}
