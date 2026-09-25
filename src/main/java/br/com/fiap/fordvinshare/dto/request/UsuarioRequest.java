package br.com.fiap.fordvinshare.dto.request;

import br.com.fiap.fordvinshare.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

	@NotBlank(message = "Nome é obrigatório")
	private String nome;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	private String email;

	@NotBlank(message = "Senha é obrigatória")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
			message = "Senha deve ter de 8 a 64 caracteres, com letra maiúscula, minúscula, número e símbolo")
	private String senha;

	@NotNull(message = "Perfil é obrigatório")
	private Perfil perfil;

	/** Obrigatório para GESTOR e CONSULTOR. */
	private Long concessionariaId;
}
