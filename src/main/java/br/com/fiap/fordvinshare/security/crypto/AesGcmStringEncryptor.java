package br.com.fiap.fordvinshare.security.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class AesGcmStringEncryptor {
	private static final byte VERSION_1 = 0x01;
	private static final int IV_LEN = 12;
	private static final int TAG_BITS = 128;

	private final SecretKey key;
	private final SecureRandom random = new SecureRandom();

	AesGcmStringEncryptor(SecretKey key) {
		this.key = key;
	}

	String encrypt(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		try {
			byte[] iv = new byte[IV_LEN];
			random.nextBytes(iv);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			ByteBuffer out = ByteBuffer.allocate(1 + iv.length + ct.length);
			out.put(VERSION_1);
			out.put(iv);
			out.put(ct);
			return Base64.getEncoder().encodeToString(out.array());
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Failed to encrypt value", e);
		}
	}

	String decrypt(String encoded) {
		if (encoded == null) {
			return null;
		}
		try {
			byte[] raw = Base64.getDecoder().decode(encoded);
			if (raw.length < 1 + IV_LEN + 1) {
				throw new IllegalArgumentException("Encrypted payload too short");
			}
			ByteBuffer buf = ByteBuffer.wrap(raw);
			byte version = buf.get();
			if (version != VERSION_1) {
				throw new IllegalArgumentException("Unsupported encrypted payload version");
			}
			byte[] iv = new byte[IV_LEN];
			buf.get(iv);
			byte[] ct = new byte[buf.remaining()];
			buf.get(ct);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] pt = cipher.doFinal(ct);
			return new String(pt, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Failed to decrypt value", e);
		}
	}
}

