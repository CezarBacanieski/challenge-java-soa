package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.response.DashboardResumoResponse;
import br.com.fiap.fordvinshare.entity.StatusLead;
import br.com.fiap.fordvinshare.repository.ClienteRepository;
import br.com.fiap.fordvinshare.repository.LeadRepository;
import br.com.fiap.fordvinshare.repository.ManutencaoRepository;
import br.com.fiap.fordvinshare.repository.VeiculoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final ClienteRepository clienteRepository;
	private final VeiculoRepository veiculoRepository;
	private final LeadRepository leadRepository;
	private final ManutencaoRepository manutencaoRepository;

	@Override
	@Transactional(readOnly = true)
	public DashboardResumoResponse obterResumo() {
		long totalClientes = clienteRepository.count();
		long totalVeiculos = veiculoRepository.count();
		long veiculosRedeOficial = veiculoRepository.countByUtilizaRedeOficialTrue();
		long totalLeadsPendentes = leadRepository.countByStatus(StatusLead.PENDENTE);

		LocalDate primeiroDiaMes = LocalDate.now().withDayOfMonth(1);
		LocalDate ultimoDiaMes = primeiroDiaMes.plusMonths(1).minusDays(1);
		long totalManutencoesMesAtual = manutencaoRepository.countByDataServicoBetween(primeiroDiaMes, ultimoDiaMes);

		return DashboardResumoResponse.builder()
				.totalClientes(totalClientes)
				.totalVeiculos(totalVeiculos)
				.percentualVeiculosRedeOficial(calcularVinShare(totalVeiculos, veiculosRedeOficial))
				.totalLeadsPendentes(totalLeadsPendentes)
				.totalManutencoesMesAtual(totalManutencoesMesAtual)
				.build();
	}

	private BigDecimal calcularVinShare(long totalVeiculos, long veiculosRedeOficial) {
		if (totalVeiculos == 0) {
			return BigDecimal.ZERO.setScale(2);
		}

		return BigDecimal.valueOf(veiculosRedeOficial)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(totalVeiculos), 2, RoundingMode.HALF_UP);
	}
}
