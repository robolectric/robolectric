package org.robolectric.shadows;

import android.text.method.PasswordTransformationMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PasswordTransformationMethodTest {

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

  @Test @Ignore("Looks like this doesn't work in Jelly Bean.")
  public void shouldNotTransformNull(){
    CharSequence output = transformationMethod.getTransformation(null, null);
    assertThat(output.toString()).isEqualTo("");
  }

  @Test
  public void shouldRetrieveAnInstance() {
    assertThat(PasswordTransformationMethod.getInstance()).isNotNull();
  }
}
