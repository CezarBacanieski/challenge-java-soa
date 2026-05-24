package br.com.fiap.fordvinshare.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityContextUtil {
	private SecurityContextUtil() {
	}

	public static Long currentUserIdOrNull() {
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
}

