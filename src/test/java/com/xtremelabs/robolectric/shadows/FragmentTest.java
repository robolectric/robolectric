package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
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

        String hello = fragment.getString(id);
        assertEquals("Hello", hello);
    }

    @Test
    public void getString_returnsStringResource() throws Exception {
        assertEquals("Howdy", fragment.getString(R.string.howdy));
    }

    @Test(expected = IllegalStateException.class)
    public void unattachedFragmentsCannotGetResources() throws Exception {
        new TestFragment().getResources();
    }

    @Test(expected = IllegalStateException.class)
    public void unattachedFragmentsCannotGetStrings() throws Exception {
        new TestFragment().getString(R.string.howdy);
    }
}
