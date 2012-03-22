package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
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

    private static class TestFragmentActivity extends FragmentActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.fragment_activity);
        }
    }
}
