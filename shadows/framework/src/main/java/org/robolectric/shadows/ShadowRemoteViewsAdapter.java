package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.app.ActivityThread;
import android.app.AppComponentFactory;
import android.app.LoadedApk;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow class for {@link RemoteViewsAdapter}. */
@Implements(value = RemoteViewsAdapter.class, isInAndroidSdk = false)
// The framework code for RemoteViewsAdapter does not work well in a Robolectric environment.
// It uses multiple Handler threads and tries to bind to a service, none of which work well in
// Robolectric.
public class ShadowRemoteViewsAdapter {

  private static final String TAG = "ShadowRemoteViewsAdapter";

  @RealObject private RemoteViewsAdapter realRemoteViewsAdapter;

  private BaseAdapter adapter;
  private boolean hasAttemptedToInitAdapter = false;

  @Implementation
  protected boolean isDataReady() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return true;
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).isDataReady();
    }
  }

  @Implementation
  protected int getCount() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getCount();
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).getCount();
    }
  }

  @Implementation
  protected Object getItem(int position) {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getItem(position);
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).getItem(position);
    }
  }

  @Implementation
  protected long getItemId(int position) {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getItemId(position);
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter)
          .getItemId(position);
    }
  }

  @Implementation
  protected boolean hasStableIds() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.hasStableIds();
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).hasStableIds();
    }
  }

  @Implementation
  protected int getItemViewType(int position) {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getItemViewType(position);
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter)
          .getItemViewType(position);
    }
  }

  @Implementation
  protected View getView(int position, View convertView, ViewGroup parent) {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getView(position, convertView, parent);
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter)
          .getView(position, convertView, parent);
    }
  }

  @Implementation
  protected int getViewTypeCount() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.getViewTypeCount();
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter)
          .getViewTypeCount();
    }
  }

  @Implementation
  protected boolean isEmpty() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      return adapter.isEmpty();
    } else {
      return reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).isEmpty();
    }
  }

  @Implementation
  protected void notifyDataSetChanged() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    } else {
      reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter).notifyDataSetChanged();
    }
  }

  @Nullable
  private BaseAdapter getAdapter() {
    if (adapter == null) {
      if (hasAttemptedToInitAdapter) {
        return null;
      } else {
        adapter = createAdapterFromIntent(realRemoteViewsAdapter.getRemoteViewsServiceIntent());
        hasAttemptedToInitAdapter = true;
      }
    }
    return adapter;
  }

  @Nullable
  private static BaseAdapter createAdapterFromIntent(Intent intent) {
    Class<?> clazz = maybeExtractClass(intent);
    if (clazz == null) {
      return null;
    }
    try {
      RemoteViewsService remoteViewsService = createRemoteViewsService(intent, clazz);
      RemoteViewsFactory remoteViewsFactory = remoteViewsService.onGetViewFactory(intent);
      remoteViewsFactory.onCreate();
      return convertToAdapter(remoteViewsFactory);
    } catch (RuntimeException e) {
      Log.e(TAG, "Could not instantiate RemoteViewsService for class " + clazz, e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static RemoteViewsService createRemoteViewsService(Intent intent, Class<?> clazz) {
    return buildService((Class<? extends RemoteViewsService>) clazz, intent).create().bind().get();
  }

  private static Class<?> maybeExtractClass(Intent intent) {
    Class<?> clazz;
    try {
      clazz = Class.forName(intent.getComponent().getClassName());
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Invalid class name provided for the intent.", e);
      return null;
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
          Log.e(TAG, "Failed to instantiate Service using AppComponentFactory", e);
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

  @ForType(RemoteViewsAdapter.class)
  interface RemoteViewsAdapterReflector {
    @Direct
    boolean isDataReady();

    @Direct
    int getCount();

    @Direct
    Object getItem(int position);

    @Direct
    long getItemId(int position);

    @Direct
    boolean hasStableIds();

    @Direct
    int getItemViewType(int position);

    @Direct
    View getView(int position, View convertView, ViewGroup parent);

    @Direct
    int getViewTypeCount();

    @Direct
    boolean isEmpty();

    @Direct
    void notifyDataSetChanged();
  }
}
