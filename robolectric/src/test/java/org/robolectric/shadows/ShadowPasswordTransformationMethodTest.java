package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.text.method.PasswordTransformationMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowPasswordTransformationMethodTest {

  private PasswordTransformationMethod transformationMethod;

  @Before
  public void setUp(){
    transformationMethod = new PasswordTransformationMethod();
  }

  @Test
  public void shouldMaskInputCharacters(){
    CharSequence output = transformationMethod.getTransformation("foobar", null);
    assertThat(output.toString()).isEqualTo("\u2022\u2022\u2022\u2022\u2022\u2022"); //using the escaped characters for cross platform compatibility.
  }

  @Test
  public void shouldTransformSpacesWithText(){
    CharSequence output = transformationMethod.getTransformation(" baz ", null);
    assertThat(output.toString()).isEqualTo("\u2022\u2022\u2022\u2022\u2022");
  }

  @Test
  public void shouldTransformSpacesWithoutText(){
    CharSequence output = transformationMethod.getTransformation("    ", null);
    assertThat(output.toString()).isEqualTo("\u2022\u2022\u2022\u2022");
  }

  @Test
  public void shouldNotTransformBlank(){
    CharSequence output = transformationMethod.getTransformation("", null);
    assertThat(output.toString()).isEqualTo("");
  }

  @Test
  public void shouldRetrieveAnInstance() {
    assertThat(PasswordTransformationMethod.getInstance()).isNotNull();
  }
}
