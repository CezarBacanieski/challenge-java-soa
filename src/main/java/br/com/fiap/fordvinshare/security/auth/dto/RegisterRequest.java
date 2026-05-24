package br.com.fiap.fordvinshare.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 254, message = "E-mail inválido")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 12, max = 72, message = "Senha deve ter entre 12 e 72 caracteres")
		@Pattern(
				regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
				message = "Senha deve conter maiúscula, minúscula, número e símbolo"
		)
		String password
) {
}

