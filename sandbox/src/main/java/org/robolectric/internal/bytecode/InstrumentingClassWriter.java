package org.robolectric.internal.bytecode;

import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * ClassWriter implementation that verifies classes by comparing type information obtained
 * from loading the classes as resources. This was taken from the ASM ClassWriter unit tests.
 */
public class InstrumentingClassWriter extends ClassWriter {

  private final ClassNodeProvider classNodeProvider;

  /**
   * Preserve stack map frames for V51 and newer bytecode. This fixes class verification errors for
   * JDK7 and JDK8. The option to disable bytecode verification was removed in JDK8.
   *
   * <p>Don't bother for V50 and earlier bytecode, because it doesn't contain stack map frames, and
   * also because ASM's stack map frame handling doesn't support the JSR and RET instructions
   * present in legacy bytecode.
   */
  public InstrumentingClassWriter(ClassNodeProvider classNodeProvider, ClassNode classNode) {
    super(classNode.version >= 51 ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
    this.classNodeProvider = classNodeProvider;
  }

  /**
   * Returns the common super type of the two given types without actually loading
   * the classes in the ClassLoader.
   */
  @Override
  protected String getCommonSuperClass(final String type1, final String type2) {
    try {
      ClassNode info1 = typeInfo(type1);
      ClassNode info2 = typeInfo(type2);
      if ((info1.access & Opcodes.ACC_INTERFACE) != 0) {
        if (typeImplements(type2, info2, type1)) {
          return type1;
        }
        if ((info2.access & Opcodes.ACC_INTERFACE) != 0) {
          if (typeImplements(type1, info1, type2)) {
            return type2;
          }
        }
        return "java/lang/Object";
      }
      if ((info2.access & Opcodes.ACC_INTERFACE) != 0) {
        if (typeImplements(type1, info1, type2)) {
          return type2;
        } else {
          return "java/lang/Object";
        }
      }
      String b1 = typeAncestors(type1, info1);
      String b2 = typeAncestors(type2, info2);
      String result = "java/lang/Object";
      int end1 = b1.length();
      int end2 = b2.length();
      while (true) {
        int start1 = b1.lastIndexOf(';', end1 - 1);
        int start2 = b2.lastIndexOf(';', end2 - 1);
        if (start1 != -1 && start2 != -1
            && end1 - start1 == end2 - start2) {
          String p1 = b1.substring(start1 + 1, end1);
          String p2 = b2.substring(start2 + 1, end2);
          if (p1.equals(p2)) {
            result = p1;
            end1 = start1;
            end2 = start2;
          } else {
            return result;
          }
        } else {
          return result;
        }
      }
    } catch (ClassNotFoundException e) {
      return "java/lang/Object"; // Handle classes that may be obfuscated
    }
  }

  private String typeAncestors(String type, ClassNode info) throws ClassNotFoundException {
    StringBuilder b = new StringBuilder();
    while (!"java/lang/Object".equals(type)) {
      b.append(';').append(type);
      type = info.superName;
      info = typeInfo(type);
    }
    return b.toString();
  }

  private boolean typeImplements(String type, ClassNode info, String itf)
      throws ClassNotFoundException {
    while (!"java/lang/Object".equals(type)) {
      List<String> itfs = info.interfaces;
      for (String itf2 : itfs) {
        if (itf2.equals(itf)) {
          return true;
        }
      }
      for (String itf1 : itfs) {
        if (typeImplements(itf1, typeInfo(itf1), itf)) {
          return true;
        }
      }
      type = info.superName;
      info = typeInfo(type);
    }
    return false;
  }

  private ClassNode typeInfo(final String type) throws ClassNotFoundException {
    return classNodeProvider.getClassNode(type);
  }
}
