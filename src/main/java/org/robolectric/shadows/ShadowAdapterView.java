package org.robolectric.shadows;

import android.view.View;
import android.widget.AdapterView;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class ShadowAdapterView extends ShadowViewGroup {
  private static int ignoreRowsAtEndOfList = 0;
  private static boolean automaticallyUpdateRowViews = true;

  @RealObject
  private AdapterView realAdapterView;


  /**
   * Check if our adapter's items have changed without {@code onChanged()} or {@code onInvalidated()} having been called.
   *
   * @deprecated No longer supported.
   * @return true if the object is valid, false if not
   * @throws RuntimeException if the items have been changed without notification
   */
  public boolean checkValidity() {
    throw new UnsupportedOperationException();
  }

  /**
   * Use this static method to turn off the feature of this class which calls getView() on all of the
   * adapter's rows in setAdapter() and after notifyDataSetChanged() or notifyDataSetInvalidated() is
   * called on the adapter. This feature is turned on by default. This sets a static on the class, so
   * set it back to true at the end of your test to avoid test pollution.
   *
   * @param shouldUpdate false to turn off the feature, true to turn it back on
   * @deprecated Not supported as of Robolectric 2.0-alpha-3.
   */
  public static void automaticallyUpdateRowViews(boolean shouldUpdate) {
    automaticallyUpdateRowViews = shouldUpdate;
  }

  public boolean performItemClick(int position) {
    return realAdapterView.performItemClick(realAdapterView.getChildAt(position),
        position, realAdapterView.getItemIdAtPosition(position));
  }

  public int findIndexOfItemContainingText(String targetText) {
    for (int i = 0; i < realAdapterView.getChildCount(); i++) {
      View childView = realAdapterView.getChildAt(i);
      String innerText = shadowOf(childView).innerText();
      if (innerText.contains(targetText)) {
        return i;
      }
    }
    return -1;
  }

  public View findItemContainingText(String targetText) {
    int itemIndex = findIndexOfItemContainingText(targetText);
    if (itemIndex == -1) {
      return null;
    }
    return realAdapterView.getChildAt(itemIndex);
  }

  public void clickFirstItemContainingText(String targetText) {
    int itemIndex = findIndexOfItemContainingText(targetText);
    if (itemIndex == -1) {
      throw new IllegalArgumentException("No item found containing text \"" + targetText + "\"");
    }
    performItemClick(itemIndex);
  }

  public void populateItems() {
    realView.measure(0, 0);
    realView.layout(0, 0, 100, 10000);
  }
}
