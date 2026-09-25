package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.ManutencaoRequest;
import br.com.fiap.fordvinshare.dto.response.ManutencaoResponse;
import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.entity.Manutencao;
import br.com.fiap.fordvinshare.entity.Veiculo;
import br.com.fiap.fordvinshare.exception.RegraNegocioException;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
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
	private final ConcessionariaRepository concessionariaRepository;

	@Override
	@Transactional
	public ManutencaoResponse registrar(ManutencaoRequest request) {
		Veiculo veiculo = buscarVeiculoPorId(request.getVeiculoId());
		Concessionaria concessionaria = resolverConcessionaria(request);

		Manutencao manutencao = Manutencao.builder()
				.veiculo(veiculo)
				.concessionaria(concessionaria)
				.dataServico(request.getDataServico())
				.tipoServico(request.getTipoServico())
				.valor(request.getValor())
				.realizadaNaRedeOficial(request.getRealizadaNaRedeOficial())
				.observacoes(request.getObservacoes())
				.build();

		Manutencao salva = manutencaoRepository.save(manutencao);
		atualizarUsoRedeOficial(veiculo);

		return toResponse(salva);
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
	@Transactional(readOnly = true)
	public ManutencaoResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		Manutencao manutencao = buscarEntidadePorId(id);
		Veiculo veiculo = manutencao.getVeiculo();
		manutencaoRepository.delete(manutencao);
		manutencaoRepository.flush();
		atualizarUsoRedeOficial(veiculo);
	}

	/**
	 * O status "utiliza rede oficial" reflete a manutenção mais recente do veículo.
	 * Assim, um cliente que saiu da rede e voltou é contabilizado novamente no VIN Share.
	 */
	private void atualizarUsoRedeOficial(Veiculo veiculo) {
		manutencaoRepository.findFirstByVeiculoIdOrderByDataServicoDescIdDesc(veiculo.getId())
				.ifPresent(ultima -> {
					veiculo.setUtilizaRedeOficial(ultima.getRealizadaNaRedeOficial());
					veiculoRepository.save(veiculo);
				});
	}

	private Concessionaria resolverConcessionaria(ManutencaoRequest request) {
		if (Boolean.TRUE.equals(request.getRealizadaNaRedeOficial()) && request.getConcessionariaId() == null) {
			throw new RegraNegocioException("Manutenção realizada na rede oficial exige a concessionária (concessionariaId)");
		}
		if (Boolean.FALSE.equals(request.getRealizadaNaRedeOficial()) && request.getConcessionariaId() != null) {
			throw new RegraNegocioException("Manutenção fora da rede oficial não deve informar concessionária");
		}
		if (request.getConcessionariaId() == null) {
			return null;
		}
		return concessionariaRepository.findById(request.getConcessionariaId())
				.orElseThrow(() -> new EntityNotFoundException("Concessionária não encontrada"));
	}

	private Manutencao buscarEntidadePorId(Long id) {
		return manutencaoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Manutenção não encontrada"));
	}

	private Veiculo buscarVeiculoPorId(Long veiculoId) {
		return veiculoRepository.findById(veiculoId)
				.orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
	}

	private ManutencaoResponse toResponse(Manutencao manutencao) {
		Concessionaria concessionaria = manutencao.getConcessionaria();
		return ManutencaoResponse.builder()
				.id(manutencao.getId())
				.veiculoId(manutencao.getVeiculo().getId())
				.vin(manutencao.getVeiculo().getVin())
				.concessionariaId(concessionaria != null ? concessionaria.getId() : null)
				.concessionariaNome(concessionaria != null ? concessionaria.getNome() : null)
				.dataServico(manutencao.getDataServico())
				.tipoServico(manutencao.getTipoServico())
				.valor(manutencao.getValor())
				.realizadaNaRedeOficial(manutencao.getRealizadaNaRedeOficial())
				.observacoes(manutencao.getObservacoes())
				.build();
	}
}
