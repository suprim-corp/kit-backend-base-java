package dev.suprim.kit.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * SecretUtils - AES-256-GCM + HKDF-SHA256 encryption utility.
 * <p>
 * Features:
 * - Per-secret key derivation using HKDF with UUID salt
 * - AES-256-GCM authenticated encryption
 * - Optional AAD (Additional Authenticated Data) support
 * - Integrity checksum via HMAC-SHA256
 */
public record SecretUtils(byte[] masterKey, CryptoProvider provider) {
	public static final String ENC_TRANSFORM = "AES/GCM/NoPadding";
	public static final String ENC_NAME = "AES-256-GCM";
	public static final String HMAC_ALGO = "HmacSHA256";

	public static final int KEY_BYTES = 32;      // 256-bit
	public static final int GCM_IV_BYTES = 12;   // 96-bit
	public static final int GCM_TAG_BITS = 128;  // 16 bytes

	/**
	 * Encrypted secret payload containing ciphertext, IV, tag, algorithm, and checksum.
	 */
	public record EncryptedSecret(
			byte[] ciphertext, byte[] iv, byte[] tag, String algo,
			byte[] checksum
	) {
		public EncryptedSecret {
			Objects.requireNonNull(ciphertext, "ciphertext");
			Objects.requireNonNull(iv, "iv");
			Objects.requireNonNull(tag, "tag");
			Objects.requireNonNull(algo, "algo");
			Objects.requireNonNull(checksum, "checksum");
		}
	}

	/**
	 * Creates SecretUtils with the default JDK crypto provider.
	 */
	public SecretUtils(byte[] masterKey) {
		this(masterKey, CryptoProvider.DEFAULT);
	}

	/**
	 * Creates SecretUtils with a custom crypto provider.
	 */
	public SecretUtils(byte[] masterKey, CryptoProvider provider) {
		if (Objects.isNull(masterKey) || masterKey.length != KEY_BYTES) {
			throw new IllegalArgumentException("masterKey must be 32 bytes (256-bit).");
		}
		this.masterKey = masterKey.clone();
		this.provider = Objects.requireNonNullElse(provider, CryptoProvider.DEFAULT);
	}

	/* ===================== Factory ===================== */

	public static SecretUtils fromBase64Key(String base64Key) {
		if (Objects.isNull(base64Key) || base64Key.isBlank()) {
			throw new IllegalArgumentException("base64Key is blank.");
		}
		byte[] key = Base64.getDecoder().decode(base64Key);
		return new SecretUtils(key);
	}

	public static SecretUtils fromHexKey(String hexKey) {
		if (Objects.isNull(hexKey) || hexKey.isBlank()) {
			throw new IllegalArgumentException("hexKey is blank.");
		}
		byte[] key = hexToBytes(hexKey);
		return new SecretUtils(key);
	}

	/* ===================== Core APIs ===================== */

	/**
	 * Encrypt plaintext for a given secretId.
	 *
	 * @param plaintext the data to encrypt
	 * @param secretId  unique identifier for key derivation
	 * @param aad       optional additional authenticated data (context binding)
	 * @return encrypted payload
	 */
	public EncryptedSecret encrypt(
			byte[] plaintext,
			UUID secretId,
			byte[] aad
	) {
		Objects.requireNonNull(plaintext, "plaintext");
		Objects.requireNonNull(secretId, "secretId");
		try {
			byte[] iv = secureRandom(GCM_IV_BYTES);
			byte[] subKey = hkdfSha256(masterKey, salt(secretId), "secrets:v1");

			Cipher cipher = provider.getCipher(ENC_TRANSFORM);
			GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_BITS, iv);
			SecretKey aesKey = new SecretKeySpec(subKey, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcm);

			if (!Objects.isNull(aad) && aad.length > 0) {
				cipher.updateAAD(aad);
			}

			byte[] ctAndTag = cipher.doFinal(plaintext);
			byte[][] split = splitCtAndTag(ctAndTag);
			byte[] checksum = hmacSha256(masterKey, plaintext);

			return new EncryptedSecret(
					split[0],
					iv,
					split[1],
					ENC_NAME,
					checksum
			);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Encrypt failed", e);
		}
	}

	/**
	 * Decrypt payload using the same secretId and AAD.
	 *
	 * @param payload  encrypted payload
	 * @param secretId unique identifier used during encryption
	 * @param aad      optional additional authenticated data (must match encryption)
	 * @return decrypted plaintext
	 */
	public byte[] decrypt(EncryptedSecret payload, UUID secretId, byte[] aad) {
		Objects.requireNonNull(payload, "payload");
		Objects.requireNonNull(secretId, "secretId");
		try {
			byte[] subKey = hkdfSha256(masterKey, salt(secretId), "secrets:v1");

			Cipher cipher = provider.getCipher(ENC_TRANSFORM);
			GCMParameterSpec gcm = new GCMParameterSpec(
					GCM_TAG_BITS,
					payload.iv()
			);
			SecretKey aesKey = new SecretKeySpec(subKey, "AES");
			cipher.init(Cipher.DECRYPT_MODE, aesKey, gcm);

			if (!Objects.isNull(aad) && aad.length > 0) {
				cipher.updateAAD(aad);
			}

			byte[] ctAndTag = joinCtAndTag(payload.ciphertext(), payload.tag());
			return cipher.doFinal(ctAndTag);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Decrypt failed", e);
		}
	}

	/* ===================== HKDF-SHA256 (RFC 5869) ===================== */

	private byte[] hkdfSha256(byte[] ikm, byte[] salt, String info) {
		try {
			byte[] prk = hmacSha256(
					Objects.isNull(salt) ? new byte[32] : salt,
					ikm
			);
			byte[] infoBytes = Objects.isNull(info) ? new byte[0] : info.getBytes(
					StandardCharsets.UTF_8);
			int l = KEY_BYTES;
			int hashLen = 32;
			int n = (int) Math.ceil((double) l / (double) hashLen);

			byte[] t = new byte[0];
			ByteBuffer okm = ByteBuffer.allocate(n * hashLen);
			for (int i = 1; i <= n; i++) {
				Mac mac = provider.getMac(HMAC_ALGO);
				mac.init(new SecretKeySpec(prk, HMAC_ALGO));
				mac.update(t);
				mac.update(infoBytes);
				mac.update((byte) i);
				t = mac.doFinal();
				okm.put(t);
			}
			byte[] out = new byte[l];
			okm.flip();
			okm.get(out, 0, l);
			return out;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("HKDF failed", e);
		}
	}

	private byte[] hmacSha256(byte[] key, byte[] data) {
		try {
			Mac mac = provider.getMac(HMAC_ALGO);
			mac.init(new SecretKeySpec(key, HMAC_ALGO));
			return mac.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("HMAC failed", e);
		}
	}

	/* ===================== Helpers ===================== */

	private static byte[] salt(UUID secretId) {
		ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		bb.putLong(secretId.getMostSignificantBits());
		bb.putLong(secretId.getLeastSignificantBits());
		return bb.array();
	}

	private static byte[] secureRandom(int n) {
		SecureRandom sr = new SecureRandom();
		byte[] out = new byte[n];
		sr.nextBytes(out);
		return out;
	}

	private static byte[][] splitCtAndTag(byte[] ctAndTag) {
		int tagLen = GCM_TAG_BITS / 8;
		int ctLen = ctAndTag.length - tagLen;
		if (ctLen <= 0) throw new IllegalArgumentException(
				"Invalid GCM output length.");
		byte[] ct = new byte[ctLen];
		byte[] tag = new byte[tagLen];
		System.arraycopy(ctAndTag, 0, ct, 0, ctLen);
		System.arraycopy(ctAndTag, ctLen, tag, 0, tagLen);
		return new byte[][]{ct, tag};
	}

	private static byte[] joinCtAndTag(byte[] ct, byte[] tag) {
		byte[] out = new byte[ct.length + tag.length];
		System.arraycopy(ct, 0, out, 0, ct.length);
		System.arraycopy(tag, 0, out, ct.length, tag.length);
		return out;
	}

	/**
	 * Generate a new random AES-256 key encoded as Base64.
	 */
	public static String generateBase64Key() {
		return generateBase64Key(CryptoProvider.DEFAULT);
	}

	/**
	 * Generate a new random AES-256 key encoded as Base64 using specified provider.
	 */
	public static String generateBase64Key(CryptoProvider provider) {
		try {
			KeyGenerator kg = provider.getKeyGenerator("AES");
			kg.init(256);
			SecretKey sk = kg.generateKey();
			return Base64.getEncoder().encodeToString(sk.getEncoded());
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Cannot generate AES key", e);
		}
	}

	public static String bytesToBase64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] base64ToBytes(String base64) {
		if (Objects.isNull(base64) || base64.isBlank()) {
			throw new IllegalArgumentException("base64 is blank.");
		}
		return Base64.getDecoder().decode(base64);
	}

	public static byte[] hexToBytes(String hex) {
		String s = hex.startsWith("0x") ? hex.substring(2) : hex;
		int len = s.length();
		if (len % 2 != 0) throw new IllegalArgumentException(
				"Invalid hex length.");
		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			out[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
			                     Character.digit(s.charAt(i + 1), 16));
		}
		return out;
	}

}
