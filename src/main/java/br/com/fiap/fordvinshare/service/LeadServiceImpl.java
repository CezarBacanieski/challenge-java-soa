package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.LeadRequest;
import br.com.fiap.fordvinshare.dto.request.LeadStatusRequest;
import br.com.fiap.fordvinshare.dto.response.LeadResponse;
import br.com.fiap.fordvinshare.entity.Lead;
import br.com.fiap.fordvinshare.entity.PrioridadeLead;
import br.com.fiap.fordvinshare.entity.StatusLead;
import br.com.fiap.fordvinshare.entity.Veiculo;
import br.com.fiap.fordvinshare.repository.LeadRepository;
import br.com.fiap.fordvinshare.repository.ManutencaoRepository;
import br.com.fiap.fordvinshare.repository.VeiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

	private static final String MOTIVO_SEM_MANUTENCAO = "Sem manutenção há mais de 6 meses";
	private static final String MOTIVO_REDE_NAO_OFICIAL = "Veículo utilizando rede não oficial";

	private final LeadRepository leadRepository;
	private final VeiculoRepository veiculoRepository;
	private final ManutencaoRepository manutencaoRepository;

	@Override
	@Transactional
	public LeadResponse gerarManual(Long veiculoId, LeadRequest request) {
		Veiculo veiculo = buscarVeiculoPorId(veiculoId);
		String motivoLead = request == null ? "Lead gerado manualmente" : request.getMotivoLead();
		PrioridadeLead prioridade = request == null ? PrioridadeLead.MEDIA : request.getPrioridade();

		Lead lead = Lead.builder()
				.veiculo(veiculo)
				.motivoLead(motivoLead)
				.status(StatusLead.PENDENTE)
				.prioridade(prioridade)
				.build();

		return toResponse(leadRepository.save(lead));
	}

	@Override
	@Transactional
	public List<LeadResponse> gerarAutomatico() {
		LocalDate dataLimite = LocalDate.now().minusDays(180);
		List<LeadResponse> leadsGerados = new ArrayList<>();

		for (Veiculo veiculo : veiculoRepository.findAll()) {
			if (leadRepository.existsByVeiculoIdAndStatus(veiculo.getId(), StatusLead.PENDENTE)) {
				continue;
			}

			boolean semManutencaoRecente = !manutencaoRepository
					.existsByVeiculoIdAndDataServicoGreaterThanEqual(veiculo.getId(), dataLimite);

			if (semManutencaoRecente) {
				leadsGerados.add(criarLeadAutomatico(veiculo, MOTIVO_SEM_MANUTENCAO, PrioridadeLead.ALTA));
			} else if (Boolean.FALSE.equals(veiculo.getUtilizaRedeOficial())) {
				leadsGerados.add(criarLeadAutomatico(veiculo, MOTIVO_REDE_NAO_OFICIAL, PrioridadeLead.MEDIA));
			}
		}

		return leadsGerados;
	}

	@Override
	@Transactional(readOnly = true)
	public List<LeadResponse> listarTodos() {
		return leadRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LeadResponse> listarPorStatus(StatusLead status) {
		return leadRepository.findByStatus(status)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public LeadResponse atualizarStatus(Long id, LeadStatusRequest request) {
		Lead lead = leadRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Lead não encontrado"));
		lead.setStatus(request.getStatus());

		return toResponse(leadRepository.save(lead));
	}

	private LeadResponse criarLeadAutomatico(Veiculo veiculo, String motivo, PrioridadeLead prioridade) {
		Lead lead = Lead.builder()
				.veiculo(veiculo)
				.motivoLead(motivo)
				.status(StatusLead.PENDENTE)
				.prioridade(prioridade)
				.build();

		return toResponse(leadRepository.save(lead));
	}

	private Veiculo buscarVeiculoPorId(Long veiculoId) {
		return veiculoRepository.findById(veiculoId)
				.orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
	}

	private LeadResponse toResponse(Lead lead) {
		return LeadResponse.builder()
				.id(lead.getId())
				.veiculoId(lead.getVeiculo().getId())
				.vin(lead.getVeiculo().getVin())
				.dataGeracao(lead.getDataGeracao())
				.motivoLead(lead.getMotivoLead())
				.status(lead.getStatus())
				.prioridade(lead.getPrioridade())
				.build();
	}
}
