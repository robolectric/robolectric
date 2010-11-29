package com.xtremelabs.robolectric.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;

public abstract class TestUtil {
    public static Map<String, String> mapOf() {
        return new HashMap<String, String>();
    }

    public static <T> Map<String, T> mapOf(String key, T value, String... keyOrValue) {
        Map<String, T> map = new HashMap<String, T>();
        map.put(key, value);
        for (int i = 0; i < keyOrValue.length; i += 2) {
            //noinspection unchecked
            map.put(keyOrValue[i], (T) keyOrValue[i + 1]);
        }
        return map;
    }

    public static <K, V> Map<K, V> mapOf(Pair<K, V>... pairs) {
        HashMap<K, V> map = new HashMap<K, V>();
        for (Pair<K, V> pair : pairs) {
            map.put(pair.key, pair.value);
        }
        return map;
    }

    public static class Pair<K, V> {
        K key;
        V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public static <K, V> Pair<K, V> pair(K key, V value) {
        return new Pair<K, V>(key, value);
    }

    public static void assertNoIntentsStarted(Activity activity) {
        verify(activity, never()).startActivity(Matchers.<Intent>any());
    }

    public static Intent getIntent(Activity activity) {
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivity(intentArgumentCaptor.capture());
        return intentArgumentCaptor.getValue();
    }

    public static <T extends View> T bindView(ViewGroup parent, int id, final T view) {
        when(parent.findViewById(id)).thenReturn(view);
        wireUpOnClick(view);
        return view;
    }

    public static <T extends View> T findView(ViewGroup parent, int id, final Class<T> viewClass) {
        //noinspection unchecked
        T view = (T) parent.findViewById(id);
        if (view == null) {
            throw new RuntimeException("couldn't find view");
        }
        return view;
    }

    public static void wireUpOnClick(final View view) {
        final ArgumentRecordingAnswer onClickArgs = new ArgumentRecordingAnswer();
        doAnswer(onClickArgs).when(view).setOnClickListener(Matchers.<View.OnClickListener>anyObject());
        when(view.performClick()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (onClickArgs.arguments != null && onClickArgs.arguments[0] != null) {
                    View.OnClickListener onClickListener = (View.OnClickListener) onClickArgs.arguments[0];
                    onClickListener.onClick(view);
                }
                return false;
            }
        });
    }

    public static void wireUpOnItemClick(final AbsSpinner view) {
        final ArgumentRecordingAnswer onClickArgs = new ArgumentRecordingAnswer();
        doAnswer(onClickArgs).when(view).setOnItemClickListener(any(AdapterView.OnItemClickListener.class));
        when(view.performItemClick(any(View.class), anyInt(), anyLong())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ((AdapterView.OnItemClickListener) onClickArgs.arguments[0]).onItemClick(null, null, 0, 0);
                return false;
            }
        });
    }

    public static <T extends View> T bindView(int id, final T view, Activity activity) {
        when(activity.findViewById(id)).thenReturn(view);
        wireUpOnClick(view);
        if (view instanceof AbsSpinner) {
            wireUpOnItemClick((AbsSpinner) view);
        }
        return view;
    }

    public static void assertUnorderedEquals(List expected, List actual) {
        HashSet expectedSet = new HashSet();
        expectedSet.addAll(expected);

        HashSet actualSet = new HashSet();
        actualSet.addAll(actual);

        assertEquals(expectedSet, actualSet);
    }

    public static void assertEquals(Collection<?> expected, Collection<?> actual) {
        org.junit.Assert.assertEquals(stringify(expected), stringify(actual));
    }

    public static String stringify(Collection<?> collection) {
        StringBuilder buf = new StringBuilder();
        for (Object o : collection) {
            if (buf.length() > 0) buf.append("\n");
            buf.append(o);
        }
        return buf.toString();
    }

    public static <T> void assertInstanceOf(Class<? extends T> expectedClass, T object) {
        Class actualClass = object.getClass();
        assertTrue(expectedClass + " should be assignable from " + actualClass,
                expectedClass.isAssignableFrom(actualClass));
    }

    public static class ArgumentRecordingAnswer implements Answer {
        private Object[] arguments;

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            arguments = invocationOnMock.getArguments();
            return null;
        }
    }
}
