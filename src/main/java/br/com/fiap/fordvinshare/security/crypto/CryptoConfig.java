package br.com.fiap.fordvinshare.security.crypto;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(CryptoConfig.CryptoProperties.class)
public class CryptoConfig {
	private static final Logger log = LoggerFactory.getLogger(CryptoConfig.class);

	@Bean
	AesGcmStringEncryptor aesGcmStringEncryptor(CryptoProperties props, Environment env) {
		byte[] keyBytes = resolveKey(props.encryptionKey(), env);
		SecretKey key = new SecretKeySpec(keyBytes, "AES");
		AesGcmStringEncryptor encryptor = new AesGcmStringEncryptor(key);
		CryptoContext.setEncryptor(encryptor);
		return encryptor;
	}

	private static byte[] resolveKey(String configured, Environment env) {
		boolean prod = env.matchesProfiles("prod");
		if (configured != null && !configured.isBlank()) {
			byte[] decoded = Base64.getDecoder().decode(configured.trim());
			if (decoded.length != 32) {
				throw new IllegalStateException("app.crypto.encryption-key must be base64 of 32 bytes (AES-256)");
			}
			return decoded;
		}

		if (prod) {
			throw new IllegalStateException("Missing required app.crypto.encryption-key in prod profile");
		}

		byte[] tmp = new byte[32];
		new SecureRandom().nextBytes(tmp);
		log.warn("Crypto key not configured; using ephemeral dev/test key. Data encrypted in DB will not be decryptable after restart.");
		return tmp;
	}

	@ConfigurationProperties(prefix = "app.crypto")
	public record CryptoProperties(String encryptionKey) {
	}
}

