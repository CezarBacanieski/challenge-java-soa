package br.com.fiap.fordvinshare.security.jwt;

import br.com.fiap.fordvinshare.security.auth.AppUser;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private static final SecureRandom RNG = new SecureRandom();

	private final JwtEncoder jwtEncoder;
	private final Duration accessTtl;

	public JwtService(JwtEncoder jwtEncoder, SecurityProperties props) {
		this.jwtEncoder = jwtEncoder;
		this.accessTtl = Duration.ofSeconds(props.jwt().accessTtlSeconds());
	}

	public String createAccessToken(AppUser user) {
		Instant now = Instant.now();
		Instant exp = now.plus(accessTtl);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ford-vinshare")
				.issuedAt(now)
				.expiresAt(exp)
				.subject(String.valueOf(user.getId()))
				.id(randomId())
				.claim("email", user.getEmail())
				.claim("role", List.of("ROLE_" + user.getRole().name()))
				.claim("tv", user.getTokenVersion())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long accessTokenTtlSeconds() {
		return accessTtl.toSeconds();
	}

	public String generateRefreshToken() {
		byte[] bytes = new byte[32];
		RNG.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public String sha256Hex(String raw) {
		try {
			var md = java.security.MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}

	private static String randomId() {
		byte[] bytes = new byte[16];
		RNG.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
