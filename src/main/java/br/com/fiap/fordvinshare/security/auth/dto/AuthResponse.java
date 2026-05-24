package br.com.fiap.fordvinshare.security.auth.dto;

import br.com.fiap.fordvinshare.security.auth.Role;

public record AuthResponse(
		Long userId,
		String email,
		Role role,
		String tokenType,
		String accessToken,
		long expiresInSeconds
) {
}

