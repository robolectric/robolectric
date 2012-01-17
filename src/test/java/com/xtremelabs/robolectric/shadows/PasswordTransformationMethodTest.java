package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PasswordTransformationMethodTest {

    private ShadowPasswordTransformationMethod transformationMethod;

    @Before
    public void setUp(){
        transformationMethod = new ShadowPasswordTransformationMethod();
    }

    @Test
    public void shouldMaskInputCharacters(){
        CharSequence output = transformationMethod.getTransformation("foobar", null);
        assertThat(output.toString(), is("\u2022\u2022\u2022\u2022\u2022\u2022")); //using the escaped characters for cross platform compatibility.
    }

    @Test
    public void shouldTransformSpacesWithText(){
        CharSequence output = transformationMethod.getTransformation(" baz ", null);
        assertThat(output.toString(), is("\u2022\u2022\u2022\u2022\u2022"));
    }

    @Test
    public void shouldTransformSpacesWithoutText(){
        CharSequence output = transformationMethod.getTransformation("    ", null);
        assertThat(output.toString(), is("\u2022\u2022\u2022\u2022"));
    }

    @Test
    public void shouldNotTransformBlank(){
        CharSequence output = transformationMethod.getTransformation("", null);
        assertThat(output.toString(), is(""));
    }

    @Test
    public void shouldNotTransformNull(){
        CharSequence output = transformationMethod.getTransformation(null, null);
        assertThat(output.toString(), is(""));
    }

    @Test
    public void shouldRetrieveAnInstance(){
        assertThat(ShadowPasswordTransformationMethod.getInstance(), is(CoreMatchers.<Object>notNullValue()));
    }
}
