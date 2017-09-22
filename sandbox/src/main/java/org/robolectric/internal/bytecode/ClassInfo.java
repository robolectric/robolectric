package org.robolectric.internal.bytecode;

import java.lang.annotation.Annotation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public class ClassInfo {
  private final String className;
  private final ClassNode classNode;

  public ClassInfo(String className, ClassNode classNode) {
    this.className = className;
    this.classNode = classNode;
  }

  public boolean isInterface() {
    return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAnnotation() {
    return (classNode.access & Opcodes.ACC_ANNOTATION) != 0;
  }

  public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
    String internalName = "L" + annotationClass.getName().replace('.', '/') + ";";
    if (classNode.visibleAnnotations == null) return false;
    for (Object visibleAnnotation : classNode.visibleAnnotations) {
      AnnotationNode annotationNode = (AnnotationNode) visibleAnnotation;
      if (annotationNode.desc.equals(internalName)) return true;
    }
    return false;
  }

  public String getName() {
    return className;
  }
}
