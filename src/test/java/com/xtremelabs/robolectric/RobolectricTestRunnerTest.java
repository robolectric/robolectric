package com.xtremelabs.robolectric;

import android.app.Application;
import android.content.res.Resources;
import android.widget.TextView;
import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;
import com.xtremelabs.robolectric.annotation.Values;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunnerTest.RunnerForTesting.class)
public class RobolectricTestRunnerTest {

    @Test
    public void shouldInitializeAndBindApplicationButNotCallOnCreate() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(MyTestApplication.class, Robolectric.application.getClass());
        assertFalse(((MyTestApplication) Robolectric.application).onCreateWasCalled);
        assertNotNull(shadowOf(Robolectric.application).getResourceLoader());
    }

    @Test public void shouldSetUpSystemResources() throws Exception {
        assertNotNull(Resources.getSystem());
        assertEquals(Robolectric.application.getResources().getString(android.R.string.copy),
                Resources.getSystem().getString(android.R.string.copy));

        assertNotNull(Robolectric.application.getResources().getString(R.string.howdy));
        try {
            Resources.getSystem().getString(R.string.howdy);
            fail("should have thrown");
        } catch (Resources.NotFoundException e) {
        }
    }

    @Test
    public void setStaticValue_shouldIgnoreFinalModifier() {
        RobolectricContext.setStaticValue(android.os.Build.class, "MODEL", "expected value");

        assertEquals("expected value", android.os.Build.MODEL);
    }

    @Test
    @EnableStrictI18n
    public void internalBeforeTest_setsShadowApplicationStrictI18n() {
        assertTrue(Robolectric.getShadowApplication().isStrictI18n());
    }

    @Test
    @DisableStrictI18n
    public void internalBeforeTest_clearsShadowApplicationStrictI18n() {
        assertFalse(Robolectric.getShadowApplication().isStrictI18n());
    }

    @Test
    @Values(locale = "fr")
    public void internalBeforeTest_setLocale() {
        assertEquals("fr", Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).getQualifiers());
    }

    @Test
    @Values(qualifiers = "fr")
    public void internalBeforeTest_testValuesResQualifiers() {
        assertEquals("fr", Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).getQualifiers());
    }

    @Test
    public void internalBeforeTest_resetsValuesResQualifiers() {
        assertEquals("", Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).getQualifiers());
    }

    @Test
    public void internalBeforeTest_doesNotSetI18nStrictModeFromSystemIfPropertyAbsent() {
        assertFalse(Robolectric.getShadowApplication().isStrictI18n());
    }

    @Test
    @EnableStrictI18n
    public void methodBlock_setsI18nStrictModeForClassHandler() {
        TextView tv = new TextView(Robolectric.application);
        try {
            tv.setText("Foo");
            fail("TextView#setText(String) should produce an i18nException");
        } catch (Exception e) {
            // Compare exception name because it was loaded in the instrumented classloader
            assertEquals("com.xtremelabs.robolectric.util.I18nException", e.getClass().getName());
        }
    }

    public static class RunnerForTesting extends TestRunners.WithDefaults {
        public static RunnerForTesting instance;
        private final AndroidManifest androidManifest;

        public RunnerForTesting(Class<?> testClass) throws InitializationError {
            super(testClass);
            instance = this;
            androidManifest = getRobolectricContext().getAppManifest();
        }

        @Override protected Application createApplication() {
            return new MyTestApplication();
        }
    }

    public static class MyTestApplication extends Application {
        private boolean onCreateWasCalled;

        @Override public void onCreate() {
            this.onCreateWasCalled = true;
        }
    }
}
