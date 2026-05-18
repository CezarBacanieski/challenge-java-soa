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
public class DashboardResumoResponse {

	private Long totalClientes;
	private Long totalVeiculos;
	private BigDecimal percentualVeiculosRedeOficial;
	private Long totalLeadsPendentes;
	private Long totalManutencoesMesAtual;
}
