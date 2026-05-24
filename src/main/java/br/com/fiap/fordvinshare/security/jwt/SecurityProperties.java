package br.com.fiap.fordvinshare.security.jwt;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		Jwt jwt,
		Cors cors,
		RateLimit rateLimit,
		Cookies cookies,
		PayloadSignature payloadSignature
) {
	public record Jwt(
			String hmacSecretBase64,
			long accessTtlSeconds,
			long refreshTtlSeconds
	) {
	}

	public record Cors(
			List<String> allowedOrigins
	) {
	}

	public record RateLimit(
			long perIpPerMinute,
			long perUserPerMinute,
			long authPerIpPerMinute
	) {
	}

	public record Cookies(
			boolean secure,
			String sameSite
	) {
	}

	public record PayloadSignature(
			boolean enabled,
			String hmacSecretBase64
	) {
	}
}

