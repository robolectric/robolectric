package org.robolectric.android.internal;

import android.security.keystore2.AndroidKeyStoreSpi;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import javax.crypto.SecretKey;

/**
 * The framework's {@code AndroidKeyStore} {@code KeyStoreSpi}, with Android's {@code
 * KeyStoreSpi#engineGetEntry}, which the JDK's differs from.
 *
 * <p>Android's libcore allows getting key entries without a password, e.g. {@code
 * keyStore.getEntry(alias, null)}, which the AndroidKeyStore relies on. The JDK requires one.
 */
public class AndroidKeyStoreSpiOnJvm extends AndroidKeyStoreSpi {
  @Override
  public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam)
      throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
    if (protParam != null || !engineContainsAlias(alias) || !engineIsKeyEntry(alias)) {
      return super.engineGetEntry(alias, protParam);
    }
    Key key = engineGetKey(alias, null);
    if (key instanceof PrivateKey) {
      return new KeyStore.PrivateKeyEntry((PrivateKey) key, engineGetCertificateChain(alias));
    } else if (key instanceof SecretKey) {
      return new KeyStore.SecretKeyEntry((SecretKey) key);
    }
    throw new UnsupportedOperationException();
  }
}
