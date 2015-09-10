package org.robolectric.shadows;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;

public class StubViewRoot implements ViewParent {

  @Override
  public void requestLayout() {
  }

  @Override
  public boolean isLayoutRequested() {
    return false;
  }

  @Override
  public void requestTransparentRegion(View child) {
  }

  @Override
  public void invalidateChild(View child, Rect r) {
  }

  @Override
  public ViewParent invalidateChildInParent(int[] location, Rect r) {
    return null;
  }

  @Override
  public ViewParent getParent() {
    return null;
  }

  @Override
  public void requestChildFocus(View child, View focused) {
  }

  @Override
  public void recomputeViewAttributes(View child) {
  }

  @Override
  public void clearChildFocus(View child) {
  }

  @Override
  public boolean getChildVisibleRect(View child, Rect r, Point offset) {
    return false;
  }

  @Override
  public View focusSearch(View v, int direction) {
    return null;
  }

  @Override
  public void bringChildToFront(View child) {
  }

  @Override
  public void focusableViewAvailable(View v) {
  }

  @Override
  public boolean showContextMenuForChild(View originalView) {
    return false;
  }

  @Override
  public void createContextMenu(ContextMenu menu) {
  }

  @Override
  public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
    return null;
  }

  @Override
  public void childDrawableStateChanged(View child) {
  }

  @Override
  public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
  }

  @Override
  public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
    return false;
  }

  @Override
  public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
    return false;
  }

  @Override
  public void childHasTransientStateChanged(View child, boolean hasTransientState) {

  }

  @Override
  public void requestFitSystemWindows() {
  }

  @Override
  public ViewParent getParentForAccessibility() {
    return null;
  }

  @Override
  public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
  }

  @Override
  public boolean canResolveLayoutDirection() {
    return false;
  }

  @Override
  public boolean isLayoutDirectionResolved() {
    return false;
  }

  @Override
  public int getLayoutDirection() {
    return 0;
  }

  @Override
  public boolean canResolveTextDirection() {
    return false;
  }

  @Override
  public boolean isTextDirectionResolved() {
    return false;
  }

  @Override
  public int getTextDirection() {
    return 0;
  }

  @Override
  public boolean canResolveTextAlignment() {
    return false;
  }

  @Override
  public boolean isTextAlignmentResolved() {
    return false;
  }

  @Override
  public int getTextAlignment() {
    return 0;
  }

  @Override
  public boolean onStartNestedScroll(View view, View view1, int i) {
    return false;
  }

  @Override
  public void onNestedScrollAccepted(View view, View view1, int i) {

  }

  @Override
  public void onStopNestedScroll(View view) {

  }

  @Override
  public void onNestedScroll(View view, int i, int i1, int i2, int i3) {

  }

  @Override
  public void onNestedPreScroll(View view, int i, int i1, int[] ints) {

  }

  @Override
  public boolean onNestedFling(View view, float v, float v1, boolean b) {
    return false;
  }

  @Override
  public boolean onNestedPreFling(View view, float v, float v1) {
    return false;
  }

  @Override
  public boolean onNestedPrePerformAccessibilityAction(View view, int i, Bundle bundle) {
    return false;
  }
}
