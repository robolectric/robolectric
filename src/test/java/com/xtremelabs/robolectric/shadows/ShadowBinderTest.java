package com.xtremelabs.robolectric.shadows;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowBinderTest {
    @Test
    public void transactCallsOnTransact() throws Exception {
        TestBinder testBinder = new TestBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        assertTrue(testBinder.transact(2, data, reply, 3));
        assertThat(testBinder.code, equalTo(2));
        assertThat(testBinder.data, sameInstance(data));
        assertThat(testBinder.reply, sameInstance(reply));
        assertThat(testBinder.flags, equalTo(3));
    }

    static class TestBinder extends Binder {
        int code;
        Parcel data;
        Parcel reply;
        int flags;

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            this.code = code;
            this.data = data;
            this.reply = reply;
            this.flags = flags;
            return true;
        }
    }
}
