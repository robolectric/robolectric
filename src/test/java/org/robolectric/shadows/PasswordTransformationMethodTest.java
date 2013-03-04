package org.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PasswordTransformationMethodTest {

    private ShadowPasswordTransformationMethod transformationMethod;

    @Before
    public void setUp(){
        transformationMethod = new ShadowPasswordTransformationMethod();
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
    public void shouldNotTransformNull(){
        CharSequence output = transformationMethod.getTransformation(null, null);
        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    public void shouldRetrieveAnInstance() {
        assertThat(ShadowPasswordTransformationMethod.getInstance()).isNotNull();
    }
}
