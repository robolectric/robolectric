package org.robolectric.annotation.processing.shadows;

import com.example.objects.OuterDummy;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.shadows.ShadowInnerDummyWithPicker.Picker;
import org.robolectric.shadow.api.ShadowPicker;

@Implements(value = OuterDummy.InnerDummy.class, shadowPicker = Picker.class)
public class ShadowInnerDummyWithPicker {

  @Implements(value = OuterDummy.InnerDummy.class, maxSdk = 21, shadowPicker = Picker.class)
  public static class ShadowInnerDummyWithPicker2 extends ShadowInnerDummyWithPicker {}

  @Implements(
      className = "com.example.objects.OuterDummy$InnerDummy2",
      maxSdk = 21,
      shadowPicker = Picker.class)
  public static class ShadowInnerDummyWithPicker3 extends ShadowInnerDummyWithPicker {}

  public static class Picker implements ShadowPicker<ShadowInnerDummyWithPicker> {
    @Override
    public Class<? extends ShadowInnerDummyWithPicker> pickShadowClass() {
      return ShadowInnerDummyWithPicker.class;
    }
  }
}
