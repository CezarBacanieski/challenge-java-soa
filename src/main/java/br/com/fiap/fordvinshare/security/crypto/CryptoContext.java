package br.com.fiap.fordvinshare.security.crypto;

final class CryptoContext {
	private static volatile AesGcmStringEncryptor ENCRYPTOR;

	private CryptoContext() {
	}

	static void setEncryptor(AesGcmStringEncryptor encryptor) {
		ENCRYPTOR = encryptor;
	}

	static AesGcmStringEncryptor getEncryptor() {
		AesGcmStringEncryptor enc = ENCRYPTOR;
		if (enc == null) {
			throw new IllegalStateException("CryptoContext not initialized (encryption key missing?)");
		}
		return enc;
	}
}

