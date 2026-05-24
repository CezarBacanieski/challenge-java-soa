package br.com.fiap.fordvinshare.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapUsersConfig {
	private static final Logger log = LoggerFactory.getLogger(BootstrapUsersConfig.class);

	@Bean
	ApplicationRunner bootstrapUsers(AppUserRepository repo, PasswordEncoder encoder, Environment env) {
		return args -> {
			createIfConfigured(repo, encoder, env, "APP_BOOTSTRAP_ADMIN_EMAIL", "APP_BOOTSTRAP_ADMIN_PASSWORD", Role.ADMIN);
			createIfConfigured(repo, encoder, env, "APP_BOOTSTRAP_ANALYST_EMAIL", "APP_BOOTSTRAP_ANALYST_PASSWORD", Role.ANALYST);
		};
	}

	private void createIfConfigured(
			AppUserRepository repo,
			PasswordEncoder encoder,
			Environment env,
			String emailKey,
			String passKey,
			Role role
	) {
		String email = env.getProperty(emailKey);
		String pass = env.getProperty(passKey);
		if (email == null || email.isBlank() || pass == null || pass.isBlank()) {
			return;
		}

		String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
		if (repo.existsByEmail(normalized)) {
			return;
		}

		AppUser user = AppUser.builder()
				.email(normalized)
				.passwordHash(encoder.encode(pass))
				.role(role)
				.enabled(true)
				.failedLoginAttempts(0)
				.lockedUntil(null)
				.tokenVersion(0)
				.build();
		repo.save(user);
		log.warn("Bootstrap user created for role={} email={}", role, normalized);
	}
}

