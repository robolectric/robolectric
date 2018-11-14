package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class ShadowAdapterView<T extends Adapter> extends ShadowViewGroup {
  private static int ignoreRowsAtEndOfList = 0;

  @RealObject
  private AdapterView<T> realAdapterView;

  private AdapterView.OnItemSelectedListener itemSelectedListener;

  @Implementation
  protected void setOnItemSelectedListener(
      AdapterView.OnItemSelectedListener itemSelectedListener) {
    this.itemSelectedListener = itemSelectedListener;
    directlyOn(realAdapterView, AdapterView.class, "setOnItemSelectedListener", ClassParameter.from(AdapterView.OnItemSelectedListener.class, itemSelectedListener));
  }

  public AdapterView.OnItemSelectedListener getItemSelectedListener() {
    return itemSelectedListener;
  }

  public boolean performItemClick(int position) {
    return realAdapterView.performItemClick(realAdapterView.getChildAt(position),
        position, realAdapterView.getItemIdAtPosition(position));
  }

  public int findIndexOfItemContainingText(String targetText) {
    for (int i = 0; i < realAdapterView.getCount(); i++) {
      View childView = realAdapterView.getAdapter().getView(i, null, new FrameLayout(realAdapterView.getContext()));
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
