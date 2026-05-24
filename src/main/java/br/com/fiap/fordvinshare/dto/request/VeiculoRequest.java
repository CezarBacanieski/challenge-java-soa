package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VeiculoRequest {

	@NotBlank(message = "VIN é obrigatório")
	@Size(min = 11, max = 17, message = "VIN deve ter entre 11 e 17 caracteres")
	@Pattern(regexp = "^[A-HJ-NPR-Z0-9]+$", message = "VIN invÃ¡lido")
	private String vin;

	@NotBlank(message = "Marca é obrigatória")
	@Size(min = 1, max = 60, message = "Marca invÃ¡lida")
	@Pattern(regexp = "^[\\p{L}0-9 .,'-]{1,60}$", message = "Marca invÃ¡lida")
	private String marca;

	@NotBlank(message = "Modelo é obrigatório")
	@Size(min = 1, max = 60, message = "Modelo invÃ¡lido")
	@Pattern(regexp = "^[\\p{L}0-9 .,'-]{1,60}$", message = "Modelo invÃ¡lido")
	private String modelo;

	@Min(value = 1900, message = "Ano de fabricação deve ser maior ou igual a 1900")
	@Max(value = 2100, message = "Ano de fabricação deve ser menor ou igual a 2100")
	private Integer anoFabricacao;

	@NotNull(message = "Cliente é obrigatório")
	private Long clienteId;

	private Boolean utilizaRedeOficial;
}
