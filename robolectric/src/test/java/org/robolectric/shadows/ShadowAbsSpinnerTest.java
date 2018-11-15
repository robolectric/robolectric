package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAbsSpinnerTest {
  private Context context;
  private Spinner spinner;
  private ShadowAbsSpinner shadowSpinner;
  private ArrayAdapter<String> arrayAdapter;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    spinner = new Spinner(context);
    shadowSpinner = shadowOf(spinner);
    String [] testItems = {"foo", "bar"};
    arrayAdapter = new MyArrayAdapter(this.context, testItems);
  }

  @Test
  public void checkSetAdapter() {
    spinner.setAdapter(arrayAdapter);
  }

  @Test
  public void getSelectedItemShouldReturnCorrectValue(){
    spinner.setAdapter(arrayAdapter);
    spinner.setSelection(0);
    assertThat((String) spinner.getSelectedItem()).isEqualTo("foo");
    assertThat((String) spinner.getSelectedItem()).isNotEqualTo("bar");

    spinner.setSelection(1);
    assertThat((String) spinner.getSelectedItem()).isEqualTo("bar");
    assertThat((String) spinner.getSelectedItem()).isNotEqualTo("foo");
  }

  @Test
  public void getSelectedItemShouldReturnNull_NoAdapterSet(){
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

  private static class MyArrayAdapter extends ArrayAdapter<String> {
    public MyArrayAdapter(Context context, String[] testItems) {
      super(context, android.R.layout.simple_spinner_item, testItems);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      return new View(getContext());
    }
  }
}
