package org.robolectric.shadows;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.HiddenApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.fest.reflect.core.Reflection.method;

/**
 * Shadow implementation for Android compatibility library's DrawerLayout.
 */
@SuppressWarnings("unused")
@Implements(value = DrawerLayout.class)
public class ShadowDrawerLayout extends ShadowViewGroup {
  @RealObject
  protected DrawerLayout realDrawerLayout;

  protected View drawerView;
  protected boolean isOpen;

  @Implementation
  public boolean isDrawerOpen(int drawerGravity){
    return false;
  }

  @Implementation
  public void addView(View viewToAdd){
    super.addView(viewToAdd, realDrawerLayout.getChildCount(), viewToAdd.getLayoutParams());
    if(realDrawerLayout.getChildCount() > 0 && drawerView == null){
      drawerView = viewToAdd;
    }
  }

  @Implementation
  public boolean isDrawerOpen(View drawer){
    return isOpen;
  }

  @Implementation
  public void openDrawer(View drawerView){
    isOpen = true;
  }

  @Implementation
  public void closeDrawers (){
    isOpen = false;
  }

  @Implementation
  public void closeDrawer(View drawerView){
    closeDrawers();
  }

}
