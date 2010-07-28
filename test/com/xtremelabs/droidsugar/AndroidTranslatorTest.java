package com.xtremelabs.droidsugar;

import android.accounts.*;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.util.*;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.droidsugar.view.FakeItemizedOverlay;
import org.junit.*;
import org.junit.runner.*;

import static com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner.proxyFor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
}
