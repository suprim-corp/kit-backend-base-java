package dev.suprim.kit.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.security.GeneralSecurityException;

/**
 * Provider interface for crypto operations.
 * Allows injection of mock providers for testing exception paths.
 */
public interface CryptoProvider {

    /**
     * Get a Cipher instance for the specified transformation.
     */
    Cipher getCipher(String transformation) throws GeneralSecurityException;

    /**
     * Get a Mac instance for the specified algorithm.
     */
    default Mac getMac(String algorithm) throws GeneralSecurityException {
        return Mac.getInstance(algorithm);
    }

    /**
     * Get a KeyGenerator instance for the specified algorithm.
     */
    default KeyGenerator getKeyGenerator(String algorithm) throws GeneralSecurityException {
        return KeyGenerator.getInstance(algorithm);
    }

    /**
     * Default provider using JDK crypto.
     */
    CryptoProvider DEFAULT = transformation -> Cipher.getInstance(transformation);
}
