package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentTest {
    private Fragment fragment;
    private FragmentActivity fragmentActivity;

    @Before
    public void setUp() throws Exception {
        fragmentActivity = new FragmentActivity();
        fragment = new TestFragment();
        fragmentActivity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();
    }

    @Test
    public void retrieveIdOfResource() {
        int id = fragment.getResources().getIdentifier("hello", "string", "com.xtremelabs.robolectric");
        assertTrue(id > 0);

        String hello = fragment.getResources().getString(id);
        assertEquals("Hello", hello);
    }

    @Test(expected = IllegalStateException.class)
    public void unattachedFragmentsCannotGetResources() throws Exception {
        new TestFragment().getResources();
    }
}
