package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.text.method.PasswordTransformationMethod;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
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
