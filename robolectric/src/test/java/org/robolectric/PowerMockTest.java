package org.robolectric;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.apache.tools.*"})
@PrepareForTest(PowerMockTest.TestStaticClass.class)
public class PowerMockTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Test
    public void powermock_shouldMockStaticMethods() {
        PowerMockito.mockStatic(TestStaticClass.class);
        Mockito.when(TestStaticClass.staticMethod()).thenReturn("hello mock");

        assertThat(TestStaticClass.staticMethod().equals("hello mock"));
    }

    static class TestStaticClass {
        public static String staticMethod() {
            return "";
        }
    }
}
