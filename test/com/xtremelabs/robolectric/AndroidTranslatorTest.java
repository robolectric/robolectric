package com.xtremelabs.robolectric;

import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.test.ClassWithNoDefaultConstructor;
import android.util.Log;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.shadows.ShadowItemizedOverlay;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(DogfoodRobolectricTestRunner.class)
public class AndroidTranslatorTest {

    @Test
    public void testStaticMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(AccountManager.class, ShadowAccountManagerForTests.class);

        Context context = mock(Context.class);
        AccountManager.get(context);
        assertThat(ShadowAccountManagerForTests.wasCalled, is(true));
        assertThat(ShadowAccountManagerForTests.context, sameInstance(context));
    }

    @Test
    public void testProtectedMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ItemizedOverlay.class, ShadowItemizedOverlay.class);

        ShadowItemizedOverlayForTests overlay = new ShadowItemizedOverlayForTests(null);
        overlay.triggerProtectedCall();

        assertThat(shadowOf(overlay).populated, is(true));
    }

    @Test
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
    }

    @Test
    public void testGeneratedDefaultConstructorIsWired() throws Exception {
        Robolectric.bindShadowClass(ClassWithNoDefaultConstructor.class, ShadowClassWithNoDefaultConstructors.class);

        Constructor<ClassWithNoDefaultConstructor> ctor = ClassWithNoDefaultConstructor.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ClassWithNoDefaultConstructor instance = ctor.newInstance();
        assertThat(Robolectric.shadowOf_(instance), not(nullValue()));
        assertThat(Robolectric.shadowOf_(instance), instanceOf(ShadowClassWithNoDefaultConstructors.class));
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
