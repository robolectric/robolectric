package com.xtremelabs.robolectric;

import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.test.ClassWithNoDefaultConstructor;
import android.util.Log;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.fakes.ShadowItemizedOverlay;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;

import static com.xtremelabs.robolectric.DogfoodRobolectricTestRunner.proxyFor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(DogfoodRobolectricTestRunner.class)
public class AndroidTranslatorTest {

    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(AccountManager.class, ShadowAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled, is(true));
        assertThat(ShadowAccountManagerForTests.context, sameInstance(context));
    }

    @Test
    public void testProtectedMethodsAreDelegated() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(ItemizedOverlay.class, ShadowItemizedOverlay.class);

        ShadowItemizedOverlayForTests overlay = new ShadowItemizedOverlayForTests(null);
        overlay.triggerProtectedCall();
        
        assertThat(((ShadowItemizedOverlay) proxyFor(overlay)).populated, is(true));
    }

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Test
    public void testGeneratedDefaultConstructorIsWired() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(ClassWithNoDefaultConstructor.class, ShadowClassWithNoDefaultConstructors.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(proxyFor(instance), not(nullValue()));
        assertThat(proxyFor(instance), instanceOf(ShadowClassWithNoDefaultConstructors.class));
    }

    public static class ShadowItemizedOverlayForTests extends ItemizedOverlay {
        public ShadowItemizedOverlayForTests(Drawable drawable) {
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

    public static class ShadowAccountManagerForTests {
        public static boolean wasCalled = false;
        public static Context context;

        public static AccountManager get(Context context) {
            wasCalled = true;
            ShadowAccountManagerForTests.context = context;
            return mock(AccountManager.class);
        }
    }

    public static class ShadowClassWithNoDefaultConstructors {
    }
}
