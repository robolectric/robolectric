package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentManagerTest {

    public static final int CONTAINER_VIEW_ID = 888;
    private TestFragmentManager manager;
    private TestFragment fragment;
    private TestFragmentActivity activity;
    private ViewGroup containerView;

    @Before
    public void setUp() throws Exception {
        activity = new TestFragmentActivity();
        activity.onCreate(null);
        manager = (TestFragmentManager) activity.getSupportFragmentManager();
        fragment = new TestFragment();
        containerView = (ViewGroup) activity.findViewById(CONTAINER_VIEW_ID);
    }

    @Test
    public void shouldFindFragmentById() throws Exception {
        manager.addFragment(CONTAINER_VIEW_ID, "tag1", fragment, false);

        assertThat(manager.findFragmentById(CONTAINER_VIEW_ID), sameInstance((Fragment) fragment));
    }

    @Test
    public void shouldFindFragmentByTag() throws Exception {
        manager.addFragment(CONTAINER_VIEW_ID, "tag1", fragment, false);

        assertThat(manager.findFragmentByTag("tag1"), sameInstance((Fragment) fragment));
    }

    @Test
    public void addFragment_shouldCallLifecycleMethods() throws Exception {
        manager.addFragment(View.NO_ID, null, fragment, false);

        fragment.transcript.assertEventsSoFar(
                "onAttach",
                "onCreate"
        );

        assertSame(activity, fragment.onAttachActivity);
        assertSame(activity, fragment.getActivity());
    }

    @Test
    public void startFragment_shouldCallLifecycleMethods() throws Exception {
        manager.addFragment(View.NO_ID, null, fragment, false);
        fragment.transcript.clear();
        manager.startFragment(fragment);

        fragment.transcript.assertEventsSoFar(
                "onCreateView",
                "onViewCreated",
                "onActivityCreated",
                "onStart"
        );

        assertEquals(fragment.onCreateViewInflater, activity.getLayoutInflater());
        assertNotNull(fragment.getView());
    }

    @Test
    public void addFragment_shouldSetTheFragmentsView() throws Exception {
        manager.addFragment(View.NO_ID, null, fragment, false);

        assertThat(fragment.getView(), sameInstance(fragment.onCreateViewReturnValue));
    }

    @Test
    public void addFragment_shouldSetTheFragmentsTag() throws Exception {
        manager.addFragment(View.NO_ID, "expected tag", fragment, false);

        assertThat(fragment.getTag(), equalTo("expected tag"));
    }

    @Test
    public void startFragment_shouldInsertTheFragmentViewIntoTheContainerView() throws Exception {
        manager.addFragment(CONTAINER_VIEW_ID, null, fragment, false);
        manager.startFragment(fragment);

        View fragmentViewParent = (View) activity.findViewById(TestFragment.FRAGMENT_VIEW_ID).getParent();
        assertThat(activity.findViewById(TestFragment.FRAGMENT_VIEW_ID), sameInstance(fragment.onCreateViewReturnValue));
        assertThat(fragmentViewParent, sameInstance((View) containerView));
    }

    @Test
    public void addFragmentWithReplace_shouldEmptyTheContainer() throws Exception {
        containerView.addView(new Button(activity));
        assertEquals(1, containerView.getChildCount());

        manager.addFragment(CONTAINER_VIEW_ID, null, fragment, true);

        assertEquals(1, containerView.getChildCount());
    }

    @Test
    public void addFragmentWithReplace_withNoContainer_shouldNotThrow() throws Exception {
        manager.addFragment(0, null, fragment, true);
        // pass
    }

    @Test
    public void addFragment_shouldSetFragmentsActivity() throws Exception {
        manager.addFragment(0, null, fragment, false);

        assertSame(activity, fragment.getActivity());
    }

    @Test
    public void getFragment_whenBundleSavedByShadowFragmentActivity_shouldGetFragmentByTagFromBundle() throws Exception {
        manager.addFragment(CONTAINER_VIEW_ID, "fragment tag", fragment, true);

        Bundle outState = new Bundle();
        shadowOf(activity).onSaveInstanceState(outState);

        Fragment retrievedFragment = manager.getFragment(outState, "fragment tag");
        assertEquals(TestFragment.class, retrievedFragment.getClass());
    }

    @Test
    public void startFragment_shouldPassSavedInstanceStateToOnCreateMethodOfFragment() throws Exception {
        shadowOf(fragment).setSavedInstanceState(new Bundle());
        manager.addFragment(CONTAINER_VIEW_ID, null, fragment, true);
        manager.startFragment(fragment);

        assertTrue(fragment.onActivityCreated_savedInstanceState != null);
    }

    private static class TestFragmentActivity extends FragmentActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            View view = new FrameLayout(this);
            view.setId(CONTAINER_VIEW_ID);
            setContentView(view);
        }
    }

}
