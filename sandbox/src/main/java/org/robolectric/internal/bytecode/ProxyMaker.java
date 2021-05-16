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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.robolectric.util.PerfStatsCollector;
import sun.misc.Unsafe;

public class ProxyMaker {
  private static final String TARGET_FIELD = "__proxy__";
  private static final Unsafe UNSAFE;
  private static final boolean DEBUG = false;

  static {
    try {
      Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      UNSAFE = (Unsafe) unsafeField.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new LinkageError(e.getMessage(), e);
    }
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

    final Class<?> proxyClass = UNSAFE.defineAnonymousClass(targetClass, bytecode, null);

    try {
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
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e);
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
