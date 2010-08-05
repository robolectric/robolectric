package com.xtremelabs.droidsugar;

import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.test.ClassWithNoDefaultConstructor;
import android.util.Log;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.droidsugar.fakes.FakeItemizedOverlay;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;

import static com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner.proxyFor;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(DroidSugarAndroidTestRunner.class)
public class AndroidTranslatorTest {

    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(AccountManager.class, FakeAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(FakeAccountManagerForTests.wasCalled, is(true));
        assertThat(FakeAccountManagerForTests.context, sameInstance(context));
    }

    @Test
    public void testProtectedMethodsAreDelegated() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(ItemizedOverlay.class, FakeItemizedOverlay.class);

        FakeItemizedOverlayForTests overlay = new FakeItemizedOverlayForTests(null);
        overlay.triggerProtectedCall();
        
        assertThat(((FakeItemizedOverlay) proxyFor(overlay)).populated, is(true));
    }

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Test
    public void testGeneratedDefaultConstructorIsWired() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(ClassWithNoDefaultConstructor.class, FakeClassWithNoDefaultConstructors.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(proxyFor(instance), not(CoreMatchers.<Object>nullValue()));
        assertThat(proxyFor(instance), instanceOf(FakeClassWithNoDefaultConstructors.class));
    }

    public static class FakeItemizedOverlayForTests extends ItemizedOverlay {
        public FakeItemizedOverlayForTests(Drawable drawable) {
            super(drawable);
        }

        @Override
        protected OverlayItem createItem(int i) {
            return null;
        }

        public void triggerProtectedCall() {
            populate();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    public static class FakeAccountManagerForTests {
        public static boolean wasCalled = false;
        public static Context context;

        public static AccountManager get(Context context) {
            wasCalled = true;
            FakeAccountManagerForTests.context = context;
            return mock(AccountManager.class);
        }
    }

    public static class FakeClassWithNoDefaultConstructors {
    }
}
