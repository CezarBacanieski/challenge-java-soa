package br.com.fiap.fordvinshare.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 254, message = "E-mail inválido")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(max = 72, message = "Senha inválida")
		String password
) {
}

