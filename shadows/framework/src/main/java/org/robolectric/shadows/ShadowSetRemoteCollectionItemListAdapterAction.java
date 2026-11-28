package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.appwidget.AppWidgetHostView;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * The action {@code RemoteViews.setRemoteAdapter(int, Intent)} records when the framework converts
 * a collection's {@code RemoteViewsService} into items. On a device {@code AppWidgetManager} fills
 * the items in from the service before the views reach the host; here they are collected from the
 * service in-process when the action is applied without them, as {@link ShadowRemoteViewsAdapter}
 * drives the service for an unconverted collection.
 */
@Implements(
    className = "android.widget.RemoteViews$SetRemoteCollectionItemListAdapterAction",
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM)
public class ShadowSetRemoteCollectionItemListAdapterAction {

  @RealObject private Object realAction;

  /**
   * Collects the service's items when the action has only its intent, or would read them from the
   * collection cache, then applies them.
   *
   * <p>The framework only applies the items when the widget's view is inside an {@link
   * AppWidgetHostView}, and otherwise logs an error and leaves the collection as it was. A widget's
   * view in {@link ShadowAppWidgetManager} is applied with one as its parent but not kept in it, so
   * a reapply, as {@code notifyAppWidgetViewDataChanged} makes, would never reach the collection;
   * such an apply is given a host view to check against.
   */
  @Implementation
  protected void apply(View root, ViewGroup rootParent, RemoteViews.ActionApplyParams params) {
    ActionReflector action = reflector(ActionReflector.class, realAction);
    Intent serviceIntent = action.getServiceIntent();
    if (serviceIntent != null && (action.getItems() == null || action.getIntentId() != -1)) {
      RemoteViews.RemoteCollectionItems items =
          (RemoteViews.RemoteCollectionItems) ShadowRemoteViewsAdapter.collectItems(serviceIntent);
      if (items != null) {
        action.setItems(items);
        action.setIntentId(-1);
      }
    }
    if (!(rootParent instanceof AppWidgetHostView)) {
      rootParent = new AppWidgetHostView(root.getContext());
    }
    action.apply(root, rootParent, params);
  }

  @ForType(className = "android.widget.RemoteViews$SetRemoteCollectionItemListAdapterAction")
  interface ActionReflector {
    @Accessor("mItems")
    RemoteViews.RemoteCollectionItems getItems();

    @Accessor("mItems")
    void setItems(RemoteViews.RemoteCollectionItems items);

    @Accessor("mServiceIntent")
    Intent getServiceIntent();

    @Accessor("mIntentId")
    int getIntentId();

    @Accessor("mIntentId")
    void setIntentId(int intentId);

    @Direct
    void apply(View root, ViewGroup rootParent, RemoteViews.ActionApplyParams params);
  }
}
