package br.com.fiap.fordvinshare.dto.response;

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
public class VeiculoResponse {

	private Long id;
	private String vin;
	private String marca;
	private String modelo;
	private Integer anoFabricacao;
	private Long clienteId;
	private String clienteNome;
	private Boolean utilizaRedeOficial;
}
