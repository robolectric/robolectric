package org.robolectric.shadows;

import android.app.appsearch.AppSearchBatchResult;
import android.app.appsearch.AppSearchResult;
import android.app.appsearch.GetSchemaResponse;
import android.app.appsearch.InternalSetSchemaResponse;
import android.app.appsearch.SearchResultPage;
import android.app.appsearch.SetSchemaResponse;
import android.app.appsearch.StorageInfo;
import android.app.appsearch.aidl.AppSearchBatchResultParcel;
import android.app.appsearch.aidl.AppSearchResultParcel;
import android.app.appsearch.aidl.AppSearchResultParcelV2;
import android.app.appsearch.aidl.CommitBlobAidlRequest;
import android.app.appsearch.aidl.GetDocumentsAidlRequest;
import android.app.appsearch.aidl.GetNamespacesAidlRequest;
import android.app.appsearch.aidl.GetNextPageAidlRequest;
import android.app.appsearch.aidl.GetSchemaAidlRequest;
import android.app.appsearch.aidl.GetStorageInfoAidlRequest;
import android.app.appsearch.aidl.GlobalSearchAidlRequest;
import android.app.appsearch.aidl.IAppSearchBatchResultCallback;
import android.app.appsearch.aidl.IAppSearchManager;
import android.app.appsearch.aidl.IAppSearchObserverProxy;
import android.app.appsearch.aidl.IAppSearchResultCallback;
import android.app.appsearch.aidl.InitializeAidlRequest;
import android.app.appsearch.aidl.InvalidateNextPageTokenAidlRequest;
import android.app.appsearch.aidl.OpenBlobForReadAidlRequest;
import android.app.appsearch.aidl.OpenBlobForWriteAidlRequest;
import android.app.appsearch.aidl.PersistToDiskAidlRequest;
import android.app.appsearch.aidl.PutDocumentsAidlRequest;
import android.app.appsearch.aidl.PutDocumentsFromFileAidlRequest;
import android.app.appsearch.aidl.RegisterObserverCallbackAidlRequest;
import android.app.appsearch.aidl.RemoveBlobAidlRequest;
import android.app.appsearch.aidl.RemoveByDocumentIdAidlRequest;
import android.app.appsearch.aidl.RemoveByQueryAidlRequest;
import android.app.appsearch.aidl.ReportUsageAidlRequest;
import android.app.appsearch.aidl.SearchAidlRequest;
import android.app.appsearch.aidl.SearchSuggestionAidlRequest;
import android.app.appsearch.aidl.SetBlobVisibilityAidlRequest;
import android.app.appsearch.aidl.SetSchemaAidlRequest;
import android.app.appsearch.aidl.UnregisterObserverCallbackAidlRequest;
import android.app.appsearch.aidl.WriteSearchResultsToFileAidlRequest;
import android.app.appsearch.safeparcel.GenericDocumentParcel;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.Collections;

/**
 * Minimal fake implementation of {@link IAppSearchManager} that completes asynchronous callbacks
 * with empty results so {@link android.app.appsearch.AppSearchManager} sessions do not hang.
 */
@SuppressWarnings({"NewApi", "rawtypes"})
class FakeAppSearchManager extends IAppSearchManager.Default {

  private final IBinder binder = new Binder();

  @Override
  public void initialize(InitializeAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromVoid());
  }

  @Override
  public void setSchema(SetSchemaAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(
        AppSearchResultParcel.fromInternalSetSchemaResponse(
            InternalSetSchemaResponse.newSuccessfulSetSchemaResponse(
                new SetSchemaResponse.Builder().build())));
  }

  @Override
  public void getSchema(GetSchemaAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(
        AppSearchResultParcel.fromGetSchemaResponse(new GetSchemaResponse.Builder().build()));
  }

  @Override
  public void getNamespaces(GetNamespacesAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromStringList(Collections.emptyList()));
  }

  @Override
  public void putDocuments(PutDocumentsAidlRequest request, IAppSearchBatchResultCallback callback)
      throws RemoteException {
    AppSearchBatchResult.Builder<String, Void> builder = new AppSearchBatchResult.Builder<>();
    for (GenericDocumentParcel doc : request.getDocumentsParcel().getDocumentParcels()) {
      builder.setSuccess(doc.getId(), null);
    }
    for (GenericDocumentParcel doc :
        request.getDocumentsParcel().getTakenActionGenericDocumentParcels()) {
      builder.setSuccess(doc.getId(), null);
    }
    callback.onResult(AppSearchBatchResultParcel.fromStringToVoid(builder.build()));
  }

  @Override
  public void getDocuments(GetDocumentsAidlRequest request, IAppSearchBatchResultCallback callback)
      throws RemoteException {
    AppSearchBatchResult.Builder<String, GenericDocumentParcel> builder =
        new AppSearchBatchResult.Builder<>();
    for (String id : request.getGetByDocumentIdRequest().getIds()) {
      builder.setFailure(id, AppSearchResult.RESULT_NOT_FOUND, "Document not found");
    }
    callback.onResult(
        AppSearchBatchResultParcel.fromStringToGenericDocumentParcel(builder.build()));
  }

  @Override
  public void search(SearchAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromSearchResultPage(new SearchResultPage()));
  }

  @Override
  public void globalSearch(GlobalSearchAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromSearchResultPage(new SearchResultPage()));
  }

  @Override
  public void getNextPage(GetNextPageAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromSearchResultPage(new SearchResultPage()));
  }

  @Override
  public void invalidateNextPageToken(InvalidateNextPageTokenAidlRequest request) {}

  @Override
  public void writeSearchResultsToFile(
      WriteSearchResultsToFileAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromVoid());
  }

  @Override
  public void putDocumentsFromFile(
      PutDocumentsFromFileAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromMigrationFailuresList(Collections.emptyList()));
  }

  @Override
  public void searchSuggestion(
      SearchSuggestionAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(
        AppSearchResultParcel.fromSearchSuggestionResultList(Collections.emptyList()));
  }

  @Override
  public void reportUsage(ReportUsageAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromVoid());
  }

  @Override
  public void removeByDocumentId(
      RemoveByDocumentIdAidlRequest request, IAppSearchBatchResultCallback callback)
      throws RemoteException {
    AppSearchBatchResult.Builder<String, Void> builder = new AppSearchBatchResult.Builder<>();
    for (String id : request.getRemoveByDocumentIdRequest().getIds()) {
      builder.setSuccess(id, null);
    }
    callback.onResult(AppSearchBatchResultParcel.fromStringToVoid(builder.build()));
  }

  @Override
  public void removeByQuery(RemoveByQueryAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromVoid());
  }

  @Override
  public void getStorageInfo(GetStorageInfoAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromStorageInfo(new StorageInfo.Builder().build()));
  }

  @Override
  public void persistToDisk(PersistToDiskAidlRequest request) {}

  @Override
  public AppSearchResultParcel registerObserverCallback(
      RegisterObserverCallbackAidlRequest request, IAppSearchObserverProxy observerProxy) {
    return AppSearchResultParcel.fromVoid();
  }

  @Override
  public AppSearchResultParcel unregisterObserverCallback(
      UnregisterObserverCallbackAidlRequest request, IAppSearchObserverProxy observerProxy) {
    return AppSearchResultParcel.fromVoid();
  }

  @Override
  public void openBlobForWrite(
      OpenBlobForWriteAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResultV2(
        AppSearchResultParcelV2.fromFailedResult(
            AppSearchResult.newFailedResult(AppSearchResult.RESULT_NOT_FOUND, "Blob not found")));
  }

  @Override
  public void commitBlob(CommitBlobAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResultV2(
        AppSearchResultParcelV2.fromFailedResult(
            AppSearchResult.newFailedResult(AppSearchResult.RESULT_NOT_FOUND, "Blob not found")));
  }

  @Override
  public void openBlobForRead(OpenBlobForReadAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResultV2(
        AppSearchResultParcelV2.fromFailedResult(
            AppSearchResult.newFailedResult(AppSearchResult.RESULT_NOT_FOUND, "Blob not found")));
  }

  @Override
  public void removeBlob(RemoveBlobAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResultV2(
        AppSearchResultParcelV2.fromFailedResult(
            AppSearchResult.newFailedResult(AppSearchResult.RESULT_NOT_FOUND, "Blob not found")));
  }

  @Override
  public void setBlobVisibility(
      SetBlobVisibilityAidlRequest request, IAppSearchResultCallback callback)
      throws RemoteException {
    callback.onResult(AppSearchResultParcel.fromVoid());
  }

  @Override
  public IBinder asBinder() {
    return binder;
  }
}
