package org.robolectric.bytecode;

import org.junit.Test;
import org.robolectric.util.Function;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class ShadowWranglerUnitTest {
    @Test
    public void intercept_shouldGetHandlerFromSetup() throws Throwable {
        final Object expectedInput = new Object();
        final Object expectedOutput = new Object();
        Function handler = mock(Function.class);
        stub(handler.call(expectedInput)).toReturn(expectedOutput);
        Setup setup = mock(Setup.class);
        stub(setup.getInterceptionHandler(anyString(), anyString())).toReturn(handler);

        assertThat(new ShadowWrangler(setup).intercept("", "", expectedInput, null, null)).isEqualTo(expectedOutput);
    }
}
