package org.robolectric.bytecode;

import android.view.View;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class InvocationProfileTest {
  @Test
  public void shouldConvertFromMethodSignature() throws Exception {
    InvocationProfile profile = new InvocationProfile("android/view/View/invalidate()V", false, getClass().getClassLoader());
    assertThat(profile.clazz).isEqualTo(View.class);
    assertThat(profile.methodName).isEqualTo("invalidate");
    assertThat(profile.isStatic).isEqualTo(false);
    assertThat(profile.paramTypes).isEmpty();
  }

  @Test
  public void shouldHandleParamTypes() throws Exception {
    InvocationProfile profile = new InvocationProfile("android/view/View/invalidate(I[ZLjava/lang/String;)Lwhatever/Foo;", false, getClass().getClassLoader());
    assertThat(profile.clazz).isEqualTo(View.class);
    assertThat(profile.methodName).isEqualTo("invalidate");
    assertThat(profile.isStatic).isEqualTo(false);
    assertThat(profile.paramTypes).containsExactly("int", "boolean[]", "java.lang.String");
  }
}
