package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class ConcessionariaRequest {

	@NotBlank(message = "Nome é obrigatório")
	private String nome;

	@NotBlank(message = "CNPJ é obrigatório")
	@Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter 14 dígitos numéricos")
	private String cnpj;

	@NotBlank(message = "Cidade é obrigatória")
	private String cidade;

	@NotBlank(message = "UF é obrigatória")
	@Pattern(regexp = "^[A-Z]{2}$", message = "UF deve conter 2 letras maiúsculas")
	private String uf;
}
