package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.database.*;
import android.net.Uri;
import android.os.Bundle;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(WithTestDefaultsRunner.class)
public class CursorWrapperTest {

    private class ForwardVerifier {

        final Cursor mockCursor;
        final CursorWrapper cursorWrapper;
        final HashMap<String, Method> cursorMethod;

        public ForwardVerifier() {
            mockCursor = mock(Cursor.class);
            cursorWrapper = new CursorWrapper(mockCursor);
            cursorMethod = new HashMap<String, Method>();

            // This works because no two methods in the Cursor interface have the same name
            for (Method m : Cursor.class.getMethods()) {
                cursorMethod.put(m.getName(), m);
            }
        }

        public void verifyForward(String methodName, Object... params) throws Exception {
            assertThat(cursorMethod.keySet(), hasItem(methodName));

            Method method = cursorMethod.get(methodName);
            method.invoke(cursorWrapper, params);
            method.invoke(verify(mockCursor, times(1)), params);
            Mockito.verifyNoMoreInteractions(mockCursor);
        }

    }

    @Test
    public void testCursorMethodsAreForwarded() throws Exception {
        ForwardVerifier v = new ForwardVerifier();

        v.verifyForward("close");
        v.verifyForward("copyStringToBuffer", 1, mock(CharArrayBuffer.class));
        v.verifyForward("deactivate");
        v.verifyForward("getBlob", 2);
        v.verifyForward("getColumnCount");
        v.verifyForward("getColumnIndex", "foo");
        v.verifyForward("getColumnIndexOrThrow", "foo");
        v.verifyForward("getColumnName", 3);
        v.verifyForward("getColumnNames");
        v.verifyForward("getCount");
        v.verifyForward("getDouble", 12);
        v.verifyForward("getExtras");
        v.verifyForward("getFloat", 4);
        v.verifyForward("getInt", 5);
        v.verifyForward("getLong", 6);
        v.verifyForward("getPosition");
        v.verifyForward("getShort", 7);
        v.verifyForward("getString", 8);
        v.verifyForward("getWantsAllOnMoveCalls");
        v.verifyForward("isAfterLast");
        v.verifyForward("isBeforeFirst");
        v.verifyForward("isClosed");
        v.verifyForward("isFirst");
        v.verifyForward("isLast");
        v.verifyForward("isNull", 10);
        v.verifyForward("move", 11);
        v.verifyForward("moveToFirst");
        v.verifyForward("moveToLast");
        v.verifyForward("moveToNext");
        v.verifyForward("moveToPosition", 13);
        v.verifyForward("moveToPrevious");
        v.verifyForward("registerContentObserver", mock(ContentObserver.class));
        v.verifyForward("registerDataSetObserver", mock(DataSetObserver.class));
        v.verifyForward("requery");
        v.verifyForward("respond", mock(Bundle.class));
        v.verifyForward("setNotificationUri", mock(ContentResolver.class), mock(Uri.class));
        v.verifyForward("unregisterContentObserver", mock(ContentObserver.class));
        v.verifyForward("unregisterDataSetObserver", mock(DataSetObserver.class));

    }

    @Test
    public void getWrappedCursor() {
        Cursor mockCursor = mock(Cursor.class);
        CursorWrapper cursorWrapper = new CursorWrapper(mockCursor);
        ShadowCursorWrapper shadow = Robolectric.shadowOf(cursorWrapper);

        assertThat(shadow.getWrappedCursor(), is(sameInstance(mockCursor)));
    }

}
