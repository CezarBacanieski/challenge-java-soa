package br.com.fiap.fordvinshare.service;

import br.com.fiap.fordvinshare.dto.request.VeiculoRequest;
import br.com.fiap.fordvinshare.dto.response.VeiculoResponse;
import br.com.fiap.fordvinshare.entity.Cliente;
import br.com.fiap.fordvinshare.entity.Veiculo;
import br.com.fiap.fordvinshare.repository.ClienteRepository;
import br.com.fiap.fordvinshare.repository.VeiculoRepository;
import br.com.fiap.fordvinshare.security.audit.AuditAction;
import br.com.fiap.fordvinshare.security.audit.AuditService;
import br.com.fiap.fordvinshare.security.util.SecurityContextUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeiculoServiceImpl implements VeiculoService {

	private final VeiculoRepository veiculoRepository;
	private final ClienteRepository clienteRepository;
	private final AuditService audit;

	@Override
	@Transactional
	public VeiculoResponse cadastrar(VeiculoRequest request) {
		Cliente cliente = buscarClientePorId(request.getClienteId());

		Veiculo veiculo = Veiculo.builder()
				.vin(request.getVin())
				.marca(request.getMarca())
				.modelo(request.getModelo())
				.anoFabricacao(request.getAnoFabricacao())
				.cliente(cliente)
				.utilizaRedeOficial(resolveUtilizaRedeOficial(request.getUtilizaRedeOficial()))
				.build();

		Veiculo saved = veiculoRepository.save(veiculo);
		audit.record(AuditAction.VEICULO_CREATE, SecurityContextUtil.currentUserIdOrNull(), "veiculo:" + saved.getId(), null, null, true, null);
		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<VeiculoResponse> listarTodos() {
		return veiculoRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public VeiculoResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<VeiculoResponse> listarPorCliente(Long clienteId) {
		if (!clienteRepository.existsById(clienteId)) {
			throw new EntityNotFoundException("Cliente não encontrado");
		}

		return veiculoRepository.findByClienteId(clienteId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional
	public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
		Veiculo veiculo = buscarEntidadePorId(id);
		Cliente cliente = buscarClientePorId(request.getClienteId());

		veiculo.setVin(request.getVin());
		veiculo.setMarca(request.getMarca());
		veiculo.setModelo(request.getModelo());
		veiculo.setAnoFabricacao(request.getAnoFabricacao());
		veiculo.setCliente(cliente);
		veiculo.setUtilizaRedeOficial(resolveUtilizaRedeOficial(request.getUtilizaRedeOficial()));

		Veiculo saved = veiculoRepository.save(veiculo);
		audit.record(AuditAction.VEICULO_UPDATE, SecurityContextUtil.currentUserIdOrNull(), "veiculo:" + saved.getId(), null, null, true, null);
		return toResponse(saved);
	}

	@Override
	@Transactional
	public void deletar(Long id) {
		Veiculo veiculo = buscarEntidadePorId(id);
		veiculoRepository.delete(veiculo);
		audit.record(AuditAction.VEICULO_DELETE, SecurityContextUtil.currentUserIdOrNull(), "veiculo:" + id, null, null, true, null);
	}

	private Veiculo buscarEntidadePorId(Long id) {
		return veiculoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
	}

	private Cliente buscarClientePorId(Long clienteId) {
		return clienteRepository.findById(clienteId)
				.orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
	}

	private Boolean resolveUtilizaRedeOficial(Boolean utilizaRedeOficial) {
		return utilizaRedeOficial == null || utilizaRedeOficial;
	}

	private VeiculoResponse toResponse(Veiculo veiculo) {
		return VeiculoResponse.builder()
				.id(veiculo.getId())
				.vin(veiculo.getVin())
				.marca(veiculo.getMarca())
				.modelo(veiculo.getModelo())
				.anoFabricacao(veiculo.getAnoFabricacao())
				.clienteId(veiculo.getCliente().getId())
				.clienteNome(veiculo.getCliente().getNome())
				.utilizaRedeOficial(veiculo.getUtilizaRedeOficial())
				.build();
	}
}
