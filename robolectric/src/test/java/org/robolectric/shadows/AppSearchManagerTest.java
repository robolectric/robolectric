package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import android.app.appsearch.AppSearchBatchResult;
import android.app.appsearch.AppSearchManager;
import android.app.appsearch.AppSearchManager.SearchContext;
import android.app.appsearch.AppSearchResult;
import android.app.appsearch.AppSearchSession;
import android.app.appsearch.BatchResultCallback;
import android.app.appsearch.GenericDocument;
import android.app.appsearch.GetByDocumentIdRequest;
import android.app.appsearch.GetSchemaResponse;
import android.app.appsearch.GlobalSearchSession;
import android.app.appsearch.PutDocumentsRequest;
import android.app.appsearch.RemoveByDocumentIdRequest;
import android.app.appsearch.SearchResult;
import android.app.appsearch.SearchResults;
import android.app.appsearch.SearchSpec;
import android.app.appsearch.SetSchemaRequest;
import android.app.appsearch.SetSchemaResponse;
import android.app.appsearch.StorageInfo;
import android.app.appsearch.observer.DocumentChangeInfo;
import android.app.appsearch.observer.ObserverCallback;
import android.app.appsearch.observer.ObserverSpec;
import android.app.appsearch.observer.SchemaChangeInfo;
import android.content.Context;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link AppSearchManager} backed by {@link FakeAppSearchManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public final class AppSearchManagerTest {

  @Test
  public void appSearchManager_notNullByDefault() {
    AppSearchManager manager = getApplicationContext().getSystemService(AppSearchManager.class);

    assertThat(manager).isNotNull();
  }

  @Test
  public void appSearchManager_disableService_returnsNull() {
    ShadowServiceManager.setServiceAvailability(Context.APP_SEARCH_SERVICE, /* available= */ false);

    AppSearchManager manager = getApplicationContext().getSystemService(AppSearchManager.class);

    assertThat(manager).isNull();
  }

  @Test
  public void createSearchSession_succeeds() throws Exception {
    AppSearchManager manager = getApplicationContext().getSystemService(AppSearchManager.class);
    SettableFuture<AppSearchResult<AppSearchSession>> sessionFuture = SettableFuture.create();

    manager.createSearchSession(
        new SearchContext.Builder("test_db").build(), directExecutor(), sessionFuture::set);
    AppSearchResult<AppSearchSession> sessionResult = sessionFuture.get();

    assertThat(sessionResult.isSuccess()).isTrue();
    sessionResult.getResultValue().close();
  }

  @Test
  public void appSearchSession_setSchema_succeeds() throws Exception {
    try (AppSearchSession session = createSearchSession()) {
      SettableFuture<AppSearchResult<SetSchemaResponse>> future = SettableFuture.create();

      session.setSchema(
          new SetSchemaRequest.Builder().build(), directExecutor(), directExecutor(), future::set);
      AppSearchResult<SetSchemaResponse> result = future.get();

      assertThat(result.isSuccess()).isTrue();
    }
  }

  @Test
  public void appSearchSession_getSchema_returnsEmptySchemas() throws Exception {
    // FakeAppSearchManager is a stateless stub, so getSchema completes with an empty schema set.
    try (AppSearchSession session = createSearchSession()) {
      SettableFuture<AppSearchResult<GetSchemaResponse>> future = SettableFuture.create();

      session.getSchema(directExecutor(), future::set);
      AppSearchResult<GetSchemaResponse> result = future.get();

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getResultValue().getSchemas()).isEmpty();
    }
  }

  @Test
  public void appSearchSession_getNamespaces_returnsEmptySet() throws Exception {
    try (AppSearchSession session = createSearchSession()) {
      SettableFuture<AppSearchResult<Set<String>>> future = SettableFuture.create();

      session.getNamespaces(directExecutor(), future::set);
      AppSearchResult<Set<String>> result = future.get();

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getResultValue()).isEmpty();
    }
  }

  @Test
  public void appSearchSession_put_succeeds() throws Exception {
    try (AppSearchSession session = createSearchSession()) {
      GenericDocument doc = new GenericDocument.Builder<>("ns", "doc1", "TestSchema").build();
      PutDocumentsRequest request =
          new PutDocumentsRequest.Builder().addGenericDocuments(doc).build();
      SettableFuture<AppSearchBatchResult<String, Void>> future = SettableFuture.create();

      session.put(request, directExecutor(), toBatchCallback(future));
      AppSearchBatchResult<String, Void> result = future.get();

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getSuccesses()).containsKey("doc1");
    }
  }

  @Test
  public void appSearchSession_getByDocumentId_returnsNotFoundFailure() throws Exception {
    // FakeAppSearchManager does not persist documents, so lookups complete with RESULT_NOT_FOUND.
    try (AppSearchSession session = createSearchSession()) {
      GetByDocumentIdRequest request =
          new GetByDocumentIdRequest.Builder("ns").addIds("doc1").build();
      SettableFuture<AppSearchBatchResult<String, GenericDocument>> future =
          SettableFuture.create();

      session.getByDocumentId(request, directExecutor(), toBatchCallback(future));
      AppSearchBatchResult<String, GenericDocument> result = future.get();

      assertThat(result.getFailures()).containsKey("doc1");
      assertThat(result.getFailures().get("doc1").getResultCode())
          .isEqualTo(AppSearchResult.RESULT_NOT_FOUND);
    }
  }

  @Test
  public void appSearchSession_search_returnsEmptyPage() throws Exception {
    try (AppSearchSession session = createSearchSession();
        SearchResults searchResults = session.search("", new SearchSpec.Builder().build())) {
      SettableFuture<AppSearchResult<List<SearchResult>>> pageFuture = SettableFuture.create();

      searchResults.getNextPage(directExecutor(), pageFuture::set);
      AppSearchResult<List<SearchResult>> pageResult = pageFuture.get();

      assertThat(pageResult.isSuccess()).isTrue();
      assertThat(pageResult.getResultValue()).isEmpty();
    }
  }

  @Test
  public void appSearchSession_remove_succeeds() throws Exception {
    try (AppSearchSession session = createSearchSession()) {
      RemoveByDocumentIdRequest request =
          new RemoveByDocumentIdRequest.Builder("ns").addIds("doc1").build();
      SettableFuture<AppSearchBatchResult<String, Void>> future = SettableFuture.create();

      session.remove(request, directExecutor(), toBatchCallback(future));
      AppSearchBatchResult<String, Void> result = future.get();

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getSuccesses()).containsKey("doc1");
    }
  }

  @Test
  public void appSearchSession_getStorageInfo_succeeds() throws Exception {
    try (AppSearchSession session = createSearchSession()) {
      SettableFuture<AppSearchResult<StorageInfo>> future = SettableFuture.create();

      session.getStorageInfo(directExecutor(), future::set);
      AppSearchResult<StorageInfo> result = future.get();

      assertThat(result.isSuccess()).isTrue();
    }
  }

  @Test
  public void globalSearchSession_search_returnsEmptyPage() throws Exception {
    // GlobalSearchSession queries across all packages on the device; FakeAppSearchManager
    // completes global searches with an empty page so callers do not hang awaiting callbacks.
    try (GlobalSearchSession session = createGlobalSearchSession();
        SearchResults searchResults = session.search("", new SearchSpec.Builder().build())) {
      SettableFuture<AppSearchResult<List<SearchResult>>> pageFuture = SettableFuture.create();

      searchResults.getNextPage(directExecutor(), pageFuture::set);
      AppSearchResult<List<SearchResult>> pageResult = pageFuture.get();

      assertThat(pageResult.isSuccess()).isTrue();
      assertThat(pageResult.getResultValue()).isEmpty();
    }
  }

  @Test
  public void globalSearchSession_registerAndUnregisterObserverCallback_succeeds()
      throws Exception {
    // registerObserverCallback and unregisterObserverCallback are synchronous Binder calls that
    // throw AppSearchException if the service returns a failed result; verify the no-op stub
    // completes both without throwing.
    try (GlobalSearchSession session = createGlobalSearchSession()) {
      ObserverSpec spec = new ObserverSpec.Builder().build();
      ObserverCallback observer =
          new ObserverCallback() {
            @Override
            public void onSchemaChanged(SchemaChangeInfo changeInfo) {}

            @Override
            public void onDocumentChanged(DocumentChangeInfo changeInfo) {}
          };

      session.registerObserverCallback("com.example.pkg", spec, directExecutor(), observer);
      session.unregisterObserverCallback("com.example.pkg", observer);
    }
  }

  private static AppSearchSession createSearchSession() throws Exception {
    AppSearchManager manager = getApplicationContext().getSystemService(AppSearchManager.class);
    SettableFuture<AppSearchResult<AppSearchSession>> sessionFuture = SettableFuture.create();
    manager.createSearchSession(
        new SearchContext.Builder("test_db").build(), directExecutor(), sessionFuture::set);
    AppSearchResult<AppSearchSession> sessionResult = sessionFuture.get();
    assertThat(sessionResult.isSuccess()).isTrue();
    return sessionResult.getResultValue();
  }

  private static GlobalSearchSession createGlobalSearchSession() throws Exception {
    AppSearchManager manager = getApplicationContext().getSystemService(AppSearchManager.class);
    SettableFuture<AppSearchResult<GlobalSearchSession>> sessionFuture = SettableFuture.create();
    manager.createGlobalSearchSession(directExecutor(), sessionFuture::set);
    AppSearchResult<GlobalSearchSession> sessionResult = sessionFuture.get();
    assertThat(sessionResult.isSuccess()).isTrue();
    return sessionResult.getResultValue();
  }

  private static <K, V> BatchResultCallback<K, V> toBatchCallback(
      SettableFuture<AppSearchBatchResult<K, V>> future) {
    return new BatchResultCallback<K, V>() {
      @Override
      public void onResult(AppSearchBatchResult<K, V> result) {
        future.set(result);
      }

      @Override
      public void onSystemError(Throwable throwable) {
        future.setException(throwable);
      }
    };
  }
}
