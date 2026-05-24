package br.com.fiap.fordvinshare.security.auth;

import br.com.fiap.fordvinshare.security.auth.dto.AuthResponse;
import br.com.fiap.fordvinshare.security.auth.dto.LoginRequest;
import br.com.fiap.fordvinshare.security.auth.dto.RegisterRequest;
import br.com.fiap.fordvinshare.security.audit.AuditAction;
import br.com.fiap.fordvinshare.security.audit.AuditService;
import br.com.fiap.fordvinshare.security.jwt.JwtService;
import br.com.fiap.fordvinshare.security.jwt.SecurityProperties;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private static final int MAX_FAILED_LOGINS = 5;
	private static final Duration LOCK_TIME = Duration.ofMinutes(15);

	private final AppUserRepository userRepo;
	private final RefreshTokenRepository refreshRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final SecurityProperties props;
	private final AuditService audit;

	private final String dummyPasswordHash;

	public AuthService(
			AppUserRepository userRepo,
			RefreshTokenRepository refreshRepo,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			SecurityProperties props,
			AuditService audit
	) {
		this.userRepo = userRepo;
		this.refreshRepo = refreshRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.props = props;
		this.audit = audit;
		this.dummyPasswordHash = passwordEncoder.encode("DUMMY_PASSWORD_DO_NOT_USE");
	}

	public record AuthResult(AuthResponse response, String refreshToken) {
	}

	@Transactional
	public AuthResult register(RegisterRequest request, String ip, String userAgent) {
		String email = normalizeEmail(request.email());
		if (userRepo.existsByEmail(email)) {
			audit.record(AuditAction.REGISTER, null, "auth/register", ip, userAgent, false, null);
			throw new IllegalArgumentException("Invalid credentials");
		}

		AppUser user = AppUser.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(request.password()))
				.role(Role.USER)
				.enabled(true)
				.failedLoginAttempts(0)
				.lockedUntil(null)
				.tokenVersion(0)
				.build();
		user = userRepo.save(user);

		AuthResult res = issueTokens(user, ip, userAgent);
		audit.record(AuditAction.REGISTER, user.getId(), "auth/register", ip, userAgent, true, null);
		return res;
	}

	@Transactional
	public AuthResult login(LoginRequest request, String ip, String userAgent) {
		String email = normalizeEmail(request.email());
		Optional<AppUser> opt = userRepo.findByEmail(email);
		if (opt.isEmpty()) {
			passwordEncoder.matches(request.password(), dummyPasswordHash);
			audit.record(AuditAction.LOGIN_FAILURE, null, "auth/login", ip, userAgent, false, null);
			throw new IllegalArgumentException("Invalid credentials");
		}

		AppUser user = opt.get();
		if (!user.isEnabled() || isLocked(user)) {
			passwordEncoder.matches(request.password(), user.getPasswordHash());
			audit.record(AuditAction.LOGIN_FAILURE, user.getId(), "auth/login", ip, userAgent, false, null);
			throw new IllegalArgumentException("Invalid credentials");
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			registerFailedLogin(user);
			audit.record(AuditAction.LOGIN_FAILURE, user.getId(), "auth/login", ip, userAgent, false, null);
			throw new IllegalArgumentException("Invalid credentials");
		}

		resetFailedLogins(user);
		AuthResult res = issueTokens(user, ip, userAgent);
		audit.record(AuditAction.LOGIN_SUCCESS, user.getId(), "auth/login", ip, userAgent, true, null);
		return res;
	}

	@Transactional
	public AuthResult refresh(String rawRefreshToken, String ip, String userAgent) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		String tokenHash = jwtService.sha256Hex(rawRefreshToken);
		RefreshToken token = refreshRepo.findByTokenHash(tokenHash)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

		Instant now = Instant.now();
		if (token.getExpiresAt().isBefore(now)) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		if (token.getRevokedAt() != null) {
			if (token.getReplacedByTokenHash() != null) {
				AppUser u = token.getUser();
				refreshRepo.revokeAllForUser(u.getId(), now);
				u.setTokenVersion(u.getTokenVersion() + 1);
				userRepo.save(u);
				audit.record(AuditAction.REFRESH_REPLAY_DETECTED, u.getId(), "auth/refresh", ip, userAgent, false, null);
			}
			throw new IllegalArgumentException("Invalid credentials");
		}

		AppUser user = token.getUser();
		if (!user.isEnabled() || isLocked(user)) {
			throw new IllegalArgumentException("Invalid credentials");
		}

		String newRaw = jwtService.generateRefreshToken();
		String newHash = jwtService.sha256Hex(newRaw);

		token.setRevokedAt(now);
		token.setReplacedByTokenHash(newHash);
		token.setLastUsedAt(now);
		refreshRepo.save(token);

		RefreshToken newToken = RefreshToken.builder()
				.user(user)
				.tokenHash(newHash)
				.createdAt(now)
				.expiresAt(now.plusSeconds(props.jwt().refreshTtlSeconds()))
				.revokedAt(null)
				.replacedByTokenHash(null)
				.lastUsedAt(now)
				.createdByIp(ip)
				.createdByUserAgent(truncate(userAgent, 255))
				.build();
		refreshRepo.save(newToken);

		String access;
		try {
			access = jwtService.createAccessToken(user);
		} catch (Exception e) {
			log.error("Failed to create access token during refresh for userId={}", user.getId(), e);
			throw e;
		}

		AuthResponse resp = new AuthResponse(
				user.getId(),
				user.getEmail(),
				user.getRole(),
				"Bearer",
				access,
				jwtService.accessTokenTtlSeconds()
		);
		audit.record(AuditAction.REFRESH_SUCCESS, user.getId(), "auth/refresh", ip, userAgent, true, null);
		return new AuthResult(resp, newRaw);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}
		String tokenHash = jwtService.sha256Hex(rawRefreshToken);
		refreshRepo.findByTokenHash(tokenHash).ifPresent(t -> {
			if (t.getRevokedAt() == null) {
				t.setRevokedAt(Instant.now());
				refreshRepo.save(t);
				audit.record(AuditAction.LOGOUT, t.getUser().getId(), "auth/logout", null, null, true, null);
			}
		});
	}

	@Transactional
	public void revokeAllSessions(Long userId) {
		if (userId == null) {
			return;
		}
		AppUser user = userRepo.findById(userId).orElse(null);
		if (user == null) {
			return;
		}
		Instant now = Instant.now();
		refreshRepo.revokeAllForUser(userId, now);
		user.setTokenVersion(user.getTokenVersion() + 1);
		userRepo.save(user);
		audit.record(AuditAction.REVOKE_ALL, userId, "auth/revoke-all", null, null, true, null);
	}

	public long refreshTtlSeconds() {
		return props.jwt().refreshTtlSeconds();
	}

	private AuthResult issueTokens(AppUser user, String ip, String userAgent) {
		Instant now = Instant.now();

		String rawRefresh = jwtService.generateRefreshToken();
		String refreshHash = jwtService.sha256Hex(rawRefresh);

		RefreshToken token = RefreshToken.builder()
				.user(user)
				.tokenHash(refreshHash)
				.createdAt(now)
				.expiresAt(now.plusSeconds(props.jwt().refreshTtlSeconds()))
				.revokedAt(null)
				.replacedByTokenHash(null)
				.lastUsedAt(now)
				.createdByIp(ip)
				.createdByUserAgent(truncate(userAgent, 255))
				.build();
		refreshRepo.save(token);

		String access;
		try {
			access = jwtService.createAccessToken(user);
		} catch (Exception e) {
			log.error("Failed to create access token for userId={}", user.getId(), e);
			throw e;
		}

		AuthResponse resp = new AuthResponse(
				user.getId(),
				user.getEmail(),
				user.getRole(),
				"Bearer",
				access,
				jwtService.accessTokenTtlSeconds()
		);
		return new AuthResult(resp, rawRefresh);
	}

	private void registerFailedLogin(AppUser user) {
		int attempts = user.getFailedLoginAttempts() + 1;
		user.setFailedLoginAttempts(attempts);
		if (attempts >= MAX_FAILED_LOGINS) {
			user.setLockedUntil(OffsetDateTime.now().plus(LOCK_TIME));
			user.setFailedLoginAttempts(0);
		}
		userRepo.save(user);
	}

	private void resetFailedLogins(AppUser user) {
		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		userRepo.save(user);
	}

	private boolean isLocked(AppUser user) {
		OffsetDateTime until = user.getLockedUntil();
		return until != null && until.isAfter(OffsetDateTime.now());
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		String trimmed = email.trim();
		String normalized = java.text.Normalizer.normalize(trimmed, java.text.Normalizer.Form.NFKC);
		return normalized.toLowerCase(java.util.Locale.ROOT);
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}
}
