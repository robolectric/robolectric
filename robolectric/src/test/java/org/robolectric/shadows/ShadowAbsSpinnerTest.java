package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowAbsSpinnerTest {
  private Spinner spinner;
  private ShadowAbsSpinner shadowSpinner;
  private ArrayAdapter<String> arrayAdapter;

  @Before
  public void setUp() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    Context context = ApplicationProvider.getApplicationContext();
    spinner = new Spinner(activity);
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(spinner);
    shadowSpinner = shadowOf(spinner);
    String[] testItems = {"foo", "bar"};
    arrayAdapter = new MyArrayAdapter(context, testItems);
  }

  @Test
  public void checkSetAdapter() {
    spinner.setAdapter(arrayAdapter);
  }

  @Test
  public void getSelectedItemShouldReturnCorrectValue() {
    spinner.setAdapter(arrayAdapter);
    spinner.setSelection(0);
    assertThat((String) spinner.getSelectedItem()).isEqualTo("foo");
    assertThat((String) spinner.getSelectedItem()).isNotEqualTo("bar");

    spinner.setSelection(1);
    assertThat((String) spinner.getSelectedItem()).isEqualTo("bar");
    assertThat((String) spinner.getSelectedItem()).isNotEqualTo("foo");
  }

  @Test
  public void getSelectedItemShouldReturnNull_NoAdapterSet() {
    assertThat(spinner.getSelectedItem()).isNull();
  }

  @Test
  public void setSelectionWithAnimatedTransition() {
    spinner.setAdapter(arrayAdapter);
    spinner.setSelection(0, true);

    assertThat((String) spinner.getSelectedItem()).isEqualTo("foo");
    assertThat((String) spinner.getSelectedItem()).isNotEqualTo("bar");

    assertThat(shadowSpinner.isAnimatedTransition()).isTrue();
  }

  @Test
  public void useRealSelection_doesNotCauseInfiniteLoop() {
    try {
      System.setProperty("robolectric.useRealSpinnerSelection", "true");
      final AtomicBoolean itemSelected = new AtomicBoolean(false);
      spinner.setOnItemSelectedListener(
          new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              itemSelected.set(true);
              // This will result in an stack overflow if the fake selection ShadowAbsSpinner
              // selection logic is used.
              spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
          });
      spinner.setAdapter(arrayAdapter);
      spinner.setSelection(1);
      shadowOf(Looper.getMainLooper()).idle();
      assertThat(itemSelected.get()).isTrue();
    } finally {
      System.clearProperty("robolectric.useRealSpinnerSelection");
    }
  }

  @Test
  public void useRealSelection_callbackCalledOnce() {
    try {
      System.setProperty("robolectric.useRealSpinnerSelection", "true");
      final AtomicInteger invocations = new AtomicInteger(0);
      spinner.setOnItemSelectedListener(
          new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              invocations.incrementAndGet();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
          });
      spinner.setAdapter(arrayAdapter);
      spinner.setSelection(1);
      shadowOf(Looper.getMainLooper()).idle();
      assertThat(invocations.get()).isEqualTo(1);
    } finally {
      System.clearProperty("robolectric.useRealSpinnerSelection");
    }
  }

  private static class MyArrayAdapter extends ArrayAdapter<String> {
    public MyArrayAdapter(Context context, String[] testItems) {
      super(context, android.R.layout.simple_spinner_item, testItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return new View(getContext());
    }
  }
}
