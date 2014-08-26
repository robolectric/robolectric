package org.robolectric.shadows;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.Spinner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AbsSpinnerTest {
  private Context context;
  private AdapterView adapterView;
  private Spinner spinner;
  private ShadowAbsSpinner shadowSpinner;
  private ArrayAdapter<String> arrayAdapter;

  @Before
  public void setUp() throws Exception {
    context = Robolectric.application;
    adapterView = new Gallery(context);
    spinner = new Spinner(context);
    shadowSpinner = (ShadowAbsSpinner) shadowOf(spinner);
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

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
  @Test (expected = IndexOutOfBoundsException.class)
  public void getSelectedItemShouldThrowException_EmptyArray(){
    spinner.setAdapter(new MyArrayAdapter(context, new String[]{}));
    spinner.getSelectedItem();
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
