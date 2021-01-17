package org.robolectric.internal.bytecode;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A more lightweight variant of {@link MutableClass}. This lets you check for basic metadata like
 * class name, interfaces, and annotation info by wrapping a {@link ClassReader}, which is
 * significantly faster than a {@link org.objectweb.asm.tree.ClassNode} object.
 */
public class ClassDetails {
  private final ClassReader classReader;
  private final String className;
  private Set<String> annotations;

  public ClassDetails(byte[] classBytes) {
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
          new AnnotationVisitor(annotations),
          ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
    String internalName = "L" + annotationClass.getName().replace('.', '/') + ";";
    return this.annotations.contains(internalName);
  }

  private static class AnnotationVisitor extends ClassVisitor {
    private final Set<String> annotations;

    public AnnotationVisitor(Set<String> annotations) {
      super(Opcodes.ASM9);
      this.annotations = annotations;
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      if (visible) {
        annotations.add(descriptor);
      }
      return null;
    }
  }
}
