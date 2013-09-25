package org.robolectric.util;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(TestRunners.WithDefaults.class)
public class FragmentTestUtilTest {
  @Test
  public void shouldStartFragment() {
    final LoginFragment fragment = new LoginFragment();
    startFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void shouldStartSupportFragment() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  private static class LoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }

  private static class LoginSupportFragment extends android.support.v4.app.Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }
}

