package org.robolectric.shadows;

import static android.credentials.selection.RequestInfo.TYPE_CREATE;
import static android.credentials.selection.RequestInfo.TYPE_GET;
import static com.google.common.truth.Truth.assertThat;

import android.credentials.CreateCredentialRequest;
import android.credentials.CredentialOption;
import android.credentials.GetCredentialRequest;
import android.credentials.selection.RequestInfo;
import android.os.Binder;
import android.os.Bundle;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.V;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = V.SDK_INT)
public final class RequestInfoBuilderTest {

  @Test
  public void constructCreateRequestInfo_returnsInfo() {
    CreateCredentialRequest createCredentialRequest =
        new CreateCredentialRequest.Builder("TYPE_CREATE", new Bundle(), new Bundle()).build();
    RequestInfo requestInfo =
        RequestInfoBuilder.newBuilder()
            .setToken(new Binder())
            .setCreateCredentialRequest(createCredentialRequest)
            .setGetCredentialRequest(null)
            .setDefaultProviderIds(new ArrayList<>())
            .setType(TYPE_CREATE)
            .setPackageName("packageName")
            .setHasPermissionToOverrideDefault(true)
            .setIsShowAllOptionsRequested(true)
            .build();
    assertThat(requestInfo).isNotNull();
    assertThat(requestInfo.getToken()).isNotNull();
    assertThat(requestInfo.getDefaultProviderIds()).isEmpty();
    assertThat(requestInfo.getType()).isEqualTo(TYPE_CREATE);
    assertThat(requestInfo.getPackageName()).isEqualTo("packageName");
    assertThat(requestInfo.getCreateCredentialRequest()).isEqualTo(createCredentialRequest);
    assertThat(requestInfo.isShowAllOptionsRequested()).isTrue();
    assertThat(requestInfo.hasPermissionToOverrideDefault()).isTrue();
  }

  @Test
  public void constructGetRequestInfo_returnsInfo() {
    GetCredentialRequest getCredentialRequest =
        new GetCredentialRequest.Builder(new Bundle())
            .addCredentialOption(new CredentialOption("option", new Bundle(), new Bundle(), true))
            .build();
    RequestInfo requestInfo =
        RequestInfoBuilder.newBuilder()
            .setToken(new Binder())
            .setCreateCredentialRequest(null)
            .setGetCredentialRequest(getCredentialRequest)
            .setDefaultProviderIds(new ArrayList<>())
            .setType(TYPE_GET)
            .setPackageName("packageName")
            .setHasPermissionToOverrideDefault(true)
            .setIsShowAllOptionsRequested(true)
            .build();
    assertThat(requestInfo).isNotNull();
    assertThat(requestInfo.getToken()).isNotNull();
    assertThat(requestInfo.getDefaultProviderIds()).isEmpty();
    assertThat(requestInfo.getType()).isEqualTo(TYPE_GET);
    assertThat(requestInfo.getPackageName()).isEqualTo("packageName");
    assertThat(requestInfo.getGetCredentialRequest()).isEqualTo(getCredentialRequest);
    assertThat(requestInfo.isShowAllOptionsRequested()).isTrue();
    assertThat(requestInfo.hasPermissionToOverrideDefault()).isTrue();
  }
}
