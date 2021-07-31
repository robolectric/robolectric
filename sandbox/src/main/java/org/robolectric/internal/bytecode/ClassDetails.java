package org.robolectric.internal.bytecode;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A more lightweight variant of {@link MutableClass}. This lets you check for basic metadata like
 * class name, interfaces, and annotation info by wrapping a {@link ClassReader}, which is
 * significantly faster than a {@link org.objectweb.asm.tree.ClassNode} object.
 */
public class ClassDetails {
  private static final String SHADOWED_OBJECT_INTERNAL_NAME =
      ShadowedObject.class.getName().replace('.', '/');
  private static final String INSTRUMENTED_INTERFACE_INTERNAL_NAME =
      InstrumentedInterface.class.getName().replace('.', '/');

  private final ClassReader classReader;
  private final String className;
  private final byte[] classBytes;
  private Set<String> annotations;
  private Set<String> interfaces;

  public ClassDetails(byte[] classBytes) {
    this.classBytes = classBytes;
    this.classReader = new ClassReader(classBytes);
    this.className = classReader.getClassName().replace('/', '.');
  }

  public boolean isInterface() {
    return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAnnotation() {
    return (classReader.getAccess() & Opcodes.ACC_ANNOTATION) != 0;
  }

  public String getName() {
    return className;
  }

  public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
    if (annotations == null) {
      this.annotations = new HashSet<>();
      classReader.accept(
          new AnnotationCollector(annotations),
          ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
    String internalName = "L" + annotationClass.getName().replace('.', '/') + ";";
    return this.annotations.contains(internalName);
  }

  public boolean isInstrumented() {
    if (this.interfaces == null) {
      this.interfaces = new HashSet<>(Arrays.asList(classReader.getInterfaces()));
    }
    return (isInterface() && this.interfaces.contains(INSTRUMENTED_INTERFACE_INTERNAL_NAME))
        || this.interfaces.contains(SHADOWED_OBJECT_INTERNAL_NAME);
  }

  public byte[] getClassBytes() {
    return classBytes;
  }

  private static class AnnotationCollector extends ClassVisitor {
    private final Set<String> annotations;

    public AnnotationCollector(Set<String> annotations) {
      super(Opcodes.ASM9);
      this.annotations = annotations;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      if (visible) {
        annotations.add(descriptor);
      }
      return null;
    }
  }
}
