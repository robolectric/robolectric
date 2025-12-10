package org.robolectric.shadows;

import android.credentials.CreateCredentialRequest;
import android.credentials.GetCredentialRequest;
import android.credentials.selection.RequestInfo;
import android.os.IBinder;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link RequestInfo} */
public final class RequestInfoBuilder {

  @Nonnull private IBinder token;

  @Nullable private CreateCredentialRequest createCredentialRequest;

  @Nonnull private List<String> defaultProviderIds;

  @Nullable private GetCredentialRequest getCredentialRequest;

  @Nonnull private String type;

  @Nonnull private String packageName;

  private boolean hasPermissionToOverrideDefault;

  private boolean isShowAllOptionsRequested;

  private RequestInfoBuilder() {}

  public static RequestInfoBuilder newBuilder() {
    return new RequestInfoBuilder();
  }

  /** Sets the request token. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setToken(IBinder token) {
    this.token = token;
    return this;
  }

  /** Sets the create credential request. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setCreateCredentialRequest(
      CreateCredentialRequest createCredentialRequest) {
    this.createCredentialRequest = createCredentialRequest;
    return this;
  }

  /** Sets the default provider IDs. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setDefaultProviderIds(List<String> defaultProviderIds) {
    this.defaultProviderIds = defaultProviderIds;
    return this;
  }

  /** Sets the get credential request. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setGetCredentialRequest(GetCredentialRequest getCredentialRequest) {
    this.getCredentialRequest = getCredentialRequest;
    return this;
  }

  /** Sets the request type. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setType(String type) {
    this.type = type;
    return this;
  }

  /** Sets the package name. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /** Sets has permission to override default. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setHasPermissionToOverrideDefault(
      boolean hasPermissionToOverrideDefault) {
    this.hasPermissionToOverrideDefault = hasPermissionToOverrideDefault;
    return this;
  }

  /** Sets show all options requested. */
  @CanIgnoreReturnValue
  public RequestInfoBuilder setIsShowAllOptionsRequested(boolean isShowAllOptionsRequested) {
    this.isShowAllOptionsRequested = isShowAllOptionsRequested;
    return this;
  }

  public RequestInfo build() {
    Preconditions.checkNotNull(token);
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(packageName);
    Preconditions.checkNotNull(defaultProviderIds);
    return ReflectionHelpers.callConstructor(
        RequestInfo.class,
        ClassParameter.from(IBinder.class, token),
        ClassParameter.from(String.class, type),
        ClassParameter.from(String.class, packageName),
        ClassParameter.from(CreateCredentialRequest.class, createCredentialRequest),
        ClassParameter.from(GetCredentialRequest.class, getCredentialRequest),
        ClassParameter.from(boolean.class, hasPermissionToOverrideDefault),
        ClassParameter.from(List.class, defaultProviderIds),
        ClassParameter.from(boolean.class, isShowAllOptionsRequested));
  }
}
