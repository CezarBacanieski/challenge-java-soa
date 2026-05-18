package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ManutencaoRequest;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import br.com.fiap.fordvinshare.entity.Manutencao;
import br.com.fiap.fordvinshare.entity.Veiculo;
import br.com.fiap.fordvinshare.repository.ManutencaoRepository;
import br.com.fiap.fordvinshare.repository.VeiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManutencaoServiceImpl implements ManutencaoService {

	private final ManutencaoRepository manutencaoRepository;
	private final VeiculoRepository veiculoRepository;

	@Override
	@Transactional
	public ManutencaoResponse registrar(ManutencaoRequest request) {
		Veiculo veiculo = buscarVeiculoPorId(request.getVeiculoId());

		Manutencao manutencao = Manutencao.builder()
				.veiculo(veiculo)
				.dataServico(request.getDataServico())
				.tipoServico(request.getTipoServico())
				.valor(request.getValor())
				.realizadaNaRedeOficial(request.getRealizadaNaRedeOficial())
				.observacoes(request.getObservacoes())
				.build();

		if (Boolean.FALSE.equals(request.getRealizadaNaRedeOficial())) {
			veiculo.setUtilizaRedeOficial(false);
			veiculoRepository.save(veiculo);
		}

		return toResponse(manutencaoRepository.save(manutencao));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ManutencaoResponse> listarPorVeiculo(Long veiculoId) {
		if (!veiculoRepository.existsById(veiculoId)) {
			throw new EntityNotFoundException("Veículo não encontrado");
		}

		return manutencaoRepository.findByVeiculoIdOrderByDataServicoDesc(veiculoId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ManutencaoResponse> listarTodas() {
		return manutencaoRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		Manutencao manutencao = manutencaoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Manutenção não encontrada"));
		manutencaoRepository.delete(manutencao);
	}

	private Veiculo buscarVeiculoPorId(Long veiculoId) {
		return veiculoRepository.findById(veiculoId)
				.orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
	}

	private ManutencaoResponse toResponse(Manutencao manutencao) {
		return ManutencaoResponse.builder()
				.id(manutencao.getId())
				.veiculoId(manutencao.getVeiculo().getId())
				.vin(manutencao.getVeiculo().getVin())
				.dataServico(manutencao.getDataServico())
				.tipoServico(manutencao.getTipoServico())
				.valor(manutencao.getValor())
				.realizadaNaRedeOficial(manutencao.getRealizadaNaRedeOficial())
				.observacoes(manutencao.getObservacoes())
				.build();
	}
}
