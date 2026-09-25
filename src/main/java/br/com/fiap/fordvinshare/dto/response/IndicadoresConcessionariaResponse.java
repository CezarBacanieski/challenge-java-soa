package br.com.fiap.fordvinshare.dto.response;

import java.math.BigDecimal;
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
public class IndicadoresConcessionariaResponse {

	private Long concessionariaId;
	private String concessionariaNome;
	private Long totalManutencoes;
	private Long veiculosAtendidos;
	private Long veiculosRetidos;
	/** Percentual dos veículos atendidos que continuam usando a rede oficial. */
	private BigDecimal taxaRetencao;
}
