package org.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import org.robolectric.R;
import org.robolectric.TestRunners;
import org.robolectric.tester.android.util.TestFragmentManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class FragmentActivityTest {

    private TestFragmentActivity activity;
    private TestFragment fragment;

    @Before
    public void setUp() throws Exception {
        activity = new TestFragmentActivity();
        activity.onCreate(null);
        fragment = (TestFragment) activity.getSupportFragmentManager().findFragmentByTag("fragment_tag");
    }

    @Test
    public void shouldHaveAFragmentManager() throws Exception {
        assertNotNull(activity.getSupportFragmentManager());
    }

    @Test
    public void viewLoader_shouldInflateFragment() throws Exception {
        assertEquals(TestFragment.class, fragment.getClass());
    }

    @Test
    public void viewLoader_shouldSetFragmentId() throws Exception {
        Fragment fragmentById = activity.getSupportFragmentManager().findFragmentById(R.id.fragment);
        assertSame(fragment, fragmentById);
    }

    @Test
    public void viewLoader_shouldInsertFragmentViewIntoLayout() throws Exception {
        assertSame(fragment.onCreateViewReturnValue, activity.findViewById(TestFragment.FRAGMENT_VIEW_ID));
    }

    @Test
    public void viewLoader_shouldSetFragmentsActivity() throws Exception {
        assertSame(activity, fragment.getActivity());
    }

    @Test
    public void viewLoader_shouldCreateContainerView() throws Exception {
        ViewGroup container = (ViewGroup) activity.findViewById(R.id.fragment);
        assertNotNull(container);
    }

    @Test
    public void viewLoader_shouldInsertFragmentViewIntoContainer() throws Exception {
        ViewGroup container = (ViewGroup) activity.findViewById(R.id.fragment);
        View fragmentView = container.findViewById(TestFragment.FRAGMENT_VIEW_ID);
        assertSame(fragment.onCreateViewReturnValue, fragmentView);
    }

    @Test
    @Ignore("Seems to be broken by 'Android Support' rev 8")
    public void onSaveInstanceState_shouldStoreListOfFragments() throws Exception {
        Fragment fragment = new TestFragment();
        int fragment_container = R.id.dynamic_fragment_container;
        activity.getSupportFragmentManager().beginTransaction().add(fragment_container, fragment).commit();
        Bundle outState = new Bundle();
        activity.onSaveInstanceState(outState);

        assertTrue(outState.containsKey(ShadowFragmentActivity.FRAGMENTS_TAG));

        Object[] states = (Object[]) outState.getSerializable(ShadowFragmentActivity.FRAGMENTS_TAG);
        SerializedFragmentState fragmentState = (SerializedFragmentState) states[1];

        assertEquals(fragmentState.id, fragment.getId());
        assertEquals(fragmentState.tag, fragment.getTag());
        assertEquals(fragmentState.fragmentClass, fragment.getClass());
        assertEquals(fragmentState.containerId, fragment_container);
    }

    @Test
    public void onSaveInstanceState_shouldCallOnSaveInstanceStateOnFragments() throws Exception {
        TestFragment fragment = new TestFragment();
        int fragment_container = R.id.dynamic_fragment_container;
        activity.getSupportFragmentManager().beginTransaction().add(fragment_container, fragment).commit();
        Bundle outState = new Bundle();
        activity.onSaveInstanceState(outState);

        assertTrue(fragment.onSaveInstanceStateWasCalled);
    }

    @Test
    public void onCreate_shouldRecreateFragments() throws Exception {
        Bundle bundle = new Bundle();
        TestFragment dynamicFrag = new TestFragment();
        int containerId = 123;
        SerializedFragmentState fragmentState = new SerializedFragmentState(containerId, dynamicFrag);
        bundle.putSerializable(ShadowFragmentActivity.FRAGMENTS_TAG, new Object[]{fragmentState});

        activity = new TestFragmentActivity();
        activity.onCreate(bundle);
        TestFragmentManager fragmentManager = (TestFragmentManager) activity.getSupportFragmentManager();
        assertEquals(2, fragmentManager.getFragments().size());

        TestFragment restoredFrag = (TestFragment) fragmentManager.getFragments().get(containerId);
        assertEquals(restoredFrag.getId(), dynamicFrag.getId());
        assertEquals(restoredFrag.getTag(), dynamicFrag.getTag());
        assertEquals(bundle, shadowOf(restoredFrag).getSavedInstanceState());
        assertSame(activity, restoredFrag.onAttachActivity);
        assertSame(activity, restoredFrag.getActivity());
        assertNull(restoredFrag.getView());
    }

    @Test
    public void onStart_shouldStartFragments() throws Exception {
        Bundle bundle = new Bundle();
        TestFragment dynamicFrag = new TestFragment();
        int containerId = 123;
        SerializedFragmentState fragmentState = new SerializedFragmentState(containerId, dynamicFrag);
        bundle.putSerializable(ShadowFragmentActivity.FRAGMENTS_TAG, new Object[]{fragmentState});

        activity = new TestFragmentActivity();
        activity.onCreate(bundle);
        shadowOf(activity).callOnStart();
        TestFragmentManager fragmentManager = (TestFragmentManager) activity.getSupportFragmentManager();
        assertEquals(2, fragmentManager.getFragments().size());
        TestFragment restoredFrag = (TestFragment) fragmentManager.getFragments().get(containerId);

        assertEquals(restoredFrag.onCreateViewInflater, activity.getLayoutInflater());
        assertNotNull(restoredFrag.getView());
    }

    @Test
    public void onPause_shouldPauseTheFragment() throws Exception {
        activity.onPause();
        assertTrue(fragment.onPauseWasCalled);
    }

    private static class TestFragmentActivity extends FragmentActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_activity);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onPause() {
            super.onPause();
        }
    }
}
