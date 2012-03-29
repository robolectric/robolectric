package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentTransaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentTransactionTest {

    private MockTestFragmentManager manager;
    private Fragment fragment;
    private FragmentTransaction txn;

    @Before
    public void setUp() throws Exception {
        manager = new MockTestFragmentManager();
        txn = new TestFragmentTransaction(manager);
        fragment = new TestFragment();
    }

    @Test
    public void addWithId_commit_shouldCallFragmentManager() throws Exception {
        txn.add(111, fragment).commit();

        assertTrue(manager.addFragmentWasCalled);
        assertEquals(manager.addFragmentContainerViewId, 111);
        assertEquals(manager.addFragmentTag, null);
        assertSame(manager.addFragmentFragment, fragment);
        assertFalse(manager.addFragmentReplace);
    }

    @Test
    public void addWithIdAndTag_commit_shouldCallFragmentManager() throws Exception {
        txn.add(111, fragment, "tag1").commit();

        assertTrue(manager.addFragmentWasCalled);
        assertEquals(manager.addFragmentContainerViewId, 111);
        assertEquals(manager.addFragmentTag, "tag1");
        assertSame(manager.addFragmentFragment, fragment);
        assertFalse(manager.addFragmentReplace);
    }

    @Test
    public void addWithTag_commit_shouldCallFragmentManager() throws Exception {
        txn.add(fragment, "tag1").commit();

        assertTrue(manager.addFragmentWasCalled);
        assertEquals(manager.addFragmentContainerViewId, View.NO_ID);
        assertEquals(manager.addFragmentTag, "tag1");
        assertSame(manager.addFragmentFragment, fragment);
        assertFalse(manager.addFragmentReplace);
    }

    @Test
    public void replaceWithId_commit_shouldCallFragmentManager() throws Exception {
        txn.replace(111, fragment).commit();

        assertTrue(manager.addFragmentWasCalled);
        assertEquals(manager.addFragmentContainerViewId, 111);
        assertEquals(manager.addFragmentTag, null);
        assertSame(manager.addFragmentFragment, fragment);
        assertTrue(manager.addFragmentReplace);
    }

    @Test
    public void replaceWithIdAndTag_commit_shouldCallFragmentManager() throws Exception {
        txn.replace(111, fragment, "tag1").commit();

        assertTrue(manager.addFragmentWasCalled);
        assertEquals(manager.addFragmentContainerViewId, 111);
        assertEquals(manager.addFragmentTag, "tag1");
        assertSame(manager.addFragmentFragment, fragment);
        assertTrue(manager.addFragmentReplace);
    }

    private static class MockTestFragmentManager extends TestFragmentManager {
        private boolean addFragmentWasCalled;
        private int addFragmentContainerViewId;
        private String addFragmentTag;
        private Fragment addFragmentFragment;
        private boolean addFragmentReplace;

        public MockTestFragmentManager() {
            super(new FragmentActivity());
        }

        @Override
        public void addFragment(int containerViewId, String tag, Fragment fragment, boolean replace) {
            addFragmentWasCalled = true;
            addFragmentContainerViewId = containerViewId;
            addFragmentTag = tag;
            addFragmentFragment = fragment;
            addFragmentReplace = replace;
        }
    }
}
