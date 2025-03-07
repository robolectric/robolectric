package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static java.util.Objects.requireNonNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.app.ActivityThread;
import android.app.AppComponentFactory;
import android.app.LoadedApk;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@code RemoteViews} that simulates its implementation. Supports setting remote
 * adapters, etc...
 */
@Implements(RemoteViews.class)
public class ShadowRemoteViews {

  @RealObject private RemoteViews realRemoteViews;

  private final List<ViewUpdater> viewUpdaters = new ArrayList<>();

  @Implementation
  protected void reapply(Context context, View v) {
    reflector(RemoteViewsReflector.class, realRemoteViews).reapply(context, v);
    for (ViewUpdater viewUpdater : viewUpdaters) {
      viewUpdater.update(v);
    }
  }

  @SuppressWarnings("unchecked")
  @Implementation
  protected void setRemoteAdapter(int viewId, Intent intent) {
    reflector(RemoteViewsReflector.class, realRemoteViews).setRemoteAdapter(viewId, intent);
    viewUpdaters.add(
        new ViewUpdater(viewId) {
          @Override
          public void doUpdate(View view) {
            requireNonNull(intent, "The intent passed to setRemoteAdapter cannot be null.");
            requireNonNull(
                intent.getComponent(),
                "The intent passed to setRemoteAdapter must have a valid component.");
            BaseAdapter adapter = createAdapterFromIntent(intent);
            ((AdapterView) view).setAdapter(adapter);
          }
        });
  }

  private static BaseAdapter createAdapterFromIntent(Intent intent) {
    Class<?> clazz = extractClass(intent);
    RemoteViewsService remoteViewsService = createRemoteViewsService(intent, clazz);
    RemoteViewsFactory remoteViewsFactory = remoteViewsService.onGetViewFactory(intent);
    remoteViewsFactory.onCreate();
    return convertToAdapter(remoteViewsFactory);
  }

  @SuppressWarnings("unchecked")
  private static RemoteViewsService createRemoteViewsService(Intent intent, Class<?> clazz) {
    return buildService((Class<? extends RemoteViewsService>) clazz, intent).create().bind().get();
  }

  private static Class<?> extractClass(Intent intent) {
    Class<?> clazz;
    try {
      clazz = Class.forName(intent.getComponent().getClassName());
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Invalid class name provided for the intent.", e);
    }
    return clazz;
  }

  private static BaseAdapter convertToAdapter(
      RemoteViewsService.RemoteViewsFactory remoteViewsFactory) {
    return new BaseAdapter() {
      @Override
      public void registerDataSetObserver(DataSetObserver observer) {}

      @Override
      public void unregisterDataSetObserver(DataSetObserver observer) {}

      @Override
      public int getCount() {
        return remoteViewsFactory.getCount();
      }

      @Override
      public Object getItem(int position) {
        return remoteViewsFactory.getViewAt(position);
      }

      @Override
      public long getItemId(int position) {
        return remoteViewsFactory.getItemId(position);
      }

      @Override
      public boolean hasStableIds() {
        return remoteViewsFactory.hasStableIds();
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        RemoteViews remoteViews = remoteViewsFactory.getViewAt(position);
        View view = remoteViews.apply(parent.getContext(), parent);
        remoteViews.reapply(parent.getContext(), view);
        return view;
      }

      @Override
      public int getViewTypeCount() {
        return remoteViewsFactory.getViewTypeCount();
      }

      @Override
      public boolean isEmpty() {
        return remoteViewsFactory.getCount() == 0;
      }
    };
  }

  private static <T extends Service> ServiceController<T> buildService(
      Class<T> serviceClass, Intent intent) {
    return ServiceController.of(instantiateService(serviceClass, intent), intent);
  }

  @SuppressWarnings({"NewApi", "unchecked"})
  private static <T extends Service> T instantiateService(Class<T> serviceClass, Intent intent) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
      final LoadedApk loadedApk = getLoadedApk();
      AppComponentFactory factory = getAppComponentFactory(loadedApk);
      if (factory != null) {
        try {
          Service instance =
              factory.instantiateService(
                  loadedApk.getClassLoader(), serviceClass.getName(), intent);
          if (instance != null && serviceClass.isAssignableFrom(instance.getClass())) {
            return (T) instance;
          }
        } catch (ReflectiveOperationException e) {
          Logger.debug("Failed to instantiate Service using AppComponentFactory", e);
        }
      }
    }
    return ReflectionHelpers.callConstructor(serviceClass);
  }

  @Nullable
  @RequiresApi(api = P)
  private static AppComponentFactory getAppComponentFactory(final LoadedApk loadedApk) {
    if (RuntimeEnvironment.getApiLevel() < P) {
      return null;
    }
    if (loadedApk == null || loadedApk.getApplicationInfo().appComponentFactory == null) {
      return null;
    }
    return loadedApk.getAppFactory();
  }

  private static LoadedApk getLoadedApk() {
    final ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return activityThread.getPackageInfo(
        activityThread.getApplication().getApplicationInfo(), null, Context.CONTEXT_INCLUDE_CODE);
  }

  @ForType(RemoteViews.class)
  private interface RemoteViewsReflector {
    @Direct
    void reapply(Context context, View v);

    @Direct
    void setRemoteAdapter(int viewId, Intent intent);
  }

  private abstract static class ViewUpdater {
    private final int viewId;

    public ViewUpdater(int viewId) {
      this.viewId = viewId;
    }

    final void update(View parent) {

      View view = parent.findViewById(viewId);
      if (view == null) {
        throw new NullPointerException(
            "couldn't find view "
                + viewId
                + " ("
                + parent.getContext().getResources().getResourceEntryName(viewId)
                + ")");
      }
      doUpdate(view);
    }

    abstract void doUpdate(View view);
  }
}
