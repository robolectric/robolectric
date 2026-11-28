package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricManager.Authenticators;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.hardware.biometrics.BiometricPrompt.AuthenticationResult;
import android.os.CancellationSignal;
import android.os.SystemClock;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.security.keystore.StrongBoxUnavailableException;
import android.security.keystore.UserNotAuthenticatedException;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests the AndroidKeyStore provider on top of Robolectric's software keystore2 daemon. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = S)
public class AndroidKeyStoreTest {
  private static final String ALIAS = "key";
  private static final byte[] PLAINTEXT = "plaintext".getBytes(UTF_8);

  private final Context context = ApplicationProvider.getApplicationContext();
  private KeyStore keyStore;

  @Before
  public void setUp() throws Exception {
    keyStore = KeyStore.getInstance("AndroidKeyStore");
    keyStore.load(null);
  }

  @Test
  public void biometricKey_requiresASecureLockScreen() throws Exception {
    KeyGenerator generator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

    // Like on a device, biometrics are only enrolled with a secure lock screen.
    assertThrows(
        InvalidAlgorithmParameterException.class,
        () -> generator.init(gcmKeyBuilder(ALIAS).setUserAuthenticationRequired(true).build()));
  }

  @Test
  public void aesGcm_encryptsAndDecrypts() throws Exception {
    SecretKey key =
        generateAesKey(
            new KeyGenParameterSpec.Builder(
                    ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE));

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    cipher.updateAAD(new byte[] {1, 2, 3});
    byte[] ciphertext = cipher.doFinal(PLAINTEXT);
    byte[] iv = cipher.getIV();

    assertThat(iv).hasLength(12);
    assertThat(ciphertext).hasLength(PLAINTEXT.length + 16);
    cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(ALIAS, null), new GCMParameterSpec(128, iv));
    cipher.updateAAD(new byte[] {1, 2, 3});
    assertThat(cipher.doFinal(ciphertext)).isEqualTo(PLAINTEXT);
  }

  @Test
  public void aesGcm_tamperedCiphertext_failsToDecrypt() throws Exception {
    SecretKey key =
        generateAesKey(
            new KeyGenParameterSpec.Builder(
                    ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE));
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] ciphertext = cipher.doFinal(PLAINTEXT);
    ciphertext[0] ^= 1;

    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, cipher.getIV()));

    assertThrows(javax.crypto.AEADBadTagException.class, () -> cipher.doFinal(ciphertext));
  }

  @Test
  public void aesCbc_interoperatesWithSoftwareAes() throws Exception {
    byte[] rawKey = new byte[32];
    rawKey[0] = 42;
    keyStore.setEntry(
        ALIAS,
        new KeyStore.SecretKeyEntry(new SecretKeySpec(rawKey, "AES")),
        new KeyProtection.Builder(KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build());
    byte[] iv = new byte[16];
    Cipher software = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
    software.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rawKey, "AES"), new IvParameterSpec(iv));
    byte[] ciphertext = software.doFinal(PLAINTEXT);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
    cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(ALIAS, null), new IvParameterSpec(iv));

    assertThat(cipher.doFinal(ciphertext)).isEqualTo(PLAINTEXT);
  }

  @Test
  public void aes_unauthorizedBlockMode_isRejected() throws Exception {
    SecretKey key =
        generateAesKey(
            new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE));
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

    assertThrows(InvalidKeyException.class, () -> cipher.init(Cipher.ENCRYPT_MODE, key));
  }

  @Test
  public void hmac_signsLikeSoftwareHmac() throws Exception {
    byte[] rawKey = "a 256-bit secret key for hmac!!!".getBytes(UTF_8);
    keyStore.setEntry(
        ALIAS,
        new KeyStore.SecretKeyEntry(new SecretKeySpec(rawKey, "HmacSHA256")),
        new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN).build());
    Mac software = Mac.getInstance("HmacSHA256", "BC");
    software.init(new SecretKeySpec(rawKey, "HmacSHA256"));

    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(keyStore.getKey(ALIAS, null));

    assertThat(mac.doFinal(PLAINTEXT)).isEqualTo(software.doFinal(PLAINTEXT));
  }

  @Test
  public void ec_signaturesVerifyWithTheCertificate() throws Exception {
    KeyPairGenerator generator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
    generator.initialize(
        new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build());
    KeyPair keyPair = generator.generateKeyPair();

    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign(keyPair.getPrivate());
    signature.update(PLAINTEXT);
    byte[] signed = signature.sign();

    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(ALIAS);
    certificate.verify(certificate.getPublicKey());
    assertThat(certificate.getPublicKey()).isEqualTo(keyPair.getPublic());
    signature.initVerify(certificate);
    signature.update(PLAINTEXT);
    assertThat(signature.verify(signed)).isTrue();
  }

  @Test
  public void rsa_decryptsOaepAndSignsPss() throws Exception {
    KeyPairGenerator generator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
    generator.initialize(
        new KeyGenParameterSpec.Builder(
                ALIAS, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_SIGN)
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            .build());
    KeyPair keyPair = generator.generateKeyPair();

    // Like on a device, the key's MGF1 digest is SHA-1.
    OAEPParameterSpec oaep =
        new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
    Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    encrypt.init(Cipher.ENCRYPT_MODE, keyPair.getPublic(), oaep);
    byte[] ciphertext = encrypt.doFinal(PLAINTEXT);
    Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    decrypt.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaep);
    assertThat(decrypt.doFinal(ciphertext)).isEqualTo(PLAINTEXT);

    Signature sign = Signature.getInstance("SHA256withRSA/PSS");
    sign.initSign(keyPair.getPrivate());
    sign.update(PLAINTEXT);
    byte[] signed = sign.sign();
    Signature verify = Signature.getInstance("SHA256withRSA/PSS");
    verify.initVerify(keyPair.getPublic());
    verify.update(PLAINTEXT);
    assertThat(verify.verify(signed)).isTrue();
  }

  @Test
  public void importedEcKey_signs() throws Exception {
    KeyPairGenerator software = KeyPairGenerator.getInstance("EC", "BC");
    software.initialize(new ECGenParameterSpec("secp256r1"));
    KeyPair keyPair = software.generateKeyPair();
    KeyPairGenerator certificateGenerator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
    certificateGenerator.initialize(
        new KeyGenParameterSpec.Builder("certificate", KeyProperties.PURPOSE_SIGN)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build());
    certificateGenerator.generateKeyPair();
    X509Certificate certificate = (X509Certificate) keyStore.getCertificate("certificate");

    keyStore.setEntry(
        ALIAS,
        new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new X509Certificate[] {certificate}),
        new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build());
    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign((PrivateKey) keyStore.getKey(ALIAS, null));
    signature.update(PLAINTEXT);
    byte[] signed = signature.sign();

    signature.initVerify(keyPair.getPublic());
    signature.update(PLAINTEXT);
    assertThat(signature.verify(signed)).isTrue();
  }

  @Test
  public void keyStore_listsAndDeletesEntries() throws Exception {
    generateAesKey(gcmKey("first"));
    generateAesKey(gcmKey("second"));

    assertThat(Collections.list(keyStore.aliases())).containsExactly("first", "second");
    assertThat(keyStore.containsAlias("first")).isTrue();

    keyStore.deleteEntry("first");

    assertThat(Collections.list(keyStore.aliases())).containsExactly("second");
    assertThat(keyStore.getKey("first", null)).isNull();
  }

  @Test
  public void getEntry_withoutPassword_returnsTheKey() throws Exception {
    SecretKey key = generateAesKey(gcmKey(ALIAS));

    KeyStore.Entry entry = keyStore.getEntry(ALIAS, null);

    assertThat(((KeyStore.SecretKeyEntry) entry).getSecretKey()).isEqualTo(key);
  }

  @Test
  public void keys_doNotLeakBetweenTests() throws Exception {
    assertThat(keyStore.size()).isEqualTo(0);
  }

  @Test
  public void keyInfo_reportsHardwareBackedKey() throws Exception {
    SecretKey key = generateAesKey(gcmKeyBuilder(ALIAS).setKeySize(256));

    KeyInfo keyInfo =
        (KeyInfo)
            SecretKeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore")
                .getKeySpec(key, KeyInfo.class);

    assertThat(keyInfo.getKeystoreAlias()).isEqualTo(ALIAS);
    assertThat(keyInfo.getKeySize()).isEqualTo(256);
    assertThat(keyInfo.getSecurityLevel())
        .isEqualTo(KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT);
    assertThat(keyInfo.getOrigin()).isEqualTo(KeyProperties.ORIGIN_GENERATED);
    assertThat(keyInfo.isUserAuthenticationRequired()).isFalse();
  }

  @Test
  public void strongBox_isUnavailableWithoutTheSystemFeature() throws Exception {
    KeyGenerator generator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
    generator.init(gcmKeyBuilder(ALIAS).setIsStrongBoxBacked(true).build());

    assertThrows(StrongBoxUnavailableException.class, generator::generateKey);
  }

  @Test
  public void strongBox_backsKeysWithTheSystemFeature() throws Exception {
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE, true);

    SecretKey key = generateAesKey(gcmKeyBuilder(ALIAS).setIsStrongBoxBacked(true));

    KeyInfo keyInfo =
        (KeyInfo)
            SecretKeyFactory.getInstance(key.getAlgorithm(), "AndroidKeyStore")
                .getKeySpec(key, KeyInfo.class);
    assertThat(keyInfo.getSecurityLevel()).isEqualTo(KeyProperties.SECURITY_LEVEL_STRONGBOX);
  }

  @Test
  public void userAuthentication_requiresASecureLockScreen() throws Exception {
    KeyGenerator generator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

    assertThrows(
        InvalidAlgorithmParameterException.class,
        () ->
            generator.init(
                gcmKeyBuilder(ALIAS)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationParameters(30, KeyProperties.AUTH_DEVICE_CREDENTIAL)
                    .build()));
  }

  @Test
  public void timeBoundKey_canBeUsedAfterTheUserAuthenticates() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    SecretKey key =
        generateAesKey(
            gcmKeyBuilder(ALIAS)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    30,
                    KeyProperties.AUTH_DEVICE_CREDENTIAL | KeyProperties.AUTH_BIOMETRIC_STRONG));
    assertThrows(
        UserNotAuthenticatedException.class,
        () -> Cipher.getInstance("AES/GCM/NoPadding").init(Cipher.ENCRYPT_MODE, key));

    authenticate(null);
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    assertThat(cipher.doFinal(PLAINTEXT)).isNotEmpty();

    SystemClock.sleep(Duration.ofSeconds(31).toMillis());
    assertThrows(
        UserNotAuthenticatedException.class,
        () -> Cipher.getInstance("AES/GCM/NoPadding").init(Cipher.ENCRYPT_MODE, key));
  }

  @Test
  public void timeBoundKey_isAuthorizedByUnlockingTheDevice() throws Exception {
    KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
    shadowOf(keyguardManager).setIsDeviceSecure(true);
    SecretKey key =
        generateAesKey(
            gcmKeyBuilder(ALIAS)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(30, KeyProperties.AUTH_DEVICE_CREDENTIAL));
    shadowOf(keyguardManager).setIsDeviceLocked(true);

    shadowOf(keyguardManager).setIsDeviceLocked(false);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    assertThat(cipher.doFinal(PLAINTEXT)).isNotEmpty();
  }

  @Test
  public void perUseKey_canBeUsedThroughBiometricPrompt() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    SecretKey key = generateAesKey(gcmKeyBuilder(ALIAS).setUserAuthenticationRequired(true));
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);

    assertThrows(Exception.class, () -> cipher.doFinal(PLAINTEXT));

    Cipher authenticated = Cipher.getInstance("AES/GCM/NoPadding");
    authenticated.init(Cipher.ENCRYPT_MODE, key);
    AuthenticationResult result = authenticate(new BiometricPrompt.CryptoObject(authenticated));
    assertThat(result.getCryptoObject().getCipher().doFinal(PLAINTEXT)).isNotEmpty();
  }

  @Test
  public void biometricKey_requiresAnEnrolledBiometric() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    shadowBiometricManager().setCanAuthenticate(false);
    KeyGenerator generator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

    assertThrows(
        InvalidAlgorithmParameterException.class,
        () -> generator.init(gcmKeyBuilder(ALIAS).setUserAuthenticationRequired(true).build()));
  }

  @Test
  public void biometricKey_isInvalidatedWhenBiometricsAreRemoved() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    SecretKey key =
        generateAesKey(
            gcmKeyBuilder(ALIAS)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true));

    shadowBiometricManager().setCanAuthenticate(false);
    shadowBiometricManager().setCanAuthenticate(true);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    assertThrows(
        KeyPermanentlyInvalidatedException.class, () -> cipher.init(Cipher.ENCRYPT_MODE, key));
  }

  @Test
  public void authBoundKeys_areInvalidatedWhenTheLockScreenIsRemoved() throws Exception {
    KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
    shadowOf(keyguardManager).setIsDeviceSecure(true);
    generateAesKey(
        gcmKeyBuilder(ALIAS)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(30, KeyProperties.AUTH_DEVICE_CREDENTIAL));

    shadowOf(keyguardManager).setIsDeviceSecure(false);

    assertThrows(UnrecoverableKeyException.class, () -> keyStore.getKey(ALIAS, null));
  }

  @Test
  public void unlockedDeviceRequiredKey_cannotBeUsedWhileLocked() throws Exception {
    KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
    SecretKey key = generateAesKey(gcmKeyBuilder(ALIAS).setUnlockedDeviceRequired(true));
    shadowOf(keyguardManager).setIsDeviceLocked(true);

    assertThrows(
        InvalidKeyException.class,
        () -> Cipher.getInstance("AES/GCM/NoPadding").init(Cipher.ENCRYPT_MODE, key));

    shadowOf(keyguardManager).setIsDeviceLocked(false);
    Cipher.getInstance("AES/GCM/NoPadding").init(Cipher.ENCRYPT_MODE, key);
  }

  @Test
  public void biometricUnlock_secretIsOnlyRecoverableAfterAnotherPrompt() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    generateAesKey(
        gcmKeyBuilder(ALIAS)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true));

    // Setting up biometric unlock: encrypt the secret and store the ciphertext and IV.
    Cipher encrypt = Cipher.getInstance("AES/GCM/NoPadding");
    encrypt.init(Cipher.ENCRYPT_MODE, keyStore.getKey(ALIAS, null));
    byte[] ciphertext =
        authenticate(new BiometricPrompt.CryptoObject(encrypt))
            .getCryptoObject()
            .getCipher()
            .doFinal(PLAINTEXT);
    byte[] iv = encrypt.getIV();

    // Unlocking later: the stored ciphertext can only be decrypted after authenticating again.
    Cipher unauthenticated = Cipher.getInstance("AES/GCM/NoPadding");
    unauthenticated.init(
        Cipher.DECRYPT_MODE, keyStore.getKey(ALIAS, null), new GCMParameterSpec(128, iv));
    assertThrows(Exception.class, () -> unauthenticated.doFinal(ciphertext));

    Cipher decrypt = Cipher.getInstance("AES/GCM/NoPadding");
    decrypt.init(Cipher.DECRYPT_MODE, keyStore.getKey(ALIAS, null), new GCMParameterSpec(128, iv));
    assertThat(
            authenticate(new BiometricPrompt.CryptoObject(decrypt))
                .getCryptoObject()
                .getCipher()
                .doFinal(ciphertext))
        .isEqualTo(PLAINTEXT);
  }

  @Test
  public void biometricUnlock_canBeSetUpAgainAfterBiometricsChange() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    KeyGenParameterSpec spec =
        gcmKeyBuilder(ALIAS)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build();
    generateAesKey(spec);
    shadowBiometricManager().setCanAuthenticate(false);
    shadowBiometricManager().setCanAuthenticate(true);

    // Apps detect the change when initializing the cipher, then delete and recreate the key.
    SecretKey invalidated = (SecretKey) keyStore.getKey(ALIAS, null);
    assertThrows(
        KeyPermanentlyInvalidatedException.class,
        () -> Cipher.getInstance("AES/GCM/NoPadding").init(Cipher.ENCRYPT_MODE, invalidated));
    keyStore.deleteEntry(ALIAS);
    generateAesKey(spec);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(ALIAS, null));
    assertThat(
            authenticate(new BiometricPrompt.CryptoObject(cipher))
                .getCryptoObject()
                .getCipher()
                .doFinal(PLAINTEXT))
        .isNotEmpty();
  }

  @Test
  public void deviceCredentialUnlock_aesCbcPkcs7RoundTripsWithinTheTimeout() throws Exception {
    KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
    shadowOf(keyguardManager).setIsDeviceSecure(true);
    SecretKey key =
        generateAesKey(
            new KeyGenParameterSpec.Builder(
                    ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(10, KeyProperties.AUTH_DEVICE_CREDENTIAL));
    shadowOf(keyguardManager).setIsDeviceLocked(true);
    shadowOf(keyguardManager).setIsDeviceLocked(false);

    Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS7Padding");
    encrypt.init(Cipher.ENCRYPT_MODE, key);
    byte[] ciphertext = encrypt.doFinal(PLAINTEXT);
    Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS7Padding");
    decrypt.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(encrypt.getIV()));

    assertThat(ciphertext).hasLength(16);
    assertThat(decrypt.doFinal(ciphertext)).isEqualTo(PLAINTEXT);
  }

  @Test
  public void rsaKey_encryptsWithoutAuthenticationButDecryptsOnlyAfterAPrompt() throws Exception {
    shadowOf(context.getSystemService(KeyguardManager.class)).setIsDeviceSecure(true);
    KeyPairGenerator generator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
    generator.initialize(
        new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_DECRYPT)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setUserAuthenticationRequired(true)
            .build());
    KeyPair keyPair = generator.generateKeyPair();
    OAEPParameterSpec oaep =
        new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

    // Public key operations happen in software, so the secret can be stored without a prompt.
    Cipher encrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    encrypt.init(Cipher.ENCRYPT_MODE, keyPair.getPublic(), oaep);
    byte[] ciphertext = encrypt.doFinal(PLAINTEXT);

    Cipher decrypt = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    decrypt.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaep);
    assertThat(
            authenticate(new BiometricPrompt.CryptoObject(decrypt))
                .getCryptoObject()
                .getCipher()
                .doFinal(ciphertext))
        .isEqualTo(PLAINTEXT);
  }

  private ShadowBiometricManager shadowBiometricManager() {
    return Shadow.extract(context.getSystemService(BiometricManager.class));
  }

  private SecretKey generateAesKey(KeyGenParameterSpec.Builder builder) throws Exception {
    return generateAesKey(builder.build());
  }

  private SecretKey generateAesKey(KeyGenParameterSpec spec) throws Exception {
    KeyGenerator generator =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
    generator.init(spec);
    return generator.generateKey();
  }

  private static KeyGenParameterSpec gcmKey(String alias) {
    return gcmKeyBuilder(alias).build();
  }

  private static KeyGenParameterSpec.Builder gcmKeyBuilder(String alias) {
    return new KeyGenParameterSpec.Builder(
            alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);
  }

  /** Authenticates the user in a BiometricPrompt and returns the result. */
  private AuthenticationResult authenticate(BiometricPrompt.CryptoObject crypto) {
    BiometricPrompt.Builder builder = new BiometricPrompt.Builder(context).setTitle("title");
    if (crypto == null) {
      builder.setAllowedAuthenticators(
          Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL);
    } else {
      builder
          .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
          .setNegativeButton("cancel", context.getMainExecutor(), (dialog, which) -> {});
    }
    BiometricPrompt prompt = builder.build();
    AtomicReference<AuthenticationResult> result = new AtomicReference<>();
    AuthenticationCallback callback =
        new AuthenticationCallback() {
          @Override
          public void onAuthenticationSucceeded(AuthenticationResult authenticationResult) {
            result.set(authenticationResult);
          }
        };
    if (crypto == null) {
      prompt.authenticate(new CancellationSignal(), Runnable::run, callback);
    } else {
      prompt.authenticate(crypto, new CancellationSignal(), Runnable::run, callback);
    }
    ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully();
    return result.get();
  }
}
