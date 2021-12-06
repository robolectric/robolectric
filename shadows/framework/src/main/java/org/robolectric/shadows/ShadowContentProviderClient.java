package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ContentProviderClient.class)
public class ShadowContentProviderClient {
  @RealObject private ContentProviderClient realContentProviderClient;

  private ContentProvider provider;

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected Bundle call(String method, String arg, Bundle extras) throws RemoteException {
    return provider.call(method, arg, extras);
  }

  @Implementation
  protected String getType(Uri uri) throws RemoteException {
    return provider.getType(uri);
  }

  @Implementation
  protected String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
    return provider.getStreamTypes(uri, mimeTypeFilter);
  }

  @Implementation
  protected Cursor query(
      Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder)
      throws RemoteException {
    return provider.query(url, projection, selection, selectionArgs, sortOrder);
  }

  @Implementation
  protected Cursor query(
      Uri url,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder,
      CancellationSignal cancellationSignal)
      throws RemoteException {
    return provider.query(url, projection, selection, selectionArgs, sortOrder, cancellationSignal);
  }

  @Implementation
  protected Uri insert(Uri url, ContentValues initialValues) throws RemoteException {
    return provider.insert(url, initialValues);
  }

  @Implementation
  protected int bulkInsert(Uri url, ContentValues[] initialValues) throws RemoteException {
    return provider.bulkInsert(url, initialValues);
  }

  @Implementation
  protected int delete(Uri url, String selection, String[] selectionArgs) throws RemoteException {
    return provider.delete(url, selection, selectionArgs);
  }

  @Implementation
  protected int update(Uri url, ContentValues values, String selection, String[] selectionArgs)
      throws RemoteException {
    return provider.update(url, values, selection, selectionArgs);
  }

  @Implementation
  protected ParcelFileDescriptor openFile(Uri url, String mode)
      throws RemoteException, FileNotFoundException {
    return provider.openFile(url, mode);
  }

  @Implementation
  protected AssetFileDescriptor openAssetFile(Uri url, String mode)
      throws RemoteException, FileNotFoundException {
    return provider.openAssetFile(url, mode);
  }

  @Implementation
  protected final AssetFileDescriptor openTypedAssetFileDescriptor(
      Uri uri, String mimeType, Bundle opts) throws RemoteException, FileNotFoundException {
    return provider.openTypedAssetFile(uri, mimeType, opts);
  }

  @Implementation
  protected ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
      throws RemoteException, OperationApplicationException {
    return provider.applyBatch(operations);
  }

  @Implementation
  protected ContentProvider getLocalContentProvider() {
    return ContentProvider.coerceToLocalContentProvider(provider.getIContentProvider());
  }

  public boolean isStable() {
    return reflector(ContentProviderClientReflector.class, realContentProviderClient).getStable();
  }

  public boolean isReleased() {
    ContentProviderClientReflector contentProviderClientReflector =
        reflector(ContentProviderClientReflector.class, realContentProviderClient);
    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.M) {
      return contentProviderClientReflector.getReleased();
    } else {
      return contentProviderClientReflector.getClosed().get();
    }
  }

  void setContentProvider(ContentProvider provider) {
    this.provider = provider;
  }

  @ForType(ContentProviderClient.class)
  interface ContentProviderClientReflector {
    @Direct
    boolean release();

    @Accessor("mStable")
    boolean getStable();

    @Accessor("mReleased")
    boolean getReleased();

    @Accessor("mClosed")
    AtomicBoolean getClosed();
  }
}
