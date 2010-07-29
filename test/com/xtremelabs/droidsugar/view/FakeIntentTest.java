package com.xtremelabs.droidsugar.view;

import android.content.Intent;
import android.net.Uri;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;

@RunWith(DroidSugarAndroidTestRunner.class)
public class FakeIntentTest extends TestCase {

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Intent.class, FakeIntent.class);
    }

    @Test
    public void testStringExtra() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("foo", "bar");

        FakeIntent fakeIntent = (FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent);
        assertEquals("bar", fakeIntent.extras.get("foo"));
    }

    @Test
    public void testIntExtra() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("foo", 2);
        assertEquals(2, ((FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent)).extras.get("foo"));
    }

    @Test
    public void testSerializableExtra() throws Exception {
        Intent intent = new Intent();
        TestSerializable serializable = new TestSerializable();
        intent.putExtra("foo", serializable);
        assertSame(serializable, ((FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent)).extras.get("foo"));
    }

    @Test
    public void testLongExtra() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("foo", 2L);
        assertEquals(2L, ((FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent)).extras.get("foo"));
    }

    @Test
    public void testGetActionReturnsWhatWasSet() throws Exception {
        Intent intent = new Intent();
        intent.setAction("foo");
        assertEquals("foo", intent.getAction());
    }

    @Test
    public void testSetData() throws Exception {
        Intent intent = new Intent();
        Uri uri = Uri.parse("content://this/and/that");
        Intent returnedIntent = intent.setData(uri);

        FakeIntent fakeIntent = (FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent);
        assertSame(uri, fakeIntent.data);
        assertSame(intent, returnedIntent);
    }

    @Test
    public void testSetClassName() throws Exception {
        Intent intent = new Intent();
        Class<? extends FakeIntentTest> thisClass = getClass();
        intent.setClassName("package.name", thisClass.getName());
        FakeIntent fakeIntent = (FakeIntent) DroidSugarAndroidTestRunner.proxyFor(intent);
        assertSame(thisClass, fakeIntent.componentClass);
        assertEquals("package.name", fakeIntent.componentPackageName);
    }

    private static class TestSerializable implements Serializable { }
}
