package com.xtremelabs.robolectric.shadows;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;

import static org.junit.Assert.*;

@RunWith(DogfoodRobolectricTestRunner.class)
public class IntentTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(Intent.class, ShadowIntent.class);
        DogfoodRobolectricTestRunner.addProxy(ComponentName.class, ShadowComponentName.class);
    }

    @Test
    public void testStringExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", "bar"));

        ShadowIntent shadowIntent = (ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent);
        assertEquals("bar", shadowIntent.extras.get("foo"));
    }

    @Test
    public void testIntExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", 2));
        ShadowIntent shadowIntent = (ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent);
        assertEquals(2, shadowIntent.extras.get("foo"));
        assertEquals(2, shadowIntent.getIntExtra("foo", -1));
    }

    @Test
    public void testSerializableExtra() throws Exception {
        Intent intent = new Intent();
        TestSerializable serializable = new TestSerializable("some string");
        assertSame(intent, intent.putExtra("foo", serializable));
        ShadowIntent shadowIntent = (ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent);
        assertEquals(serializable, shadowIntent.extras.get("foo"));
        assertNotSame(serializable, shadowIntent.extras.get("foo"));
        assertEquals(serializable, shadowIntent.getSerializableExtra("foo"));
        assertNotSame(serializable, shadowIntent.getSerializableExtra("foo"));
    }

    @Test
    public void testParcelableExtra() throws Exception {
        Intent intent = new Intent();
        Parcelable parcelable = new TestParcelable();
        assertSame(intent, intent.putExtra("foo", parcelable));
        ShadowIntent shadowIntent = (ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent);
        assertSame(parcelable, shadowIntent.extras.get("foo"));
        assertSame(parcelable, shadowIntent.getParcelableExtra("foo"));
    }

    @Test
    public void testLongExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", 2L));
        assertEquals(2L, ((ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent)).extras.get("foo"));
    }

    @Test
    public void testHasExtra() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.putExtra("foo", ""));
        assertTrue(intent.hasExtra("foo"));
        assertFalse(intent.hasExtra("bar"));
    }

    @Test
    public void testGetActionReturnsWhatWasSet() throws Exception {
        Intent intent = new Intent();
        assertSame(intent, intent.setAction("foo"));
        assertEquals("foo", intent.getAction());
    }

    @Test
    public void testSetData() throws Exception {
        Intent intent = new Intent();
        Uri uri = Uri.parse("content://this/and/that");
        Intent returnedIntent = intent.setData(uri);

        ShadowIntent shadowIntent = (ShadowIntent) DogfoodRobolectricTestRunner.shadowOf(intent);
        assertSame(uri, shadowIntent.data);
        assertSame(intent, returnedIntent);
    }

    @Test
    public void testSetClassName() throws Exception {
        Intent intent = new Intent();
        Class<? extends IntentTest> thisClass = getClass();
        intent.setClassName("package.name", thisClass.getName());
        assertSame(thisClass.getName(), intent.getComponent().getClassName());
        assertEquals("package.name", intent.getComponent().getPackageName());
    }

    private static class TestSerializable implements Serializable {
        private String someValue;

        public TestSerializable(String someValue) {
            this.someValue = someValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSerializable that = (TestSerializable) o;

            if (someValue != null ? !someValue.equals(that.someValue) : that.someValue != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return someValue != null ? someValue.hashCode() : 0;
        }
    }

    private class TestParcelable implements Parcelable {
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
