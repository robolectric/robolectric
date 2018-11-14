package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Dialog.class)
public class ShadowDialog {

  @RealObject private Dialog realDialog;

  private boolean isShowing;
  Context context;
  private int layoutId;
  private int themeId;
  private View inflatedView;
  private boolean hasBeenDismissed;
  protected CharSequence title;
  private DialogInterface.OnCancelListener onCancelListener;
  private Window window;
  private Activity ownerActivity;
  private boolean hasShownBefore;
  private static final ArrayList<Dialog> shownDialogs = new ArrayList<>();
  private boolean isCancelableOnTouchOutside;

  @Resetter
  public static void reset() {
    setLatestDialog(null);
    shownDialogs.clear();
  }

  public static Dialog getLatestDialog() {
    ShadowDialog dialog = ShadowApplication.getInstance().getLatestDialog();
    return dialog == null ? null : dialog.realDialog;
  }

  public static void setLatestDialog(ShadowDialog latestDialog) {
    ShadowApplication shadowApplication = ShadowApplication.getInstance();
    if (shadowApplication != null) shadowApplication.setLatestDialog(latestDialog);
  }

  @Implementation
  protected void show() {
    setLatestDialog(this);
    shownDialogs.add(realDialog);
    directlyOn(realDialog, Dialog.class).show();
  }

  @Implementation
  protected void dismiss() {
    directlyOn(realDialog, Dialog.class).dismiss();
    hasBeenDismissed = true;
  }

  public void clickOn(int viewId) {
    realDialog.findViewById(viewId).performClick();
  }

  @Implementation
  protected void setCanceledOnTouchOutside(boolean flag) {
    isCancelableOnTouchOutside = flag;
    directlyOn(realDialog, Dialog.class).setCanceledOnTouchOutside(flag);
  }

  public boolean isCancelable() {
    return ReflectionHelpers.getField(realDialog, "mCancelable");
  }

  public boolean isCancelableOnTouchOutside() {
    return isCancelableOnTouchOutside;
  }

  public DialogInterface.OnCancelListener getOnCancelListener() {
    return onCancelListener;
  }

  @Implementation
  protected void setOnCancelListener(DialogInterface.OnCancelListener listener) {
    this.onCancelListener = listener;
    directlyOn(realDialog, Dialog.class).setOnCancelListener(listener);
  }

  public boolean hasBeenDismissed() {
    return hasBeenDismissed;
  }

  public CharSequence getTitle() {
    ShadowWindow shadowWindow = Shadow.extract(realDialog.getWindow());
    return shadowWindow.getTitle();
  }

  public void clickOnText(int textId) {
    if (inflatedView == null) {
      inflatedView = LayoutInflater.from(context).inflate(layoutId, null);
    }
    String text = realDialog.getContext().getResources().getString(textId);
    if (!clickOnText(inflatedView, text)) {
      throw new IllegalArgumentException("Text not found: " + text);
    }
  }

  public void clickOnText(String text) {
    if (!clickOnText(inflatedView, text)) {
      throw new IllegalArgumentException("Text not found: " + text);
    }
  }

  private boolean clickOnText(View view, String text) {
    ShadowView shadowView = Shadow.extract(view);
    if (text.equals(shadowView.innerText())) {
      view.performClick();
      return true;
    }
    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View child = viewGroup.getChildAt(i);
        if (clickOnText(child, text)) {
          return true;
        }
      }
    }
    return false;
  }

  public static List<Dialog> getShownDialogs() {
    return shownDialogs;
  }

  public void callOnCreate(Bundle bundle) {
    ReflectionHelpers.callInstanceMethod(realDialog, "onCreate", ClassParameter.from(Bundle.class, bundle));
  }
}
