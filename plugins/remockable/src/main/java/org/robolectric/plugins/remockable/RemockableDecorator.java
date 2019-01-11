package org.robolectric.plugins.remockable;

import com.google.auto.service.AutoService;
import java.lang.reflect.Modifier;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.internal.bytecode.ClassInstrumentor.Decorator;
import org.robolectric.internal.bytecode.MutableClass;
import org.robolectric.internal.bytecode.ShadowConstants;

@AutoService(Decorator.class)
public class RemockableDecorator implements Decorator {

  private static final Type CLASS_TYPE = Type.getType(Class.class);
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  static final String MOCK_FIELD_NAME = "__robo_mock__";
  private static final Type REMOCKABLE_TYPE = Type.getType(Remockable.class);
  private static final Method CHECK_MOCK_METHOD =
      new Method("checkMock",
          "("
              + "[Z"                  // result: [true] iff mock handled invocation
              + "Ljava/lang/Object;"  // mock
              + "Ljava/lang/Object;"  // obj
              + "Ljava/lang/Class;"   // class
              + "Ljava/lang/String;"  // method name
              + "[Ljava/lang/Class;"  // param types
              + "[Ljava/lang/Object;" // params
              + ")"
              + "Ljava/lang/Object;");

  @Override
  public void decorate(MutableClass mutableClass) {
    mutableClass.addField(0, new FieldNode(Opcodes.ACC_PUBLIC,
        MOCK_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));
  }

  /**
   * Generates this code:
   * ```java
   * if (__robo_mock__ != null) {
   *   boolean[] wasHandled = new boolean[1];
   *   Object answer = Remockable.checkMock(wasHandled, __robo_mock__, this,
   *       this.getClass(), [arg types], [args]);
   *   if (wasHandled[0]) {
   *     return answer;
   *   }
   * }
   * ```
   */
  @Override
  public void decorateMethodPreClassHandler(MutableClass mutableClass, MethodNode originalMethod,
      String originalMethodName, GeneratorAdapter generator) {
    boolean isStatic = Modifier.isStatic(originalMethod.access);
    boolean isNormalInstanceMethod = !isStatic
        && !originalMethodName.equals(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    // maybe perform direct call...
    if (isNormalInstanceMethod) {
      Type classType = mutableClass.getClassType();

      Label noMock = new Label();
      generator.loadThis();                                         // this
      generator.getField(classType, RemockableDecorator.MOCK_FIELD_NAME, OBJECT_TYPE);  // contents of this.__robo_mock__
      generator.ifNull(noMock);                 // is null?

      // give the mock a chance to handle this invocation...
      generator.push(1);                          // 1
      generator.newArray(Type.BOOLEAN_TYPE);      // Z[Z?]
      generator.dup();                            // Z[Z?], Z[Z?]

      generator.loadThis();                       // Z[Z?], Z[Z?]
      generator.getField(classType, RemockableDecorator.MOCK_FIELD_NAME, OBJECT_TYPE);
                                                  // Z[Z?], Z[Z?], __robo_mock__
      generator.loadThis();                       // Z[Z?], Z[Z?], __robo_mock__, this
      generator.push(classType);     // Z[Z?], Z[Z?], __robo_mock__, this, Class
      generator.push(originalMethodName);         // Z[Z?], Z[Z?], __robo_mock__, this, Class, String

      // create array of param types
      Type[] argumentTypes = Type.getArgumentTypes(originalMethod.desc);
      generator.push(argumentTypes.length);             // n
      generator.newArray(CLASS_TYPE);                   // I[n]
      for (int i = 0; i < argumentTypes.length; i++) {
        generator.dup();
        generator.push(i);
        generator.push(argumentTypes[i]);
        generator.arrayStore(CLASS_TYPE);
      }                              // Z[Z?], Z[Z?], __robo_mock__, this, Class, String, Class[]
      generator.loadArgArray();      // Z[Z?], Z[Z?], __robo_mock__, this, Class, String, Class[], Object[]

      Label unstubbed = new Label();
      generator.visitMethodInsn(Opcodes.INVOKESTATIC, REMOCKABLE_TYPE.getInternalName(),
          CHECK_MOCK_METHOD.getName(), CHECK_MOCK_METHOD.getDescriptor(), false);
                                                  // Z[Z?], val
      generator.swap();                           // val, Z[Z?]
      generator.push(0);                          // val, Z[Z?], 0
      generator.arrayLoad(Type.BOOLEAN_TYPE);     // val, Z?
      generator.ifZCmp(Opcodes.IFEQ, unstubbed);  // val
      generator.checkCast(Type.getReturnType(originalMethod.desc));
      generator.returnValue();

      generator.mark(unstubbed);
      generator.pop();
      generator.mark(noMock);
    }
  }

}
