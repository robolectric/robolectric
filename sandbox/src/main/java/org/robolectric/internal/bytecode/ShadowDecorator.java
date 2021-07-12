package org.robolectric.internal.bytecode;

import com.google.auto.service.AutoService;
import javax.annotation.Priority;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/** Decorator which adds Robolectric's shadowing behavior to a class. */
@AutoService(ClassInstrumentor.Decorator.class)
@Priority(Integer.MIN_VALUE)
public class ShadowDecorator implements ClassInstrumentor.Decorator {
  private static final String OBJECT_DESC = Type.getDescriptor(Object.class);
  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";

  @Override
  public void decorate(MutableClass mutableClass) {
    mutableClass.addInterface(Type.getInternalName(ShadowedObject.class));

    mutableClass.addField(
        0,
        new FieldNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME,
            OBJECT_DESC,
            OBJECT_DESC,
            null));

    addRoboGetDataMethod(mutableClass);
  }

  private void addRoboGetDataMethod(MutableClass mutableClass) {
    MethodNode initMethodNode =
        new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            ShadowConstants.GET_ROBO_DATA_METHOD_NAME,
            GET_ROBO_DATA_SIGNATURE,
            null,
            null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    generator.loadThis(); // this
    generator.getField(
        mutableClass.classType,
        ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME,
        OBJECT_TYPE); // contents of __robo_data__
    generator.returnValue();
    generator.endMethod();
    mutableClass.addMethod(initMethodNode);
  }
}
