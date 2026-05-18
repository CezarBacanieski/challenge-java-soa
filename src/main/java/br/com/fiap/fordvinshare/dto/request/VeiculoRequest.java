package br.com.fiap.fordvinshare.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	private String vin;

	@NotBlank(message = "Marca é obrigatória")
	private String marca;

	@NotBlank(message = "Modelo é obrigatório")
	private String modelo;

	@Min(value = 1900, message = "Ano de fabricação deve ser maior ou igual a 1900")
	@Max(value = 2100, message = "Ano de fabricação deve ser menor ou igual a 2100")
	private Integer anoFabricacao;

	@NotNull(message = "Cliente é obrigatório")
	private Long clienteId;

	private Boolean utilizaRedeOficial;
}
