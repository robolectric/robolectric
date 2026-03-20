package org.robolectric.shadows;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class ShadowAdapterView<T extends Adapter> extends ShadowViewGroup {

  @RealObject private AdapterView<T> realAdapterView;

  private AdapterView.OnItemSelectedListener itemSelectedListener;

  @Filter(order = Order.BEFORE)
  protected void setOnItemSelectedListener(
      AdapterView.OnItemSelectedListener itemSelectedListener) {
    this.itemSelectedListener = itemSelectedListener;
  }

  public AdapterView.OnItemSelectedListener getItemSelectedListener() {
    return itemSelectedListener;
  }

  public boolean performItemClick(int position) {
    return realAdapterView.performItemClick(
        realAdapterView.getChildAt(position),
        position,
        realAdapterView.getItemIdAtPosition(position));
  }

  public int findIndexOfItemContainingText(String targetText) {
    for (int i = 0; i < realAdapterView.getCount(); i++) {
      View childView =
          realAdapterView
              .getAdapter()
              .getView(i, null, new FrameLayout(realAdapterView.getContext()));
      ShadowView shadowView = Shadow.extract(childView);
      String innerText = shadowView.innerText();
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

  public void selectItemWithText(String s) {
    int itemIndex = findIndexOfItemContainingText(s);
    realAdapterView.setSelection(itemIndex);
  }
}
