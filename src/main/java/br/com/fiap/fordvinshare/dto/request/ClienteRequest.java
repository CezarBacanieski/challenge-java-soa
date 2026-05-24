package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class ClienteRequest {

	@NotBlank(message = "Nome é obrigatório")
	@Size(min = 1, max = 120, message = "Nome invÃ¡lido")
	@Pattern(regexp = "^[\\p{L} .,'-]{1,120}$", message = "Nome invÃ¡lido")
	private String nome;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	@Size(max = 254, message = "E-mail invÃ¡lido")
	private String email;

	@Size(max = 30, message = "Telefone invÃ¡lido")
	@Pattern(regexp = "^(\\+?[1-9]\\d{7,14})?$", message = "Telefone invÃ¡lido")
	private String telefone;
}
