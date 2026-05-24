package br.com.fiap.fordvinshare.security.filter;

import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import br.com.fiap.fordvinshare.security.jwt.SecurityProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple in-memory rate limiter (academic scope).
 *
 * It is NOT distributed; in real production you'd typically use Redis / API Gateway / WAF.
 */
public class RateLimitingFilter extends OncePerRequestFilter {
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final SecurityProperties props;
	private final ObjectMapper objectMapper;

	private final Cache<String, WindowCounter> counters = Caffeine.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(10))
			.maximumSize(50_000)
			.build();

	public RateLimitingFilter(SecurityProperties props, ObjectMapper objectMapper) {
		this.props = props;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return !(path != null && path.startsWith("/api/"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		String ip = clientIp(request);

		long limitIp = path.startsWith("/api/auth/") ? props.rateLimit().authPerIpPerMinute() : props.rateLimit().perIpPerMinute();
		if (!allow("ip:" + ip, limitIp)) {
			writeTooManyRequests(response);
			return;
		}

		Long userId = authenticatedUserId();
		if (userId != null) {
			if (!allow("u:" + userId, props.rateLimit().perUserPerMinute())) {
				writeTooManyRequests(response);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean allow(String key, long limit) {
		if (limit <= 0) {
			return true;
		}
		long nowMs = System.currentTimeMillis();
		WindowCounter counter = counters.get(key, k -> new WindowCounter(nowMs));
		return counter.tryIncrement(nowMs, limit);
	}

	private void writeTooManyRequests(HttpServletResponse response) throws IOException {
		response.setStatus(429);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(ErroResponse.builder()
				.status(429)
				.erro("Muitas requisições. Tente novamente em instantes.")
				.timestamp(java.time.LocalDateTime.now())
				.build()));
	}

	private static Long authenticatedUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwt) {
			try {
				return Long.valueOf(jwt.getName());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private static String clientIp(HttpServletRequest req) {
		String xff = req.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			String first = xff.split(",")[0].trim();
			if (!first.isBlank()) {
				return first;
			}
		}
		return req.getRemoteAddr();
	}

	private static final class WindowCounter {
		private final AtomicLong windowStartMs = new AtomicLong();
		private final AtomicInteger count = new AtomicInteger();

		WindowCounter(long nowMs) {
			windowStartMs.set(nowMs);
		}

		boolean tryIncrement(long nowMs, long limit) {
			long start = windowStartMs.get();
			if (nowMs - start >= WINDOW.toMillis()) {
				windowStartMs.set(nowMs);
				count.set(0);
			}
			return count.incrementAndGet() <= limit;
		}
	}
}
