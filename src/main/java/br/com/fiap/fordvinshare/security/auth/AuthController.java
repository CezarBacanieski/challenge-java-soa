package br.com.fiap.fordvinshare.security.auth;

import br.com.fiap.fordvinshare.security.auth.dto.LoginRequest;
import br.com.fiap.fordvinshare.security.auth.dto.RegisterRequest;
import br.com.fiap.fordvinshare.security.jwt.SecurityProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação (JWT + refresh rotativo)")
public class AuthController {
	private static final String REFRESH_COOKIE = "refresh_token";

	private final AuthService authService;
	private final SecurityProperties props;

	public AuthController(AuthService authService, SecurityProperties props) {
		this.authService = authService;
		this.props = props;
	}

	@PostMapping("/register")
	@Operation(summary = "Registrar novo usuário (role USER)", operationId = "authRegister")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
		AuthService.AuthResult result = authService.register(request, clientIp(http), userAgent(http));
		return okWithRefreshCookie(result.refreshToken(), result.response());
	}

	@PostMapping("/login")
	@Operation(summary = "Login", operationId = "authLogin")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
		AuthService.AuthResult result = authService.login(request, clientIp(http), userAgent(http));
		return okWithRefreshCookie(result.refreshToken(), result.response());
	}

	@PostMapping("/refresh")
	@Operation(summary = "Rotacionar refresh token e emitir novo access token", operationId = "authRefresh")
	public ResponseEntity<?> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletRequest http
	) {
		AuthService.AuthResult result = authService.refresh(refreshToken, clientIp(http), userAgent(http));
		return okWithRefreshCookie(result.refreshToken(), result.response());
	}

	@PostMapping("/logout")
	@Operation(summary = "Logout (revoga refresh token atual)", operationId = "authLogout")
	public ResponseEntity<?> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		authService.logout(refreshToken);
		return noContentClearingRefreshCookie();
	}

	@PostMapping("/revoke-all")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Revogar todas as sessões (refresh) do usuário atual", operationId = "authRevokeAll")
	public ResponseEntity<?> revokeAll(JwtAuthenticationToken auth) {
		Long userId = null;
		try {
			userId = Long.valueOf(auth.getName());
		} catch (NumberFormatException ignored) {
		}
		authService.revokeAllSessions(userId);
		return noContentClearingRefreshCookie();
	}

	private ResponseEntity<?> okWithRefreshCookie(String refreshToken, Object body) {
		boolean secure = props.cookies() != null && props.cookies().secure();
		String sameSite = props.cookies() != null && props.cookies().sameSite() != null && !props.cookies().sameSite().isBlank()
				? props.cookies().sameSite()
				: "Strict";

		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
				.httpOnly(true)
				.secure(secure)
				.path("/api/auth")
				.sameSite(sameSite)
				.maxAge(authService.refreshTtlSeconds())
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(body);
	}

	private ResponseEntity<?> noContentClearingRefreshCookie() {
		boolean secure = props.cookies() != null && props.cookies().secure();
		String sameSite = props.cookies() != null && props.cookies().sameSite() != null && !props.cookies().sameSite().isBlank()
				? props.cookies().sameSite()
				: "Strict";

		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(secure)
				.path("/api/auth")
				.sameSite(sameSite)
				.maxAge(0)
				.build();
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.build();
	}

	private static String userAgent(HttpServletRequest req) {
		String ua = req.getHeader("User-Agent");
		return ua == null ? "" : ua;
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
}
