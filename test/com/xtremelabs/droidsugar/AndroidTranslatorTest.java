package com.xtremelabs.droidsugar;

import android.accounts.*;
import android.content.*;
import android.util.*;
import org.junit.*;
import org.junit.runner.*;

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
    public void testPrintlnWorks() throws Exception {
        Log.println(1, "tag", "msg");
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
