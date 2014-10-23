package org.robolectric.shadows;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.Field;

import static org.robolectric.Robolectric.*;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlertDialog.class)
public class ShadowAlertDialog extends ShadowDialog {
  @RealObject
  private AlertDialog realAlertDialog;

  private CharSequence[] items;
  private DialogInterface.OnClickListener clickListener;
  private boolean isMultiItem;
  private boolean isSingleItem;
  private DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener;
  private FrameLayout custom;

  /**
   * Non-Android accessor.
   *
   * @return the most recently created {@code AlertDialog}, or null if none has been created during this test run
   */
  public static AlertDialog getLatestAlertDialog() {
    ShadowAlertDialog dialog = Robolectric.getShadowApplication().getLatestAlertDialog();
    return dialog == null ? null : dialog.realAlertDialog;
  }

  public FrameLayout getCustomView() {
    if (custom == null) {
      custom = new FrameLayout(context);
    }
    return custom;
  }

  /**
   * Resets the tracking of the most recently created {@code AlertDialog}
   */
  public static void reset() {
    getShadowApplication().setLatestAlertDialog(null);
  }

  /**
   * Simulates a click on the {@code Dialog} item indicated by {@code index}. Handles both multi- and single-choice dialogs, tracks which items are currently
   * checked and calls listeners appropriately.
   *
   * @param index the index of the item to click on
   */
  public void clickOnItem(int index) {
    shadowOf(realAlertDialog.getListView()).performItemClick(index);
  }

  @Override public CharSequence getTitle() {
    return getShadowAlertController().getTitle();
  }

  /**
   * Non-Android accessor.
   *
   * @return the items that are available to be clicked on
   */
  public CharSequence[] getItems() {
    Adapter adapter = getShadowAlertController().getAdapter();
    int count = adapter.getCount();
    CharSequence[] items = new CharSequence[count];
    for (int i = 0; i < items.length; i++) {
      items[i] = (CharSequence) adapter.getItem(i);
    }
    return items;
  }

  public Adapter getAdapter() {
    return getShadowAlertController().getAdapter();
  }

  /**
   * Non-Android accessor.
   *
   * @return the message displayed in the dialog
   */
  public CharSequence getMessage() {
    return getShadowAlertController().getMessage();
  }

  @Implementation
  public void show() {
    super.show();
    getShadowApplication().setLatestAlertDialog(this);
  }

  /**
   * Non-Android accessor.
   *
   * @return return the view set with {@link AlertDialog.Builder#setView(View)}
   */
  public View getView() {
    return getShadowAlertController().getView();
  }

  /**
   * Non-Android accessor.
   *
   * @return return the view set with {@link AlertDialog.Builder#setCustomTitle(View)}
   */
  public View getCustomTitleView() {
    return getShadowAlertController().getCustomTitleView();
  }

  public ShadowAlertController getShadowAlertController() {
    try {
      Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
      mAlert.setAccessible(true);
      return shadowOf_(mAlert.get(realAlertDialog));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Shadows the {@code android.app.AlertDialog.Builder} class.
   */
  @Implements(AlertDialog.Builder.class)
  public static class ShadowBuilder {
  }
}
