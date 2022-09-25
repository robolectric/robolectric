package org.robolectric.internal.bytecode;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.V1_7;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.robolectric.util.PerfStatsCollector;
import sun.misc.Unsafe;

/**
 * Defines proxy classes that can invoke methods names transformed with a {@link MethodMapper}. It
 * is primarily used to invoke the original $$robo$$-prefixed methods, but it can technically
 * support arbitrary naming schemes.
 *
 * @deprecated This is incompatible with JDK17+. Use a {@link
 *     org.robolectric.util.reflector.Reflector} interface with {@link
 *     org.robolectric.util.reflector.Direct}.
 */
@Deprecated
public class ProxyMaker {
  private static final String TARGET_FIELD = "__proxy__";
  private static final Class UNSAFE_CLASS = Unsafe.class;
  private static final Class LOOKUP_CLASS = MethodHandles.Lookup.class;
  private static final Unsafe UNSAFE;
  private static final java.lang.reflect.Method DEFINE_ANONYMOUS_CLASS;

  private static final MethodHandles.Lookup LOOKUP;
  private static final java.lang.reflect.Method HIDDEN_DEFINE_METHOD;
  private static final Object HIDDEN_CLASS_OPTIONS;

  private static final boolean DEBUG = false;

  static {
    try {
      Field unsafeField = UNSAFE_CLASS.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      UNSAFE = (Unsafe) unsafeField.get(null);

      // Unsafe.defineAnonymousClass() has been deprecated in Java 15 and removed in Java 17. Its
      // usage should be replace by MethodHandles.Lookup.defineHiddenClass() which was introduced in
      // Java 15. For now, try and support both older and newer Java versions.
      DEFINE_ANONYMOUS_CLASS = getDefineAnonymousClass();
      if (DEFINE_ANONYMOUS_CLASS == null) {
        LOOKUP = getTrustedLookup();

        Class classOptionClass = Class.forName(LOOKUP_CLASS.getName() + "$ClassOption");
        HIDDEN_CLASS_OPTIONS = Array.newInstance(classOptionClass, 1);
        Array.set(HIDDEN_CLASS_OPTIONS, 0, Enum.valueOf(classOptionClass, "NESTMATE"));
        HIDDEN_DEFINE_METHOD =
            LOOKUP_CLASS.getMethod(
                "defineHiddenClass", byte[].class, boolean.class, HIDDEN_CLASS_OPTIONS.getClass());
      } else {
        LOOKUP = null;
        HIDDEN_DEFINE_METHOD = null;
        HIDDEN_CLASS_OPTIONS = null;
      }
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  private static java.lang.reflect.Method getDefineAnonymousClass() {
    try {
      return UNSAFE_CLASS.getMethod(
          "defineAnonymousClass", Class.class, byte[].class, Object[].class);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static MethodHandles.Lookup getTrustedLookup() throws ReflectiveOperationException {
    Field trustedLookupField = LOOKUP_CLASS.getDeclaredField("IMPL_LOOKUP");
    java.lang.reflect.Method baseMethod = UNSAFE_CLASS.getMethod("staticFieldBase", Field.class);
    Object lookupBase = baseMethod.invoke(UNSAFE, trustedLookupField);
    java.lang.reflect.Method offsetMethod =
        UNSAFE_CLASS.getMethod("staticFieldOffset", Field.class);
    Object lookupOffset = offsetMethod.invoke(UNSAFE, trustedLookupField);

    java.lang.reflect.Method getObjectMethod =
        UNSAFE_CLASS.getMethod("getObject", Object.class, long.class);
    return (MethodHandles.Lookup) getObjectMethod.invoke(UNSAFE, lookupBase, lookupOffset);
  }

  private final MethodMapper methodMapper;
  private final ClassValueMap<Factory> factories;

  public ProxyMaker(MethodMapper methodMapper) {
    this.methodMapper = methodMapper;
    factories =
        new ClassValueMap<Factory>() {
          @Override
          protected Factory computeValue(Class<?> type) {
            return PerfStatsCollector.getInstance()
                .measure("createProxyFactory", () -> createProxyFactory(type));
          }
        };
  }

  public <T> T createProxy(Class<T> targetClass, T target) {
    return PerfStatsCollector.getInstance()
        .measure(
            "createProxyInstance",
            () -> factories.get(targetClass).createProxy(targetClass, target));
  }

  <T> Factory createProxyFactory(Class<T> targetClass) {
    Type targetType = Type.getType(targetClass);
    String targetName = targetType.getInternalName();
    String proxyName = targetName + "$GeneratedProxy";
    Type proxyType = Type.getType("L" + proxyName.replace('.', '/') + ";");
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES| ClassWriter.COMPUTE_MAXS);
    writer.visit(
        V1_7,
        ACC_PUBLIC | ACC_SYNTHETIC | ACC_SUPER | ACC_FINAL,
        proxyName,
        null,
        targetName,
        null);

    writer.visitField(
        ACC_PUBLIC | ACC_SYNTHETIC, TARGET_FIELD, targetType.getDescriptor(), null, null);

    for (java.lang.reflect.Method method : targetClass.getMethods()) {
      if (!shouldProxy(method)) continue;

      Method proxyMethod = Method.getMethod(method);
      GeneratorAdapter m =
          new GeneratorAdapter(
              ACC_PUBLIC | ACC_SYNTHETIC, Method.getMethod(method), null, null, writer);
      m.loadThis();
      m.getField(proxyType, TARGET_FIELD, targetType);
      m.loadArgs();
      String targetMethod = methodMapper.getName(targetClass.getName(), method.getName());
      // In Java 8 we could use invokespecial here but not in 7, from jvm spec:
      // If an invokespecial instruction names a method which is not an instance
      // initialization method, then the type of the target reference on the operand
      // stack must be assignment compatible with the current class (JLS §5.2).
      m.visitMethodInsn(INVOKEVIRTUAL, targetName, targetMethod, proxyMethod.getDescriptor(), false);
      m.returnValue();
      m.endMethod();
    }

    writer.visitEnd();

    byte[] bytecode = writer.toByteArray();

    if (DEBUG) {
      File file = new File("/tmp", targetClass.getCanonicalName() + "-DirectProxy.class");
      System.out.println("Generated Direct Proxy: " + file.getAbsolutePath());
      try (OutputStream out = new FileOutputStream(file)) {
        out.write(bytecode);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      final Class<?> proxyClass = defineHiddenClass(targetClass, bytecode);
      final Field field = proxyClass.getDeclaredField(TARGET_FIELD);
      return new Factory() {
        @Override public <E> E createProxy(Class<E> targetClass, E target) {
          try {
            Object proxy = UNSAFE.allocateInstance(proxyClass);

            field.set(proxy, target);

            return targetClass.cast(proxy);
          } catch (Throwable t) {
            throw new AssertionError(t);
          }
        }
      };
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static Class<?> defineHiddenClass(Class<?> targetClass, byte[] bytes)
      throws ReflectiveOperationException {
    if (DEFINE_ANONYMOUS_CLASS != null) {
      return (Class<?>) DEFINE_ANONYMOUS_CLASS.invoke(UNSAFE, targetClass, bytes, (Object[]) null);
    } else {
      MethodHandles.Lookup lookup = (MethodHandles.Lookup) LOOKUP.in(targetClass);
      MethodHandles.Lookup definedLookup =
          (MethodHandles.Lookup)
              HIDDEN_DEFINE_METHOD.invoke(lookup, bytes, false, HIDDEN_CLASS_OPTIONS);
      return definedLookup.lookupClass();
    }
  }

  private static boolean shouldProxy(java.lang.reflect.Method method) {
    int modifiers = method.getModifiers();
    return !Modifier.isAbstract(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isPrivate(
        modifiers) && !Modifier.isNative(modifiers);
  }

  interface MethodMapper {
    String getName(String className, String methodName);
  }

  interface Factory {
    <T> T createProxy(Class<T> targetClass, T target);
  }
}
