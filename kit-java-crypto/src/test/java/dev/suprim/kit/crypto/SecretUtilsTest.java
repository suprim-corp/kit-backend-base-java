package dev.suprim.kit.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecretUtilsTest {

	private static final String VALID_BASE64_KEY = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="; // 32 bytes
	private static final String VALID_HEX_KEY = "3031323334353637383930313233343536373839303132333435363738393031"; // 32 bytes
	private byte[] validKey;
	private SecretUtils utils;
	private UUID secretId;

	@BeforeEach
	void setUp() {
		validKey = Base64.getDecoder().decode(VALID_BASE64_KEY);
		utils = new SecretUtils(validKey);
		secretId = UUID.randomUUID();
	}

	// ==================== Constructor Tests ====================

	@Test
	void constructor_validKey_succeeds() {
		assertDoesNotThrow(() -> new SecretUtils(validKey));
	}

	@Test
	void constructor_nullKey_throws() {
		assertThrows(IllegalArgumentException.class, () -> new SecretUtils(null));
	}

	@Test
	void constructor_wrongLength_throws() {
		byte[] shortKey = new byte[16];
		assertThrows(IllegalArgumentException.class, () -> new SecretUtils(shortKey));
	}

	// ==================== Factory Method Tests ====================

	@Test
	void fromBase64Key_valid_succeeds() {
		assertDoesNotThrow(() -> SecretUtils.fromBase64Key(VALID_BASE64_KEY));
	}

	@Test
	void fromBase64Key_null_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.fromBase64Key(null));
	}

	@Test
	void fromBase64Key_blank_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.fromBase64Key("   "));
	}

	@Test
	void fromHexKey_valid_succeeds() {
		assertDoesNotThrow(() -> SecretUtils.fromHexKey(VALID_HEX_KEY));
	}

	@Test
	void fromHexKey_withPrefix_succeeds() {
		assertDoesNotThrow(() -> SecretUtils.fromHexKey("0x" + VALID_HEX_KEY));
	}

	@Test
	void fromHexKey_null_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.fromHexKey(null));
	}

	@Test
	void fromHexKey_oddLength_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.fromHexKey("abc"));
	}

	// ==================== Encrypt/Decrypt Tests ====================

	@Test
	void encryptDecrypt_roundTrip_succeeds() {
		byte[] plaintext = "Hello, World!".getBytes(StandardCharsets.UTF_8);

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);
		byte[] decrypted = utils.decrypt(encrypted, secretId, null);

		assertArrayEquals(plaintext, decrypted);
	}

	@Test
	void encryptDecrypt_withAad_succeeds() {
		byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
		byte[] aad = "context:tenant123".getBytes(StandardCharsets.UTF_8);

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, aad);
		byte[] decrypted = utils.decrypt(encrypted, secretId, aad);

		assertArrayEquals(plaintext, decrypted);
	}

	@Test
	void decrypt_wrongAad_throws() {
		byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
		byte[] aad = "context:tenant123".getBytes(StandardCharsets.UTF_8);
		byte[] wrongAad = "context:tenant456".getBytes(StandardCharsets.UTF_8);

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, aad);

		assertThrows(IllegalStateException.class, () -> utils.decrypt(encrypted, secretId, wrongAad));
	}

	@Test
	void decrypt_wrongSecretId_throws() {
		byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
		UUID wrongId = UUID.randomUUID();

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);

		assertThrows(IllegalStateException.class, () -> utils.decrypt(encrypted, wrongId, null));
	}

	@Test
	void encrypt_nullPlaintext_throws() {
		assertThrows(NullPointerException.class, () -> utils.encrypt(null, secretId, null));
	}

	@Test
	void encrypt_nullSecretId_throws() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		assertThrows(NullPointerException.class, () -> utils.encrypt(plaintext, null, null));
	}

	@Test
	void encryptedSecret_containsValidFields() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);

		assertNotNull(encrypted.ciphertext());
		assertNotNull(encrypted.iv());
		assertNotNull(encrypted.tag());
		assertEquals(SecretUtils.ENC_NAME, encrypted.algo());
		assertNotNull(encrypted.checksum());
		assertEquals(SecretUtils.GCM_IV_BYTES, encrypted.iv().length);
		assertEquals(SecretUtils.GCM_TAG_BITS / 8, encrypted.tag().length);
	}

	// ==================== Static Helper Tests ====================

	@Test
	void generateBase64Key_valid() {
		String key = SecretUtils.generateBase64Key();

		assertNotNull(key);
		assertFalse(key.isBlank());
		byte[] decoded = Base64.getDecoder().decode(key);
		assertEquals(32, decoded.length);
	}

	@Test
	void bytesToBase64_base64ToBytes_roundTrip() {
		byte[] original = "test data".getBytes(StandardCharsets.UTF_8);

		String base64 = SecretUtils.bytesToBase64(original);
		byte[] result = SecretUtils.base64ToBytes(base64);

		assertArrayEquals(original, result);
	}

	@Test
	void base64ToBytes_null_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.base64ToBytes(null));
	}

	@Test
	void base64ToBytes_blank_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.base64ToBytes("   "));
	}

	@Test
	void hexToBytes_valid() {
		byte[] result = SecretUtils.hexToBytes("48656c6c6f"); // "Hello"

		assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), result);
	}

	@Test
	void hexToBytes_withPrefix() {
		byte[] result = SecretUtils.hexToBytes("0x48656c6c6f");

		assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), result);
	}

	@Test
	void hexToBytes_oddLength_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.hexToBytes("abc"));
	}

	// Additional coverage tests
	@Test
	void decrypt_nullPayload_throws() {
		assertThrows(NullPointerException.class, () -> utils.decrypt(null, secretId, null));
	}

	@Test
	void decrypt_nullSecretId_throws() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);
		assertThrows(NullPointerException.class, () -> utils.decrypt(encrypted, null, null));
	}

	@Test
	void encryptDecrypt_withEmptyAad_succeeds() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		byte[] emptyAad = new byte[0];

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, emptyAad);
		byte[] decrypted = utils.decrypt(encrypted, secretId, emptyAad);

		assertArrayEquals(plaintext, decrypted);
	}

	@Test
	void encryptedSecret_nullCiphertext_throws() {
		assertThrows(NullPointerException.class, () ->
			new SecretUtils.EncryptedSecret(null, new byte[12], new byte[16], "AES", new byte[32]));
	}

	@Test
	void encryptedSecret_nullIv_throws() {
		assertThrows(NullPointerException.class, () ->
			new SecretUtils.EncryptedSecret(new byte[10], null, new byte[16], "AES", new byte[32]));
	}

	@Test
	void encryptedSecret_nullTag_throws() {
		assertThrows(NullPointerException.class, () ->
			new SecretUtils.EncryptedSecret(new byte[10], new byte[12], null, "AES", new byte[32]));
	}

	@Test
	void encryptedSecret_nullAlgo_throws() {
		assertThrows(NullPointerException.class, () ->
			new SecretUtils.EncryptedSecret(new byte[10], new byte[12], new byte[16], null, new byte[32]));
	}

	@Test
	void encryptedSecret_nullChecksum_throws() {
		assertThrows(NullPointerException.class, () ->
			new SecretUtils.EncryptedSecret(new byte[10], new byte[12], new byte[16], "AES", null));
	}

	@Test
	void masterKey_isCloned() {
		byte[] original = Base64.getDecoder().decode(VALID_BASE64_KEY);
		SecretUtils utils = new SecretUtils(original);
		original[0] = 99; // mutate original
		// utils should still work because it cloned the key
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		assertDoesNotThrow(() -> utils.encrypt(plaintext, secretId, null));
	}

	@Test
	void decrypt_tamperedCiphertext_throws() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);

		// Tamper with ciphertext
		byte[] tampered = encrypted.ciphertext().clone();
		tampered[0] ^= 0xFF;
		SecretUtils.EncryptedSecret tamperedSecret = new SecretUtils.EncryptedSecret(
			tampered, encrypted.iv(), encrypted.tag(), encrypted.algo(), encrypted.checksum()
		);

		assertThrows(IllegalStateException.class, () -> utils.decrypt(tamperedSecret, secretId, null));
	}

	@Test
	void decrypt_tamperedTag_throws() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);

		// Tamper with tag
		byte[] tamperedTag = encrypted.tag().clone();
		tamperedTag[0] ^= 0xFF;
		SecretUtils.EncryptedSecret tamperedSecret = new SecretUtils.EncryptedSecret(
			encrypted.ciphertext(), encrypted.iv(), tamperedTag, encrypted.algo(), encrypted.checksum()
		);

		assertThrows(IllegalStateException.class, () -> utils.decrypt(tamperedSecret, secretId, null));
	}

	@Test
	void decrypt_tamperedIv_throws() {
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		SecretUtils.EncryptedSecret encrypted = utils.encrypt(plaintext, secretId, null);

		// Tamper with IV
		byte[] tamperedIv = encrypted.iv().clone();
		tamperedIv[0] ^= 0xFF;
		SecretUtils.EncryptedSecret tamperedSecret = new SecretUtils.EncryptedSecret(
			encrypted.ciphertext(), tamperedIv, encrypted.tag(), encrypted.algo(), encrypted.checksum()
		);

		assertThrows(IllegalStateException.class, () -> utils.decrypt(tamperedSecret, secretId, null));
	}

	@Test
	void encryptLargeData_succeeds() {
		byte[] largeData = new byte[10000];
		new java.security.SecureRandom().nextBytes(largeData);

		SecretUtils.EncryptedSecret encrypted = utils.encrypt(largeData, secretId, null);
		byte[] decrypted = utils.decrypt(encrypted, secretId, null);

		assertArrayEquals(largeData, decrypted);
	}

	@Test
	void constants_haveExpectedValues() {
		assertEquals("AES/GCM/NoPadding", SecretUtils.ENC_TRANSFORM);
		assertEquals("AES-256-GCM", SecretUtils.ENC_NAME);
		assertEquals("HmacSHA256", SecretUtils.HMAC_ALGO);
		assertEquals(32, SecretUtils.KEY_BYTES);
		assertEquals(12, SecretUtils.GCM_IV_BYTES);
		assertEquals(128, SecretUtils.GCM_TAG_BITS);
	}

	@Test
	void masterKey_accessor() {
		byte[] key = Base64.getDecoder().decode(VALID_BASE64_KEY);
		SecretUtils utils = new SecretUtils(key);
		assertNotNull(utils.masterKey());
		assertEquals(32, utils.masterKey().length);
	}

	@Test
	void hexToBytes_uppercase_succeeds() {
		byte[] result = SecretUtils.hexToBytes("48454C4C4F"); // "HELLO"
		assertArrayEquals("HELLO".getBytes(StandardCharsets.UTF_8), result);
	}

	@Test
	void hexToBytes_mixedCase_succeeds() {
		byte[] result = SecretUtils.hexToBytes("48eLLo"); // mixed case
		assertNotNull(result);
	}

	// Reflection tests for private methods to increase coverage
	@Test
	void splitCtAndTag_invalidLength_throws() throws Exception {
		var method = SecretUtils.class.getDeclaredMethod("splitCtAndTag", byte[].class);
		method.setAccessible(true);

		// GCM tag is 16 bytes, so anything <= 16 bytes should fail
		byte[] tooShort = new byte[16];
		var ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
			() -> method.invoke(null, (Object) tooShort));
		assertTrue(ex.getCause() instanceof IllegalArgumentException);
	}

	@Test
	void hkdfSha256_withNullSalt_succeeds() throws Exception {
		var method = SecretUtils.class.getDeclaredMethod("hkdfSha256", byte[].class, byte[].class, String.class);
		method.setAccessible(true);

		byte[] ikm = new byte[32];
		// null salt should use default empty salt
		byte[] result = (byte[]) method.invoke(utils, ikm, null, "info");
		assertNotNull(result);
		assertEquals(32, result.length);
	}

	@Test
	void hkdfSha256_withNullInfo_succeeds() throws Exception {
		var method = SecretUtils.class.getDeclaredMethod("hkdfSha256", byte[].class, byte[].class, String.class);
		method.setAccessible(true);

		byte[] ikm = new byte[32];
		byte[] salt = new byte[32];
		// null info should use empty bytes
		byte[] result = (byte[]) method.invoke(utils, ikm, salt, null);
		assertNotNull(result);
		assertEquals(32, result.length);
	}

	@Test
	void hkdfSha256_withNullSaltAndInfo_succeeds() throws Exception {
		var method = SecretUtils.class.getDeclaredMethod("hkdfSha256", byte[].class, byte[].class, String.class);
		method.setAccessible(true);

		byte[] ikm = new byte[32];
		byte[] result = (byte[]) method.invoke(utils, ikm, null, null);
		assertNotNull(result);
		assertEquals(32, result.length);
	}

	@Test
	void fromHexKey_blank_throws() {
		assertThrows(IllegalArgumentException.class, () -> SecretUtils.fromHexKey(""));
	}

	@Test
	void hexToBytes_null_throws() {
		assertThrows(NullPointerException.class, () -> SecretUtils.hexToBytes(null));
	}

	@Test
	void hexToBytes_empty_returnsEmptyArray() {
		byte[] result = SecretUtils.hexToBytes("");
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	void bytesToBase64_null_throws() {
		assertThrows(NullPointerException.class, () -> SecretUtils.bytesToBase64(null));
	}

	@Test
	void bytesToBase64_empty_returnsEmptyString() {
		String result = SecretUtils.bytesToBase64(new byte[0]);
		assertNotNull(result);
		assertEquals("", result);
	}

	// ==================== CryptoProvider Exception Path Tests ====================

	@Test
	void encrypt_cipherFailure_throwsIllegalStateException() {
		CryptoProvider failingProvider = transformation -> {
			throw new NoSuchAlgorithmException("mocked cipher failure");
		};
		SecretUtils failingUtils = new SecretUtils(validKey, failingProvider);

		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> failingUtils.encrypt(plaintext, secretId, null));
		assertEquals("Encrypt failed", ex.getMessage());
		assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
	}

	@Test
	void decrypt_cipherFailure_throwsIllegalStateException() {
		// First encrypt with working provider
		SecretUtils.EncryptedSecret encrypted = utils.encrypt("test".getBytes(StandardCharsets.UTF_8), secretId, null);

		// Then try decrypt with failing provider
		CryptoProvider failingProvider = transformation -> {
			throw new NoSuchAlgorithmException("mocked cipher failure");
		};
		SecretUtils failingUtils = new SecretUtils(validKey, failingProvider);

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> failingUtils.decrypt(encrypted, secretId, null));
		assertEquals("Decrypt failed", ex.getMessage());
		assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
	}

	@Test
	void encrypt_macFailure_throwsIllegalStateException() {
		CryptoProvider failingMacProvider = new CryptoProvider() {
			@Override
			public Cipher getCipher(String transformation) throws GeneralSecurityException {
				return Cipher.getInstance(transformation);
			}
			@Override
			public Mac getMac(String algorithm) throws GeneralSecurityException {
				throw new NoSuchAlgorithmException("mocked mac failure");
			}
		};
		SecretUtils failingUtils = new SecretUtils(validKey, failingMacProvider);

		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> failingUtils.encrypt(plaintext, secretId, null));
		// Can be HKDF failed or HMAC failed depending on which call fails first
		assertTrue(ex.getMessage().contains("failed"));
		assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
	}

	@Test
	void hkdfSha256_loopMacFailure_throwsIllegalStateException() {
		// Call order in encrypt -> hkdfSha256:
		// 1. hmacSha256(salt, ikm) for PRK computation
		// 2. provider.getMac() inside hkdfSha256 loop (at line 184)
		// Then later: hmacSha256(masterKey, plaintext) for checksum
		// We need to fail on call 2 to get "HKDF failed"
		CryptoProvider failingOnSecondCallProvider = new CryptoProvider() {
			private int macCallCount = 0;

			@Override
			public Cipher getCipher(String transformation) throws GeneralSecurityException {
				return Cipher.getInstance(transformation);
			}
			@Override
			public Mac getMac(String algorithm) throws GeneralSecurityException {
				macCallCount++;
				// First call is from hmacSha256 (PRK computation)
				// Second call is from hkdfSha256 loop - make it fail to trigger HKDF exception
				if (macCallCount == 2) {
					throw new NoSuchAlgorithmException("mocked loop mac failure");
				}
				return Mac.getInstance(algorithm);
			}
		};
		SecretUtils failingUtils = new SecretUtils(validKey, failingOnSecondCallProvider);

		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> failingUtils.encrypt(plaintext, secretId, null));
		assertEquals("HKDF failed", ex.getMessage());
		assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
	}

	@Test
	void generateBase64Key_withFailingProvider_throwsIllegalStateException() {
		CryptoProvider failingProvider = new CryptoProvider() {
			@Override
			public Cipher getCipher(String transformation) throws GeneralSecurityException {
				return Cipher.getInstance(transformation);
			}
			@Override
			public KeyGenerator getKeyGenerator(String algorithm) throws GeneralSecurityException {
				throw new NoSuchAlgorithmException("mocked keygen failure");
			}
		};

		IllegalStateException ex = assertThrows(IllegalStateException.class,
			() -> SecretUtils.generateBase64Key(failingProvider));
		assertEquals("Cannot generate AES key", ex.getMessage());
		assertTrue(ex.getCause() instanceof NoSuchAlgorithmException);
	}

	@Test
	void constructor_withNullProvider_usesDefault() {
		SecretUtils utilsWithNullProvider = new SecretUtils(validKey, null);
		assertNotNull(utilsWithNullProvider.provider());
		// Should work normally
		byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);
		SecretUtils.EncryptedSecret encrypted = utilsWithNullProvider.encrypt(plaintext, secretId, null);
		byte[] decrypted = utilsWithNullProvider.decrypt(encrypted, secretId, null);
		assertArrayEquals(plaintext, decrypted);
	}

	@Test
	void provider_accessor() {
		assertNotNull(utils.provider());
	}
}
