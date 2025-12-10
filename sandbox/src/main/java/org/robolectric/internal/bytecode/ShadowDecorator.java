package org.robolectric.internal.bytecode;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
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

  // The Android framework uses reflection to iterate over fields on these camera2 classes. There
  // was an Android framework bug that included __robo_data__ fields when iterating. This framework
  // has been fixed in Android C (37), but it's simpler to make these fields private so it doesn't
  // interfere with this reflection. Note that we don't want to make all __robo_data__ fields
  // private for performance reasons.
  private static final ImmutableSet<String> CLASSES_WITH_PRIVATE_ROBO_DATA_FIELDS =
      ImmutableSet.of(
          "android.hardware.camera2.CameraCharacteristics",
          "android.hardware.camera2.CaptureRequest",
          "android.hardware.camera2.CaptureResult");

  @Override
  public void decorate(MutableClass mutableClass) {
    mutableClass.addInterface(Type.getInternalName(ShadowedObject.class));
    int access = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT;
    if (CLASSES_WITH_PRIVATE_ROBO_DATA_FIELDS.contains(mutableClass.getName())) {
      access |= Opcodes.ACC_PRIVATE;
    } else {
      access |= Opcodes.ACC_PUBLIC;
    }
    mutableClass.addField(
        0,
        new FieldNode(
            access, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

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
