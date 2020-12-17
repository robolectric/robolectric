package org.robolectric.internal.bytecode;

import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MutableClass {
  public final ClassNode classNode;
  final InstrumentationConfiguration config;
  final ClassNodeProvider classNodeProvider;

  final String internalClassName;
  private final String className;
  final Type classType;
  final ImmutableSet<String> foundMethods;

  MutableClass(ClassNode classNode, InstrumentationConfiguration config,
      ClassNodeProvider classNodeProvider) {
    this.classNode = classNode;
    this.config = config;
    this.classNodeProvider = classNodeProvider;
    this.internalClassName = classNode.name;
    this.className = classNode.name.replace('/', '.');
    this.classType = Type.getObjectType(internalClassName);

    List<String> foundMethods = new ArrayList<>(classNode.methods.size());
    for (MethodNode methodNode : getMethods()) {
      foundMethods.add(methodNode.name + methodNode.desc);
    }
    this.foundMethods = ImmutableSet.copyOf(foundMethods);
  }

  public boolean isInterface() {
    return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public boolean isAnnotation() {
    return (classNode.access & Opcodes.ACC_ANNOTATION) != 0;
  }

  public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
    String internalName = "L" + annotationClass.getName().replace('.', '/') + ";";
    if (classNode.visibleAnnotations == null) {
      return false;
    }

    for (Object visibleAnnotation : classNode.visibleAnnotations) {
      AnnotationNode annotationNode = (AnnotationNode) visibleAnnotation;
      if (annotationNode.desc.equals(internalName)) {
        return true;
      }
    }
    return false;
  }

  public String getName() {
    return className;
  }

  public Iterable<? extends MethodNode> getMethods() {
    return new ArrayList<>(classNode.methods);
  }

  public void addMethod(MethodNode methodNode) {
    classNode.methods.add(methodNode);
  }

  public List<FieldNode> getFields() {
    return classNode.fields;
  }

  public void addField(int index, FieldNode fieldNode) {
    classNode.fields.add(index, fieldNode);
  }

  public void addInterface(String internalName) {
    classNode.interfaces.add(internalName);
  }
}
