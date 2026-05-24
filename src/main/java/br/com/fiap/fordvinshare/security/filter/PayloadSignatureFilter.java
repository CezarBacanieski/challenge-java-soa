package br.com.fiap.fordvinshare.security.filter;

import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import br.com.fiap.fordvinshare.security.jwt.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Optional payload integrity check (academic scope).
 *
 * Clients can send: X-Payload-Signature: v1=<base64(hmacSHA256(body))>
 */
public class PayloadSignatureFilter extends OncePerRequestFilter {
	private static final String HEADER = "X-Payload-Signature";

	private final boolean enabled;
	private final SecretKeySpec key;
	private final ObjectMapper objectMapper;

	public PayloadSignatureFilter(SecurityProperties props, ObjectMapper objectMapper) {
		this.enabled = props.payloadSignature().enabled();
		this.objectMapper = objectMapper;
		String secretB64 = props.payloadSignature().hmacSecretBase64();
		this.key = secretB64 == null || secretB64.isBlank()
				? null
				: new SecretKeySpec(Base64.getDecoder().decode(secretB64.trim()), "HmacSHA256");
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled || key == null) {
			return true;
		}
		String path = request.getRequestURI();
		if (path == null || !path.startsWith("/api/") || path.startsWith("/api/auth/")) {
			return true;
		}
		String method = request.getMethod();
		return !("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		byte[] body = request.getInputStream().readAllBytes();
		CachedBodyRequestWrapper wrapped = new CachedBodyRequestWrapper(request, body);

		String sigHeader = request.getHeader(HEADER);
		if (sigHeader == null || !sigHeader.startsWith("v1=")) {
			writeBadSig(response);
			return;
		}

		byte[] expected = hmac(body);
		byte[] provided;
		try {
			provided = Base64.getDecoder().decode(sigHeader.substring(3).trim());
		} catch (IllegalArgumentException e) {
			writeBadSig(response);
			return;
		}

		if (!MessageDigest.isEqual(expected, provided)) {
			writeBadSig(response);
			return;
		}

		filterChain.doFilter(wrapped, response);
	}

	private byte[] hmac(byte[] body) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			return mac.doFinal(body == null ? new byte[0] : body);
		} catch (Exception e) {
			throw new IllegalStateException("HMAC failure", e);
		}
	}

	private void writeBadSig(HttpServletResponse response) throws IOException {
		response.resetBuffer();
		response.setStatus(400);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(ErroResponse.builder()
				.status(400)
				.erro("Assinatura de payload inválida ou ausente.")
				.timestamp(LocalDateTime.now())
				.build()));
		response.flushBuffer();
	}
}
