package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.app.ActivityThread;
import android.app.AppComponentFactory;
import android.app.LoadedApk;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.android.internal.widget.IRemoteViewsFactory;
import com.google.common.base.Throwables;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow class for {@code android.widget.RemoteViewsAdapter}. */
@Implements(
    className = "android.widget.RemoteViewsAdapter",
    isInAndroidSdk = false,
    maxSdk = CINNAMON_BUN)
// The framework code for RemoteViewsAdapter does not work well in a Robolectric environment.
// It uses multiple Handler threads and tries to bind to a service, none of which work well in
// Robolectric.
public class ShadowRemoteViewsAdapter extends ShadowBaseAdapter {

  private static final String TAG = "ShadowRemoteViewsAdapter";

  @RealObject private /* RemoteViewsAdapter */ Object realRemoteViewsAdapter;

  private BaseAdapter adapter;
  @Nullable private IRemoteViewsFactory factory;
  private boolean hasAttemptedToInitAdapter = false;

  /**
   * Connects to the collection's {@code RemoteViewsService} in-process instead of binding to it:
   * the service's factory loads (see {@link #factoryFor}), then the collection view hears that the
   * adapter is connected on the main thread, as after a bind on a device, and sets it as its
   * adapter.
   */
  @Implementation(maxSdk = O_MR1, methodName = "requestBindService")
  protected boolean requestBindServiceBeforeP() {
    connect();
    return true;
  }

  /** As {@link #requestBindServiceBeforeP()}. */
  @Implementation(minSdk = P)
  protected void requestBindService() {
    connect();
  }

  private void connect() {
    if (getAdapter() == null) {
      return;
    }
    Object callback = ReflectionHelpers.getField(realRemoteViewsAdapter, "mCallback");
    if (callback instanceof WeakReference) {
      callback = ((WeakReference<?>) callback).get();
    }
    if (callback == null) {
      return;
    }
    Object connectionCallback = callback;
    new Handler(Looper.getMainLooper())
        .post(() ->
            ReflectionHelpers.callInstanceMethod(connectionCallback, "onRemoteAdapterConnected"));
  }

  /**
   * Forgets the factories {@code RemoteViewsService} keeps for each intent, a static cache that
   * would otherwise carry factories, and their data, from one test to the next.
   */
  @Resetter
  public static void reset() {
    String cache =
        RuntimeEnvironment.getApiLevel() > BAKLAVA ? "sFactoriesCache" : "sRemoteViewFactories";
    Map<?, ?> factories = ReflectionHelpers.getStaticField(RemoteViewsService.class, cache);
    if (factories != null) {
      factories.clear();
    }
  }

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

  /**
   * Calls the factory's {@code onDataSetChanged()} and then tells the adapter's observers, such as
   * the collection view, as the framework does once the factory has reloaded.
   */
  @Implementation
  protected void notifyDataSetChanged() {
    BaseAdapter adapter = getAdapter();
    if (adapter != null) {
      call(factory::onDataSetChanged);
      reflector(BaseAdapterReflector.class, realRemoteViewsAdapter)
          .getDataSetObservable()
          .notifyChanged();
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
        factory =
            factoryFor(
                reflector(RemoteViewsAdapterReflector.class, realRemoteViewsAdapter)
                    .getRemoteViewsServiceIntent());
        if (factory != null) {
          // The first load, as the framework's adapter does once connected to a new factory.
          if (!call(factory::isCreated)) {
            call(factory::onDataSetChanged);
          }
          adapter = convertToAdapter(factory);
        }
        hasAttemptedToInitAdapter = true;
      }
    }
    return adapter;
  }

  /**
   * The factory the collection's {@code RemoteViewsService} serves for {@code intent}, from the
   * binder its {@code onBind} returns, as the framework's adapter binds to it: the service keeps
   * one factory per intent, created with {@code onCreate()} once, and its binder flags each row as
   * a collection child. Null if the service cannot be created.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private static IRemoteViewsFactory factoryFor(Intent intent) {
    Class<?> clazz = maybeExtractClass(intent);
    if (clazz == null) {
      return null;
    }
    try {
      RemoteViewsService service =
          buildService((Class<? extends RemoteViewsService>) clazz, intent).create().get();
      return IRemoteViewsFactory.Stub.asInterface(service.onBind(intent));
    } catch (RuntimeException e) {
      Log.e(TAG, "Could not instantiate RemoteViewsService for class " + clazz, e);
      return null;
    }
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

  private BaseAdapter convertToAdapter(IRemoteViewsFactory remoteViewsFactory) {
    return new BaseAdapter() {
      @Override
      public void registerDataSetObserver(DataSetObserver observer) {}

      @Override
      public void unregisterDataSetObserver(DataSetObserver observer) {}

      @Override
      public int getCount() {
        return call(remoteViewsFactory::getCount);
      }

      @Override
      public Object getItem(int position) {
        return call(() -> remoteViewsFactory.getViewAt(position));
      }

      @Override
      public long getItemId(int position) {
        return call(() -> remoteViewsFactory.getItemId(position));
      }

      @Override
      public boolean hasStableIds() {
        return call(remoteViewsFactory::hasStableIds);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        RemoteViews remoteViews = call(() -> remoteViewsFactory.getViewAt(position));
        RowFrame frame =
            convertView instanceof RowFrame
                ? (RowFrame) convertView
                : new RowFrame(parent.getContext());
        frame.apply(remoteViews, interactionHandler());
        return frame;
      }

      @Override
      public int getViewTypeCount() {
        return call(remoteViewsFactory::getViewTypeCount);
      }

      @Override
      public boolean isEmpty() {
        return call(remoteViewsFactory::getCount) == 0;
      }
    };
  }

  /**
   * Returns the items of the collection the {@code RemoteViewsService} behind {@code intent}
   * serves, collected through its binder's {@code getRemoteCollectionItems}, as {@code
   * AppWidgetManager} does for a converted collection; null if the service cannot be created.
   */
  @Nullable
  static Object collectItems(Intent intent) {
    IRemoteViewsFactory factory = factoryFor(intent);
    if (factory == null) {
      return null;
    }
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel > BAKLAVA) {
      // Reloads the factory first, as AppWidgetManager asks it to.
      return call(
          () -> factory.getRemoteCollectionItems(Integer.MAX_VALUE, Integer.MAX_VALUE, true));
    }
    // The size caps the binder takes: one on SDK 35, two on 36. It reloads the factory first.
    Class<?>[] types =
        apiLevel == BAKLAVA ? new Class<?>[] {int.class, int.class} : new Class<?>[] {int.class};
    Object[] caps =
        apiLevel == BAKLAVA
            ? new Object[] {Integer.MAX_VALUE, Integer.MAX_VALUE}
            : new Object[] {Integer.MAX_VALUE};
    try {
      return IRemoteViewsFactory.class
          .getMethod("getRemoteCollectionItems", types)
          .invoke(factory, caps);
    } catch (InvocationTargetException e) {
      Throwables.throwIfUnchecked(e.getCause());
      throw new IllegalStateException(e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("IRemoteViewsFactory.getRemoteCollectionItems", e);
    }
  }

  /** A call to the factory's binder, which runs in-process and so never fails remotely. */
  private interface BinderCall<T> {
    T call() throws RemoteException;
  }

  /** A call to the factory's binder that returns nothing. */
  private interface VoidBinderCall {
    void call() throws RemoteException;
  }

  private static <T> T call(BinderCall<T> binderCall) {
    try {
      return binderCall.call();
    } catch (RemoteException e) {
      throw e.rethrowAsRuntimeException();
    }
  }

  private static void call(VoidBinderCall binderCall) {
    try {
      binderCall.call();
    } catch (RemoteException e) {
      throw e.rethrowAsRuntimeException();
    }
  }

  /**
   * The interaction handler the collection view was given by the widget's {@code RemoteViews}, with
   * which the framework applies each row; null when there is none.
   */
  @Nullable
  private Object interactionHandler() {
    String field =
        RuntimeEnvironment.getApiLevel() >= S
            ? "mRemoteViewsInteractionHandler"
            : "mRemoteViewsOnClickHandler";
    return ReflectionHelpers.getField(realRemoteViewsAdapter, field);
  }

  /**
   * A row, wrapped in a frame as the framework's adapter wraps each one: a click on the collection
   * looks for the row's fill-in intent inside the item view it is given. A recycled frame reapplies
   * a row of the same layout over the views it already has.
   */
  private static final class RowFrame extends FrameLayout {
    private int layoutId;

    RowFrame(Context context) {
      super(context);
    }

    void apply(RemoteViews remoteViews, @Nullable Object handler) {
      Class<?> handlerType =
          RuntimeEnvironment.getApiLevel() >= S
              ? classForName("android.widget.RemoteViews$InteractionHandler")
              : classForName("android.widget.RemoteViews$OnClickHandler");
      View row = getChildCount() == 1 ? getChildAt(0) : null;
      if (row != null && layoutId == remoteViews.getLayoutId()) {
        ReflectionHelpers.callInstanceMethod(
            remoteViews,
            "reapply",
            ClassParameter.from(Context.class, getContext()),
            ClassParameter.from(View.class, row),
            ClassParameter.from(handlerType, handler));
        return;
      }
      removeAllViews();
      row =
          ReflectionHelpers.callInstanceMethod(
              remoteViews,
              "apply",
              ClassParameter.from(Context.class, getContext()),
              ClassParameter.from(ViewGroup.class, this),
              ClassParameter.from(handlerType, handler));
      layoutId = remoteViews.getLayoutId();
      addView(row);
    }

    private static Class<?> classForName(String name) {
      try {
        return Class.forName(name, false, RemoteViews.class.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new LinkageError(name, e);
      }
    }
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

  @ForType(BaseAdapter.class)
  interface BaseAdapterReflector {
    @Accessor("mDataSetObservable")
    DataSetObservable getDataSetObservable();
  }

  @ForType(className = "android.widget.RemoteViewsAdapter")
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

    Intent getRemoteViewsServiceIntent();
  }
}
