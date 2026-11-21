package org.robolectric.shadows;

import android.content.pm.PackageManager;
import android.hardware.security.keymint.Algorithm;
import android.hardware.security.keymint.BlockMode;
import android.hardware.security.keymint.Digest;
import android.hardware.security.keymint.EcCurve;
import android.hardware.security.keymint.ErrorCode;
import android.hardware.security.keymint.HardwareAuthenticatorType;
import android.hardware.security.keymint.KeyOrigin;
import android.hardware.security.keymint.KeyParameter;
import android.hardware.security.keymint.KeyParameterValue;
import android.hardware.security.keymint.KeyPurpose;
import android.hardware.security.keymint.PaddingMode;
import android.hardware.security.keymint.SecurityLevel;
import android.hardware.security.keymint.Tag;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.system.keystore2.AuthenticatorSpec;
import android.system.keystore2.Authorization;
import android.system.keystore2.CreateOperationResponse;
import android.system.keystore2.Domain;
import android.system.keystore2.EphemeralStorageKeyResponse;
import android.system.keystore2.IKeystoreOperation;
import android.system.keystore2.IKeystoreSecurityLevel;
import android.system.keystore2.IKeystoreService;
import android.system.keystore2.KeyDescriptor;
import android.system.keystore2.KeyEntryResponse;
import android.system.keystore2.KeyMetadata;
import android.system.keystore2.KeyParameters;
import android.system.keystore2.OperationChallenge;
import android.system.keystore2.ResponseCode;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;
import org.robolectric.RuntimeEnvironment;

/**
 * A software keystore2 daemon, served as {@code IKeystoreService} so that the framework's
 * AndroidKeyStore provider runs unchanged on top of it.
 *
 * <p>It generates, imports and stores keys in memory, performs the KeyMint operations on them with
 * the JVM's crypto providers, and enforces the key authorizations that apps rely on: purposes,
 * modes, paddings and digests, validity dates, user authentication, unlocked-device-required and
 * usage count limits. User authentication is driven by the existing test APIs: {@link
 * ShadowKeyguardManager} for the secure lock screen and its unlocking, {@link
 * ShadowBiometricManager} for enrolled biometrics, and {@link ShadowBiometricPrompt} for
 * authenticating. StrongBox is available when the {@link PackageManager#FEATURE_STRONGBOX_KEYSTORE}
 * system feature is.
 */
class KeystoreServiceDelegate extends IKeystoreService.Stub
    implements ShadowServiceManager.ResettableService {
  static final String SERVICE_NAME = "android.system.keystore2.IKeystoreService/default";

  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static final Map<Long, KeyEntry> entries = new HashMap<>();

  @GuardedBy("lock")
  private static final List<AuthToken> authTokens = new ArrayList<>();

  @GuardedBy("lock")
  private static final List<Operation> operationsAwaitingAuthorization = new ArrayList<>();

  @GuardedBy("lock")
  private static long nextKeyId = 1;

  private static final SecureRandom random = new SecureRandom();

  private final SecurityLevelDelegate trustedEnvironment =
      new SecurityLevelDelegate(SecurityLevel.TRUSTED_ENVIRONMENT);
  private final SecurityLevelDelegate strongBox =
      new SecurityLevelDelegate(SecurityLevel.STRONGBOX);

  @Override
  public void reset() {
    synchronized (lock) {
      entries.clear();
      authTokens.clear();
      operationsAwaitingAuthorization.clear();
      nextKeyId = 1;
    }
  }

  /**
   * Records a successful user authentication, as the system does when the user unlocks the device
   * or authenticates in a {@code BiometricPrompt}.
   *
   * @param challenge the operation challenge that the authentication is for, or 0
   * @param userSecureId the GateKeeper secure user id of the user
   * @param authenticatorId the biometric secure id, or 0 for a device credential
   * @param authenticatorType a {@link HardwareAuthenticatorType}
   */
  static void addAuthToken(
      long challenge, long userSecureId, long authenticatorId, int authenticatorType) {
    synchronized (lock) {
      authTokens.add(
          new AuthToken(
              challenge,
              userSecureId,
              authenticatorId,
              authenticatorType,
              SystemClock.elapsedRealtime()));
    }
  }

  /**
   * Records a successful user authentication for the operation in a {@code BiometricPrompt}'s
   * {@code CryptoObject}, which is the most recently started operation that awaits authorization.
   *
   * <p>On a device the authentication is for the operation handle of the {@code CryptoObject}, but
   * the JVM has no API to get the operation from a {@code Cipher}, {@code Signature} or {@code
   * Mac}.
   */
  static void addAuthTokenForLatestOperation(
      long userSecureId, long authenticatorId, int authenticatorType) {
    synchronized (lock) {
      for (int i = operationsAwaitingAuthorization.size() - 1; i >= 0; i--) {
        Operation operation = operationsAwaitingAuthorization.get(i);
        List<Long> secureIds = longValues(operation.entry.authorizations, Tag.USER_SECURE_ID);
        int types = intValue(operation.entry.authorizations, Tag.USER_AUTH_TYPE, 0);
        if ((types & authenticatorType) != 0
            && (secureIds.contains(userSecureId) || secureIds.contains(authenticatorId))) {
          operationsAwaitingAuthorization.remove(i);
          addAuthToken(operation.challenge, userSecureId, authenticatorId, authenticatorType);
          return;
        }
      }
    }
  }

  /**
   * Permanently invalidates the keys that require user authentication, as keystore2 does when the
   * user removes their secure lock screen.
   */
  static void onLockScreenRemoved() {
    synchronized (lock) {
      for (KeyEntry entry : entries.values()) {
        if (hasTag(entry.authorizations, Tag.USER_SECURE_ID)) {
          entry.invalidated = true;
        }
      }
    }
  }

  // IKeystoreService

  @Override
  public IKeystoreSecurityLevel getSecurityLevel(int securityLevel) {
    switch (securityLevel) {
      case SecurityLevel.TRUSTED_ENVIRONMENT:
        return trustedEnvironment;
      case SecurityLevel.STRONGBOX:
        if (RuntimeEnvironment.getApplication()
            .getPackageManager()
            .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
          return strongBox;
        }
        throw error(ErrorCode.HARDWARE_TYPE_UNAVAILABLE, "No StrongBox");
      default:
        throw error(ErrorCode.HARDWARE_TYPE_UNAVAILABLE, "Unsupported security level");
    }
  }

  @Override
  public KeyEntryResponse getKeyEntry(KeyDescriptor descriptor) {
    synchronized (lock) {
      KeyEntry entry = find(descriptor);
      KeyEntryResponse response = new KeyEntryResponse();
      response.metadata = entry.metadata();
      if (entry.hasKey()) {
        response.iSecurityLevel = securityLevelFor(entry.securityLevel);
      }
      return response;
    }
  }

  @Override
  public void updateSubcomponent(
      KeyDescriptor descriptor, @Nullable byte[] publicCert, @Nullable byte[] certificateChain) {
    synchronized (lock) {
      KeyEntry entry = findOrNull(descriptor);
      if (entry == null) {
        if (publicCert == null && certificateChain == null) {
          return;
        }
        entry = newEntry(descriptor, SecurityLevel.SOFTWARE, new ArrayList<>());
      }
      entry.certificate = publicCert;
      entry.certificateChain = certificateChain;
      entry.modificationTimeMs = ShadowSystem.currentTimeMillis();
    }
  }

  @Override
  public KeyDescriptor[] listEntries(int domain, long nspace) {
    return listEntriesBatched(domain, nspace, null);
  }

  @Override
  public KeyDescriptor[] listEntriesBatched(
      int domain, long nspace, @Nullable String startingPastAlias) {
    synchronized (lock) {
      TreeMap<String, KeyDescriptor> result = new TreeMap<>();
      for (KeyEntry entry : entries.values()) {
        if (entry.domain == domain
            && entry.nspace == nspace
            && (startingPastAlias == null || entry.alias.compareTo(startingPastAlias) > 0)) {
          result.put(entry.alias, entry.userDescriptor());
        }
      }
      return result.values().toArray(new KeyDescriptor[0]);
    }
  }

  @Override
  public int getNumberOfEntries(int domain, long nspace) {
    return listEntries(domain, nspace).length;
  }

  @Override
  public void deleteKey(KeyDescriptor descriptor) {
    synchronized (lock) {
      KeyEntry entry = findOrNull(descriptor);
      if (entry == null) {
        throw error(ResponseCode.KEY_NOT_FOUND, "Key not found");
      }
      entries.remove(entry.id);
    }
  }

  @Override
  public KeyDescriptor grant(KeyDescriptor descriptor, int granteeUid, int accessVector) {
    throw unsupported("grant");
  }

  @Override
  public void ungrant(KeyDescriptor descriptor, int granteeUid) {
    throw unsupported("ungrant");
  }

  @Override
  public byte[] getSupplementaryAttestationInfo(int tag) {
    throw unsupported("getSupplementaryAttestationInfo");
  }

  @Override
  public int getInterfaceVersion() {
    return VERSION;
  }

  @Override
  public String getInterfaceHash() {
    return HASH;
  }

  static UnsupportedOperationException unsupported(String method) {
    return new UnsupportedOperationException(method + " is not supported in Robolectric");
  }

  private IKeystoreSecurityLevel securityLevelFor(int securityLevel) {
    return securityLevel == SecurityLevel.STRONGBOX ? strongBox : trustedEnvironment;
  }

  @GuardedBy("lock")
  private static KeyEntry find(KeyDescriptor descriptor) {
    KeyEntry entry = findOrNull(descriptor);
    if (entry == null) {
      throw error(ResponseCode.KEY_NOT_FOUND, "Key not found");
    }
    if (entry.invalidated) {
      throw error(ResponseCode.KEY_PERMANENTLY_INVALIDATED, "The secure lock screen was removed");
    }
    return entry;
  }

  @GuardedBy("lock")
  @Nullable
  private static KeyEntry findOrNull(KeyDescriptor descriptor) {
    if (descriptor.domain == Domain.KEY_ID) {
      return entries.get(descriptor.nspace);
    }
    for (KeyEntry entry : entries.values()) {
      if (entry.domain == descriptor.domain
          && entry.nspace == descriptor.nspace
          && entry.alias.equals(descriptor.alias)) {
        return entry;
      }
    }
    return null;
  }

  /** Creates an entry for {@code descriptor}, replacing any existing one, like keystore2. */
  @GuardedBy("lock")
  private static KeyEntry newEntry(
      KeyDescriptor descriptor, int securityLevel, List<KeyParameter> authorizations) {
    if (descriptor.domain != Domain.APP && descriptor.domain != Domain.SELINUX) {
      throw error(ResponseCode.INVALID_ARGUMENT, "Unsupported domain " + descriptor.domain);
    }
    if (descriptor.alias == null) {
      throw error(ResponseCode.INVALID_ARGUMENT, "Missing alias");
    }
    KeyEntry existing = findOrNull(descriptor);
    if (existing != null) {
      entries.remove(existing.id);
    }
    KeyEntry entry =
        new KeyEntry(
            nextKeyId++,
            descriptor.domain,
            descriptor.nspace,
            descriptor.alias,
            securityLevel,
            authorizations);
    entries.put(entry.id, entry);
    return entry;
  }

  /** {@code IKeystoreSecurityLevel} for one security level. */
  private final class SecurityLevelDelegate extends IKeystoreSecurityLevel.Stub {
    private final int securityLevel;

    SecurityLevelDelegate(int securityLevel) {
      this.securityLevel = securityLevel;
    }

    @Override
    public KeyMetadata generateKey(
        KeyDescriptor descriptor,
        @Nullable KeyDescriptor attestationKey,
        KeyParameter[] params,
        int flags,
        byte[] entropy) {
      List<KeyParameter> authorizations = authorizations(params, KeyOrigin.GENERATED);
      KeyMaterial material = generateMaterial(authorizations);
      return store(descriptor, params, authorizations, material);
    }

    @Override
    public KeyMetadata importKey(
        KeyDescriptor descriptor,
        @Nullable KeyDescriptor attestationKey,
        KeyParameter[] params,
        int flags,
        byte[] keyData) {
      List<KeyParameter> authorizations = authorizations(params, KeyOrigin.IMPORTED);
      KeyMaterial material = importMaterial(authorizations, keyData);
      return store(descriptor, params, authorizations, material);
    }

    @Override
    public CreateOperationResponse createOperation(
        KeyDescriptor descriptor, KeyParameter[] params, boolean forced) {
      KeyEntry entry;
      synchronized (lock) {
        entry = find(descriptor);
      }
      if (!entry.hasKey()) {
        throw error(ResponseCode.KEY_NOT_FOUND, "Entry has no key");
      }
      return new Operation(entry, Arrays.asList(params)).begin();
    }

    @Override
    public void deleteKey(KeyDescriptor descriptor) {}

    @Override
    public KeyMetadata importWrappedKey(
        KeyDescriptor descriptor,
        KeyDescriptor wrappingKey,
        @Nullable byte[] maskingKey,
        KeyParameter[] params,
        AuthenticatorSpec[] authenticators) {
      throw unsupported("importWrappedKey");
    }

    @Override
    public EphemeralStorageKeyResponse convertStorageKeyToEphemeral(KeyDescriptor storageKey) {
      throw unsupported("convertStorageKeyToEphemeral");
    }

    @Override
    public int getInterfaceVersion() {
      return VERSION;
    }

    @Override
    public String getInterfaceHash() {
      return HASH;
    }

    private KeyMetadata store(
        KeyDescriptor descriptor,
        KeyParameter[] params,
        List<KeyParameter> authorizations,
        KeyMaterial material) {
      byte[][] certificates = material.keyPair == null ? null : certificates(params, material);
      synchronized (lock) {
        KeyEntry entry = newEntry(descriptor, securityLevel, authorizations);
        entry.secret = material.secret;
        entry.keyPair = material.keyPair;
        if (certificates != null) {
          entry.certificate = certificates[0];
          entry.certificateChain = certificates[1];
        }
        entry.remainingUses = intValue(authorizations, Tag.USAGE_COUNT_LIMIT, -1);
        entry.modificationTimeMs = ShadowSystem.currentTimeMillis();
        return entry.metadata();
      }
    }
  }

  /** The tags of key generation and import parameters that are not authorizations of the key. */
  private static boolean isAuthorization(int tag) {
    switch (tag) {
      case Tag.APPLICATION_ID:
      case Tag.APPLICATION_DATA:
      case Tag.ATTESTATION_CHALLENGE:
      case Tag.ATTESTATION_APPLICATION_ID:
      case Tag.CERTIFICATE_SERIAL:
      case Tag.CERTIFICATE_SUBJECT:
      case Tag.CERTIFICATE_NOT_BEFORE:
      case Tag.CERTIFICATE_NOT_AFTER:
      case Tag.INCLUDE_UNIQUE_ID:
      case Tag.RESET_SINCE_ID_ROTATION:
        return false;
      default:
        return true;
    }
  }

  private static List<KeyParameter> authorizations(KeyParameter[] params, int origin) {
    List<KeyParameter> authorizations = new ArrayList<>();
    for (KeyParameter param : params) {
      if (isAuthorization(param.tag)) {
        authorizations.add(param);
      }
    }
    authorizations.add(param(Tag.ORIGIN, KeyParameterValue.origin(origin)));
    return authorizations;
  }

  private static KeyMaterial generateMaterial(List<KeyParameter> authorizations) {
    int algorithm = intValue(authorizations, Tag.ALGORITHM, -1);
    int keySize = intValue(authorizations, Tag.KEY_SIZE, -1);
    try {
      switch (algorithm) {
        case Algorithm.AES:
          if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw error(ErrorCode.UNSUPPORTED_KEY_SIZE, "AES key size " + keySize);
          }
          return KeyMaterial.secret(randomBytes(keySize / 8));
        case Algorithm.HMAC:
          if (keySize < 64 || keySize > 512 || keySize % 8 != 0) {
            throw error(ErrorCode.UNSUPPORTED_KEY_SIZE, "HMAC key size " + keySize);
          }
          return KeyMaterial.secret(randomBytes(keySize / 8));
        case Algorithm.RSA:
          {
            if (keySize < 512 || keySize > 8192 || keySize % 8 != 0) {
              throw error(ErrorCode.UNSUPPORTED_KEY_SIZE, "RSA key size " + keySize);
            }
            long exponent = longValue(authorizations, Tag.RSA_PUBLIC_EXPONENT, 65537);
            KeyPairGenerator generator = software(a -> KeyPairGenerator.getInstance("RSA", a));
            generator.initialize(
                new RSAKeyGenParameterSpec(keySize, BigInteger.valueOf(exponent)), random);
            return KeyMaterial.keyPair(generator.generateKeyPair());
          }
        case Algorithm.EC:
          {
            int curve = ecCurve(authorizations, keySize);
            if (!hasTag(authorizations, Tag.EC_CURVE)) {
              authorizations.add(param(Tag.EC_CURVE, KeyParameterValue.ecCurve(curve)));
            }
            if (!hasTag(authorizations, Tag.KEY_SIZE)) {
              authorizations.add(
                  param(Tag.KEY_SIZE, KeyParameterValue.integer(ecCurveSize(curve))));
            }
            KeyPairGenerator generator = software(a -> KeyPairGenerator.getInstance("EC", a));
            generator.initialize(new ECGenParameterSpec(ecCurveName(curve)), random);
            return KeyMaterial.keyPair(generator.generateKeyPair());
          }
        default:
          throw error(ErrorCode.UNSUPPORTED_ALGORITHM, "Algorithm " + algorithm);
      }
    } catch (GeneralSecurityException e) {
      throw error(ErrorCode.UNKNOWN_ERROR, e.toString());
    }
  }

  private static KeyMaterial importMaterial(List<KeyParameter> authorizations, byte[] keyData) {
    int algorithm = intValue(authorizations, Tag.ALGORITHM, -1);
    try {
      switch (algorithm) {
        case Algorithm.AES:
        case Algorithm.HMAC:
          if (!hasTag(authorizations, Tag.KEY_SIZE)) {
            authorizations.add(param(Tag.KEY_SIZE, KeyParameterValue.integer(keyData.length * 8)));
          }
          return KeyMaterial.secret(keyData.clone());
        case Algorithm.RSA:
          {
            RSAPrivateCrtKey privateKey =
                (RSAPrivateCrtKey)
                    software(a -> KeyFactory.getInstance("RSA", a))
                        .generatePrivate(new PKCS8EncodedKeySpec(keyData));
            PublicKey publicKey =
                software(a -> KeyFactory.getInstance("RSA", a))
                    .generatePublic(
                        new RSAPublicKeySpec(
                            privateKey.getModulus(), privateKey.getPublicExponent()));
            if (!hasTag(authorizations, Tag.KEY_SIZE)) {
              authorizations.add(
                  param(
                      Tag.KEY_SIZE,
                      KeyParameterValue.integer(privateKey.getModulus().bitLength())));
            }
            if (!hasTag(authorizations, Tag.RSA_PUBLIC_EXPONENT)) {
              authorizations.add(
                  param(
                      Tag.RSA_PUBLIC_EXPONENT,
                      KeyParameterValue.longInteger(
                          privateKey.getPublicExponent().longValueExact())));
            }
            return KeyMaterial.keyPair(new KeyPair(publicKey, privateKey));
          }
        case Algorithm.EC:
          {
            ECPrivateKey privateKey =
                (ECPrivateKey)
                    software(a -> KeyFactory.getInstance("EC", a))
                        .generatePrivate(new PKCS8EncodedKeySpec(keyData));
            ECParameterSpec spec = privateKey.getParams();
            int size = spec.getOrder().bitLength();
            int curve = ecCurve(authorizations, size);
            // Use the named curve's parameters, which the providers' key factories recognize.
            spec = ecParameterSpec(curve);
            ECPoint point = multiply(spec, spec.getGenerator(), privateKey.getS());
            PublicKey publicKey =
                software(a -> KeyFactory.getInstance("EC", a))
                    .generatePublic(new ECPublicKeySpec(point, spec));
            if (!hasTag(authorizations, Tag.EC_CURVE)) {
              authorizations.add(param(Tag.EC_CURVE, KeyParameterValue.ecCurve(curve)));
            }
            if (!hasTag(authorizations, Tag.KEY_SIZE)) {
              authorizations.add(param(Tag.KEY_SIZE, KeyParameterValue.integer(size)));
            }
            return KeyMaterial.keyPair(new KeyPair(publicKey, privateKey));
          }
        default:
          throw error(ErrorCode.UNSUPPORTED_ALGORITHM, "Algorithm " + algorithm);
      }
    } catch (GeneralSecurityException | ClassCastException | ArithmeticException e) {
      throw error(ErrorCode.INVALID_ARGUMENT, e.toString());
    }
  }

  /** One crypto operation, from {@code createOperation} to {@code finish} or {@code abort}. */
  private static final class Operation extends IKeystoreOperation.Stub {
    private final KeyEntry entry;
    private final List<KeyParameter> params;
    private final int purpose;
    private final int algorithm;
    private final ByteArrayOutputStream input = new ByteArrayOutputStream();
    private final ByteArrayOutputStream aad = new ByteArrayOutputStream();
    private int blockMode = -1;
    private int padding = PaddingMode.NONE;
    private int digest = Digest.NONE;
    private int macLengthBits;
    @Nullable private byte[] nonce;
    private long challenge;
    private boolean authorized;
    private boolean finished;

    Operation(KeyEntry entry, List<KeyParameter> params) {
      this.entry = entry;
      this.params = params;
      this.purpose = intValue(params, Tag.PURPOSE, -1);
      this.algorithm = intValue(entry.authorizations, Tag.ALGORITHM, -1);
    }

    CreateOperationResponse begin() {
      List<KeyParameter> key = entry.authorizations;
      if (!intValues(key, Tag.PURPOSE).contains(purpose)) {
        throw error(ErrorCode.INCOMPATIBLE_PURPOSE, "Purpose " + purpose + " not authorized");
      }
      checkValidity(key);
      if (hasTag(key, Tag.UNLOCKED_DEVICE_REQUIRED) && ShadowKeyguardManager.isDeviceLockedNow()) {
        throw error(ErrorCode.DEVICE_LOCKED, "Device is locked");
      }
      if (entry.remainingUses == 0) {
        throw error(ErrorCode.KEY_MAX_OPS_EXCEEDED, "Usage count limit exceeded");
      }
      checkUserAuthentication(key);
      List<KeyParameter> responseParams = new ArrayList<>();
      switch (algorithm) {
        case Algorithm.AES:
          beginAes(key, responseParams);
          break;
        case Algorithm.HMAC:
          beginHmac(key);
          break;
        case Algorithm.RSA:
        case Algorithm.EC:
          beginAsymmetric(key);
          break;
        default:
          throw error(ErrorCode.UNSUPPORTED_ALGORITHM, "Algorithm " + algorithm);
      }

      CreateOperationResponse response = new CreateOperationResponse();
      response.iOperation = this;
      if (challenge != 0) {
        response.operationChallenge = new OperationChallenge();
        response.operationChallenge.challenge = challenge;
      }
      if (!responseParams.isEmpty()) {
        response.parameters = new KeyParameters();
        response.parameters.keyParameter = responseParams.toArray(new KeyParameter[0]);
      }
      return response;
    }

    private void checkValidity(List<KeyParameter> key) {
      long now = ShadowSystem.currentTimeMillis();
      Long activeTime = dateValue(key, Tag.ACTIVE_DATETIME);
      if (activeTime != null && now < activeTime) {
        throw error(ErrorCode.KEY_NOT_YET_VALID, "Key not yet valid");
      }
      boolean origination = purpose == KeyPurpose.ENCRYPT || purpose == KeyPurpose.SIGN;
      Long expireTime =
          dateValue(key, origination ? Tag.ORIGINATION_EXPIRE_DATETIME : Tag.USAGE_EXPIRE_DATETIME);
      if (expireTime != null && now > expireTime) {
        throw error(ErrorCode.KEY_EXPIRED, "Key expired");
      }
    }

    private void checkUserAuthentication(List<KeyParameter> key) {
      List<Long> secureIds = longValues(key, Tag.USER_SECURE_ID);
      if (secureIds.isEmpty()) {
        authorized = true;
        return;
      }
      int timeoutSeconds = intValue(key, Tag.AUTH_TIMEOUT, -1);
      if (timeoutSeconds < 0) {
        // The key must be authorized for this operation, e.g. through a BiometricPrompt with a
        // CryptoObject. update() and finish() check that it was.
        do {
          challenge = random.nextLong();
        } while (challenge == 0);
        synchronized (lock) {
          operationsAwaitingAuthorization.add(this);
        }
        return;
      }
      AuthToken token = findAuthToken(secureIds, intValue(key, Tag.USER_AUTH_TYPE, 0), null);
      if (token == null
          || SystemClock.elapsedRealtime() - token.timeReceivedMs > timeoutSeconds * 1000L) {
        throw error(ErrorCode.KEY_USER_NOT_AUTHENTICATED, "User not authenticated");
      }
      authorized = true;
    }

    private void beginAes(List<KeyParameter> key, List<KeyParameter> responseParams) {
      blockMode = authorizedEnum(key, Tag.BLOCK_MODE, ErrorCode.INCOMPATIBLE_BLOCK_MODE);
      padding = authorizedEnum(key, Tag.PADDING, ErrorCode.INCOMPATIBLE_PADDING_MODE);
      if (purpose != KeyPurpose.ENCRYPT && purpose != KeyPurpose.DECRYPT) {
        throw error(ErrorCode.UNSUPPORTED_PURPOSE, "AES purpose " + purpose);
      }
      if (blockMode == BlockMode.GCM || blockMode == BlockMode.CTR) {
        if (padding != PaddingMode.NONE) {
          throw error(ErrorCode.INCOMPATIBLE_PADDING_MODE, "Padding requires ECB or CBC");
        }
      }
      if (blockMode == BlockMode.GCM) {
        macLengthBits = intValue(params, Tag.MAC_LENGTH, -1);
        if (macLengthBits < 0) {
          throw error(ErrorCode.MISSING_MAC_LENGTH, "Missing MAC length");
        }
        if (macLengthBits > 128 || macLengthBits % 8 != 0) {
          throw error(ErrorCode.UNSUPPORTED_MAC_LENGTH, "MAC length " + macLengthBits);
        }
        if (macLengthBits < intValue(key, Tag.MIN_MAC_LENGTH, 0)) {
          throw error(ErrorCode.INVALID_MAC_LENGTH, "MAC length " + macLengthBits);
        }
      }
      if (blockMode == BlockMode.ECB) {
        return;
      }
      int nonceLength = blockMode == BlockMode.GCM ? 12 : 16;
      nonce = blobValue(params, Tag.NONCE);
      if (nonce == null) {
        if (purpose == KeyPurpose.DECRYPT) {
          throw error(ErrorCode.MISSING_NONCE, "Missing IV");
        }
        nonce = randomBytes(nonceLength);
        responseParams.add(param(Tag.NONCE, KeyParameterValue.blob(nonce)));
      } else {
        if (purpose == KeyPurpose.ENCRYPT && !hasTag(key, Tag.CALLER_NONCE)) {
          throw error(ErrorCode.CALLER_NONCE_PROHIBITED, "Caller-provided IV not permitted");
        }
        if (nonce.length != nonceLength) {
          throw error(ErrorCode.INVALID_NONCE, "Invalid IV length " + nonce.length);
        }
      }
    }

    private void beginHmac(List<KeyParameter> key) {
      if (purpose != KeyPurpose.SIGN && purpose != KeyPurpose.VERIFY) {
        throw error(ErrorCode.UNSUPPORTED_PURPOSE, "HMAC purpose " + purpose);
      }
      digest = intValue(key, Tag.DIGEST, Digest.NONE);
      if (hasTag(params, Tag.DIGEST) && intValue(params, Tag.DIGEST, -1) != digest) {
        throw error(ErrorCode.INCOMPATIBLE_DIGEST, "Digest not authorized");
      }
      int digestBits = digestSizeBits(digest);
      if (purpose == KeyPurpose.SIGN) {
        macLengthBits = intValue(params, Tag.MAC_LENGTH, -1);
        if (macLengthBits < 0) {
          throw error(ErrorCode.MISSING_MAC_LENGTH, "Missing MAC length");
        }
        if (macLengthBits > digestBits || macLengthBits % 8 != 0) {
          throw error(ErrorCode.UNSUPPORTED_MAC_LENGTH, "MAC length " + macLengthBits);
        }
        if (macLengthBits < intValue(key, Tag.MIN_MAC_LENGTH, 0)) {
          throw error(ErrorCode.INVALID_MAC_LENGTH, "MAC length " + macLengthBits);
        }
      }
    }

    private void beginAsymmetric(List<KeyParameter> key) {
      switch (purpose) {
        case KeyPurpose.SIGN:
          digest = authorizedEnum(key, Tag.DIGEST, ErrorCode.INCOMPATIBLE_DIGEST);
          if (algorithm == Algorithm.RSA) {
            padding = authorizedEnum(key, Tag.PADDING, ErrorCode.INCOMPATIBLE_PADDING_MODE);
            if (padding == PaddingMode.RSA_PSS && digest == Digest.NONE) {
              throw error(ErrorCode.INCOMPATIBLE_DIGEST, "PSS requires a digest");
            }
          }
          break;
        case KeyPurpose.DECRYPT:
          if (algorithm != Algorithm.RSA) {
            throw error(ErrorCode.UNSUPPORTED_PURPOSE, "EC keys can't decrypt");
          }
          padding = authorizedEnum(key, Tag.PADDING, ErrorCode.INCOMPATIBLE_PADDING_MODE);
          if (padding == PaddingMode.RSA_OAEP) {
            digest = authorizedEnum(key, Tag.DIGEST, ErrorCode.INCOMPATIBLE_DIGEST);
          }
          break;
        case KeyPurpose.AGREE_KEY:
          if (algorithm != Algorithm.EC) {
            throw error(ErrorCode.UNSUPPORTED_PURPOSE, "RSA keys can't agree keys");
          }
          break;
        default:
          throw error(ErrorCode.UNSUPPORTED_PURPOSE, "Purpose " + purpose);
      }
    }

    /**
     * Returns the operation's value of {@code tag}, which must be one of the key's authorized
     * values, or the key's only value if the operation doesn't specify one.
     */
    private int authorizedEnum(List<KeyParameter> key, int tag, int incompatibleError) {
      List<Integer> authorized = intValues(key, tag);
      int value;
      if (hasTag(params, tag)) {
        value = intValue(params, tag, -1);
      } else if (authorized.size() == 1) {
        value = authorized.get(0);
      } else if (tag == Tag.PADDING || tag == Tag.DIGEST) {
        value = tag == Tag.PADDING ? PaddingMode.NONE : Digest.NONE;
      } else {
        throw error(incompatibleError, "Missing parameter " + tag);
      }
      if (!authorized.contains(value)) {
        throw error(incompatibleError, "Parameter " + tag + " value " + value + " not authorized");
      }
      return value;
    }

    // IKeystoreOperation

    @Override
    public void updateAad(byte[] aadInput) {
      checkActive();
      if (blockMode != BlockMode.GCM) {
        throw error(ErrorCode.INVALID_TAG, "AAD requires GCM");
      }
      if (input.size() > 0) {
        throw error(ErrorCode.INVALID_TAG, "AAD after input");
      }
      aad.write(aadInput, 0, aadInput.length);
    }

    @Override
    @Nullable
    public byte[] update(byte[] data) {
      checkActive();
      input.write(data, 0, data.length);
      return null;
    }

    @Override
    @Nullable
    public byte[] finish(@Nullable byte[] data, @Nullable byte[] signature) {
      checkActive();
      if (data != null) {
        input.write(data, 0, data.length);
      }
      end();
      byte[] output = compute(input.toByteArray(), signature);
      synchronized (lock) {
        if (entry.remainingUses > 0 && --entry.remainingUses == 0) {
          entries.remove(entry.id);
        }
      }
      return output;
    }

    @Override
    public void abort() {
      if (finished) {
        throw error(ErrorCode.INVALID_OPERATION_HANDLE, "Operation finished");
      }
      end();
    }

    private void end() {
      finished = true;
      synchronized (lock) {
        operationsAwaitingAuthorization.remove(this);
      }
    }

    @Override
    public int getInterfaceVersion() {
      return VERSION;
    }

    @Override
    public String getInterfaceHash() {
      return HASH;
    }

    private void checkActive() {
      if (finished) {
        throw error(ErrorCode.INVALID_OPERATION_HANDLE, "Operation finished");
      }
      if (!authorized) {
        AuthToken token =
            findAuthToken(
                longValues(entry.authorizations, Tag.USER_SECURE_ID),
                intValue(entry.authorizations, Tag.USER_AUTH_TYPE, 0),
                challenge);
        if (token == null) {
          throw error(ErrorCode.KEY_USER_NOT_AUTHENTICATED, "Operation not authorized");
        }
        authorized = true;
      }
    }

    private byte[] compute(byte[] data, @Nullable byte[] signature) {
      try {
        switch (algorithm) {
          case Algorithm.AES:
            return aes(data);
          case Algorithm.HMAC:
            return hmac(data, signature);
          case Algorithm.RSA:
            return purpose == KeyPurpose.SIGN ? sign(data) : rsaDecrypt(data);
          case Algorithm.EC:
            return purpose == KeyPurpose.SIGN ? sign(data) : agree(data);
          default:
            throw error(ErrorCode.UNSUPPORTED_ALGORITHM, "Algorithm " + algorithm);
        }
      } catch (javax.crypto.AEADBadTagException e) {
        throw error(ErrorCode.VERIFICATION_FAILED, e.toString());
      } catch (javax.crypto.IllegalBlockSizeException e) {
        throw error(ErrorCode.INVALID_INPUT_LENGTH, e.toString());
      } catch (javax.crypto.BadPaddingException e) {
        throw error(ErrorCode.INVALID_ARGUMENT, e.toString());
      } catch (GeneralSecurityException e) {
        throw error(ErrorCode.UNKNOWN_ERROR, e.toString());
      }
    }

    private byte[] aes(byte[] data) throws GeneralSecurityException {
      String mode;
      switch (blockMode) {
        case BlockMode.ECB:
          mode = "ECB";
          break;
        case BlockMode.CBC:
          mode = "CBC";
          break;
        case BlockMode.CTR:
          mode = "CTR";
          break;
        case BlockMode.GCM:
          mode = "GCM";
          break;
        default:
          throw error(ErrorCode.UNSUPPORTED_BLOCK_MODE, "Block mode " + blockMode);
      }
      String transformation =
          "AES/" + mode + "/" + (padding == PaddingMode.PKCS7 ? "PKCS5Padding" : "NoPadding");
      Cipher cipher = cipher(transformation);
      int cipherMode = purpose == KeyPurpose.ENCRYPT ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
      Key key = new SecretKeySpec(entry.secret, "AES");
      if (blockMode == BlockMode.GCM) {
        cipher.init(cipherMode, key, new GCMParameterSpec(macLengthBits, nonce));
        cipher.updateAAD(aad.toByteArray());
      } else if (nonce != null) {
        cipher.init(cipherMode, key, new IvParameterSpec(nonce));
      } else {
        cipher.init(cipherMode, key);
      }
      return cipher.doFinal(data);
    }

    private byte[] hmac(byte[] data, @Nullable byte[] signature) throws GeneralSecurityException {
      String name = "Hmac" + digestName(digest).replace("-", "");
      Mac mac = software(a -> Mac.getInstance(name, a));
      mac.init(new SecretKeySpec(entry.secret, name));
      byte[] result = mac.doFinal(data);
      if (purpose == KeyPurpose.SIGN) {
        return Arrays.copyOf(result, macLengthBits / 8);
      }
      if (signature == null
          || signature.length > result.length
          || signature.length < intValue(entry.authorizations, Tag.MIN_MAC_LENGTH, 0) / 8
          || !MessageDigest.isEqual(signature, Arrays.copyOf(result, signature.length))) {
        throw error(ErrorCode.VERIFICATION_FAILED, "HMAC verification failed");
      }
      return null;
    }

    private byte[] sign(byte[] data) throws GeneralSecurityException {
      PrivateKey privateKey = entry.keyPair.getPrivate();
      if (algorithm == Algorithm.RSA && padding == PaddingMode.NONE) {
        if (digest != Digest.NONE) {
          data = software(a -> MessageDigest.getInstance(digestName(digest), a)).digest(data);
        }
        Cipher cipher = cipher("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
      }
      String digestName = digest == Digest.NONE ? "NONE" : digestName(digest).replace("-", "");
      Signature signer;
      if (algorithm == Algorithm.EC) {
        signer = software(a -> Signature.getInstance(digestName + "withECDSA", a));
      } else if (padding == PaddingMode.RSA_PSS) {
        signer = software(a -> Signature.getInstance("RSASSA-PSS", a));
        signer.initSign(privateKey);
        int saltLength = digestSizeBits(digest) / 8;
        signer.setParameter(
            new PSSParameterSpec(
                digestName(digest),
                "MGF1",
                new MGF1ParameterSpec(digestName(digest)),
                saltLength,
                1));
        signer.update(data);
        return signer.sign();
      } else {
        signer = software(a -> Signature.getInstance(digestName + "withRSA", a));
      }
      signer.initSign(privateKey);
      signer.update(data);
      return signer.sign();
    }

    private byte[] rsaDecrypt(byte[] data) throws GeneralSecurityException {
      Cipher cipher;
      switch (padding) {
        case PaddingMode.RSA_OAEP:
          {
            cipher = cipher("RSA/ECB/OAEPPadding");
            int mgfDigest = intValue(params, Tag.RSA_OAEP_MGF_DIGEST, Digest.SHA1);
            cipher.init(
                Cipher.DECRYPT_MODE,
                entry.keyPair.getPrivate(),
                new OAEPParameterSpec(
                    digestName(digest),
                    "MGF1",
                    new MGF1ParameterSpec(digestName(mgfDigest)),
                    PSource.PSpecified.DEFAULT));
            return cipher.doFinal(data);
          }
        case PaddingMode.RSA_PKCS1_1_5_ENCRYPT:
          cipher = cipher("RSA/ECB/PKCS1Padding");
          break;
        case PaddingMode.NONE:
          cipher = cipher("RSA/ECB/NoPadding");
          break;
        default:
          throw error(ErrorCode.UNSUPPORTED_PADDING_MODE, "Padding " + padding);
      }
      cipher.init(Cipher.DECRYPT_MODE, entry.keyPair.getPrivate());
      return cipher.doFinal(data);
    }

    private byte[] agree(byte[] peerPublicKey) throws GeneralSecurityException {
      PublicKey peer =
          software(a -> KeyFactory.getInstance("EC", a))
              .generatePublic(new X509EncodedKeySpec(peerPublicKey));
      KeyAgreement agreement = software(a -> KeyAgreement.getInstance("ECDH", a));
      agreement.init(entry.keyPair.getPrivate());
      agreement.doPhase(peer, true);
      return agreement.generateSecret();
    }
  }

  /**
   * Returns a cipher for {@code transformation} from a software provider, never from the
   * AndroidKeyStore providers that call into this daemon.
   */
  private static Cipher cipher(String transformation) throws GeneralSecurityException {
    return software(provider -> Cipher.getInstance(transformation, provider));
  }

  private interface ProviderLookup<T> {
    T get(Provider provider) throws GeneralSecurityException;
  }

  /**
   * Returns the implementation from the first software provider that has one, never from the
   * AndroidKeyStore providers that call into this daemon.
   */
  private static <T> T software(ProviderLookup<T> lookup) throws GeneralSecurityException {
    GeneralSecurityException failure = null;
    for (Provider provider : Security.getProviders()) {
      if (!provider.getName().startsWith("AndroidKeyStore")) {
        try {
          return lookup.get(provider);
        } catch (GeneralSecurityException e) {
          failure = e;
        }
      }
    }
    throw failure != null ? failure : new java.security.NoSuchAlgorithmException();
  }

  @Nullable
  private static AuthToken findAuthToken(
      List<Long> secureIds, int authenticatorTypes, @Nullable Long challenge) {
    synchronized (lock) {
      AuthToken newest = null;
      for (AuthToken token : authTokens) {
        if ((challenge == null || token.challenge == challenge)
            && (authenticatorTypes & token.authenticatorType) != 0
            && (secureIds.contains(token.userSecureId) || secureIds.contains(token.authenticatorId))
            && (newest == null || token.timeReceivedMs >= newest.timeReceivedMs)) {
          newest = token;
        }
      }
      return newest;
    }
  }

  /** Returns the certificate and the DER-encoded certificate chain for a new key pair. */
  private static byte[][] certificates(KeyParameter[] params, KeyMaterial material) {
    List<KeyParameter> list = Arrays.asList(params);
    byte[] subject = blobValue(list, Tag.CERTIFICATE_SUBJECT);
    if (subject == null || subject.length == 0) {
      subject = new X500Principal("CN=Android Keystore Key").getEncoded();
    }
    byte[] serial = blobValue(list, Tag.CERTIFICATE_SERIAL);
    BigInteger serialNumber = serial == null ? BigInteger.ONE : new BigInteger(1, serial);
    Long notBefore = dateValue(list, Tag.CERTIFICATE_NOT_BEFORE);
    Long notAfter = dateValue(list, Tag.CERTIFICATE_NOT_AFTER);
    KeyPair keyPair = material.keyPair;
    boolean canSign = intValues(list, Tag.PURPOSE).contains(KeyPurpose.SIGN);
    try {
      // Like KeyMint, self-sign the certificate if the key can sign. Otherwise sign it with an
      // unrelated key, so that its signature doesn't verify.
      PrivateKey signingKey = canSign ? keyPair.getPrivate() : unrelatedSigningKey();
      byte[] certificate =
          Der.certificate(
              serialNumber,
              subject,
              subject,
              notBefore == null ? 0 : notBefore,
              notAfter == null ? 253402300799000L : notAfter,
              keyPair.getPublic(),
              signingKey);
      return new byte[][] {certificate, null};
    } catch (GeneralSecurityException e) {
      throw error(ErrorCode.UNKNOWN_ERROR, e.toString());
    }
  }

  @GuardedBy("lock")
  @Nullable
  private static PrivateKey unrelatedSigningKey;

  private static PrivateKey unrelatedSigningKey() throws GeneralSecurityException {
    synchronized (lock) {
      if (unrelatedSigningKey == null) {
        KeyPairGenerator generator = software(a -> KeyPairGenerator.getInstance("EC", a));
        generator.initialize(new ECGenParameterSpec("secp256r1"), random);
        unrelatedSigningKey = generator.generateKeyPair().getPrivate();
      }
      return unrelatedSigningKey;
    }
  }

  /** A minimal DER encoder for X.509 certificates. */
  private static final class Der {
    private static final byte[] SHA256_WITH_RSA =
        sequence(
            tlv(0x06, hex("2a864886f70d01010b")), // 1.2.840.113549.1.1.11
            tlv(0x05, new byte[0]));
    private static final byte[] ECDSA_WITH_SHA256 =
        sequence(tlv(0x06, hex("2a8648ce3d040302"))); // 1.2.840.10045.4.3.2

    static byte[] certificate(
        BigInteger serial,
        byte[] issuer,
        byte[] subject,
        long notBefore,
        long notAfter,
        PublicKey publicKey,
        PrivateKey signingKey)
        throws GeneralSecurityException {
      boolean rsa = signingKey.getAlgorithm().equals("RSA");
      byte[] algorithm = rsa ? SHA256_WITH_RSA : ECDSA_WITH_SHA256;
      byte[] tbs =
          sequence(
              tlv(0xa0, tlv(0x02, new byte[] {2})), // version v3
              tlv(0x02, serial.toByteArray()),
              algorithm,
              issuer,
              sequence(time(notBefore), time(notAfter)),
              subject,
              publicKey.getEncoded());
      Signature signature =
          software(a -> Signature.getInstance(rsa ? "SHA256withRSA" : "SHA256withECDSA", a));
      signature.initSign(signingKey);
      signature.update(tbs);
      byte[] signed = signature.sign();
      byte[] bitString = new byte[signed.length + 1];
      System.arraycopy(signed, 0, bitString, 1, signed.length);
      return sequence(tbs, algorithm, tlv(0x03, bitString));
    }

    private static byte[] time(long millis) {
      SimpleDateFormat format;
      int tag;
      Date date = new Date(millis);
      SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.US);
      year.setTimeZone(TimeZone.getTimeZone("UTC"));
      int y = Integer.parseInt(year.format(date));
      if (y >= 1950 && y < 2050) {
        format = new SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US);
        tag = 0x17; // UTCTime
      } else {
        format = new SimpleDateFormat("yyyyMMddHHmmss'Z'", Locale.US);
        tag = 0x18; // GeneralizedTime
      }
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      return tlv(tag, format.format(date).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private static byte[] sequence(byte[]... elements) {
      ByteArrayOutputStream content = new ByteArrayOutputStream();
      for (byte[] element : elements) {
        content.write(element, 0, element.length);
      }
      return tlv(0x30, content.toByteArray());
    }

    private static byte[] tlv(int tag, byte[] value) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(tag);
      int length = value.length;
      if (length < 0x80) {
        out.write(length);
      } else {
        int bytes = (Integer.SIZE - Integer.numberOfLeadingZeros(length) + 7) / 8;
        out.write(0x80 | bytes);
        for (int i = bytes - 1; i >= 0; i--) {
          out.write(length >>> (8 * i));
        }
      }
      out.write(value, 0, value.length);
      return out.toByteArray();
    }

    private static byte[] hex(String hex) {
      return new BigInteger(hex, 16).toByteArray();
    }
  }

  // Key parameter helpers

  private static KeyParameter param(int tag, KeyParameterValue value) {
    KeyParameter param = new KeyParameter();
    param.tag = tag;
    param.value = value;
    return param;
  }

  private static boolean hasTag(List<KeyParameter> params, int tag) {
    for (KeyParameter param : params) {
      if (param.tag == tag) {
        return true;
      }
    }
    return false;
  }

  private static List<Integer> intValues(List<KeyParameter> params, int tag) {
    List<Integer> values = new ArrayList<>();
    for (KeyParameter param : params) {
      if (param.tag == tag) {
        values.add(intOf(param.value));
      }
    }
    return values;
  }

  private static int intValue(List<KeyParameter> params, int tag, int defaultValue) {
    List<Integer> values = intValues(params, tag);
    return values.isEmpty() ? defaultValue : values.get(0);
  }

  private static List<Long> longValues(List<KeyParameter> params, int tag) {
    List<Long> values = new ArrayList<>();
    for (KeyParameter param : params) {
      if (param.tag == tag) {
        values.add(param.value.getLongInteger());
      }
    }
    return values;
  }

  private static long longValue(List<KeyParameter> params, int tag, long defaultValue) {
    List<Long> values = longValues(params, tag);
    return values.isEmpty() ? defaultValue : values.get(0);
  }

  @Nullable
  private static Long dateValue(List<KeyParameter> params, int tag) {
    for (KeyParameter param : params) {
      if (param.tag == tag) {
        return param.value.getDateTime();
      }
    }
    return null;
  }

  @Nullable
  private static byte[] blobValue(List<KeyParameter> params, int tag) {
    for (KeyParameter param : params) {
      if (param.tag == tag) {
        return param.value.getBlob();
      }
    }
    return null;
  }

  /** Returns the integer or enum value of {@code value}. */
  private static int intOf(KeyParameterValue value) {
    switch (value.getTag()) {
      case KeyParameterValue.algorithm:
        return value.getAlgorithm();
      case KeyParameterValue.blockMode:
        return value.getBlockMode();
      case KeyParameterValue.paddingMode:
        return value.getPaddingMode();
      case KeyParameterValue.digest:
        return value.getDigest();
      case KeyParameterValue.ecCurve:
        return value.getEcCurve();
      case KeyParameterValue.origin:
        return value.getOrigin();
      case KeyParameterValue.keyPurpose:
        return value.getKeyPurpose();
      case KeyParameterValue.hardwareAuthenticatorType:
        return value.getHardwareAuthenticatorType();
      case KeyParameterValue.securityLevel:
        return value.getSecurityLevel();
      case KeyParameterValue.integer:
        return value.getInteger();
      default:
        throw error(ErrorCode.INVALID_TAG, "Not an integer value: " + value.getTag());
    }
  }

  // Algorithm helpers

  private static int ecCurve(List<KeyParameter> params, int keySize) {
    if (hasTag(params, Tag.EC_CURVE)) {
      return intValue(params, Tag.EC_CURVE, -1);
    }
    switch (keySize) {
      case 224:
        return EcCurve.P_224;
      case 256:
        return EcCurve.P_256;
      case 384:
        return EcCurve.P_384;
      case 521:
        return EcCurve.P_521;
      default:
        throw error(ErrorCode.UNSUPPORTED_KEY_SIZE, "EC key size " + keySize);
    }
  }

  private static String ecCurveName(int curve) {
    switch (curve) {
      case EcCurve.P_224:
        return "secp224r1";
      case EcCurve.P_256:
        return "secp256r1";
      case EcCurve.P_384:
        return "secp384r1";
      case EcCurve.P_521:
        return "secp521r1";
      default:
        throw error(ErrorCode.UNSUPPORTED_EC_CURVE, "EC curve " + curve);
    }
  }

  private static int ecCurveSize(int curve) {
    switch (curve) {
      case EcCurve.P_224:
        return 224;
      case EcCurve.P_256:
        return 256;
      case EcCurve.P_384:
        return 384;
      case EcCurve.P_521:
        return 521;
      default:
        throw error(ErrorCode.UNSUPPORTED_EC_CURVE, "EC curve " + curve);
    }
  }

  private static ECParameterSpec ecParameterSpec(int curve) throws GeneralSecurityException {
    AlgorithmParameters parameters = software(a -> AlgorithmParameters.getInstance("EC", a));
    try {
      parameters.init(new ECGenParameterSpec(ecCurveName(curve)));
    } catch (java.security.spec.InvalidParameterSpecException e) {
      throw new GeneralSecurityException(e);
    }
    return parameters.getParameterSpec(ECParameterSpec.class);
  }

  /** Computes {@code k * point} on a prime curve, to derive an imported key's public key. */
  private static ECPoint multiply(ECParameterSpec spec, ECPoint point, BigInteger k) {
    ECPoint result = ECPoint.POINT_INFINITY;
    ECPoint addend = point;
    for (int i = 0; i < k.bitLength(); i++) {
      if (k.testBit(i)) {
        result = add(spec, result, addend);
      }
      addend = add(spec, addend, addend);
    }
    return result;
  }

  private static ECPoint add(ECParameterSpec spec, ECPoint a, ECPoint b) {
    if (a.equals(ECPoint.POINT_INFINITY)) {
      return b;
    }
    if (b.equals(ECPoint.POINT_INFINITY)) {
      return a;
    }
    BigInteger p = ((ECFieldFp) spec.getCurve().getField()).getP();
    BigInteger lambda;
    if (a.equals(b)) {
      if (a.getAffineY().signum() == 0) {
        return ECPoint.POINT_INFINITY;
      }
      BigInteger x = a.getAffineX();
      lambda =
          x.pow(2)
              .multiply(BigInteger.valueOf(3))
              .add(spec.getCurve().getA())
              .multiply(a.getAffineY().shiftLeft(1).modInverse(p))
              .mod(p);
    } else {
      if (a.getAffineX().equals(b.getAffineX())) {
        return ECPoint.POINT_INFINITY;
      }
      lambda =
          b.getAffineY()
              .subtract(a.getAffineY())
              .multiply(b.getAffineX().subtract(a.getAffineX()).modInverse(p))
              .mod(p);
    }
    BigInteger x = lambda.pow(2).subtract(a.getAffineX()).subtract(b.getAffineX()).mod(p);
    BigInteger y = lambda.multiply(a.getAffineX().subtract(x)).subtract(a.getAffineY()).mod(p);
    return new ECPoint(x, y);
  }

  private static String digestName(int digest) {
    switch (digest) {
      case Digest.MD5:
        return "MD5";
      case Digest.SHA1:
        return "SHA-1";
      case Digest.SHA_2_224:
        return "SHA-224";
      case Digest.SHA_2_256:
        return "SHA-256";
      case Digest.SHA_2_384:
        return "SHA-384";
      case Digest.SHA_2_512:
        return "SHA-512";
      default:
        throw error(ErrorCode.UNSUPPORTED_DIGEST, "Digest " + digest);
    }
  }

  private static int digestSizeBits(int digest) {
    switch (digest) {
      case Digest.MD5:
        return 128;
      case Digest.SHA1:
        return 160;
      case Digest.SHA_2_224:
        return 224;
      case Digest.SHA_2_256:
        return 256;
      case Digest.SHA_2_384:
        return 384;
      case Digest.SHA_2_512:
        return 512;
      default:
        throw error(ErrorCode.UNSUPPORTED_DIGEST, "Digest " + digest);
    }
  }

  private static byte[] randomBytes(int length) {
    byte[] bytes = new byte[length];
    random.nextBytes(bytes);
    return bytes;
  }

  private static ServiceSpecificException error(int errorCode, String message) {
    return new ServiceSpecificException(errorCode, message);
  }

  private static final class KeyMaterial {
    @Nullable final byte[] secret;
    @Nullable final KeyPair keyPair;

    private KeyMaterial(@Nullable byte[] secret, @Nullable KeyPair keyPair) {
      this.secret = secret;
      this.keyPair = keyPair;
    }

    static KeyMaterial secret(byte[] secret) {
      return new KeyMaterial(secret, null);
    }

    static KeyMaterial keyPair(KeyPair keyPair) {
      return new KeyMaterial(null, keyPair);
    }
  }

  private static final class KeyEntry {
    final long id;
    final int domain;
    final long nspace;
    final String alias;
    final int securityLevel;
    final List<KeyParameter> authorizations;
    @Nullable byte[] secret;
    @Nullable KeyPair keyPair;
    @Nullable byte[] certificate;
    @Nullable byte[] certificateChain;
    int remainingUses = -1;
    long modificationTimeMs;
    boolean invalidated;

    KeyEntry(
        long id,
        int domain,
        long nspace,
        String alias,
        int securityLevel,
        List<KeyParameter> authorizations) {
      this.id = id;
      this.domain = domain;
      this.nspace = nspace;
      this.alias = alias;
      this.securityLevel = securityLevel;
      this.authorizations = authorizations;
    }

    boolean hasKey() {
      return secret != null || keyPair != null;
    }

    KeyDescriptor userDescriptor() {
      KeyDescriptor descriptor = new KeyDescriptor();
      descriptor.domain = domain;
      descriptor.nspace = nspace;
      descriptor.alias = alias;
      return descriptor;
    }

    KeyMetadata metadata() {
      KeyMetadata metadata = new KeyMetadata();
      metadata.key = new KeyDescriptor();
      metadata.key.domain = Domain.KEY_ID;
      metadata.key.nspace = id;
      metadata.keySecurityLevel = securityLevel;
      metadata.authorizations = new Authorization[authorizations.size()];
      for (int i = 0; i < authorizations.size(); i++) {
        Authorization authorization = new Authorization();
        authorization.securityLevel = securityLevel;
        authorization.keyParameter = authorizations.get(i);
        metadata.authorizations[i] = authorization;
      }
      metadata.certificate = certificate;
      metadata.certificateChain = certificateChain;
      metadata.modificationTimeMs = modificationTimeMs;
      return metadata;
    }
  }

  private static final class AuthToken {
    final long challenge;
    final long userSecureId;
    final long authenticatorId;
    final int authenticatorType;
    final long timeReceivedMs;

    AuthToken(
        long challenge,
        long userSecureId,
        long authenticatorId,
        int authenticatorType,
        long timeReceivedMs) {
      this.challenge = challenge;
      this.userSecureId = userSecureId;
      this.authenticatorId = authenticatorId;
      this.authenticatorType = authenticatorType;
      this.timeReceivedMs = timeReceivedMs;
    }
  }
}
