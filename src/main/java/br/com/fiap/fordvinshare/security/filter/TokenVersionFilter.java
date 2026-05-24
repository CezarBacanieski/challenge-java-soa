package br.com.fiap.fordvinshare.security.filter;

import br.com.fiap.fordvinshare.dto.response.ErroResponse;
import br.com.fiap.fordvinshare.security.auth.AppUser;
import br.com.fiap.fordvinshare.security.auth.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Best-effort access token invalidation by checking a "token version" against DB.
 * This is a tradeoff (DB hit) but keeps the portfolio project closer to real-world revocation requirements.
 */
public class TokenVersionFilter extends OncePerRequestFilter {
	private final AppUserRepository userRepo;
	private final ObjectMapper objectMapper;

	private final Cache<Long, Integer> tokenVersions = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(30))
			.maximumSize(50_000)
			.build();

	public TokenVersionFilter(AppUserRepository userRepo, ObjectMapper objectMapper) {
		this.userRepo = userRepo;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path != null && path.startsWith("/api/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken jwtAuth) {
			Jwt jwt = jwtAuth.getToken();
			Long userId = safeLong(jwt.getSubject());
			Integer tv = jwt.getClaim("tv");
			if (userId == null || tv == null) {
				deny(response);
				return;
			}
			Integer current = tokenVersions.get(userId, id -> userRepo.findById(id).map(AppUser::getTokenVersion).orElse(-1));
			if (current == null || current != tv) {
				deny(response);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private void deny(HttpServletResponse response) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(401);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(ErroResponse.builder()
				.status(401)
				.erro("Não autenticado")
				.timestamp(LocalDateTime.now())
				.build()));
	}

	private static Long safeLong(String s) {
		try {
			return s == null ? null : Long.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
